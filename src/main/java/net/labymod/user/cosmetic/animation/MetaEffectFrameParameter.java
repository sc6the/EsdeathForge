package net.labymod.user.cosmetic.animation;

// Verbatim port of LabyMod 3's MetaEffectFrameParameter: the per-frame parameters the geometry
// effects read (movement-driven physics values + slim/right-side flags).
public class MetaEffectFrameParameter {
   public float forward;
   public float gravity;
   public float strafe;
   public float renderYawOffset;
   public float pitch;
   public boolean isSlim;
   public boolean rightSide;
}
