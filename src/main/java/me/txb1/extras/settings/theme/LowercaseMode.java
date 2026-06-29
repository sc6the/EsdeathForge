package me.txb1.extras.settings.theme;

import java.io.File;
import java.nio.file.Files;
import net.minecraft.client.Minecraft;

// Global "All Lowercase" toggle, chosen in the Theme menu. When on, MixinFontRenderer lowercases
// every rendered string (menus, HUD, chat, world text — everything that goes through FontRenderer).
// Persisted to .minecraft/EsdeathClient/lowercase.txt so it survives restarts.
public final class LowercaseMode {
   public static boolean enabled;

   private static final File FILE = new File(Minecraft.getMinecraft().mcDataDir, "EsdeathClient/lowercase.txt");

   private LowercaseMode() {
   }

   static {
      try {
         if (FILE.isFile()) {
            enabled = "on".equals(new String(Files.readAllBytes(FILE.toPath()), "UTF-8").trim());
         }
      } catch (Throwable ignored) {
      }
   }

   public static void set(boolean on) {
      enabled = on;
      try {
         FILE.getParentFile().mkdirs();
         Files.write(FILE.toPath(), (on ? "on" : "off").getBytes("UTF-8"));
      } catch (Throwable ignored) {
      }
   }
}
