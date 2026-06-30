package me.txb1.forge.mixin;

import me.txb1.extras.cosmetics.cosmetics.CosmeticCrown;
import me.txb1.extras.cosmetics.cosmetics.CosmeticHalo;
import me.txb1.extras.cosmetics.cosmetics.CosmeticKagune;
import me.txb1.extras.cosmetics.cosmetics.CosmeticRabbitEar;
import me.txb1.extras.cosmetics.cosmetics.CosmeticReifen;
import me.txb1.extras.cosmetics.cosmetics.CosmeticRotate;
import me.txb1.extras.cosmetics.cosmetics.CosmeticStripes;
import me.txb1.extras.cosmetics.cosmetics.CosmeticSusanoo;
import me.txb1.extras.cosmetics.cosmetics.CosmeticTail;
import me.txb1.extras.cosmetics.cosmetics.CosmeticTopHat;
import me.txb1.extras.cosmetics.cosmetics.bandana.CosmeticBandana;
import me.txb1.extras.cosmetics.cosmetics.bandana.CosmeticBandanaSnowy;
import me.txb1.extras.cosmetics.cosmetics.effects.CosmeticCreeperEffect;
import me.txb1.extras.cosmetics.cosmetics.effects.CosmeticWitherEffect;
import me.txb1.extras.cosmetics.cosmetics.wings.CosmeticDarkWings;
import me.txb1.extras.cosmetics.cosmetics.wings.CosmeticFliege;
import me.txb1.extras.cosmetics.cosmetics.wings.CosmeticSnowyWings;
import me.txb1.extras.cosmetics.cosmetics.wings.CosmeticWings;
import me.txb1.player.capesystem.CapeModel;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Forge port of the standalone's edited RenderPlayer constructor, which registered every Esdeath
// cosmetic (and the animated CapeModel) as render layers. addLayer is public + generic in 1.8.9,
// so we just append them at the constructor TAIL. The 1-arg RenderPlayer(RenderManager) ctor
// delegates to this 2-arg one, so both client renderers get the layers.
@Mixin(RenderPlayer.class)
public class MixinRenderPlayer {

   // NOTE: Mixin only allows RETURN (not TAIL) for constructor targets.
   @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/RenderManager;Z)V", at = @At("RETURN"))
   private void esdeath$registerCosmetics(RenderManager renderManager, boolean useSmallArms, CallbackInfo ci) {
      RenderPlayer self = (RenderPlayer) (Object) this;
      self.addLayer(new CapeModel(self));
      self.addLayer(new CosmeticTopHat(self));
      self.addLayer(new CosmeticBandana(self));
      self.addLayer(new CosmeticBandanaSnowy(self));
      self.addLayer(new CosmeticRabbitEar(self));
      self.addLayer(new CosmeticStripes(self));
      self.addLayer(new CosmeticReifen(self));
      self.addLayer(new CosmeticSusanoo(self));
      self.addLayer(new CosmeticTail(self));
      self.addLayer(new CosmeticKagune(self));
      self.addLayer(new CosmeticCrown(self));
      self.addLayer(new CosmeticHalo(self));
      self.addLayer(new CosmeticRotate(self));
      self.addLayer(new CosmeticWings(self));
      self.addLayer(new CosmeticDarkWings(self));
      self.addLayer(new CosmeticSnowyWings(self));
      self.addLayer(new CosmeticFliege(self));
      self.addLayer(new CosmeticWitherEffect(self));
      self.addLayer(new CosmeticCreeperEffect(self));
      // LabyMod cosmetics as OFFLINE cosmetics: renders only the LOCAL player's selected LabyMod
      // cosmetics (equipped in the cosmetic menu's "Labymod" category) using each cosmetic's
      // default_data + dl.labymod.net Bedrock geometry/textures. Does NOT show other players' owned
      // cosmetics. Selectable names are registered by LabyOfflineCatalog from the remote index.
      self.addLayer(new me.txb1.extras.cosmetics.cosmetics.laby.CosmeticLabyGeometry(self));
      // full OAM (OldAnimationsMod) cosmetic set — geometry + animation ported verbatim
      for (me.txb1.extras.cosmetics.CosmeticBase layer : me.txb1.extras.cosmetics.oam.OamCosmeticManager.build(self)) {
         self.addLayer(layer);
      }
   }
}
