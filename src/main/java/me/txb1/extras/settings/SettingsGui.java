package me.txb1.extras.settings;

import java.awt.Color;
import java.io.IOException;
import me.txb1.EsdeathClient;
import me.txb1.extras.settings.guisettings.GuiSettings;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class SettingsGui extends GuiScreen {
   int bottom;
   int load;
   int right;
   int x;
   private Module module;
   int lastload;
   int y;

   @Override
   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      this.module.mouseClicked(var1, var2, var3, this.x, this.y, this.height, this.width);
      if (((var1) > (this.x + 5)) && ((var2) > (this.y + 5)) && ((var1) < (this.x + 70)) && ((var2) < (this.y + 18))) {
         this.mc.displayGuiScreen(new GuiIngameMenu());
         this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
      }

      super.mouseClicked(var1, var2, var3);
   }

   public SettingsGui(Module var1) {
      this.x = 0;
      this.y = 0;
      this.right = 0;
      this.bottom = 0;
      this.load = 100;
      this.lastload = 100;
      this.module = var1;
   }

   @Override
   public void onResize(Minecraft var1, int var2, int var3) {
      this.mc.displayGuiScreen(new GuiIngameMenu());
   }

   @Override
   protected void actionPerformed(GuiButton var1) throws IOException {
      super.actionPerformed(var1);
   }

   @Override
   protected void keyTyped(char var1, int var2) throws IOException {
      this.module.onSettingsKeyType(var1, var2);
      super.keyTyped(var1, var2);
   }

   @Override
   public void updateScreen() {
      super.updateScreen();
   }

   @Override
   protected void mouseReleased(int var1, int var2, int var3) {
      super.mouseReleased(var1, var2, var3);
   }

   @Override
   public void initGui() {
      super.initGui();
   }

   @Override
   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      Color var4 = new Color(255, 255, 255, 23);
      Color var5 = new Color(122, 193, 95, 30);
      Color var6 = GuiSettings.umrandung;

      label15: {
         try {
            this.x = SettingsUtil.vals.get("x");
            this.y = SettingsUtil.vals.get("y");
            this.right = SettingsUtil.vals.get("right");
            this.bottom = SettingsUtil.vals.get("bottom");
            this.module.onSettingsDrawScreen(var1, var2, this.x, this.y, this.right, this.bottom, this.height, this.width);
            Gui.drawRect(this.x, this.y, this.right, this.bottom, var4.getRGB());
            Gui.drawRect(this.x + 5, this.y + 5, this.x + 70, this.y + 18, var5.getRGB());
            int var7 = 3;
            Gui.drawRect(this.x - var7, this.y, this.x, this.bottom, var6.getRGB());
            Gui.drawRect(this.x - var7, this.y - var7, this.right + var7, this.y, var6.getRGB());
            Gui.drawRect(this.right + var7, this.y, this.right, this.bottom, var6.getRGB());
            Gui.drawRect(this.x - var7, this.bottom + var7, this.right + var7, this.bottom, var6.getRGB());
            this.fontRendererObj.drawStringWithShadow("<< Zurück", this.x + 10, this.y + 8, EsdeathClient.getInstance().rainbow(500));
         } catch (Exception var8) {
            break label15;
         }

      }

      super.drawScreen(var1, var2, var3);
   }

   @Override
   public void mouseClickMove(int var1, int var2, int var3, long var4) {
   }
}
