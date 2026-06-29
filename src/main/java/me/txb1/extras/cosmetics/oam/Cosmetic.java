package me.txb1.extras.cosmetics.oam;

// Offline shim for OAM's data-driven Cosmetic (the original pulled per-user JSON from a backend).
// EsdeathForge has no backend, so every accessor returns the "unset" value and each cosmetic
// falls back to its built-in defaults (default tail count, default texture variant, default RGB).
// Custom colour/transparency is layered on top by CosmeticModelRenderer via CosmeticController.
public class Cosmetic {
   public double height;
   // display name of the cosmetic this shim belongs to (set by OamCosmeticLayer), used to look up
   // the user's chosen variant options (e.g. Konoha "renegade") from CosmeticController.
   public String cosmeticName;

   public boolean isMulticolor(String key) {
      return false;
   }

   // length != 3 -> renderRGB uses the cosmetic's default colour
   public int[] getRGB(String key) {
      return new int[]{0};
   }

   public int getInteger(String key) {
      try {
         return Integer.parseInt(this.getString(key));
      } catch (NumberFormatException e) {
         return 0;
      }
   }

   public float getFloat(String key) {
      try {
         return Float.parseFloat(this.getString(key));
      } catch (NumberFormatException e) {
         return 0.0f;
      }
   }

   public String getString(String key) {
      if (this.cosmeticName == null) {
         return "null";
      }
      return me.txb1.extras.cosmetics.CosmeticController.getOption(this.cosmeticName, key, "null");
   }
}
