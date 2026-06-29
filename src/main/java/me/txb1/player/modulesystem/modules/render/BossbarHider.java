package me.txb1.player.modulesystem.modules.render;

import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;

// BossbarHider: hides the boss health bar HUD (wither/dragon, and Hypixel boss bars). The render is
// cancelled in ForgeEventBridge via RenderGameOverlayEvent.Pre(BOSSHEALTH) when active.
public class BossbarHider extends Module {
   public static boolean active;

   public BossbarHider() {
      super("BossbarHider", "BossbarHider", Category.RENDER, false);
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
