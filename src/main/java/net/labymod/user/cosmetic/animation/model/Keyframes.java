package net.labymod.user.cosmetic.animation.model;

import java.util.ArrayList;
import java.util.List;

// Verbatim port of LabyMod 3's Keyframes (per-channel timeline with linear / sigmoid interpolation).
public class Keyframes {
   private List<KeyframeVector> keyframes = new ArrayList<KeyframeVector>();
   private final float defaultX;
   private final float defaultY;
   private final float defaultZ;

   public Keyframes(float defaultX, float defaultY, float defaultZ) {
      this.defaultX = defaultX;
      this.defaultY = defaultY;
      this.defaultZ = defaultZ;
   }

   public KeyframeVector get(long offset) {
      KeyframeVector from = new KeyframeVector(0L, (double)this.defaultX, (double)this.defaultY, (double)this.defaultZ, false);

      for (int i = 0; i < this.keyframes.size(); i++) {
         KeyframeVector current = this.keyframes.get(i);
         if (current.offset >= offset) {
            return this.process(from, current, offset, current.smooth);
         }

         from = current;
      }

      return from;
   }

   public long getLength() {
      return this.keyframes.isEmpty() ? 0L : this.keyframes.get(this.keyframes.size() - 1).offset;
   }

   public void add(long offset, KeyframeVector vector) {
      this.keyframes.add(vector);
   }

   public void add(long offset, double x, double y, double z, boolean smooth) {
      this.keyframes.add(new KeyframeVector(offset, x, y, z, smooth));
   }

   private KeyframeVector process(KeyframeVector from, KeyframeVector to, long offset, boolean interpolate) {
      long progress = offset - from.offset;
      long duration = to.offset - from.offset;
      KeyframeVector vector = new KeyframeVector(offset);
      vector.x = process(from.x, to.x, progress, duration, interpolate);
      vector.y = process(from.y, to.y, progress, duration, interpolate);
      vector.z = process(from.z, to.z, progress, duration, interpolate);
      return vector;
   }

   public static double process(double x, double x2, long progress, long duration, boolean interpolate) {
      if (x == x2 || duration == 0L || progress > duration) {
         return x2;
      } else {
         return interpolate ? interpolate(x, x2, (double)progress, (double)duration) : linear(x, x2, progress, duration);
      }
   }

   public static double interpolate(double startY, double endY, double currentTime, double endTime) {
      return startY != endY && endTime != 0.0 && !(currentTime > endTime) ? startY + sigmoid(currentTime / endTime * 4.0) * (endY - startY) : endY;
   }

   public static double linear(double root, double target, long progress, long animationDuration) {
      if (root != target && animationDuration != 0L && progress <= animationDuration) {
         double difference = root - target;
         return root - difference / (double)animationDuration * (double)progress;
      } else {
         return target;
      }
   }

   private static double sigmoid(double input) {
      return 1.0 / (1.0 + Math.exp(-input * 2.0 + 4.0));
   }
}
