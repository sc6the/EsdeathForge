package net.labymod.user.cosmetic.geometry.effect.effects;

import net.labymod.user.cosmetic.animation.MetaEffectFrameParameter;
import net.labymod.user.cosmetic.geometry.effect.GeometryEffect;
import net.labymod.user.cosmetic.geometry.render.GeometryModelRenderer;
import net.labymod.user.cosmetic.remote.objects.data.RemoteData;

// Verbatim port of LM3 GeometryGlow: bone "glow_..." renders fullbright (disables lighting/lightmap).
public class GeometryGlow extends GeometryEffect {
   public GeometryGlow(String name, GeometryModelRenderer model) {
      super(name, model);
   }

   @Override
   protected boolean parse() {
      return true;
   }

   @Override
   protected int getParametersAmount() {
      return 0;
   }

   @Override
   public void apply(RemoteData remoteData, MetaEffectFrameParameter meta) {
      this.model.glow = true;
   }
}
