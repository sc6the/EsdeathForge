package me.txb1.player.modulesystem.modules.render;

import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.gui.GuiScreen;

// BlockOverlay (bundled me.aycy.blockoverlay, replaces the old BlockOutLine). The mod itself co-loads
// and renders the custom block overlay via DrawBlockHighlightEvent; this module is its entry point in
// the Esdeath list. The edit pencil opens the BlockOverlay config GUI (getCustomSettingsGui).
public class BlockOverlayMod extends Module {
   public BlockOverlayMod() {
      super("BlockOverlay", "BlockOverlay", Category.RENDER, true);
   }

   @Override
   public GuiScreen getCustomSettingsGui() {
      try {
         return new me.aycy.blockoverlay.gui.GuiBlockOverlay();
      } catch (Throwable t) {
         return null;
      }
   }
}
