package me.txb1.extras.settings.anzeige;

import com.darkmagician6.eventapi.EventManager;
import java.awt.Color;
import java.io.IOException;
import me.txb1.extras.settings.guisettings.GuiSettings;
import me.txb1.player.events.EventRender;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

public class AnzeigeGui extends GuiScreen {
   private GuiScreen back;
   private int released;
   private Cordinates cordinates;
   private Module module;

   private Integer getDist(int var1, int var2) {
      return ((var1) >= (var2)) ? var1 - var2 : var2 - var1;
   }

   @Override
   public void drawScreen(int var1, int var2, float var3) {
      GlStateManager.pushMatrix();
      this.mc
         .fontRendererObj
         .drawStringWithShadow(
            String.valueOf(new StringBuilder().append("x").append(var1).append(" y").append(var2)),
            this.width / 2 - 60,
            this.height / 2 + 20,
            new Color(0, 0, 0).getRGB()
         );
      GlStateManager.popMatrix();
      Color var4 = GuiSettings.umrandung;

      // Always-visible centre guides: a full-height vertical line and full-width horizontal line
      // through the screen centre. They turn green when the dragged element snaps onto that axis.
      int cxg = this.width / 2;
      int cyg = this.height / 2;
      boolean vAlign = this.getDist(var1, cxg) < 3;
      boolean hAlign = this.getDist(var2, cyg) < 3;
      Gui.drawRect(cxg, 0, cxg + 1, this.height, vAlign ? 0xFF55FF55 : 0x66FFFFFF);
      Gui.drawRect(0, cyg, this.width, cyg + 1, hAlign ? 0xFF55FF55 : 0x66FFFFFF);

      Gui.drawRect(2, 2, this.width - 2, 1, var4.getRGB());
      Gui.drawRect(1, 2, 2, this.height - 2, var4.getRGB());
      Gui.drawRect(this.width - 1, 2, this.width - 2, this.height - 2, var4.getRGB());
      super.drawScreen(var1, var2, var3);
   }

   @Override
   public void mouseClickMove(int var1, int var2, int var3, long var4) {
      if (((var1 % 2) == 0)) {
         this.cordinates.setX(var1);
         
      } else {
         this.cordinates.setX(var1 + 1);
      }

      if (((var2 % 2) == 0)) {
         this.cordinates.setY(var2);
         
      } else {
         this.cordinates.setY(var2 + 1);
      }

      AnzeigeSettings.getCords().put(this.module.getName().toLowerCase(), this.cordinates);
   }

   @Override
   public void initGui() {
      super.initGui();
   }

   @Override
   protected void mouseReleased(int var1, int var2, int var3) {
      if (((this.released) >= (1))) {
         this.released = 0;
         System.out.println("release");
         this.mc.displayGuiScreen(this.back);
         
      } else {
         this.released = this.released + 1;
      }

      super.mouseReleased(var1, var2, var3);
   }

   @Override
   public void updateScreen() {
      EventRender var1 = new EventRender();
      EventManager.call(var1);
      super.updateScreen();
   }

   @Override
   protected void actionPerformed(GuiButton var1) throws IOException {
      super.actionPerformed(var1);
   }

   @Override
   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      super.mouseClicked(var1, var2, var3);
   }

   @Override
   public void onResize(Minecraft var1, int var2, int var3) {
      this.mc.displayGuiScreen(new GuiIngameMenu());
   }

   @Override
   protected void keyTyped(char var1, int var2) throws IOException {
      super.keyTyped(var1, var2);
   }

   public AnzeigeGui(Module var1, GuiScreen var2) {
      this.released = 0;
      this.module = var1;
      this.back = var2;
      this.cordinates = AnzeigeSettings.getCords(this.module.getName().toLowerCase());
   }

}
