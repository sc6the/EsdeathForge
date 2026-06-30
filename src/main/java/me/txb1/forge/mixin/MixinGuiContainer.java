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

   // ShinyPots: potion liquid colour as slot background. ShopHelper: affordable-item slot highlight.
   // Drawn behind the item (HEAD of drawSlot). GL is translated to guiLeft/guiTop here, so the slot's
   // display coords land correctly.
   @Inject(method = "drawSlot", at = @At("HEAD"))
   private void esdeath$slotOverlay(net.minecraft.inventory.Slot slot, CallbackInfo ci) {
      if (slot == null) {
         return;
      }
      net.minecraft.item.ItemStack s = slot.getStack();
      if (s == null) {
         return;
      }
      int x = slot.xDisplayPosition;
      int y = slot.yDisplayPosition;
      if (me.txb1.player.modulesystem.modules.utils.ShinyPots.active
            && s.getItem() instanceof net.minecraft.item.ItemPotion) {
         int color = s.getItem().getColorFromItemStack(s, 0) & 0xFFFFFF;
         int a = Math.max(0, Math.min(255, me.txb1.player.modulesystem.modules.utils.ShinyPots.opacity));
         net.minecraft.client.gui.Gui.drawRect(x, y, x + 16, y + 16, (a << 24) | color);
      }
      if (me.txb1.player.modulesystem.modules.utils.ShopHelper.active
            && me.txb1.player.modulesystem.modules.utils.ShopHelper.highlightAffordable) {
         int color = me.txb1.player.modulesystem.modules.utils.ShopHelper.affordableColor(s);
         if (color != 0) {
            int a = Math.max(0, Math.min(255, me.txb1.player.modulesystem.modules.utils.ShopHelper.opacity));
            net.minecraft.client.gui.Gui.drawRect(x, y, x + 16, y + 16, (a << 24) | color);
         }
      }
   }
}
