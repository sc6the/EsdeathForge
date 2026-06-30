package net.labymod.user.cosmetic.animation;

// Verbatim port of LabyMod 3's EnumAnimationMetaType (anim_time_update flag keys).
public enum EnumAnimationMetaType {
   TRIGGER("trigger", 't'),
   PROBABILITY("probability", 'p'),
   FORCE("force", 'f'),
   QUEUE("queue", 'q'),
   SPEED("speed", 's'),
   CONDITION("condition", 'c');

   private String key;
   private char shortcut;

   public String getKey() {
      return this.key;
   }

   public char getShortcut() {
      return this.shortcut;
   }

   public static EnumAnimationMetaType get(String argument) {
      for (EnumAnimationMetaType meta : values()) {
         if (meta.key.equals(argument) || meta.shortcut == argument.charAt(0)) {
            return meta;
         }
      }

      return null;
   }

   private EnumAnimationMetaType(String key, char shortcut) {
      this.key = key;
      this.shortcut = shortcut;
   }
}
