package me.txb1.extras.cosmetics.laby;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

// Resolves a player's LabyMod cape. The old dl.labymod.net/cosmetics/0/textures/<uuid>.png path is
// dead (404 for everyone); the live cape texture is served by the laby.net items CDN keyed by the
// player's account UUID: https://items.laby.net/<CAPE_GROUP>/textures/<uuid>.png (verified 200 vs
// 404 for owners/non-owners). Returns the 22x17 LabyMod cape PNG, which is NOT the vanilla 64x32
// layout -- LayerLabyCape renders it with a matching cape model. We fetch asynchronously, validate
// the PNG, upload it on the render thread, and cache per UUID (with a negative cache so missing
// capes aren't re-requested every frame). Gated on the cape priority being set to Labymod.
public final class LabyCapes {

   public static final String COSMETIC_NAME = "LabyMod Cape";
   // The "Cloak" cosmetic group id on the laby.net items CDN (the dl.labymod.net/capes/<uuid>.png
   // redirect resolves here). Per-user cape = <group>/textures/<dashed-lowercase-uuid>.png.
   private static final String CLOAK_URL =
      "https://items.laby.net/00e8db8c-79b2-4158-88a7-7c2aa17121b3/textures/%s.png";

   private static final ConcurrentHashMap<UUID, ResourceLocation> CACHE = new ConcurrentHashMap<UUID, ResourceLocation>();
   private static final Set<UUID> PENDING = ConcurrentHashMap.newKeySet();
   private static final Set<UUID> FAILED = ConcurrentHashMap.newKeySet();

   private LabyCapes() {
   }

   // LabyMod capes render (yours + other players') only when the cape priority is set to Labymod.
   public static boolean enabled() {
      return me.txb1.extras.capes.CapePriority.isLabymod();
   }

   // Cached cape for this player, or null if none / not fetched yet (kicks off an async fetch).
   public static ResourceLocation get(UUID uuid) {
      if (uuid == null) {
         return null;
      }
      ResourceLocation rl = CACHE.get(uuid);
      if (rl != null) {
         return rl;
      }
      if (FAILED.contains(uuid) || PENDING.contains(uuid)) {
         return null;
      }
      fetch(uuid);
      return null;
   }

   private static void fetch(final UUID uuid) {
      PENDING.add(uuid);
      Thread t = new Thread("LabyCape-" + uuid) {
         @Override
         public void run() {
            try {
               byte[] data = download(String.format(CLOAK_URL, uuid.toString()));
               if (data == null || !isPng(data)) {
                  FAILED.add(uuid);
                  return;
               }
               final BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
               if (img == null) {
                  FAILED.add(uuid);
                  return;
               }
               Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                  @Override
                  public void run() {
                     try {
                        DynamicTexture tex = new DynamicTexture(img);
                        ResourceLocation loc = Minecraft.getMinecraft().getTextureManager()
                           .getDynamicTextureLocation("labycape_" + uuid.toString().replace("-", ""), tex);
                        CACHE.put(uuid, loc);
                     } catch (Throwable err) {
                        FAILED.add(uuid);
                     }
                  }
               });
            } catch (Throwable err) {
               FAILED.add(uuid);
            } finally {
               PENDING.remove(uuid);
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
         con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) EsdeathClient");
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
