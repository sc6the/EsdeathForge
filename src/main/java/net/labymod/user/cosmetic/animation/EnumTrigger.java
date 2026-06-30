package net.labymod.user.cosmetic.animation;

// Verbatim port of LabyMod 3's EnumTrigger.
public enum EnumTrigger {
   MOVING,
   IDLE,
   START_MOVING,
   STOP_MOVING,
   START_SNEAKING,
   STOP_SNEAKING,
   SNEAK_MOVING,
   SNEAK_IDLE;

   public static EnumTrigger getById(String id) {
      for (EnumTrigger trigger : values()) {
         if (trigger.name().equals(id)) {
            return trigger;
         }
      }

      return null;
   }
}
