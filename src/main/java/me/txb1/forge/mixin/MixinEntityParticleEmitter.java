package me.txb1.forge.mixin;

import me.txb1.player.modulesystem.modules.render.Particles;
import net.minecraft.client.particle.EntityParticleEmitter;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

// Particles module: scale the CRIT_MAGIC particle count emitted per tick (vanilla spawns 16).
// Ported from ParticleMultiplier's MixinEntityParticleEmitter, reading the Esdeath module config.
@Mixin(EntityParticleEmitter.class)
public class MixinEntityParticleEmitter {
   @Shadow
   private EnumParticleTypes particleTypes;

   @ModifyConstant(method = "onUpdate", constant = @Constant(intValue = 16))
   private int esdeath$scaleCount(int original) {
      if (this.particleTypes != EnumParticleTypes.CRIT_MAGIC) {
         return original;
      }
      double mult = Particles.effectiveMultiplier();
      if (mult == 1.0) {
         return original;
      }
      long scaled = Math.round((double) original * mult);
      if (scaled < 0L) {
         return 0;
      }
      return scaled > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) scaled;
   }
}
