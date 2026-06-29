package me.txb1.forge.mixin;

import me.txb1.player.modulesystem.modules.render.InventorySnow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Inventory Snow: draws the falling-snow overlay inside container screens, injected right AFTER the
// container's background layer (the panel) and before the slots/items — so the snow falls behind the
// items, over the panel. GL is at the screen origin at this point (the background layer uses absolute
// coords), so a full-screen draw lands correctly.
@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer {

   @Inject(
      method = "drawScreen",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGuiContainerBackgroundLayer(FII)V",
         shift = At.Shift.AFTER
      )
   )
   private void esdeath$inventorySnow(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
      if (InventorySnow.active) {
         ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
         me.txb1.extras.snow.SnowRenderer.render(sr.getScaledWidth(), sr.getScaledHeight(), mouseX, mouseY);
      }
   }
}
