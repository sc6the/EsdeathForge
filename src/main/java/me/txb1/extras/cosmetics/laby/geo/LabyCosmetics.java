package me.txb1.extras.cosmetics.laby.geo;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
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
      // Maps each entry of a user's cosmetic data array to its meaning ("texture","rgb","offset",
      // "side"...). Drives RemoteData.loadData exactly like LM3.
      public String[] options;
      // Mirrored ARM/LEG cosmetics render on both limbs; mirror_type controls how the mirrored side
      // is transformed (DUPLICATE / MIRROR / ROTATE=rotate 180). LM3 RemoteObject defaults this to
      // DUPLICATE when absent — defaulting to null instead made a sneaker's 2nd boot hit the ROTATE
      // branch and face backwards.
      public boolean mirror = false;
      @SerializedName("mirror_type") public String mirrorType = "DUPLICATE";
      // The default appearance data LabyMod uses for preview / when the wearer hasn't customised it
      // (first entry is the default texture UUID, then the rgb/offset values per `options`). We use
      // this verbatim as the data array to render a cosmetic OFFLINE (no ownership needed).
      @SerializedName("default_data") public String[] defaultData;
   }

   private static final Map<Integer, Meta> INDEX = new ConcurrentHashMap<Integer, Meta>();
   private static volatile boolean indexLoaded;
   private static volatile boolean indexPending;

   private static final Map<Integer, LabyGeo> GEO = new ConcurrentHashMap<Integer, LabyGeo>();
   private static final Set<Integer> GEO_PENDING = ConcurrentHashMap.newKeySet();
   private static final Set<Integer> GEO_FAILED = ConcurrentHashMap.newKeySet();

   // Real ported LabyMod geometry engine (BlockBenchLoader: model tree + named-bone map) per id.
   // We keep the loader (not just the root renderer) so we can resolve the "color_N" bones and tint
   // them from the user's per-cosmetic RGB data (LM3's GeometryColor effect).
   private static final Map<Integer, net.labymod.user.cosmetic.geometry.BlockBenchLoader> ENGINE =
      new ConcurrentHashMap<Integer, net.labymod.user.cosmetic.geometry.BlockBenchLoader>();
   private static final Set<Integer> ENGINE_PENDING = ConcurrentHashMap.newKeySet();
   private static final Set<Integer> ENGINE_FAILED = ConcurrentHashMap.newKeySet();

   // Parsed animation.json (idle keyframes) per cosmetic id.
   private static final Map<Integer, net.labymod.user.cosmetic.animation.AnimationLoader> ANIM =
      new ConcurrentHashMap<Integer, net.labymod.user.cosmetic.animation.AnimationLoader>();
   private static final Set<Integer> ANIM_PENDING = ConcurrentHashMap.newKeySet();
   private static final Set<Integer> ANIM_FAILED = ConcurrentHashMap.newKeySet();

   // Per-texture cache, keyed by the texture UUID the user picked (so two users with the same
   // cosmetic but different texture choices each get their own).
   private static final Map<String, ResourceLocation> TEX = new ConcurrentHashMap<String, ResourceLocation>();
   private static final Set<String> TEX_PENDING = ConcurrentHashMap.newKeySet();
   private static final Set<String> TEX_FAILED = ConcurrentHashMap.newKeySet();

   // Per-texture alpha depth map (built from the same PNG as the texture), used by the extrude effect
   // to cull voxel faces. Keyed identically to TEX.
   private static final Map<String, net.labymod.user.cosmetic.custom.DepthMap> DEPTH =
      new ConcurrentHashMap<String, net.labymod.user.cosmetic.custom.DepthMap>();

   // The depth map for a cosmetic texture, or null until the texture has been fetched + decoded.
   public static net.labymod.user.cosmetic.custom.DepthMap depthMap(int id, String dir, String textureUuid) {
      if (textureUuid == null) {
         return null;
      }
      return DEPTH.get((dir == null ? "" : dir) + "/" + textureUuid);
   }

   private LabyCosmetics() {
   }

   public static Meta meta(int id) {
      ensureIndex();
      return INDEX.get(id);
   }

   // True once the remote cosmetic index has been fetched + parsed.
   public static boolean isIndexLoaded() {
      return indexLoaded;
   }

   // Kick off the index fetch (no-op if already loading/loaded). Lets startup warm the catalog so the
   // offline LabyMod cosmetics appear in the menu without waiting for the first meta() lookup.
   public static void loadIndex() {
      ensureIndex();
   }

   // Snapshot of every catalogued cosmetic (id -> Meta), for building the offline selectable list.
   public static java.util.Collection<Meta> allMetas() {
      ensureIndex();
      return INDEX.values();
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
                  // index.json is { "cosmetics": { "<id>": { ...Meta... }, ... } }
                  Gson gson = new Gson();
                  JsonElement root = new JsonParser().parse(json);
                  if (root != null && root.isJsonObject()) {
                     JsonObject obj = root.getAsJsonObject();
                     JsonObject cosmetics = obj.has("cosmetics") && obj.get("cosmetics").isJsonObject()
                        ? obj.getAsJsonObject("cosmetics") : obj;
                     for (Map.Entry<String, JsonElement> en : cosmetics.entrySet()) {
                        if (en.getValue() == null || !en.getValue().isJsonObject()) {
                           continue;
                        }
                        Meta m = gson.fromJson(en.getValue(), Meta.class);
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

   // Real LabyMod geometry engine renderer for a cosmetic, or null until loaded / on failure.
   // Parsing + model-tree build happens off-thread; the display list is compiled lazily on the
   // render thread inside GeometryModelRenderer.render(...).
   public static net.labymod.user.cosmetic.geometry.BlockBenchLoader geometryEngine(final int id) {
      net.labymod.user.cosmetic.geometry.BlockBenchLoader g = ENGINE.get(id);
      if (g != null) {
         return g;
      }
      if (ENGINE_FAILED.contains(id) || ENGINE_PENDING.contains(id)) {
         return null;
      }
      ENGINE_PENDING.add(id);
      Thread t = new Thread("LabyGeoEngine-" + id) {
         @Override
         public void run() {
            try {
               String json = get(String.format("https://dl.labymod.net/cosmetics/%d/geo.json", id));
               if (json == null) {
                  ENGINE_FAILED.add(id);
                  return;
               }
               net.labymod.user.cosmetic.geometry.BlockBenchLoader loader =
                  new net.labymod.user.cosmetic.geometry.GeometryLoader(json).toBlockBenchLoader();
               if (loader == null) {
                  ENGINE_FAILED.add(id);
               } else {
                  ENGINE.put(id, loader);
               }
            } catch (Throwable t2) {
               ENGINE_FAILED.add(id);
            } finally {
               ENGINE_PENDING.remove(id);
            }
         }
      };
      t.setDaemon(true);
      t.start();
      return null;
   }

   // A selectable "style" of a cosmetic = one of its layer_<uuid>_<name> variants (e.g. the katana's
   // backleft / dual / hipleft). The GeometryLayer effect shows a variant only when the user's texture
   // UUID equals that layer's uuid, so picking a style == setting the cosmetic's texture data to the
   // layer's uuid (dashed form).
   public static final class Style {
      public final String uuid; // dashed UUID string used as the texture data value
      public final String name; // display name (the bone-name suffix)

      Style(String uuid, String name) {
         this.uuid = uuid;
         this.name = name;
      }
   }

   // Enumerate a cosmetic's styles from its (loaded) geometry. Empty while the geometry is still
   // downloading or if the cosmetic has only a single variant.
   public static java.util.List<Style> styles(int id) {
      java.util.ArrayList<Style> out = new java.util.ArrayList<Style>();
      net.labymod.user.cosmetic.geometry.BlockBenchLoader loader = geometryEngine(id);
      if (loader == null) {
         return out;
      }
      java.util.LinkedHashMap<String, String> byUuid = new java.util.LinkedHashMap<String, String>();
      for (String bone : loader.getItems().keySet()) {
         if (bone == null || !bone.startsWith("layer_")) {
            continue;
         }
         String[] p = bone.split("_");
         if (p.length < 2) {
            continue;
         }
         String id32 = p[1];
         if (id32.length() != 32) {
            continue; // skip layer_slim / layer_right (skin/side filters, not styles)
         }
         // style name = the bone suffix after the uuid (skip the "_negate" inverse-variant bones)
         StringBuilder nm = new StringBuilder();
         for (int i = 2; i < p.length; i++) {
            if ("negate".equals(p[i])) {
               nm.setLength(0);
               break;
            }
            if (nm.length() > 0) {
               nm.append(' ');
            }
            nm.append(p[i]);
         }
         if (nm.length() == 0) {
            continue;
         }
         try {
            String dashed = id32.substring(0, 8) + "-" + id32.substring(8, 12) + "-" + id32.substring(12, 16)
               + "-" + id32.substring(16, 20) + "-" + id32.substring(20, 32);
            if (!byUuid.containsKey(dashed)) {
               byUuid.put(dashed, prettify(nm.toString()));
            }
         } catch (Exception ignored) {
         }
      }
      for (java.util.Map.Entry<String, String> e : byUuid.entrySet()) {
         out.add(new Style(e.getKey(), e.getValue()));
      }
      return out;
   }

   private static String prettify(String s) {
      if (s.isEmpty()) {
         return s;
      }
      String[] words = s.split(" ");
      StringBuilder b = new StringBuilder();
      for (String w : words) {
         if (w.isEmpty()) {
            continue;
         }
         if (b.length() > 0) {
            b.append(' ');
         }
         b.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
      }
      return b.toString();
   }

   // Parsed animation.json for a cosmetic, or null until loaded / on failure / when empty.
   public static net.labymod.user.cosmetic.animation.AnimationLoader animationEngine(final int id) {
      net.labymod.user.cosmetic.animation.AnimationLoader a = ANIM.get(id);
      if (a != null) {
         return a;
      }
      if (ANIM_FAILED.contains(id) || ANIM_PENDING.contains(id)) {
         return null;
      }
      ANIM_PENDING.add(id);
      Thread t = new Thread("LabyAnim-" + id) {
         @Override
         public void run() {
            try {
               String json = get(String.format("https://dl.labymod.net/cosmetics/%d/animation.json", id));
               if (json == null) {
                  ANIM_FAILED.add(id);
                  return;
               }
               net.labymod.user.cosmetic.animation.AnimationLoader loader =
                  new net.labymod.user.cosmetic.animation.AnimationLoader(json).load();
               if (loader.isEmpty()) {
                  ANIM_FAILED.add(id); // nothing to animate; stop re-fetching
               } else {
                  ANIM.put(id, loader);
               }
            } catch (Throwable t2) {
               ANIM_FAILED.add(id);
            } finally {
               ANIM_PENDING.remove(id);
            }
         }
      };
      t.setDaemon(true);
      t.start();
      return null;
   }

   // The cosmetic's texture, fetched exactly like LabyMod 3 (UserTextureContainer.resolved):
   //   - texture_directory set  -> https://dl.labymod.net/textures/<dir>/<textureUuid>   (NO .png!)
   //   - texture_directory null -> https://dl.labymod.net/cosmetics/<id>/textures/<textureUuid>.png
   // textureUuid is the texture id the user picked (data[0] for TYPE_BOUND, the user's UUID for
   // USER_BOUND). Cached/keyed by textureUuid (+ dir) since users pick different textures.
   public static ResourceLocation cosmeticTexture(final int id, final String dir, final String textureUuid) {
      if (textureUuid == null) {
         return null;
      }
      final String key = (dir == null ? "" : dir) + "/" + textureUuid;
      ResourceLocation rl = TEX.get(key);
      if (rl != null) {
         return rl;
      }
      if (TEX_FAILED.contains(key) || TEX_PENDING.contains(key)) {
         return null;
      }
      TEX_PENDING.add(key);
      final String url = (dir != null && !dir.isEmpty())
         ? "https://dl.labymod.net/textures/" + dir + "/" + textureUuid
         : String.format("https://dl.labymod.net/cosmetics/%d/textures/%s.png", id, textureUuid);
      Thread t = new Thread("LabyTex-" + key) {
         @Override
         public void run() {
            try {
               byte[] data = getBytes(url);
               if (data == null || !isPng(data)) {
                  TEX_FAILED.add(key);
                  return;
               }
               final BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
               if (img == null) {
                  TEX_FAILED.add(key);
                  return;
               }
               // Build the alpha depth map off-thread (extrude effect uses it to cull voxel faces).
               try {
                  DEPTH.put(key, new net.labymod.user.cosmetic.custom.DepthMap(img));
               } catch (Throwable ignored) {
               }
               Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                  @Override
                  public void run() {
                     try {
                        DynamicTexture tex = new DynamicTexture(img);
                        TEX.put(key, Minecraft.getMinecraft().getTextureManager()
                           .getDynamicTextureLocation("labytex_" + key.replace('/', '_'), tex));
                     } catch (Throwable err) {
                        TEX_FAILED.add(key);
                     }
                  }
               });
            } catch (Throwable t2) {
               TEX_FAILED.add(key);
            } finally {
               TEX_PENDING.remove(key);
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
