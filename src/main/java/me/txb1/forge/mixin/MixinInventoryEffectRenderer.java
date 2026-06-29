package me.txb1.forge.mixin;

import net.minecraft.client.renderer.InventoryEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;

// Keep the inventory (and any potion-bearing container) centered. Vanilla's updateActivePotionEffects
// shifts guiLeft right and draws an effect list down the left side when the player has active potion
// effects — which pushes the whole inventory off-center. We make the "are there active effects?"
// check always report empty, so it takes the centered branch (guiLeft centered, hasActivePotionEffects
// false → no side list). The client's PotionHUD already shows effects.
//
// NOTE: implemented with @Redirect on the Collection.isEmpty() call rather than @Shadow-ing guiLeft/
// xSize (which live in the GuiContainer superclass and can't be resolved as inherited shadows here).
@Mixin(InventoryEffectRenderer.class)
public abstract class MixinInventoryEffectRenderer {

   @Redirect(
      method = "updateActivePotionEffects",
      at = @At(value = "INVOKE", target = "Ljava/util/Collection;isEmpty()Z")
   )
   private boolean esdeath$keepCentered(Collection<?> effects) {
      return true; // pretend no active effects -> inventory stays centered, side list not drawn
   }
}
