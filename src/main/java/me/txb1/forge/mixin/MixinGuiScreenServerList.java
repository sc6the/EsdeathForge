package me.txb1.forge.mixin;

import me.txb1.forge.gui.EsdeathMainMenu;
import me.txb1.forge.gui.server.EsdeathServerListGui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenServerList;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Adds an "Add to Server list" button to the vanilla Direct-Connect dialog. Clicking it opens the
// add-server GUI with the address from the IP box pre-filled, instead of connecting straight away.
@Mixin(GuiScreenServerList.class)
public abstract class MixinGuiScreenServerList extends GuiScreen {
   private static final int ESD_ADD_TO_LIST = 9301;

   // The IP text box (no MCP name in stable_22, so we shadow it by its SRG name).
   @Shadow
   private GuiTextField field_146302_g;

   @Inject(method = "initGui", at = @At("TAIL"))
   private void esdeath$addButton(CallbackInfo ci) {
      this.buttonList.add(new GuiButton(ESD_ADD_TO_LIST, this.width / 2 - 100, this.height / 4 + 144 + 12, "Add to Server list"));
   }

   @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
   private void esdeath$onAction(GuiButton button, CallbackInfo ci) {
      if (button.id == ESD_ADD_TO_LIST) {
         String ip = this.field_146302_g != null ? this.field_146302_g.getText() : "";
         new EsdeathServerListGui(new EsdeathMainMenu()).beginAddFromAddress(ip);
         ci.cancel();
      }
   }
}
