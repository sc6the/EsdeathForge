package me.txb1.extras.settings.anzeige;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

// Generic drag-to-reposition editor for HUD elements that aren't "visual" modules (the scoreboard
// and chat). The stored value in AnzeigeSettings under `key` is an OFFSET (dx, dy) added to the
// element's normal position; the consuming mixins (MixinGuiIngame / MixinGuiNewChat) read the same
// key. A ghost box previews where the element will sit. ESC saves + returns.
public class HudOffsetGui extends GuiScreen {
   private final GuiScreen back;
   private final String key;
   private final String label;
   private final int boxW;
   private final int boxH;

   private Cordinates off;
   private boolean dragging;
   private int grabDX;
   private int grabDY;

   public HudOffsetGui(GuiScreen back, String key, String label, int boxW, int boxH) {
      this.back = back;
      this.key = key;
      this.label = label;
      this.boxW = boxW;
      this.boxH = boxH;
   }

   @Override
   public void initGui() {
      this.off = AnzeigeSettings.getCords(this.key);
   }

   // anchor (top-left of the ghost) at offset (0,0): scoreboard hugs the right edge, chat the bottom-left
   private int baseX() {
      return "chat".equals(this.key) ? 2 : this.width - this.boxW - 2;
   }

   private int baseY() {
      return "chat".equals(this.key) ? this.height - this.boxH - 28 : this.height / 2 - this.boxH / 2;
   }

   private int boxX() {
      return baseX() + this.off.getX();
   }

   private int boxY() {
      return baseY() + this.off.getY();
   }

   @Override
   protected void mouseClicked(int mx, int my, int btn) {
      if (btn == 0 && mx >= boxX() && mx <= boxX() + this.boxW && my >= boxY() && my <= boxY() + this.boxH) {
         this.dragging = true;
         this.grabDX = mx - boxX();
         this.grabDY = my - boxY();
      }
   }

   @Override
   protected void mouseClickMove(int mx, int my, int btn, long time) {
      if (this.dragging) {
         this.off.setX(mx - this.grabDX - baseX());
         this.off.setY(my - this.grabDY - baseY());
         AnzeigeSettings.getCords().put(this.key, this.off);
      }
   }

   @Override
   protected void mouseReleased(int mx, int my, int btn) {
      if (this.dragging) {
         this.dragging = false;
         AnzeigeSettings.save();
      }
   }

   @Override
   protected void keyTyped(char c, int code) {
      if (code == 1) { // ESC
         AnzeigeSettings.save();
         this.mc.displayGuiScreen(this.back);
      }
   }

   @Override
   public void drawScreen(int mx, int my, float pt) {
      this.drawDefaultBackground();
      int bx = boxX();
      int by = boxY();

      // centre guides — green when the box's centre lines up with the screen centre
      int cxg = this.width / 2;
      int cyg = this.height / 2;
      int boxCx = bx + this.boxW / 2;
      int boxCy = by + this.boxH / 2;
      Gui.drawRect(cxg, 0, cxg + 1, this.height, Math.abs(boxCx - cxg) < 3 ? 0xFF55FF55 : 0x66FFFFFF);
      Gui.drawRect(0, cyg, this.width, cyg + 1, Math.abs(boxCy - cyg) < 3 ? 0xFF55FF55 : 0x66FFFFFF);

      Gui.drawRect(bx, by, bx + this.boxW, by + this.boxH, 0x66000000);
      // themed outline
      Gui.drawRect(bx - 1, by - 1, bx + this.boxW + 1, by, 0xFF55FFFF);
      Gui.drawRect(bx - 1, by + this.boxH, bx + this.boxW + 1, by + this.boxH + 1, 0xFF55FFFF);
      Gui.drawRect(bx - 1, by, bx, by + this.boxH, 0xFF55FFFF);
      Gui.drawRect(bx + this.boxW, by, bx + this.boxW + 1, by + this.boxH, 0xFF55FFFF);
      this.drawCenteredString(this.fontRendererObj, this.label, bx + this.boxW / 2, by + this.boxH / 2 - 4, 0xFFFFFF);

      this.drawCenteredString(this.fontRendererObj, "§7Drag the box to reposition — ESC to save", this.width / 2, 8, -1);
      this.drawString(this.fontRendererObj, "§7offset x" + this.off.getX() + " y" + this.off.getY(), 4, 4, -1);
   }
}
