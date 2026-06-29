package me.txb1.extras.sound;

import me.txb1.EsdeathClient;

// User-set volume multiplier for redstone-component sounds (buttons, levers, pressure plates,
// pistons, note blocks, repeaters/comparators, dispensers — all of which play "random.click",
// "tile.piston.*" or "note.*"). 1.0 = unchanged, 0 = muted. Persisted in FireDB. Read by
// RedstoneSoundHandler; edited by the sound-sliders GUI.
public final class RedstoneVolume {
   private static float value = 1.0F;
   private static boolean loaded;

   private RedstoneVolume() {
   }

   public static float get() {
      ensure();
      return value;
   }

   public static void set(float v) {
      if (v < 0.0F) {
         v = 0.0F;
      } else if (v > 2.0F) {
         v = 2.0F;
      }
      value = v;
   }

   private static void ensure() {
      if (loaded) {
         return;
      }
      loaded = true;
      try {
         Object v = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("redstone_volume");
         if (v != null) {
            value = Float.parseFloat(String.valueOf(v));
         }
      } catch (Exception ignored) {
      }
   }

   public static void save() {
      try {
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("redstone_volume", String.valueOf(value));
         EsdeathClient.getInstance().getFireDB().getDataBase().push();
      } catch (Exception ignored) {
      }
   }
}
