package me.txb1.forge.mixin;

import me.txb1.forge.ParticleCustomizerHolder;
import net.minecraft.client.particle.EntityFX;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// ParticleCustomiser, ported from its coremod ASM (EntityFXTransformer/RenderParticleVisitor): the
// original rewrote every particleRed/Green/Blue/Alpha/Scale field read inside EntityFX.renderParticle
// to call a getter. We do the same with @Redirect on those GETFIELDs, routing them through the
// customiser's per-particle settings (colour / scale / opacity, incl. chroma). The shadow fields
// supply the original value (EntityFX's colour fields are protected) for the un-customised case.
@Mixin(EntityFX.class)
public abstract class MixinEntityFX {
   @Shadow protected float particleRed;
   @Shadow protected float particleGreen;
   @Shadow protected float particleBlue;
   @Shadow protected float particleAlpha;
   @Shadow protected float particleScale;

   @Redirect(method = "renderParticle", at = @At(value = "FIELD",
      target = "Lnet/minecraft/client/particle/EntityFX;particleRed:F", opcode = Opcodes.GETFIELD))
   private float esdeath$red(EntityFX self) {
      return ParticleCustomizerHolder.getRed(self, this.particleRed);
   }

   @Redirect(method = "renderParticle", at = @At(value = "FIELD",
      target = "Lnet/minecraft/client/particle/EntityFX;particleGreen:F", opcode = Opcodes.GETFIELD))
   private float esdeath$green(EntityFX self) {
      return ParticleCustomizerHolder.getGreen(self, this.particleGreen);
   }

   @Redirect(method = "renderParticle", at = @At(value = "FIELD",
      target = "Lnet/minecraft/client/particle/EntityFX;particleBlue:F", opcode = Opcodes.GETFIELD))
   private float esdeath$blue(EntityFX self) {
      return ParticleCustomizerHolder.getBlue(self, this.particleBlue);
   }

   @Redirect(method = "renderParticle", at = @At(value = "FIELD",
      target = "Lnet/minecraft/client/particle/EntityFX;particleAlpha:F", opcode = Opcodes.GETFIELD))
   private float esdeath$alpha(EntityFX self) {
      return ParticleCustomizerHolder.getAlpha(self, this.particleAlpha);
   }

   @Redirect(method = "renderParticle", at = @At(value = "FIELD",
      target = "Lnet/minecraft/client/particle/EntityFX;particleScale:F", opcode = Opcodes.GETFIELD))
   private float esdeath$scale(EntityFX self) {
      return ParticleCustomizerHolder.getScale(self, this.particleScale);
   }
}
