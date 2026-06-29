package me.proxycracked.universalaccountmanager;

import com.google.gson.*;
import me.proxycracked.universalaccountmanager.auth.Account;
import me.proxycracked.universalaccountmanager.auth.TokenAuth;
import me.proxycracked.universalaccountmanager.skin.ForceSkinLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

// EsdeathClient port: stripped the Forge @Mod/event-bus glue. init() is called
// from EsdeathClient.onEnable; the "Accounts" entry point is the Esdeath account
// button -> new GuiAccountManager(...). (Events.java + the force-skin Mixin were
// dropped; force-skin override needs a patch-mc ASM transformer to re-add.)
public class UniversalAccountManager {
  private static final Minecraft mc = Minecraft.getMinecraft();
  private static final File file = new File(mc.mcDataDir, "universalaccountmanager_accounts.json");
  private static final File prefsFile = new File(mc.mcDataDir, "universalaccountmanager_prefs.json");
  // Persisted copies of cookie exports so cookie accounts can re-auth instead
  // of expiring. See CookieAuth#refreshAll.
  public static final File cookiesDir = new File(mc.mcDataDir, "universalaccountmanager_cookies");
  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public static final ArrayList<Account> accounts = new ArrayList<>();
  public static boolean streamerMode = false;
  public static final String HIDDEN_LABEL = "[HIDDEN]";

  public static void init() {

    // One-shot migration from the old TokenManager filename.
    File legacy = new File(mc.mcDataDir, "tokenmanager_accounts.json");
    if (!file.exists() && legacy.exists()) {
      if (legacy.renameTo(file)) {
        System.out.println("[UniversalAccountManager] Migrated tokenmanager_accounts.json -> universalaccountmanager_accounts.json");
      } else {
        System.err.println("[UniversalAccountManager] Failed to migrate legacy accounts file");
      }
    }

    if (!file.exists()) {
      try {
        if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
          if (file.createNewFile()) {
            System.out.println("[UniversalAccountManager] Created universalaccountmanager_accounts.json");
          }
        }
      } catch (IOException e) {
        System.err.println("[UniversalAccountManager] Couldn't create universalaccountmanager_accounts.json");
      }
    }
    load();
    loadPrefs();
    autoImportLauncherAccount();
    resort();
    ForceSkinLoader.init();
  }

  public static void loadPrefs() {
    if (!prefsFile.exists() || prefsFile.length() == 0) return;
    try (BufferedReader r = new BufferedReader(new FileReader(prefsFile))) {
      JsonElement json = new JsonParser().parse(r);
      if (json instanceof JsonObject) {
        JsonObject o = json.getAsJsonObject();
        streamerMode = Optional.ofNullable(o.get("streamerMode")).map(JsonElement::getAsBoolean).orElse(false);
      }
    } catch (Exception e) {
      System.err.println("[UniversalAccountManager] Failed to load prefs: " + e.getMessage());
    }
  }

  public static void savePrefs() {
    try (PrintWriter w = new PrintWriter(new FileWriter(prefsFile))) {
      JsonObject o = new JsonObject();
      o.addProperty("streamerMode", streamerMode);
      w.println(gson.toJson(o));
    } catch (IOException e) {
      System.err.println("[UniversalAccountManager] Failed to save prefs: " + e.getMessage());
    }
  }

  // Adds the account Minecraft was launched with (the launcher session) to the
  // saved account list, so it shows up in the manager without manual entry.
  // Skips if a matching account (by UUID, falling back to username) already
  // exists with a still-valid access token. Only overwrites the stored token
  // when the existing one is expired/invalid.
  private static void autoImportLauncherAccount() {
    try {
      Session s = mc.getSession();
      if (s == null) return;
      final String username = s.getUsername();
      String tmpUuid = s.getPlayerID();
      final String token = s.getToken();
      if (username == null || username.isEmpty()) return;
      if (token == null || token.isEmpty()) return;
      if (tmpUuid == null) tmpUuid = "";
      final String uuid = tmpUuid;
      // Skip the offline-mode placeholder Forge ships with.
      if ("Player".equals(username) && uuid.isEmpty()) return;

      final Account match = findMatch(uuid, username);

      if (match == null) {
        accounts.add(new Account(Account.TYPE_LAUNCHER, "", token, username, uuid));
        save();
        System.out.println("[UniversalAccountManager] Auto-imported launcher account: " + username);
        return;
      }

      // Already present — validate the stored token off the main thread so
      // startup isn't blocked. Only refresh if the existing token is expired.
      Thread t = new Thread(() -> {
        try {
          String existing = match.getAccessToken();
          boolean expired = existing == null || existing.isEmpty() || !TokenAuth.validate(existing);
          if (!expired) return;
          match.setType(Account.TYPE_LAUNCHER);
          match.setAccessToken(token);
          match.setUsername(username);
          if (!uuid.isEmpty()) match.setUuid(uuid);
          save();
          System.out.println("[UniversalAccountManager] Refreshed expired token for: " + username);
        } catch (Exception ignored) {
          //
        }
      }, "UniversalAccountManager-LauncherImport");
      t.setDaemon(true);
      t.start();
    } catch (Exception e) {
      System.err.println("[UniversalAccountManager] Failed to auto-import launcher account: " + e.getMessage());
    }
  }

  private static Account findMatch(String uuid, String username) {
    for (Account a : accounts) {
      boolean uuidMatch = uuid != null && !uuid.isEmpty()
        && a.getUuid() != null && uuid.equalsIgnoreCase(a.getUuid());
      boolean nameMatch = (uuid == null || uuid.isEmpty())
        && a.getUsername() != null && username.equalsIgnoreCase(a.getUsername());
      if (uuidMatch || nameMatch) return a;
    }
    return null;
  }

  public static void load() {
    accounts.clear();
    if (!file.exists() || file.length() == 0) return;
    try (BufferedReader r = new BufferedReader(new FileReader(file))) {
      JsonElement json = new JsonParser().parse(r);
      if (json instanceof JsonArray) {
        for (JsonElement el : json.getAsJsonArray()) {
          JsonObject o = el.getAsJsonObject();
          Account acc = new Account(
            Optional.ofNullable(o.get("type")).map(JsonElement::getAsString).orElse(Account.TYPE_MS),
            Optional.ofNullable(o.get("refreshToken")).map(JsonElement::getAsString).orElse(""),
            Optional.ofNullable(o.get("accessToken")).map(JsonElement::getAsString).orElse(""),
            Optional.ofNullable(o.get("username")).map(JsonElement::getAsString).orElse(""),
            Optional.ofNullable(o.get("uuid")).map(JsonElement::getAsString).orElse("")
          );
          acc.setPinned(Optional.ofNullable(o.get("pinned")).map(JsonElement::getAsBoolean).orElse(false));
          acc.setCookieFile(Optional.ofNullable(o.get("cookieFile")).map(JsonElement::getAsString).orElse(""));
          accounts.add(acc);
        }
      }
    } catch (Exception e) {
      System.err.println("[UniversalAccountManager] Failed to load accounts: " + e.getMessage());
    }
  }

  // Sort pinned accounts (alphabetical by username, case-insensitive) above
  // unpinned ones. Unpinned accounts keep their relative order so manual
  // reorder via drag/Ctrl-arrow stays sticky.
  public static void resort() {
    accounts.sort(Comparator
      .comparing((Account a) -> a.isPinned() ? 0 : 1)
      .thenComparing(a -> a.isPinned() ? safeName(a) : ""));
  }

  // Index immediately after the lowest-positioned pinned account (i.e. the
  // first index a non-pinned account is allowed to occupy). Returns 0 when
  // no accounts are pinned.
  public static int firstUnpinnedIndex() {
    int i = 0;
    for (; i < accounts.size(); i++) if (!accounts.get(i).isPinned()) break;
    return i;
  }

  private static String safeName(Account a) {
    String n = a.getUsername();
    return n == null ? "" : n.toLowerCase();
  }

  // Copies a selected cookie export into the managed cookies folder, keyed by
  // the account (uuid preferred, else username), and returns the stored path.
  // Overwrites any previous copy for the same key so re-importing refreshes it.
  public static String storeCookieFile(File src, String key) throws IOException {
    if (!cookiesDir.exists()) cookiesDir.mkdirs();
    String safe = key == null ? "" : key.replaceAll("[^a-zA-Z0-9_-]", "_");
    if (safe.isEmpty()) safe = "cookie-" + System.currentTimeMillis();
    File dest = new File(cookiesDir, safe + ".txt");
    try (InputStream in = new FileInputStream(src);
         OutputStream out = new FileOutputStream(dest)) {
      byte[] buf = new byte[8192];
      int n;
      while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
    }
    return dest.getAbsolutePath();
  }

  public static void save() {
    try (PrintWriter w = new PrintWriter(new FileWriter(file))) {
      JsonArray arr = new JsonArray();
      for (Account a : accounts) {
        JsonObject o = new JsonObject();
        o.addProperty("type", a.getType());
        o.addProperty("refreshToken", a.getRefreshToken());
        o.addProperty("accessToken", a.getAccessToken());
        o.addProperty("username", a.getUsername());
        o.addProperty("uuid", a.getUuid());
        o.addProperty("pinned", a.isPinned());
        o.addProperty("cookieFile", a.getCookieFile());
        arr.add(o);
      }
      w.println(gson.toJson(arr));
    } catch (IOException e) {
      System.err.println("[UniversalAccountManager] Failed to save accounts: " + e.getMessage());
    }
  }
}
