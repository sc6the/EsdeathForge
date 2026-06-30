package net.labymod.user.cosmetic.animation;

// LabyMod 3's animation conditions. (Condition evaluation against live entity motion is simplified
// to "always met" in this debloated port — idle cosmetics generally declare no conditions.)
public enum EnumCondition {
   MOTION_BACKWARDS,
   NO_MOTION,
   MOTION_FORWARD,
   SNEAKING,
   NOT_SNEAKING,
   IN_WATER,
   NOT_IN_WATER,
   ON_GROUND,
   IN_AIR;
}
