package me.txb1.extras.cosmetics.laby;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

// Resolves the LOCAL player's real premium UUID from their in-game name. With
// UniversalAccountManager the active session is often offline/cracked, so
// mc.getSession().getProfile().getId() is an offline UUID (derived from the name) that laby.net
// has no cosmetics for. LabyMod cosmetics are keyed by the real premium UUID, so for self we look
// the name up via Mojang's name->profile API and use that UUID for the cosmetic lookup instead.
public final class SelfLabyIdentity {

   private static final String API = "https://api.mojang.com/users/profiles/minecraft/%s";
   private static final Pattern DASHLESS = Pattern.compile(
      "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})");

   // name (lowercased) -> resolved premium UUID. Absent name with a started lookup sits in PENDING.
   private static final ConcurrentHashMap<String, UUID> RESOLVED = new ConcurrentHashMap<String, UUID>();
   private static final java.util.Set<String> PENDING = ConcurrentHashMap.newKeySet();

   private SelfLabyIdentity() {
   }

   // Premium UUID for the given name, or null until the async Mojang lookup completes / on failure.
   public static UUID premiumUuid(String name) {
      if (name == null || name.isEmpty()) {
         return null;
      }
      String key = name.toLowerCase();
      UUID u = RESOLVED.get(key);
      if (u != null) {
         return u;
      }
      if (!PENDING.contains(key)) {
         fetch(name, key);
      }
      return null;
   }

   private static void fetch(final String name, final String key) {
      PENDING.add(key);
      Thread t = new Thread("LabySelfUuid-" + name) {
         @Override
         public void run() {
            UUID resolved = null;
            try {
               String json = download(String.format(API, name));
               if (json != null) {
                  JsonObject o = new JsonParser().parse(json).getAsJsonObject();
                  if (o.has("id")) {
                     String id = o.get("id").getAsString();
                     resolved = parseDashless(id);
                  }
               }
            } catch (Throwable ignored) {
            } finally {
               if (resolved != null) {
                  RESOLVED.put(key, resolved);
               }
               PENDING.remove(key);
            }
         }
      };
      t.setDaemon(true);
      t.start();
   }

   private static UUID parseDashless(String id) {
      if (id == null) {
         return null;
      }
      if (id.indexOf('-') >= 0) {
         try {
            return UUID.fromString(id);
         } catch (Throwable t) {
            return null;
         }
      }
      java.util.regex.Matcher m = DASHLESS.matcher(id);
      if (!m.matches()) {
         return null;
      }
      try {
         return UUID.fromString(m.group(1) + "-" + m.group(2) + "-" + m.group(3) + "-" + m.group(4) + "-" + m.group(5));
      } catch (Throwable t) {
         return null;
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
         con.setRequestProperty("User-Agent", "EsdeathClient");
         int code = con.getResponseCode();
         if (code != 200) {
            // 204/404 = no such premium account (cracked-only name) -> leave unresolved.
            return null;
         }
         InputStream in = con.getInputStream();
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         byte[] buf = new byte[4096];
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
