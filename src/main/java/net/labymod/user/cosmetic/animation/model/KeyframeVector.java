package net.labymod.user.cosmetic.animation.model;

// Verbatim port of LabyMod 3's KeyframeVector.
public class KeyframeVector {
   public long offset;
   public double x;
   public double y;
   public double z;
   public boolean smooth;

   public KeyframeVector(long offset, double x, double y, double z, boolean smooth) {
      this.offset = offset;
      this.x = x;
      this.y = y;
      this.z = z;
      this.smooth = smooth;
   }

   public KeyframeVector(long offset) {
      this(offset, 0.0, 0.0, 0.0, false);
   }

   public KeyframeVector invert() {
      return new KeyframeVector(this.offset, -this.x, -this.y, -this.z, this.smooth);
   }
}
