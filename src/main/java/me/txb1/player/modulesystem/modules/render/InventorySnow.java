package me.txb1.player.modulesystem.modules.render;

import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;

// Inventory Snow: renders the falling-snow overlay inside container GUIs (player inventory, chests,
// …). The actual draw is in MixinGuiContainer (after the background layer, behind the items). Shares
// the appearance settings (amount / colour / transparency) with the main-menu snow, edited in
// Theme -> Snow.
public class InventorySnow extends Module {
   public static boolean active;

   public InventorySnow() {
      super("InventorySnow", "Inventory Snow", Category.RENDER, false);
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
