package me.proxycracked.universalaccountmanager.skin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

// Runtime side of the Force Skin feature. Owns the dynamic skin texture the
// MixinAbstractClientPlayer hands back for the local player. The on-disk
// shape (config/skinforce/skin.png + skin.cfg) is managed by ForceSkinManager;
// this class only reads it and exposes it as a ResourceLocation.
//
// reload() is callable at runtime so the Force Skin GUI's Apply/Enable/Disable
// buttons can update the live session without requiring a restart. It
// deletes the previously-registered dynamic texture before re-registering so
// the TextureManager doesn't leak GL handles.
public final class ForceSkinLoader {

    private static final String TEXTURE_KEY = "uam_force_skin";

    private static ResourceLocation skinLocation = null;
    private static boolean slim = false;
    private static boolean hasSkin = false;

    private ForceSkinLoader() {}

    public static void init() {
        File dir = ForceSkinManager.dir();
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("[UniversalAccountManager] Couldn't create " + dir.getAbsolutePath());
        }
        reload();
    }

    // Re-read skin.png + skin.cfg from disk. Safe to call from any thread that
    // currently holds the GL context — i.e. event handlers and GUI button
    // callbacks. The Force Skin GUI calls it on the same thread that ran the
    // file write, which on 1.8.9 Forge is the render thread.
    public static synchronized void reload() {
        Minecraft mc = Minecraft.getMinecraft();
        TextureManager tm = mc.getTextureManager();

        slim = readSlimFlag();

        File skinFile = ForceSkinManager.skinFile();
        if (!skinFile.isFile()) {
            clearTexture(tm);
            return;
        }

        BufferedImage img = null;
        try (FileInputStream in = new FileInputStream(skinFile)) {
            img = ImageIO.read(in);
        } catch (Exception e) {
            System.err.println("[UniversalAccountManager] Force Skin: failed to read skin.png: " + e.getMessage());
        }

        if (img == null) {
            clearTexture(tm);
            return;
        }

        // Drop the old dynamic texture before replacing it — otherwise the
        // TextureManager keeps the underlying GL texture alive.
        if (skinLocation != null) {
            try { tm.deleteTexture(skinLocation); } catch (Exception ignored) {}
        }

        skinLocation = tm.getDynamicTextureLocation(TEXTURE_KEY, new DynamicTexture(img));
        hasSkin = skinLocation != null;
    }

    private static void clearTexture(TextureManager tm) {
        if (skinLocation != null) {
            try { tm.deleteTexture(skinLocation); } catch (Exception ignored) {}
        }
        skinLocation = null;
        hasSkin = false;
    }

    private static boolean readSlimFlag() {
        File cfg = ForceSkinManager.cfgFile();
        if (!cfg.isFile()) return false;
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(cfg)) {
            props.load(in);
        } catch (Exception e) {
            System.err.println("[UniversalAccountManager] Force Skin: failed to read skin.cfg: " + e.getMessage());
            return false;
        }
        String v = props.getProperty("slim", "false");
        return Boolean.parseBoolean(v.trim());
    }

    // Posts reload() to Minecraft's main thread. Worker threads (the Force
    // Skin GUI's Apply/Enable/Disable callbacks) must use this — touching the
    // TextureManager off-thread crashes the client.
    public static void scheduleReload() {
        try {
            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                @Override public void run() { reload(); }
            });
        } catch (Exception e) {
            System.err.println("[UniversalAccountManager] Force Skin: failed to schedule reload: " + e.getMessage());
        }
    }

    public static ResourceLocation getSkinLocation() { return skinLocation; }
    public static boolean isSlim()                   { return slim; }
    public static boolean hasSkin()                  { return hasSkin; }
}
