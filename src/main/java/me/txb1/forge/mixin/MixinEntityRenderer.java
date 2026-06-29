package me.txb1.forge.mixin;

import me.txb1.player.modulesystem.modules.player.NoHurtCam;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// NoHurtCam: cancel the damage camera tilt/shake when the module is on. The standalone used an
// ASM head-gate on EntityRenderer.hurtCameraEffect(F)V; here it's a cancellable HEAD @Inject.
@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

   @Inject(method = "hurtCameraEffect(F)V", at = @At("HEAD"), cancellable = true)
   private void esdeath$noHurtCam(float partialTicks, CallbackInfo ci) {
      if (NoHurtCam.active) {
         ci.cancel();
      }
   }
}
