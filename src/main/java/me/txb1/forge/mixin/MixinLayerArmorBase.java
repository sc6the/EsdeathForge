package me.txb1.forge.mixin;

import me.txb1.player.modulesystem.modules.render.ArmorHider;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// ArmorHider module: cancel the armor layer render for hidden slots (ported from ArmorHider-1.0's
// MixinLayerArmorBase). slot: 1=boots 2=legs 3=chest 4=helmet.
@Mixin(LayerArmorBase.class)
public class MixinLayerArmorBase {
   @Inject(method = "renderLayer", at = @At("HEAD"), cancellable = true)
   private void esdeath$hideArmor(EntityLivingBase entity, float limbSwing, float limbSwingAmount,
         float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, int armorSlot,
         CallbackInfo ci) {
      if (ArmorHider.isHidden(armorSlot)) {
         ci.cancel();
      }
   }
}
