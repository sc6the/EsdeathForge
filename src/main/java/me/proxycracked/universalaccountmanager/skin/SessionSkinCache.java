package me.proxycracked.universalaccountmanager.skin;

import me.proxycracked.universalaccountmanager.utils.HttpUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

// Async loader + cache for the FULL skin texture (not the cropped head) of a
// given username/UUID, plus the model variant (default/slim). Used by the
// live skin preview in GuiChangeSkin / GuiChangeUsername.
public final class SessionSkinCache {
  // Sentinel for "fetch finished, no skin found / network failed". Lets
  // the UI distinguish loading from permanent failure.
  public static final CachedSkin UNAVAILABLE = new CachedSkin(null, "default");

  public static final class CachedSkin {
    public final ResourceLocation rl;
    public final String type; // "default" or "slim"
    public CachedSkin(ResourceLocation rl, String type) {
      this.rl = rl;
      this.type = type;
    }
    public boolean isUnavailable() { return rl == null; }
  }

  private static final Map<String, CachedSkin> cache = new HashMap<>();
  private static final Set<String> loading = new HashSet<>();

  private static final ExecutorService EXEC = Executors.newSingleThreadExecutor(new ThreadFactory() {
    private final AtomicInteger n = new AtomicInteger(0);
    @Override public Thread newThread(Runnable r) {
      Thread t = new Thread(r, "UniversalAccountManager-SessionSkin-" + n.incrementAndGet());
      t.setDaemon(true);
      return t;
    }
  });

  private SessionSkinCache() {}

  public static CachedSkin get(String username, String uuid) {
    String key = key(username, uuid);
    if (key == null) return UNAVAILABLE;
    synchronized (cache) {
      CachedSkin c = cache.get(key);
      if (c != null) return c;
      if (loading.contains(key)) return null;
      loading.add(key);
    }
    EXEC.submit(() -> fetch(key, username, uuid));
    return null;
  }

  public static void invalidate(String username, String uuid) {
    String key = key(username, uuid);
    if (key == null) return;
    synchronized (cache) {
      cache.remove(key);
      loading.remove(key);
    }
  }

  // Seed the cache directly from a known skin URL + variant, bypassing
  // SkinChanger.resolveSkin. Used right after a skin upload — Mojang's
  // sessionserver caches profile textures for ~60s, so resolveSkin would
  // return the OLD skin URL for that window. We already know the URL we
  // just uploaded, so download it and replace the cache entry.
  public static void putFromUrl(String username, String uuid, String url, String variant) {
    String key = key(username, uuid);
    if (key == null || url == null || url.isEmpty()) return;
    synchronized (cache) {
      cache.remove(key);
      loading.add(key);
    }
    EXEC.submit(() -> fetchFromUrl(key, url, variant));
  }

  private static String key(String username, String uuid) {
    if (uuid != null && !uuid.isEmpty()) return uuid.replace("-", "").toLowerCase();
    if (username != null && !username.isEmpty()) return "name:" + username.toLowerCase();
    return null;
  }

  private static void fetch(String key, String username, String uuid) {
    try {
      SkinChanger.SkinInfo info = SkinChanger.resolveSkin(username);
      if (info == null) {
        markUnavailable(key);
        return;
      }
      loadAndStore(key, info.url, info.variant);
    } catch (Exception e) {
      markUnavailable(key);
    }
  }

  private static void fetchFromUrl(String key, String url, String variant) {
    try {
      loadAndStore(key, url, variant);
    } catch (Exception e) {
      markUnavailable(key);
    }
  }

  private static void loadAndStore(String key, String url, String variant) throws Exception {
    byte[] bytes = HttpUtils.getBytes(url);
    BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
    if (img == null) {
      markUnavailable(key);
      return;
    }
    // Run through Mojang's skin processor: forces alpha=255 on base-layer
    // regions (head/body/limbs) so opaque front faces don't bleed through
    // to the back, clears overlay-region pixels that shouldn't render
    // (hat/jacket transparency markers from legacy skins), and pads 64x32
    // legacy sheets up to 64x64 with the correct arm/leg copy.
    BufferedImage processed = new ImageBufferDownload().parseUserSkin(img);
    final BufferedImage finalImg = (processed != null) ? processed : img;
    final String type = "slim".equals(variant) ? "slim" : "default";
    Minecraft.getMinecraft().addScheduledTask(() -> {
      try {
        DynamicTexture tex = new DynamicTexture(finalImg);
        ResourceLocation rl = Minecraft.getMinecraft().getTextureManager()
          .getDynamicTextureLocation("uam_session_skin_" + key, tex);
        synchronized (cache) {
          cache.put(key, new CachedSkin(rl, type));
          loading.remove(key);
        }
      } catch (Exception e) {
        markUnavailable(key);
      }
    });
  }

  private static void markUnavailable(String key) {
    synchronized (cache) {
      cache.put(key, UNAVAILABLE);
      loading.remove(key);
    }
  }
}
