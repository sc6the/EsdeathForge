package me.txb1.forge.mixin;

import me.txb1.player.modulesystem.modules.render.Particles;
import me.txb1.player.modulesystem.modules.visuals.ReachDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Particles module: alwaysSharpness -> emit the enchant sparkle on every melee attack, even when
// the server doesn't (or before its packet arrives). Marks the target so MixinNetHandlerPlayClient
// can drop the duplicate server particle. Ported from ParticleMultiplier.
@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
   @Inject(method = "attackEntity", at = @At("TAIL"))
   private void esdeath$alwaysSharpness(EntityPlayer playerIn, Entity targetEntity, CallbackInfo ci) {
      Minecraft mc = Minecraft.getMinecraft();
      // ReachDisplay: attackEntity only fires on a registered melee hit, so this is the exact moment
      // to measure reach (distance from the player's eyes to the point on the target that was hit).
      if (targetEntity != null && playerIn != null) {
         double reach;
         Vec3 eyes = playerIn.getPositionEyes(1.0F);
         MovingObjectPosition mop = mc.objectMouseOver;
         if (mop != null && mop.hitVec != null && mop.entityHit == targetEntity) {
            reach = eyes.distanceTo(mop.hitVec);
         } else {
            // fallback: eyes to the nearest face of the target's bounding box
            reach = playerIn.getDistanceToEntity(targetEntity) - (double) targetEntity.width / 2.0;
         }
         ReachDisplay.recordHit(reach);
      }
      if (Particles.effectiveAlwaysSharpness()) {
         if (mc.effectRenderer != null && targetEntity != null) {
            mc.effectRenderer.emitParticleAtEntity(targetEntity, EnumParticleTypes.CRIT_MAGIC);
            Particles.markSelfSharpness(targetEntity.getEntityId());
         }
      }
   }
}
