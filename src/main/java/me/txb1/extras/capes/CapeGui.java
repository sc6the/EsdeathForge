package me.txb1.extras.capes;

import java.awt.Color;
import java.io.IOException;
import me.txb1.EsdeathClient;
import me.txb1.MessageHelper;
import me.txb1.player.buttons.CapeButton;
import me.txb1.utils.EsdeathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class CapeGui extends me.txb1.EsdeathGuiScreen {
   private GuiButton button_refresh;
   private GuiButton button_info;

   @Override
   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      super.mouseClicked(var1, var2, var3);
   }

   @Override
   protected void mouseReleased(int var1, int var2, int var3) {
      super.mouseReleased(var1, var2, var3);
   }

   @Override
   protected void actionPerformed(CapeButton var1) throws IOException {
      if ((var1.displayString.contains("§c§m"))) {
         MessageHelper.sendMessage("buy");
         Minecraft.getMinecraft().displayGuiScreen((GuiScreen)null);
         
      } else {
         MessageHelper.sendMessage("cape.update");
         EsdeathClient.getInstance().getServer().setCape(var1.displayString.replace("§a", ""));
         EsdeathClient.getInstance().getPlayer(this.mc.thePlayer.getUniqueID().toString()).loadCape();
      }

      super.actionPerformed(var1);
   }

   public static void fail(String var0) {
      FontRenderer var1 = Minecraft.getMinecraft().fontRendererObj;
      var1.drawString(var0, 30.0F, 5.0F, Color.RED.getRGB(), true);
   }

   @Override
   public void initGui() {
      if (!(EsdeathUtils.isOnline(this.mc.thePlayer.getName()))) {
         this.mc.displayGuiScreen(null);
         MessageHelper.sendMessage("loading");
      } else {
         label66: {
            try {
               String[] var1 = EsdeathClient.getInstance().getServer().getAllCapes().split(":");
               String var2 = EsdeathClient.getInstance().getServer().getRank();
               int var3 = 0;
               int var4 = 1;
               String[] var5 = var1;
               int var6 = var1.length;
               int var7 = 0;

               while (((var7) < (var6))) {
                  String var8 = var5[var7];
                  String var9 = var8.split("-")[1].replace(".png", "");
                  if ((var9.equalsIgnoreCase("Premium"))
                        && (
                           (var2.equalsIgnoreCase("premium"))
                              || (var2.equalsIgnoreCase("Chef"))
                              || (var2.equalsIgnoreCase("Epic"))
                        )
                     || (var9.equalsIgnoreCase("Epic"))
                        && ((var2.equalsIgnoreCase("Epic")) || (var2.equalsIgnoreCase("Chef")))
                     || (var9.equalsIgnoreCase("Chef")) && (var2.equalsIgnoreCase("Chef"))) {
                     this.capeButtonList
                        .add(
                           new CapeButton(
                              var3,
                              20 + 20 * var3 + var3 * 50,
                              30 + 30 * var4,
                              60,
                              20,
                              var8.split("-")[0]
                           )
                        );
                     
                  } else {
                     this.capeButtonList
                        .add(
                           new CapeButton(
                              var3,
                              20 + 20 * var3 + var3 * 50,
                              30 + 30 * var4,
                              60,
                              20,
                              String.valueOf(
                                 new StringBuilder()
                                    .append("§c§m")
                                    .append(var8.split("-")[0])
                                    .append("§4(§c")
                                    .append(var9)
                                    .append("§4)")
                              )
                           )
                        );
                  }

                  if (((++var3) > (5))) {
                     var3 = 0;
                     var4++;
                  }

                  var7++;
                  
               }

               this.button_info = new GuiButton(0, 20, 160, 100, 20, "Refresh");
               this.buttonList.add(this.button_info);
               this.buttonList.add(new GuiButton(1, 20, 185, 100, 20, "Animierte Capes"));
               this.buttonList.add(new GuiButton(2, 20, 210, 100, 20, "Remove"));
            } catch (Exception var10) {
               var10.printStackTrace();
               break label66;
            }

         }

         super.initGui();
      }
   }

   @Override
   public void updateScreen() {
      super.updateScreen();
   }

   @Override
   protected void actionPerformed(GuiButton var1) throws IOException {
      if (((var1.id) == 0)) {
         EsdeathClient.getInstance().getPlayerMapList().forEach(var0 -> {
            var0.unloadTextures();
            var0.getCapes().clear();
            var0.loadCape();
         });
         
      } else if (((var1.id) == (1))) {
         this.mc.displayGuiScreen(new AnimatedCapeGui());
         
      } else {
         EsdeathClient.getInstance().getThreadHelper().getThreadpool().submit(() -> {
            EsdeathClient.getInstance().getServer().removeCape();
            EsdeathClient.getInstance().getPlayer(this.mc.thePlayer.getUniqueID().toString()).unloadTextures();
            EsdeathClient.getInstance().getPlayer(this.mc.thePlayer.getUniqueID().toString()).getCapes().clear();
            EsdeathClient.getInstance().getPlayer(this.mc.thePlayer.getUniqueID().toString()).getImagesLoading().clear();
            MessageHelper.sendMessage("cape.update");
         });
         Minecraft.getMinecraft().displayGuiScreen(null);
      }

      super.actionPerformed(var1);
   }

   @Override
   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      super.drawScreen(var1, var2, var3);
   }

   @Override
   protected void keyTyped(char var1, int var2) throws IOException {
      super.keyTyped(var1, var2);
   }
}
