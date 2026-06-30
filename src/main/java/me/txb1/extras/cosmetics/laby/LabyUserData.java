package me.txb1.extras.cosmetics.laby;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Replicates LabyMod 3's per-user cosmetic lookup: GET https://dl.labymod.net/userdata/<uuid>.json,
// whose {"c":[{"i":<cosmeticId>,"d":[..data..]}]} lists which cosmetics that player has equipped
// (the same data laby.net/@user shows). We fetch+cache per UUID so each player's owned LabyMod
// cosmetics render automatically, with their real per-user config (colours / texture ids in `d`).
public final class LabyUserData {

   // laby.net is the live endpoint (the old dl.labymod.net/userdata/<uuid>.json 301-redirects here).
   private static final String URL = "https://laby.net/api/v3/user/%s/userdata";
   private static final String[] EMPTY = new String[0];

   private static final Map<UUID, Map<Integer, String[]>> CACHE = new ConcurrentHashMap<UUID, Map<Integer, String[]>>();
   private static final Set<UUID> PENDING = ConcurrentHashMap.newKeySet();

   private LabyUserData() {
   }

   // Equipped cosmetics for a player (id -> data array), fetching asynchronously on first request.
   // Returns null until loaded; an empty map means "loaded, owns nothing".
   public static Map<Integer, String[]> get(UUID uuid) {
      if (uuid == null) {
         return null;
      }
      Map<Integer, String[]> cached = CACHE.get(uuid);
      if (cached != null) {
         return cached;
      }
      if (!PENDING.contains(uuid)) {
         fetch(uuid);
      }
      return null;
   }

   public static boolean owns(UUID uuid, int cosmeticId) {
      Map<Integer, String[]> m = get(uuid);
      return m != null && m.containsKey(cosmeticId);
   }

   // Inject equipped cosmetics for a player straight from the LabyConnect socket
   // (PacketUpdateCosmetics / PacketActionBroadcast COSMETIC_CHANGE — same {"c":[...]} format as the
   // REST endpoint). This is authoritative + live: it overrides the cached/REST data so cosmetic
   // changes show immediately, and seeds the cache so no REST fetch is needed for tracked players.
   public static void feed(UUID uuid, String json) {
      if (uuid == null || json == null) {
         return;
      }
      try {
         Map<Integer, String[]> result = new HashMap<Integer, String[]>();
         parse(json, result);
         CACHE.put(uuid, result);
         PENDING.remove(uuid);
      } catch (Throwable ignored) {
      }
   }

   // Cosmetics removed for a player (socket sent null) -> they now own nothing.
   public static void invalidate(UUID uuid) {
      if (uuid != null) {
         CACHE.put(uuid, new HashMap<Integer, String[]>());
         PENDING.remove(uuid);
      }
   }

   // Data array the player has for a cosmetic, or empty if absent.
   public static String[] data(UUID uuid, int cosmeticId) {
      Map<Integer, String[]> m = get(uuid);
      if (m == null) {
         return EMPTY;
      }
      String[] d = m.get(cosmeticId);
      return d == null ? EMPTY : d;
   }

   private static void fetch(final UUID uuid) {
      PENDING.add(uuid);
      Thread t = new Thread("LabyUserData-" + uuid) {
         @Override
         public void run() {
            Map<Integer, String[]> result = new HashMap<Integer, String[]>();
            try {
               String json = download(String.format(URL, uuid.toString()));
               if (json != null) {
                  parse(json, result);
               }
            } catch (Throwable ignored) {
            } finally {
               CACHE.put(uuid, result); // store even on failure/empty -> stops re-fetching every frame
               PENDING.remove(uuid);
            }
         }
      };
      t.setDaemon(true);
      t.start();
   }

   private static void parse(String json, Map<Integer, String[]> out) {
      JsonElement root = new JsonParser().parse(json);
      if (!root.isJsonObject() || !root.getAsJsonObject().has("c")) {
         return;
      }
      JsonArray arr = root.getAsJsonObject().get("c").getAsJsonArray();
      for (JsonElement e : arr) {
         if (!e.isJsonObject()) {
            continue;
         }
         JsonObject o = e.getAsJsonObject();
         if (!o.has("i")) {
            continue;
         }
         int id = o.get("i").getAsInt();
         List<String> data = new ArrayList<String>();
         if (o.has("d") && o.get("d").isJsonArray()) {
            for (JsonElement de : o.get("d").getAsJsonArray()) {
               data.add(de.isJsonNull() ? null : de.getAsString());
            }
         }
         out.put(id, data.toArray(new String[0]));
      }
   }

   private static String download(String url) {
      HttpURLConnection con = null;
      try {
         con = (HttpURLConnection) new URL(url).openConnection();
         con.setRequestMethod("GET");
         con.setConnectTimeout(5000);
         con.setReadTimeout(5000);
         con.setInstanceFollowRedirects(true);
         // laby.net is Cloudflare-fronted and 403s non-browser User-Agents, so use a browser UA.
         con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) EsdeathClient");
         con.setRequestProperty("Accept", "application/json");
         int code = con.getResponseCode();
         if (code != 200) {
            System.out.println("[LabyCosmetics] userdata HTTP " + code + " for " + url);
            return null;
         }
         InputStream in = con.getInputStream();
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         byte[] buf = new byte[8192];
         int n;
         while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
         }
         in.close();
         return new String(out.toByteArray(), "UTF-8");
      } catch (Throwable t) {
         return null;
      } finally {
         if (con != null) {
            con.disconnect();
         }
      }
   }
}
