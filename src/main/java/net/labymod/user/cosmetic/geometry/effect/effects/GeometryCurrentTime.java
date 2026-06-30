package net.labymod.user.cosmetic.geometry.effect.effects;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import net.labymod.user.cosmetic.animation.MetaEffectFrameParameter;
import net.labymod.user.cosmetic.geometry.effect.GeometryEffect;
import net.labymod.user.cosmetic.geometry.effect.util.PhysicMapping;
import net.labymod.user.cosmetic.geometry.render.GeometryModelRenderer;
import net.labymod.user.cosmetic.remote.objects.data.RemoteData;

// Verbatim port of LM3 GeometryCurrentTime: rotates a bone based on real wall-clock time (e.g. clock
// hands).
public class GeometryCurrentTime extends GeometryEffect {
   private PhysicMapping mapping = PhysicMapping.X;
   private boolean mirror;
   private int cycle = 1;
   private int offset;
   private int interval = 1;

   public GeometryCurrentTime(String name, GeometryModelRenderer model) {
      super(name, model);
   }

   @Override
   protected boolean parse() {
      String mappingString = this.getParameter(0, 1);
      String mirrorString = this.getParameter(1, 1);
      this.mapping = PhysicMapping.get(mappingString.charAt(0));
      this.mirror = mirrorString.charAt(0) == 'n';
      this.cycle = Math.max(1, Integer.parseInt(this.getParameter(2)));
      this.offset = Integer.parseInt(this.getParameter(3));
      this.interval = Math.max(1, Integer.parseInt(this.getParameter(4)));
      return true;
   }

   @Override
   protected int getParametersAmount() {
      return 5;
   }

   @Override
   public void apply(RemoteData remoteData, MetaEffectFrameParameter meta) {
      ZonedDateTime nowZoned = ZonedDateTime.now();
      Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone()).toInstant();
      Duration duration = Duration.between(midnight, Instant.now());
      long millisToday = duration.toMillis();
      long time = millisToday % (1000L * (long)this.cycle);
      float seconds = (float)((int)(time / (long)this.interval) * this.interval) / 1000.0F;
      float progress = seconds % (float)this.cycle + (float)this.offset;
      float rotation = (float)Math.toRadians((double)(360.0F / (float)this.cycle * progress * (float)(this.mirror ? -1 : 1)));
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
