package me.txb1.extras.autotext;

import java.awt.Color;
import java.io.IOException;
import me.txb1.MessageHelper;
import me.txb1.player.modulesystem.modules.player.AutoText;
import net.minecraft.client.gui.GuiScreen;

public class AutoTextKeyGui extends GuiScreen {

   @Override
   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      super.mouseClicked(var1, var2, var3);
   }

   @Override
   protected void mouseReleased(int var1, int var2, int var3) {
      super.mouseReleased(var1, var2, var3);
   }

   @Override
   protected void keyTyped(char var1, int var2) throws IOException {
      this.mc.displayGuiScreen(null);
      AutoText.prepareKey = var2;
      this.mc.displayGuiScreen(null);
      MessageHelper.sendMessage("autotext.write");
      super.keyTyped(var1, var2);
   }

   @Override
   public void updateScreen() {
      super.updateScreen();
   }

   @Override
   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.fontRendererObj
         .drawStringWithShadow("Type your Key", (float)(this.width / 2 - 40), (float)(this.height / 2 - 20), Color.RED.getRGB());
      super.drawScreen(var1, var2, var3);
   }

   @Override
   public void initGui() {
      super.initGui();
   }

}
