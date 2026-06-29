package me.txb1.extras.cosmetics.laby.geo;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

// Catalog + geometry + type-texture cache for the LabyMod geometry cosmetics. The index
// (dl.labymod.net/cosmetics/index.json) gives each cosmetic's metadata; geometry is
// cosmetics/<id>/geo.json (parsed by LabyGeo), the shared TYPE_BOUND texture is
// cosmetics/<id>/texture.png. All fetches are async + cached (+ negative cache).
public final class LabyCosmetics {

   public static final class Meta {
      public int id;
      public String name;
      @SerializedName("texture_type") public String textureType = "TYPE_BOUND";
      @SerializedName("texture_directory") public String textureDirectory;
      @SerializedName("attached_to") public String attachedTo = "BODY";
      public double scale = 1.0D;
      @SerializedName("hide_cape") public boolean hideCape = false;
      public String type = "COSMETIC";
   }

   private static final Map<Integer, Meta> INDEX = new ConcurrentHashMap<Integer, Meta>();
   private static volatile boolean indexLoaded;
   private static volatile boolean indexPending;

   private static final Map<Integer, LabyGeo> GEO = new ConcurrentHashMap<Integer, LabyGeo>();
   private static final Set<Integer> GEO_PENDING = ConcurrentHashMap.newKeySet();
   private static final Set<Integer> GEO_FAILED = ConcurrentHashMap.newKeySet();

   private static final Map<Integer, ResourceLocation> TEX = new ConcurrentHashMap<Integer, ResourceLocation>();
   private static final Set<Integer> TEX_PENDING = ConcurrentHashMap.newKeySet();
   private static final Set<Integer> TEX_FAILED = ConcurrentHashMap.newKeySet();

   private LabyCosmetics() {
   }

   public static Meta meta(int id) {
      ensureIndex();
      return INDEX.get(id);
   }

   private static void ensureIndex() {
      if (indexLoaded || indexPending) {
         return;
      }
      indexPending = true;
      Thread t = new Thread("LabyIndex") {
         @Override
         public void run() {
            try {
               String json = get("https://dl.labymod.net/cosmetics/index.json");
               if (json != null) {
                  Map<String, Meta> raw = new Gson().fromJson(json, new TypeToken<Map<String, Meta>>() {}.getType());
                  if (raw != null) {
                     for (Meta m : raw.values()) {
                        if (m != null) {
                           INDEX.put(m.id, m);
                        }
                     }
                  }
               }
               indexLoaded = true;
            } catch (Throwable ignored) {
            } finally {
               indexPending = false;
            }
         }
      };
      t.setDaemon(true);
      t.start();
   }

   // Parsed geometry for a cosmetic, or null until loaded / on failure.
   public static LabyGeo geometry(int id) {
      LabyGeo g = GEO.get(id);
      if (g != null) {
         return g;
      }
      if (GEO_FAILED.contains(id) || GEO_PENDING.contains(id)) {
         return null;
      }
      GEO_PENDING.add(id);
      Thread t = new Thread("LabyGeo-" + id) {
         @Override
         public void run() {
            try {
               String json = get(String.format("https://dl.labymod.net/cosmetics/%d/geo.json", id));
               LabyGeo geo = json == null ? null : LabyGeo.parse(json);
               if (geo == null) {
                  GEO_FAILED.add(id);
               } else {
                  GEO.put(id, geo);
               }
            } catch (Throwable t2) {
               GEO_FAILED.add(id);
            } finally {
               GEO_PENDING.remove(id);
            }
         }
      };
      t.setDaemon(true);
      t.start();
      return null;
   }

   // Shared TYPE_BOUND texture cosmetics/<id>/texture.png.
   public static ResourceLocation typeTexture(int id) {
      ResourceLocation rl = TEX.get(id);
      if (rl != null) {
         return rl;
      }
      if (TEX_FAILED.contains(id) || TEX_PENDING.contains(id)) {
         return null;
      }
      TEX_PENDING.add(id);
      Thread t = new Thread("LabyTypeTex-" + id) {
         @Override
         public void run() {
            try {
               byte[] data = getBytes(String.format("https://dl.labymod.net/cosmetics/%d/texture.png", id));
               if (data == null || !isPng(data)) {
                  TEX_FAILED.add(id);
                  return;
               }
               final BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
               if (img == null) {
                  TEX_FAILED.add(id);
                  return;
               }
               Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                  @Override
                  public void run() {
                     try {
                        DynamicTexture tex = new DynamicTexture(img);
                        TEX.put(id, Minecraft.getMinecraft().getTextureManager()
                           .getDynamicTextureLocation("labygeotex_" + id, tex));
                     } catch (Throwable err) {
                        TEX_FAILED.add(id);
                     }
                  }
               });
            } catch (Throwable t2) {
               TEX_FAILED.add(id);
            } finally {
               TEX_PENDING.remove(id);
            }
         }
      };
      t.setDaemon(true);
      t.start();
      return null;
   }

   private static String get(String url) {
      byte[] b = getBytes(url);
      try {
         return b == null ? null : new String(b, "UTF-8");
      } catch (Throwable t) {
         return null;
      }
   }

   private static byte[] getBytes(String url) {
      HttpURLConnection con = null;
      try {
         con = (HttpURLConnection) new URL(url).openConnection();
         con.setRequestMethod("GET");
         con.setConnectTimeout(5000);
         con.setReadTimeout(5000);
         con.setInstanceFollowRedirects(true);
         con.setRequestProperty("User-Agent", "EsdeathClient");
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
      return b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 0x50 && b[2] == 0x4E && b[3] == 0x47;
   }
}
