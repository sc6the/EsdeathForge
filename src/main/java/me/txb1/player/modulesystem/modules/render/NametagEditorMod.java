package me.txb1.player.modulesystem.modules.render;

import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.gui.GuiScreen;

// NametagEditor (bundled fiw.nametageditor, replaces the old CleanNames). The mod co-loads and renders
// custom nametag backgrounds / scale / offset; this module is its entry point. The edit pencil opens
// the Nametag Editor GUI.
public class NametagEditorMod extends Module {
   public NametagEditorMod() {
      super("NametagEditor", "NametagEditor", Category.RENDER, true);
   }

   @Override
   public GuiScreen getCustomSettingsGui() {
      try {
         return new fiw.nametageditor.GUIMain();
      } catch (Throwable t) {
         return null;
      }
   }
}
