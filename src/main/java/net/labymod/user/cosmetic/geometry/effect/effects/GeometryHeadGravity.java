package net.labymod.user.cosmetic.geometry.effect.effects;

import net.labymod.user.cosmetic.animation.MetaEffectFrameParameter;
import net.labymod.user.cosmetic.geometry.effect.GeometryEffect;
import net.labymod.user.cosmetic.geometry.effect.util.PhysicMapping;
import net.labymod.user.cosmetic.geometry.render.GeometryModelRenderer;
import net.labymod.user.cosmetic.remote.objects.data.RemoteData;

// Verbatim port of LM3 GeometryHeadGravity: bone "headgravity_<strength>_<map>_<mirror>" rotates from
// the player's head pitch.
public class GeometryHeadGravity extends GeometryEffect {
   private PhysicMapping mapping = PhysicMapping.X;
   private double strength = 1.0;
   private boolean mirror = false;

   public GeometryHeadGravity(String name, GeometryModelRenderer model) {
      super(name, model);
   }

   @Override
   protected boolean parse() {
      this.strength = (double)Integer.parseInt(this.getParameter(0)) / 100.0;
      String mappingString = this.getParameter(1, 1);
      String mirrorString = this.getParameter(2, 1);
      this.mapping = PhysicMapping.get(mappingString.charAt(0));
      this.mirror = mirrorString.charAt(0) == 'n';
      return true;
   }

   @Override
   protected int getParametersAmount() {
      return 5;
   }

   @Override
   public void apply(RemoteData remoteData, MetaEffectFrameParameter meta) {
      float rotation = (float)(-Math.toRadians((double)(meta.pitch * (float)(this.mirror ? -1 : 1)) * this.strength));
      switch (this.mapping) {
         case X:
            this.model.rotateAngleX = rotation;
            break;
         case Y:
            this.model.rotateAngleY = rotation;
            break;
         case Z:
            this.model.rotateAngleZ = rotation;
         default:
      }
   }
}
