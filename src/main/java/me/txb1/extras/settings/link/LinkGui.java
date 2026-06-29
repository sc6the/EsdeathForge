package me.txb1.extras.settings.link;

import java.awt.Color;
import java.io.IOException;
import me.txb1.EsdeathClient;
import me.txb1.MessageHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class LinkGui extends GuiScreen {
   private GuiTextField status;
   private GuiButton refresh;

   @Override
   public void initGui() {
      int var1 = 0;
      this.status = new GuiTextField(0, this.mc.fontRendererObj, 10, 10, 210, 20);
      this.refresh = new GuiButton(0, 10, 58, 100, 20, "Link");
      this.buttonList.add(this.refresh);
      super.initGui();
   }

   @Override
   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      this.status.mouseClicked(var1, var2, var3);
      super.mouseClicked(var1, var2, var3);
   }

   @Override
   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.status.drawTextBox();
      this.fontRendererObj.drawStringWithShadow("§aInfos: www.EsdeathClient.de/profile", 10.0F, 45.0F, -1);
      super.drawScreen(var1, var2, var3);
   }

   @Override
   protected void actionPerformed(GuiButton var1) throws IOException {
      EsdeathClient.getInstance().getThreadHelper().getThreadpool().submit(() -> {
         try {
            String var1x = EsdeathClient.getInstance().getServer().linkAccount(this.status.getText());
            if ((var1x.equalsIgnoreCase("true"))) {
               MessageHelper.sendMessage("answer.true");
               
            } else {
               MessageHelper.sendMessage("answer.false");
            }
         } catch (Exception var3) {
            MessageHelper.sendMessage("server");
            return;
         }

                     ;
         
      });
      this.mc.displayGuiScreen(null);
      super.actionPerformed(var1);
   }

   @Override
   public void updateScreen() {
      super.updateScreen();
   }

   @Override
   protected void mouseReleased(int var1, int var2, int var3) {
      super.mouseReleased(var1, var2, var3);
   }

   public static void fail(String var0) {
      FontRenderer var1 = Minecraft.getMinecraft().fontRendererObj;
      var1.drawString(var0, 30.0F, 5.0F, Color.RED.getRGB(), true);
   }

   @Override
   protected void keyTyped(char var1, int var2) throws IOException {
      this.status.textboxKeyTyped(var1, var2);
      super.keyTyped(var1, var2);
   }
}
