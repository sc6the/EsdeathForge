package net.labymod.user.cosmetic.geometry.effect;

import net.labymod.user.cosmetic.geometry.effect.effects.GeometryColor;
import net.labymod.user.cosmetic.geometry.effect.effects.GeometryCurrentTime;
import net.labymod.user.cosmetic.geometry.effect.effects.GeometryExtrude;
import net.labymod.user.cosmetic.geometry.effect.effects.GeometryGlow;
import net.labymod.user.cosmetic.geometry.effect.effects.GeometryHeadGravity;
import net.labymod.user.cosmetic.geometry.effect.effects.GeometryLayer;
import net.labymod.user.cosmetic.geometry.effect.effects.GeometryOrientation;
import net.labymod.user.cosmetic.geometry.effect.effects.GeometryPhysic;
import net.labymod.user.cosmetic.geometry.render.GeometryModelRenderer;

// Verbatim port of LabyMod 3's EnumEffectType: maps a bone-name prefix to its effect class and
// reflectively builds + loads the effect. (EXTRUDE is a no-op stub here — its depth-map extrusion
// path isn't ported — so extrude bones just render as plain geometry.)
public enum EnumEffectType {
   PHYSICS("physics", GeometryPhysic.class),
   HEAD_GRAVITY("headgravity", GeometryHeadGravity.class),
   COLOR("color", GeometryColor.class),
   LAYER("layer", GeometryLayer.class),
   EXTRUDE("extrude", GeometryExtrude.class),
   GLOW("glow", GeometryGlow.class),
   ORIENTATION("orientation", GeometryOrientation.class),
   CURRENT_TIME("currenttime", GeometryCurrentTime.class);

   private final String prefix;
   private final Class<? extends GeometryEffect> clazz;

   private EnumEffectType(String prefix, Class<? extends GeometryEffect> clazz) {
      this.prefix = prefix;
      this.clazz = clazz;
   }

   public static EnumEffectType getEffectType(String name) {
      for (EnumEffectType type : values()) {
         if (name.startsWith(type.prefix + "_")) {
            return type;
         }
      }

      return null;
   }

   public static GeometryEffect createEffect(String name, GeometryModelRenderer model) throws Exception {
      EnumEffectType type = getEffectType(name);
      return type == null ? null : type.create(name, model);
   }

   public GeometryEffect create(String name, GeometryModelRenderer model) throws Exception {
      return this.clazz.getConstructor(String.class, GeometryModelRenderer.class).newInstance(name, model).load();
   }
}
