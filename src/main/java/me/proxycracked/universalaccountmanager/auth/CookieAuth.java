package me.proxycracked.universalaccountmanager.auth;

import com.google.gson.Gson;
import me.proxycracked.universalaccountmanager.UniversalAccountManager;
import net.minecraft.util.Session;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Pattern;

// Cookie-based MS/MC login. Adapted from ksyzov/AccountManager (Vxrtrauter's
// cookie support). Reads a Netscape-format cookie export, walks the
// sisu.xboxlive.com -> login.live.com -> minecraft.net redirect chain to
// extract an Xbox access token, then exchanges it for a Minecraft access token.
public final class CookieAuth {
  private CookieAuth() {}

  private static final Gson gson = new Gson();
  private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36";

  private static final ExecutorService EXEC = Executors.newFixedThreadPool(3, new ThreadFactory() {
    private final AtomicInteger n = new AtomicInteger(0);
    @Override public Thread newThread(Runnable r) {
      Thread t = new Thread(r, "UniversalAccountManager-Cookie-" + n.incrementAndGet());
      t.setDaemon(true);
      return t;
    }
  });

  // Re-derives a fresh access token for every cookie account that has a stored
  // cookie file, by re-walking the redirect chain with the long-lived
  // login.live.com cookies. Called on each account-list refresh so cookie
  // accounts don't expire/grey-out when their short-lived MC token dies.
  // onUpdate (nullable) fires after each success so the GUI can repaint.
  public static void refreshAll(Runnable onUpdate) {
    for (final Account acc : new ArrayList<>(UniversalAccountManager.accounts)) {
      if (!acc.isCookie()) continue;
      String path = acc.getCookieFile();
      if (path == null || path.isEmpty()) continue; // legacy account, no stored file
      final File f = new File(path);
      if (!f.exists()) continue; // file moved/deleted — leave flag untouched
      loginFromFile(f, s -> {}, EXEC)
        .thenAccept(result -> {
          acc.setAccessToken(result.accessToken);
          acc.setUsername(result.session.getUsername());
          acc.setUuid(result.session.getPlayerID());
          acc.setAvailable(Boolean.TRUE);
          UniversalAccountManager.save();
          if (onUpdate != null) onUpdate.run();
        })
        .exceptionally(err -> {
          acc.setAvailable(Boolean.FALSE);
          System.err.println("[UniversalAccountManager] Cookie re-auth failed for "
            + acc.getUsername() + ": " + err.getMessage());
          return null;
        });
    }
  }

  public static class CookieResult {
    public final Session session;
    public final String accessToken;
    public CookieResult(Session session, String accessToken) {
      this.session = session;
      this.accessToken = accessToken;
    }
  }

  private static class McResponse { String access_token; }
  private static class ProfileResponse { String name; String id; }

  public static CompletableFuture<CookieResult> loginFromFile(File cookieFile, Consumer<String> status, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        status.accept("&fReading cookie file...&r");
        Map<String, String> cookies = parseCookieFile(cookieFile);
        if (cookies.isEmpty()) {
          throw new Exception("No login.live.com cookies found in file");
        }
        String cookieString = buildCookieString(cookies);

        status.accept("&fStarting Microsoft authentication (1/3)...&r");
        String location3 = followRedirectChain(cookieString, status);

        status.accept("&fExtracting access token...&r");
        int idx = location3.indexOf("accessToken=");
        if (idx < 0) throw new Exception("No accessToken in redirect URL");
        String accessTokenB64 = location3.substring(idx + "accessToken=".length());
        String decoded = new String(Base64.getDecoder().decode(accessTokenB64), StandardCharsets.UTF_8);

        String[] parts = decoded.split("\"rp://api.minecraftservices.com/\",");
        if (parts.length < 2) throw new Exception("Failed to decode access token");
        String rest = parts[1];
        String token = rest.split("\"Token\":\"")[1].split("\"")[0];
        String uhs = rest.split(Pattern.quote("{\"DisplayClaims\":{\"xui\":[{\"uhs\":\""))[1].split("\"")[0];
        String xblToken = "XBL3.0 x=" + uhs + ";" + token;

        status.accept("&fLogging into Minecraft services...&r");
        McResponse mc = postMinecraftLogin(xblToken);
        if (mc == null || mc.access_token == null) throw new Exception("Minecraft service rejected the token");

        status.accept("&fFetching Minecraft profile...&r");
        ProfileResponse profile = getMinecraftProfile(mc.access_token);
        if (profile == null || profile.name == null) throw new Exception("Could not fetch Minecraft profile");

        Session session = new Session(profile.name, profile.id, mc.access_token, Session.Type.MOJANG.toString());
        return new CookieResult(session, mc.access_token);
      } catch (InterruptedException e) {
        throw new CancellationException("Cookie login cancelled!");
      } catch (Exception e) {
        throw new CompletionException("Cookie login failed!", e);
      }
    }, executor);
  }

  public static Map<String, String> parseCookieFile(File file) throws IOException {
    Map<String, String> cookies = new HashMap<>();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("#") || line.trim().isEmpty()) continue;
        String[] parts = line.split("\t", -1);
        if (parts.length > 6 && parts[0].endsWith("login.live.com")) {
          String name = parts[5].trim();
          if (!cookies.containsKey(name)) {
            cookies.put(name, parts[6].trim());
          }
        }
      }
    }
    return cookies;
  }

  public static String buildCookieString(Map<String, String> cookies) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> e : cookies.entrySet()) {
      if (sb.length() > 0) sb.append("; ");
      sb.append(e.getKey()).append("=").append(e.getValue());
    }
    return sb.toString();
  }

  private static String followRedirectChain(String cookieString, Consumer<String> status) throws Exception {
    String url1 = "https://sisu.xboxlive.com/connect/XboxLive/?state=login"
      + "&cobrandId=8058f65d-ce06-4c30-9559-473c9275a65d"
      + "&tid=896928775"
      + "&ru=https%3A%2F%2Fwww.minecraft.net%2Fen-us%2Flogin"
      + "&aid=1142970254";

    String location1 = redirectGet(url1, null);
    if (location1 == null) throw new Exception("Redirect failed at step 1 (sisu.xboxlive.com)");
    location1 = location1.replace(" ", "%20");

    status.accept("&fProcessing Microsoft redirect (2/3)...&r");
    String location2 = redirectGet(location1, cookieString);
    if (location2 == null) throw new Exception("Redirect failed at step 2 (login.live.com)");

    status.accept("&fFinalizing Microsoft redirect (3/3)...&r");
    String location3 = redirectGet(location2, cookieString);
    if (location3 == null) throw new Exception("Redirect failed at step 3 (minecraft.net)");

    return location3;
  }

  private static String redirectGet(String url, String cookieString) throws Exception {
    HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,"
      + "image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
    conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
    conn.setRequestProperty("Accept-Language", "en-US;q=0.8");
    conn.setRequestProperty("User-Agent", UA);
    if (cookieString != null) conn.setRequestProperty("Cookie", cookieString);
    conn.setInstanceFollowRedirects(false);
    conn.connect();
    String location = conn.getHeaderField("Location");
    conn.disconnect();
    return location;
  }

  private static McResponse postMinecraftLogin(String xblToken) throws Exception {
    String url = "https://api.minecraftservices.com/authentication/login_with_xbox";
    String payload = "{\"identityToken\":\"" + xblToken + "\",\"ensureLegacyEnabled\":true}";
    HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestProperty("Accept", "application/json");
    conn.setDoOutput(true);
    try (OutputStream os = conn.getOutputStream()) {
      os.write(payload.getBytes(StandardCharsets.UTF_8));
    }
    StringBuilder body = new StringBuilder();
    try (InputStream is = conn.getInputStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) body.append(line);
    } finally {
      conn.disconnect();
    }
    return gson.fromJson(body.toString(), McResponse.class);
  }

  private static ProfileResponse getMinecraftProfile(String accessToken) throws Exception {
    String url = "https://api.minecraftservices.com/minecraft/profile";
    HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
    conn.setRequestProperty("Accept", "application/json");
    StringBuilder body = new StringBuilder();
    try (InputStream is = conn.getInputStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) body.append(line);
    } finally {
      conn.disconnect();
    }
    return gson.fromJson(body.toString(), ProfileResponse.class);
  }
}
