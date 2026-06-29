package me.proxycracked.universalaccountmanager.skin;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

// On-disk side of the Force Skin feature. Owns the config/skinforce/skin.png +
// skin.cfg layout (kept from the standalone ForceSkin mod days so existing
// users' files keep working). Any write here schedules ForceSkinLoader.reload()
// so the live session updates the local player's skin without a restart.
public final class ForceSkinManager {
  private ForceSkinManager() {}

  public static File dir() {
    return new File(Minecraft.getMinecraft().mcDataDir, "config/skinforce");
  }

  public static File skinFile() { return new File(dir(), "skin.png"); }
  public static File cfgFile()  { return new File(dir(), "skin.cfg"); }
  public static File skinDisabledFile() { return new File(dir(), "skin.png.disabled"); }
  public static File cfgDisabledFile()  { return new File(dir(), "skin.cfg.disabled"); }

  public static boolean exists()        { return skinFile().isFile(); }
  public static boolean isDisabled()    { return skinDisabledFile().isFile(); }

  // Soft-off: rename skin.png/skin.cfg to a *.disabled companion so the
  // loader stops picking them up (this launch and the next), but the data
  // stays around for a one-click re-enable. Uses Files.move atomically —
  // the bytes are never deleted, only renamed. Returns true if anything moved.
  public static boolean disable() {
    boolean moved = false;
    try {
      File s = skinFile(), sd = skinDisabledFile();
      if (s.isFile()) {
        Files.move(s.toPath(), sd.toPath(), StandardCopyOption.REPLACE_EXISTING);
        moved = true;
      }
      File c = cfgFile(), cd = cfgDisabledFile();
      if (c.isFile()) {
        Files.move(c.toPath(), cd.toPath(), StandardCopyOption.REPLACE_EXISTING);
        moved = true;
      }
    } catch (Exception ignored) {}
    if (moved) ForceSkinLoader.scheduleReload();
    return moved;
  }

  // Reverse of disable(). Returns true if the live skin file now exists.
  public static boolean enable() {
    try {
      File sd = skinDisabledFile(), s = skinFile();
      if (sd.isFile()) {
        Files.move(sd.toPath(), s.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
      File cd = cfgDisabledFile(), c = cfgFile();
      if (cd.isFile()) {
        Files.move(cd.toPath(), c.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (Exception ignored) {}
    boolean live = skinFile().isFile();
    ForceSkinLoader.scheduleReload();
    return live;
  }

  // Downloads skinUrl to skin.png and writes "slim=<bool>" to skin.cfg. Any
  // failure mid-write leaves the prior files alone — we stage to a .tmp first.
  public static void applyFromUrl(String skinUrl, boolean slim) throws Exception {
    HttpURLConnection conn = (HttpURLConnection) new URL(skinUrl).openConnection();
    conn.setConnectTimeout(10000);
    conn.setReadTimeout(15000);
    conn.setRequestProperty("User-Agent", "UniversalAccountManager");
    try (InputStream in = conn.getInputStream()) {
      java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
      byte[] chunk = new byte[8192];
      int n;
      while ((n = in.read(chunk)) > 0) buf.write(chunk, 0, n);
      applyFromBytes(buf.toByteArray(), slim);
    }
  }

  // Writes raw PNG bytes to skin.png and "slim=<bool>" to skin.cfg. Stages to
  // skin.png.tmp so a half-written file never replaces the live one.
  public static void applyFromBytes(byte[] pngBytes, boolean slim) throws Exception {
    if (pngBytes == null || pngBytes.length == 0) throw new Exception("Empty skin data");
    File d = dir();
    if (!d.isDirectory() && !d.mkdirs()) {
      throw new Exception("Couldn't create " + d.getAbsolutePath());
    }
    File tmp = new File(d, "skin.png.tmp");
    try (OutputStream out = new FileOutputStream(tmp)) {
      out.write(pngBytes);
    }

    File target = skinFile();
    if (target.exists() && !target.delete()) {
      throw new Exception("Couldn't replace " + target.getAbsolutePath());
    }
    if (!tmp.renameTo(target)) {
      throw new Exception("Couldn't move skin.png into place");
    }

    try (OutputStream out = new FileOutputStream(cfgFile())) {
      out.write(("slim=" + slim).getBytes(StandardCharsets.UTF_8));
    }

    // A fresh apply makes any prior *.disabled snapshot stale — toss it.
    File sd = skinDisabledFile(); if (sd.isFile()) sd.delete();
    File cd = cfgDisabledFile();  if (cd.isFile()) cd.delete();

    ForceSkinLoader.scheduleReload();
  }
}
