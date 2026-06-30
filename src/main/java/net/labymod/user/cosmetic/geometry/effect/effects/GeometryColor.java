package net.labymod.user.cosmetic.geometry.effect.effects;

import java.awt.Color;
import net.labymod.user.cosmetic.animation.MetaEffectFrameParameter;
import net.labymod.user.cosmetic.geometry.effect.GeometryEffect;
import net.labymod.user.cosmetic.geometry.render.GeometryModelRenderer;
import net.labymod.user.cosmetic.remote.objects.data.RemoteData;

// Verbatim port of LM3 GeometryColor: bone "color_<i>" tints itself with the user's i-th RGB choice
// (or a cycling rainbow for "color_rainbow_<ms>").
public class GeometryColor extends GeometryEffect {
   private int index = 0;
   private boolean rainbow = false;
   private long cycleDuration = 0L;

   public GeometryColor(String name, GeometryModelRenderer model) {
      super(name, model);
   }

   @Override
   protected boolean parse() {
      String parameter = this.getParameter(0);
      if (parameter.equalsIgnoreCase("rainbow")) {
         this.rainbow = true;
         this.cycleDuration = Long.parseLong(this.getParameter(1));
      } else {
         this.index = Integer.parseInt(parameter);
      }

      return true;
   }

   @Override
   protected int getParametersAmount() {
      return 1;
   }

   @Override
   public void apply(RemoteData remoteData, MetaEffectFrameParameter meta) {
      if (this.rainbow) {
         this.model.color = new Color(Color.HSBtoRGB((float)(System.currentTimeMillis() % this.cycleDuration) / (float)this.cycleDuration, 0.8F, 0.8F));
      } else if (remoteData.colors.length > this.index) {
         this.model.color = remoteData.colors[this.index];
      }
   }
}
