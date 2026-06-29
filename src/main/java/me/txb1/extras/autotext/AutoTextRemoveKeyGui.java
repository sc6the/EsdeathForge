package me.txb1.extras.autotext;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import me.txb1.MessageHelper;
import me.txb1.player.modulesystem.modules.player.AutoText;
import me.txb1.utils.Text;
import net.minecraft.client.gui.GuiScreen;

public class AutoTextRemoveKeyGui extends GuiScreen {

   @Override
   protected void mouseReleased(int var1, int var2, int var3) {
      super.mouseReleased(var1, var2, var3);
   }

   @Override
   public void initGui() {
      super.initGui();
   }

   @Override
   protected void keyTyped(char var1, int var2) throws IOException {
      this.mc.displayGuiScreen(null);
      ArrayList var3 = new ArrayList();
      Iterator var4 = AutoText.texts.iterator();

      while ((var4.hasNext())) {
         Text var5 = (Text)var4.next();
         if (((var5.getKey()) == (var2))) {
            var3.add(var5);
         }

      }

      var4 = var3.iterator();

      while ((var4.hasNext())) {
         Text var7 = (Text)var4.next();
         AutoText.texts.remove(var7);
         MessageHelper.sendMessage("autotext.remove");
         
      }

      super.keyTyped(var1, var2);
   }

   @Override
   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.fontRendererObj
         .drawStringWithShadow("Type your Key to remove", (float)(this.width / 2 - 90), (float)(this.height / 2 - 20), Color.RED.getRGB());
      super.drawScreen(var1, var2, var3);
   }

   @Override
   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      super.mouseClicked(var1, var2, var3);
   }

   @Override
   public void updateScreen() {
      super.updateScreen();
   }
}
