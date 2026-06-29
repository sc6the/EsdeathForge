package me.txb1.extras.cosmetics.laby;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

// Per-user cosmetic textures from the LabyMod CDN: https://dl.labymod.net/cosmetics/<id>/textures/
// <textureUuid>.png (the textureUuid comes from the player's userdata `d` array). Async fetch, PNG
// validation, render-thread upload, cache keyed by id|uuid. Used by the per-user-textured cosmetics
// (cap, bandana, watch, …) and the cloak.
public final class LabyTextures {

   private static final String URL = "https://dl.labymod.net/cosmetics/%d/textures/%s.png";
   private static final ConcurrentHashMap<String, ResourceLocation> CACHE = new ConcurrentHashMap<String, ResourceLocation>();
   private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();
   private static final Set<String> FAILED = ConcurrentHashMap.newKeySet();

   private LabyTextures() {
   }

   // Texture for cosmetic <id> bound to <textureUuid> (a UUID string from the user's cosmetic data).
   // Returns null until fetched / if missing; kicks off the async fetch on first request.
   public static ResourceLocation get(int cosmeticId, String textureUuid) {
      if (textureUuid == null || textureUuid.isEmpty()) {
         return null;
      }
      String key = cosmeticId + "|" + textureUuid;
      ResourceLocation rl = CACHE.get(key);
      if (rl != null) {
         return rl;
      }
      if (FAILED.contains(key) || PENDING.contains(key)) {
         return null;
      }
      fetch(cosmeticId, textureUuid, key);
      return null;
   }

   private static void fetch(final int cosmeticId, final String textureUuid, final String key) {
      PENDING.add(key);
      Thread t = new Thread("LabyTexture-" + key) {
         @Override
         public void run() {
            try {
               byte[] data = download(String.format(URL, cosmeticId, textureUuid));
               if (data == null || !isPng(data)) {
                  FAILED.add(key);
                  return;
               }
               final BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
               if (img == null) {
                  FAILED.add(key);
                  return;
               }
               Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                  @Override
                  public void run() {
                     try {
                        DynamicTexture tex = new DynamicTexture(img);
                        ResourceLocation loc = Minecraft.getMinecraft().getTextureManager()
                           .getDynamicTextureLocation("labytex_" + cosmeticId + "_" + textureUuid.replace("-", ""), tex);
                        CACHE.put(key, loc);
                     } catch (Throwable err) {
                        FAILED.add(key);
                     }
                  }
               });
            } catch (Throwable err) {
               FAILED.add(key);
            } finally {
               PENDING.remove(key);
            }
         }
      };
      t.setDaemon(true);
      t.start();
   }

   private static byte[] download(String url) {
      HttpURLConnection con = null;
      try {
         con = (HttpURLConnection) new URL(url).openConnection();
         con.setRequestMethod("GET");
         con.setConnectTimeout(5000);
         con.setReadTimeout(5000);
         con.setInstanceFollowRedirects(true);
         con.setRequestProperty("User-Agent", "EsdeathClient");
         con.setRequestProperty("Accept", "image/png");
         if (con.getResponseCode() != 200) {
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
         return out.toByteArray();
      } catch (Throwable t) {
         return null;
      } finally {
         if (con != null) {
            con.disconnect();
         }
      }
   }

   private static boolean isPng(byte[] b) {
      return b.length > 8
         && (b[0] & 0xFF) == 0x89 && b[1] == 0x50 && b[2] == 0x4E && b[3] == 0x47
         && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A;
   }
}
