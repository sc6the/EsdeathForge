package me.txb1.extras.snow;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.nio.file.Files;

// Appearance/config for the falling-snow overlay (main-menu snow + the Inventory Snow module share
// these). No external config lib — persisted as a single key=value line in EsdeathClient/snow.txt.
//   menuEnabled : snow on the main menu (toggled in the Theme -> Snow screen)
//   amount      : number of snowflakes (10..400)
//   color       : flake colour (RGB)
//   alpha       : flake transparency (0..255)
public final class SnowSettings {
   public static boolean menuEnabled = false;
   public static int amount = 90;
   public static int color = 0xFFFFFF;
   public static int alpha = 170;
   // cursor connecting-lines reach as a percentage (0 = off, 100 = max). Flake-to-flake links are
   // always on; this only controls the lines drawn toward the mouse cursor.
   public static int cursorLines = 100;

   private static final File FILE = new File(Minecraft.getMinecraft().mcDataDir, "EsdeathClient/snow.txt");

   private SnowSettings() {
   }

   static {
      load();
   }

   public static int clampAmount(int v) {
      return Math.max(10, Math.min(400, v));
   }

   public static int clampAlpha(int v) {
      return Math.max(0, Math.min(255, v));
   }

   public static int clampPercent(int v) {
      return Math.max(0, Math.min(100, v));
   }

   private static void load() {
      try {
         if (!FILE.isFile()) {
            return;
         }
         String s = new String(Files.readAllBytes(FILE.toPath()), "UTF-8").trim();
         for (String part : s.split(";")) {
            int eq = part.indexOf('=');
            if (eq <= 0) {
               continue;
            }
            String k = part.substring(0, eq).trim();
            String v = part.substring(eq + 1).trim();
            if ("menu".equals(k)) {
               menuEnabled = Boolean.parseBoolean(v);
            } else if ("amount".equals(k)) {
               amount = clampAmount(Integer.parseInt(v));
            } else if ("color".equals(k)) {
               color = Integer.parseInt(v, 16) & 0xFFFFFF;
            } else if ("alpha".equals(k)) {
               alpha = clampAlpha(Integer.parseInt(v));
            } else if ("cursor".equals(k)) {
               cursorLines = clampPercent(Integer.parseInt(v));
            }
         }
      } catch (Throwable ignored) {
      }
   }

   public static void save() {
      try {
         FILE.getParentFile().mkdirs();
         String s = "menu=" + menuEnabled
            + ";amount=" + amount
            + ";color=" + String.format("%06X", color & 0xFFFFFF)
            + ";alpha=" + alpha
            + ";cursor=" + cursorLines;
         Files.write(FILE.toPath(), s.getBytes("UTF-8"));
      } catch (Throwable ignored) {
      }
   }
}
