package me.txb1.player.modulesystem.modules.player;

import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;

// ProjectL (ported from ProjectL-0.2): on mouse movement, snap the body/head rotation "prev" values
// to current so the body follows the camera instantly with no interpolation lag. Driven by a
// MouseEvent handler in ForgeEventBridge gated on `active`.
public class ProjectL extends Module {
   public static boolean active;

   public ProjectL() {
      super("ProjectL", "ProjectL", Category.PLAYER, false);
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
