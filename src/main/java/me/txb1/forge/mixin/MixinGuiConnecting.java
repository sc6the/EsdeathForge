package me.txb1.forge.mixin;

import me.txb1.forge.gui.server.ColorUtil;
import me.txb1.forge.gui.server.EsdeathConnect;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Replaces the generic "Connecting to the server." line with "Connecting to <name> (<address>)",
// using the name/address stashed by EsdeathConnect and a shortened long-address form.
@Mixin(GuiConnecting.class)
public abstract class MixinGuiConnecting {
   @Redirect(
      method = "drawScreen",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/I18n;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"))
   private String esdeath$connectingText(String key, Object[] args) {
      if ("connect.connecting".equals(key) && EsdeathConnect.pendingName != null) {
         return "Connecting to " + ColorUtil.translate(EsdeathConnect.pendingName)
            + " §7(" + EsdeathConnect.truncate(EsdeathConnect.pendingIp) + ")";
      }
      return I18n.format(key, args);
   }
}
