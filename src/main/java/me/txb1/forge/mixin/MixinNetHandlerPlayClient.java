package me.txb1.forge.mixin;

import me.txb1.player.modulesystem.modules.render.Particles;
import me.txb1.player.modulesystem.modules.render.TimeChanger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Particles module: intercept the server's per-hit particle emission. cleanView hides the local
// player's own particles; alwaysSharpness de-dupes the server's CRIT_MAGIC against the one we emit
// ourselves in MixinPlayerControllerMP. Ported from ParticleMultiplier.
@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
   @Redirect(
      method = "handleAnimation",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/particle/EffectRenderer;emitParticleAtEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/EnumParticleTypes;)V"
      )
   )
   private void esdeath$emit(EffectRenderer effectRenderer, Entity entity, EnumParticleTypes type) {
      if (Particles.effectiveCleanView()) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.thePlayer != null && entity == mc.thePlayer) {
            return;
         }
      }
      if (type == EnumParticleTypes.CRIT_MAGIC) {
         if (Particles.effectiveMultiplier() == 0.0) {
            return;
         }
         if (Particles.effectiveAlwaysSharpness() && Particles.isDuplicateServerSharpness(entity.getEntityId())) {
            return;
         }
      }
      effectRenderer.emitParticleAtEntity(entity, type);
   }

   // TimeChanger (based on TimeChanger 1.8): re-lock the world time to the slider value after the
   // server's time-update packet is applied, so the server resync can't override the chosen time.
   @Inject(method = "handleTimeUpdate", at = @At("TAIL"))
   private void esdeath$lockTime(S03PacketTimeUpdate packet, CallbackInfo ci) {
      if (TimeChanger.active && Minecraft.getMinecraft().theWorld != null) {
         Minecraft.getMinecraft().theWorld.setWorldTime(TimeChanger.time);
      }
   }
}
