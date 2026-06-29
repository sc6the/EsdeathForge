package me.txb1.forge.mixin;

import me.proxycracked.universalaccountmanager.skin.ForceSkinLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Force Skin: the UAM's ForceSkinLoader exposes the user's chosen skin as a dynamic texture, but
// nothing rendered it -- the backend wrote skin.png + uploaded the GL texture, yet the player still
// showed their real skin. This is the missing consumer: for the LOCAL player only, getLocationSkin
// returns the forced texture and getSkinType returns its model variant, so the override is visible
// to the user immediately (no restart). Other players are untouched.
@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer {

   @Inject(method = "getLocationSkin()Lnet/minecraft/util/ResourceLocation;", at = @At("HEAD"), cancellable = true)
   private void esdeath$forceSkin(CallbackInfoReturnable<ResourceLocation> cir) {
      if (!isLocal()) {
         return;
      }
      // session override (in-game Skinchanger) takes priority over the persistent Force Skin
      if (me.proxycracked.universalaccountmanager.skin.SessionSkin.hasSkin()) {
         cir.setReturnValue(me.proxycracked.universalaccountmanager.skin.SessionSkin.getSkinLocation());
         return;
      }
      if (ForceSkinLoader.hasSkin()) {
         ResourceLocation rl = ForceSkinLoader.getSkinLocation();
         if (rl != null) {
            cir.setReturnValue(rl);
         }
      }
   }

   @Inject(method = "getSkinType()Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
   private void esdeath$forceSkinType(CallbackInfoReturnable<String> cir) {
      if (!isLocal()) {
         return;
      }
      if (me.proxycracked.universalaccountmanager.skin.SessionSkin.hasSkin()) {
         cir.setReturnValue(me.proxycracked.universalaccountmanager.skin.SessionSkin.isSlim() ? "slim" : "default");
         return;
      }
      if (ForceSkinLoader.hasSkin()) {
         cir.setReturnValue(ForceSkinLoader.isSlim() ? "slim" : "default");
      }
   }

   // LabyMod cape: when the "LabyMod Cape" cosmetic is equipped, return the player's LabyMod cape
   // (fetched from dl.labymod.net by UUID) for any player, so capes show on yourself and others.
   @Inject(method = "getLocationCape()Lnet/minecraft/util/ResourceLocation;", at = @At("HEAD"), cancellable = true)
   private void esdeath$labyCape(CallbackInfoReturnable<ResourceLocation> cir) {
      if (!me.txb1.extras.cosmetics.laby.LabyCapes.enabled()) {
         return;
      }
      ResourceLocation rl = me.txb1.extras.cosmetics.laby.LabyCapes.get(
         ((AbstractClientPlayer) (Object) this).getUniqueID());
      if (rl != null) {
         cir.setReturnValue(rl);
      }
   }

   private boolean isLocal() {
      Minecraft mc = Minecraft.getMinecraft();
      return mc != null && (Object) this == mc.thePlayer;
   }
}
