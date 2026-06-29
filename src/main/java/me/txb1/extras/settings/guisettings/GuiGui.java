package me.txb1.extras.settings.guisettings;

import java.awt.Color;
import java.io.IOException;
import javax.swing.JColorChooser;
import me.txb1.EsdeathClient;
import me.txb1.extras.settings.SettingsUtil;
import me.txb1.player.buttons.CustomButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class GuiGui extends me.txb1.EsdeathGuiScreen {
   int y;
   int right;
   int x;
   int bottom;

   @Override
   protected void actionPerformed(GuiButton var1) throws IOException {
      super.actionPerformed(var1);
   }

   @Override
   public void mouseClickMove(int var1, int var2, int var3, long var4) {
   }

   @Override
   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      if (((var1) > (this.x + 5)) && ((var2) > (this.y + 5)) && ((var1) < (this.x + 70)) && ((var2) < (this.y + 18))) {
         this.mc.displayGuiScreen(new GuiIngameMenu());
         this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
      }

      super.mouseClicked(var1, var2, var3);
   }

   @Override
   protected void actionPerformed(CustomButton var1) throws IOException {
      if ((var1.displayString.equalsIgnoreCase("§eColorPicker"))) {
         System.out.println("clicked");
         Color var2 = JColorChooser.showDialog(null, "Choose a color", Color.RED);
         if (((var2) == null)) {
            return;
         }

         if (((var2.getTransparency()) > (90))) {
            return;
         }

         GuiSettings.umrandung = var2;
         if (((78 ^ 1 ^ 34 ^ 126) & (40 + 146 - 99 + 93 ^ 136 + 134 - 181 + 78 ^ -1))
            != ((109 + 107 - 55 + 7 ^ 17 + 95 - 12 + 48) & (9 + 62 - -173 + 8 ^ 188 + 92 - 192 + 104 ^ -1))) {
            return;
         }
      } else if ((var1.displayString.contains("§eButton -"))) {
         String var4 = var1.displayString.replace("§eButton - ", "");
         if ((var4.equalsIgnoreCase("Schwarz"))) {
            GuiSettings.button = "Weiß";
            
         } else {
            GuiSettings.button = "Schwarz";
         }
      }
   }

   @Override
   public void initGui() {
      super.initGui();
   }

   @Override
   public void onResize(Minecraft var1, int var2, int var3) {
      this.mc.displayGuiScreen(new GuiIngameMenu());
   }

   @Override
   public void updateScreen() {
      super.updateScreen();
   }

   private void setUpButtons() {
      this.customButtonList.clear();
      this.customButtonList
         .add(
            new CustomButton(
               0,
               this.x + 5,
               this.y + (this.bottom - this.y) / 5,
               (int)((double)(this.bottom - this.y) / 2.6),
               (this.bottom - this.y) / 3,
               "§eColorPicker"
            )
         );
      this.customButtonList
         .add(
            new CustomButton(
               1,
               this.x + 5,
               this.y + (this.bottom - this.y) / 5 + (this.bottom - this.y) / 3 + (this.bottom - this.y) / 12,
               (int)((double)(this.bottom - this.y) / 2.6),
               (this.bottom - this.y) / 3,
               String.valueOf(new StringBuilder().append("§eButton - ").append(GuiSettings.button))
            )
         );
   }

   @Override
   protected void mouseReleased(int var1, int var2, int var3) {
      super.mouseReleased(var1, var2, var3);
   }

   public GuiGui() {
      this.x = 0;
      this.y = 0;
      this.right = 0;
      this.bottom = 0;
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
            Gui.drawRect(this.x, this.y, this.right, this.bottom, var4.getRGB());
            Gui.drawRect(this.x + 5, this.y + 5, this.x + 70, this.y + 18, var5.getRGB());
            this.fontRendererObj.drawStringWithShadow("<< Zurück", this.x + 10, this.y + 8, EsdeathClient.getInstance().rainbow(500));
            this.setUpButtons();
            int var7 = 3;
            Gui.drawRect(this.x - var7, this.y, this.x, this.bottom, var6.getRGB());
            Gui.drawRect(this.x - var7, this.y - var7, this.right + var7, this.y, var6.getRGB());
            Gui.drawRect(this.right + var7, this.y, this.right, this.bottom, var6.getRGB());
            Gui.drawRect(this.x - var7, this.bottom + var7, this.right + var7, this.bottom, var6.getRGB());
         } catch (Exception var9) {
            break label15;
         }

      }

      super.drawScreen(var1, var2, var3);
   }
}
