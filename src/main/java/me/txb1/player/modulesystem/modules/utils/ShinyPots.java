package me.txb1.player.modulesystem.modules.utils;

import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;

// ShinyPots: renders a potion's liquid colour as the slot background, so potions are easy to tell
// apart at a glance. The actual draw is in MixinGuiContainer#esdeath$shinyPots (per-slot).
public class ShinyPots extends Module {
   public static boolean active;
   public static int opacity = 144; // slot background alpha (0..255)

   public ShinyPots() {
      super("ShinyPots", "ShinyPots", Category.UTILS, true);
   }

   @Override
   public net.minecraft.client.gui.GuiScreen getCustomSettingsGui() {
      return new UtilSettingsGui(this, new me.txb1.forge.gui.EsdeathIngameMenu())
         .slider("Opacity", 0, 255, () -> opacity, v -> opacity = v);
   }

   @Override
   public void onEnable() {
      active = true;
   }

   @Override
   public void onDisable() {
      active = false;
   }
}
