package me.txb1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.txb1.player.buttons.CapeButton;
import me.txb1.player.buttons.CosmeticButton;
import me.txb1.player.buttons.CustomButton;
import me.txb1.player.modulesystem.ModuleButton;
import net.minecraft.client.gui.GuiScreen;

// Forge port: the standalone build added capeButtonList/customButtonList/cosmeticButtonList
// (plus their render + click dispatch) directly to vanilla GuiScreen. Since we can't edit
// vanilla under Forge, the Esdeath GUIs now extend this base instead, which carries the same
// three custom button lists and replicates GuiScreen's draw/click/release dispatch for them.
public abstract class EsdeathGuiScreen extends GuiScreen {
   protected List<CapeButton> capeButtonList = new ArrayList<CapeButton>();
   protected List<CustomButton> customButtonList = new ArrayList<CustomButton>();
   protected List<CosmeticButton> cosmeticButtonList = new ArrayList<CosmeticButton>();
   // the standalone added moduleButtonList to vanilla GuiScreen too (used by the in-game module menu)
   public List<ModuleButton> moduleButtonList = new ArrayList<ModuleButton>();

   // NOTE: deliberately no initGui() override. The standalone's vanilla GuiScreen.initGui() did
   // NOT clear these custom lists, and several Esdeath GUIs (CosmeticGui, GuiGui, CapeGui) populate
   // their list and THEN call super.initGui() last -- clearing here would wipe what they just added.

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      super.drawScreen(mouseX, mouseY, partialTicks);
      for (ModuleButton b : this.moduleButtonList) {
         b.drawButton(this.mc, mouseX, mouseY);
      }
      for (CapeButton b : this.capeButtonList) {
         b.drawButton(this.mc, mouseX, mouseY);
      }
      for (CustomButton b : this.customButtonList) {
         b.drawButton(this.mc, mouseX, mouseY);
      }
      for (CosmeticButton b : this.cosmeticButtonList) {
         b.drawButton(this.mc, mouseX, mouseY);
      }
   }

   @Override
   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      for (ModuleButton b : this.moduleButtonList) {
         if (b.mousePressed(this.mc, mouseX, mouseY)) {
            b.playPressSound(this.mc.getSoundHandler());
            this.actionPerformed(b);
         }
      }
      for (CapeButton b : this.capeButtonList) {
         if (b.mousePressed(this.mc, mouseX, mouseY)) {
            b.playPressSound(this.mc.getSoundHandler());
            this.actionPerformed(b);
         }
      }
      for (CustomButton b : this.customButtonList) {
         if (b.mousePressed(this.mc, mouseX, mouseY)) {
            b.playPressSound(this.mc.getSoundHandler());
            this.actionPerformed(b);
         }
      }
      for (CosmeticButton b : this.cosmeticButtonList) {
         if (b.mousePressed(this.mc, mouseX, mouseY)) {
            b.playPressSound(this.mc.getSoundHandler());
            this.actionPerformed(b);
         }
      }
   }

   @Override
   protected void mouseReleased(int mouseX, int mouseY, int state) {
      super.mouseReleased(mouseX, mouseY, state);
      for (ModuleButton b : this.moduleButtonList) {
         b.mouseReleased(mouseX, mouseY);
      }
      for (CapeButton b : this.capeButtonList) {
         b.mouseReleased(mouseX, mouseY);
      }
      for (CustomButton b : this.customButtonList) {
         b.mouseReleased(mouseX, mouseY);
      }
      for (CosmeticButton b : this.cosmeticButtonList) {
         b.mouseReleased(mouseX, mouseY);
      }
   }

   // Overloads the Esdeath GUIs override for their custom buttons (mirror GuiScreen.actionPerformed).
   protected void actionPerformed(ModuleButton button) throws IOException {
   }

   protected void actionPerformed(CapeButton button) throws IOException {
   }

   protected void actionPerformed(CustomButton button) throws IOException {
   }

   protected void actionPerformed(CosmeticButton button) throws IOException {
   }
}
