package net.labymod.user.cosmetic.animation.model;

// Verbatim port of LabyMod 3's BoneAnimation (rotation/position/scale timelines for one bone).
public class BoneAnimation {
   public Keyframes rotation = new Keyframes(0.0F, 0.0F, 0.0F);
   public Keyframes position = new Keyframes(0.0F, 0.0F, 0.0F);
   public Keyframes scale = new Keyframes(1.0F, 1.0F, 1.0F);

   public long getLength() {
      return Math.max(Math.max(this.position.getLength(), this.rotation.getLength()), this.scale.getLength());
   }
}
