package me.txb1.forge.gui.art;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.proxycracked.universalaccountmanager.utils.HttpUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// Wallhaven.cc search provider for the Art gallery. Uses the official JSON API
// (https://wallhaven.cc/api/v1/search) — no key needed for SFW browsing. Thumbnails are downloaded
// and uploaded as dynamic textures lazily (cached by wallpaper id), the same pattern the skin
// preview uses. Designed as a self-contained provider so more sources can be added later.
public final class WallhavenClient {
   private WallhavenClient() {
   }

   // one search result
   public static final class Result {
      public final String id;
      public final String thumbUrl; // small thumbnail
      public final String fullUrl;  // full-resolution image (the "path")
      public final String resolution;

      Result(String id, String thumbUrl, String fullUrl, String resolution) {
         this.id = id;
         this.thumbUrl = thumbUrl;
         this.fullUrl = fullUrl;
         this.resolution = resolution;
      }
   }

   // Blocking search (call off-thread). query may be empty -> top wallpapers. page is 1-based.
   public static List<Result> search(String query, int page) throws Exception {
      StringBuilder url = new StringBuilder("https://wallhaven.cc/api/v1/search?purity=100&categories=111");
      url.append("&page=").append(Math.max(1, page));
      if (query != null && !query.trim().isEmpty()) {
         url.append("&sorting=relevance&q=").append(URLEncoder.encode(query.trim(), "UTF-8"));
      } else {
         url.append("&sorting=toplist&topRange=1M");
      }
      String json = HttpUtils.get(url.toString());
      List<Result> out = new ArrayList<Result>();
      JsonElement root = new JsonParser().parse(json);
      if (!root.isJsonObject()) {
         return out;
      }
      JsonArray data = root.getAsJsonObject().getAsJsonArray("data");
      if (data == null) {
         return out;
      }
      for (JsonElement el : data) {
         JsonObject o = el.getAsJsonObject();
         String id = str(o, "id");
         String full = str(o, "path");
         String res = str(o, "resolution");
         String thumb = full;
         if (o.has("thumbs") && o.get("thumbs").isJsonObject()) {
            JsonObject th = o.getAsJsonObject("thumbs");
            if (th.has("small")) {
               thumb = th.get("small").getAsString();
            } else if (th.has("large")) {
               thumb = th.get("large").getAsString();
            }
         }
         if (id != null && full != null) {
            out.add(new Result(id, thumb, full, res));
         }
      }
      return out;
   }

   private static String str(JsonObject o, String key) {
      return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
   }

   // ---- thumbnail texture cache (lazy, async) ----
   private static final Map<String, ResourceLocation> THUMBS = new ConcurrentHashMap<String, ResourceLocation>();
   private static final Set<String> LOADING = new HashSet<String>();

   // Returns the thumbnail texture for a result, or null while it's still downloading. Kicks off the
   // async download the first time it's requested.
   public static ResourceLocation thumb(Result r) {
      ResourceLocation rl = THUMBS.get(r.id);
      if (rl != null) {
         return rl;
      }
      synchronized (LOADING) {
         if (LOADING.contains(r.id)) {
            return null;
         }
         LOADING.add(r.id);
      }
      final String id = r.id;
      final String thumbUrl = r.thumbUrl;
      new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               byte[] bytes = HttpUtils.getBytes(thumbUrl);
               final BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
               if (img != null) {
                  Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                     @Override
                     public void run() {
                        try {
                           ResourceLocation loc = Minecraft.getMinecraft().getTextureManager()
                              .getDynamicTextureLocation("wh_" + id, new DynamicTexture(img));
                           THUMBS.put(id, loc);
                        } catch (Throwable ignored) {
                        }
                     }
                  });
               }
            } catch (Throwable ignored) {
            } finally {
               synchronized (LOADING) {
                  LOADING.remove(id);
               }
            }
         }
      }, "Wallhaven-Thumb").start();
      return null;
   }

   // Download the full-resolution image bytes (blocking — call off-thread).
   public static byte[] downloadFull(Result r) throws Exception {
      return HttpUtils.getBytes(r.fullUrl);
   }
}
