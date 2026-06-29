package me.txb1.player.modulesystem.modules.player;

import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.gui.GuiScreen;

// "1.7 Animations" — front-end for the bundled anims mod (com.syuto.animations), which provides the
// 1.7-style item animations via its own (0.7-compatible) mixin. This module's edit pencil opens
// anims' AnimationsGui (its 16 modes + scale/swing-speed sliders, also reachable via /animations).
public class ItemAnimation extends Module {
   public ItemAnimation() {
      super("ItemAnimation", "1.7 Animations", Category.PLAYER, true);
   }

   @Override
   public GuiScreen getCustomSettingsGui() {
      try {
         return new com.syuto.animations.screens.AnimationsGui();
      } catch (Throwable t) {
         return null;
      }
   }
}
