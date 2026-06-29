package me.txb1.player.modulesystem.modules.visuals;

import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;

// Statsify (bundled com.strawberry.statsify): Hypixel tab-list stats. The mod co-loads and works via
// its own commands; this toggleable module flips its tab-stats display by running Statsify's own
// "tablist" toggle command on enable/disable.
public class Statsify extends Module {
   public Statsify() {
      super("Statsify", "Statsify", Category.VISUAL, false);
   }

   @Override
   public void onEnable() {
      toggleStatsifyTab();
   }

   @Override
   public void onDisable() {
      toggleStatsifyTab();
   }

   private void toggleStatsifyTab() {
      try {
         if (Minecraft.getMinecraft().thePlayer != null) {
            ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, "tablist");
         }
      } catch (Throwable ignored) {
      }
   }
}
