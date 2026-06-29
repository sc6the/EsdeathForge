package me.txb1.extras.capes;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import me.txb1.EsdeathClient;
import me.txb1.MessageHelper;
import me.txb1.player.buttons.CustomButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class AnimatedCapeGui extends me.txb1.EsdeathGuiScreen {
   private int capessize;

   @Override
   protected void actionPerformed(GuiButton var1) throws IOException {
      if (((var1.id) == 0)) {
         EsdeathClient.getInstance().getPlayerMapList().forEach(var0 -> {
            var0.unloadTextures();
            var0.getCapes().clear();
            var0.loadCape();
         });
         
      } else if (((var1.id) == (1))) {
         EsdeathClient.getInstance().getThreadHelper().getThreadpool().submit(() -> {
            EsdeathClient.getInstance().getServer().removeCape();
            EsdeathClient.getInstance().getPlayer(this.mc.thePlayer.getUniqueID().toString()).getCapes().clear();
            EsdeathClient.getInstance().getPlayer(this.mc.thePlayer.getUniqueID().toString()).getImagesLoading().clear();
            MessageHelper.sendMessage("cape.update");
         });
         Minecraft.getMinecraft().displayGuiScreen((GuiScreen)null);
      }

      Minecraft.getMinecraft().displayGuiScreen((GuiScreen)null);
      super.actionPerformed(var1);
   }

   @Override
   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      super.mouseClicked(var1, var2, var3);
   }

   @Override
   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.fontRendererObj
         .drawStringWithShadow(
            String.valueOf(new StringBuilder().append(MessageHelper.getMessage("capes.animated.amount")).append(this.capessize)),
            20.0F,
            30.0F,
            EsdeathClient.getInstance().rainbow(500)
         );
      super.drawScreen(var1, var2, var3);
   }

   @Override
   public void updateScreen() {
      super.updateScreen();
   }

   @Override
   public void initGui() {
      label14: {
         try {
            EsdeathClient.getInstance()
               .getThreadHelper()
               .getThreadpool()
               .submit(
                  () -> {
                     ArrayList var1 = EsdeathClient.getInstance().getServer().getMyAnimatedCapes();
                     int var2x = 0;
                     int var3 = 1;
                     Iterator var4 = var1.iterator();

                     while ((var4.hasNext())) {
                        String var5 = (String)var4.next();
                        this.customButtonList
                           .add(new CustomButton(var2x, 20 + 20 * var2x + var2x * 50, 30 + 30 * var3, 60, 20, var5));
                        if (((++var2x) > (5))) {
                           var2x = 0;
                           var3++;
                        }

                     }

                     this.capessize = var1.size();
                  }
               );
            this.buttonList.add(new GuiButton(0, 20, 185, 100, 20, "Refresh"));
            this.buttonList.add(new GuiButton(1, 20, 210, 100, 20, "Remove"));
         } catch (Exception var2) {
            break label14;
         }

      }

      super.initGui();
   }

   @Override
   protected void mouseReleased(int var1, int var2, int var3) {
      super.mouseReleased(var1, var2, var3);
   }

   @Override
   protected void keyTyped(char var1, int var2) throws IOException {
      super.keyTyped(var1, var2);
   }

   public static void fail(String var0) {
      FontRenderer var1 = Minecraft.getMinecraft().fontRendererObj;
      var1.drawString(var0, 30.0F, 5.0F, Color.RED.getRGB(), true);
   }

   public AnimatedCapeGui() {
      this.capessize = 0;
   }

   @Override
   protected void actionPerformed(CustomButton var1) throws IOException {
      MessageHelper.sendMessage("cape.update");
      EsdeathClient.getInstance().getServer().setAnimatedCape(var1.displayString);
      EsdeathClient.getInstance().getPlayer(this.mc.thePlayer.getUniqueID().toString()).loadCape();
      super.actionPerformed(var1);
   }

}
