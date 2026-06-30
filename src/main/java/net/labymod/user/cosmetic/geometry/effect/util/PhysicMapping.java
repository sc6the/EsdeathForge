package net.labymod.user.cosmetic.geometry.effect.util;

// Verbatim port of LabyMod 3's PhysicMapping (axis/source mapping for physics-driven effects).
public enum PhysicMapping {
   X,
   Y,
   Z,
   F,
   G,
   S,
   N;

   public static PhysicMapping get(char character) {
      return character == 'x'
         ? X
         : (character == 'y' ? Y : (character == 'z' ? Z : (character == 'f' ? F : (character == 's' ? S : (character == 'g' ? G : N)))));
   }
}
