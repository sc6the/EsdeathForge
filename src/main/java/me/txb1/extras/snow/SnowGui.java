package me.txb1.extras.snow;

import java.io.IOException;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

// Snow customisation (opened from Theme -> Snow): toggle the main-menu snow, and tune the amount,
// colour and transparency shared by the main-menu snow and the Inventory Snow module. Live snow is
// drawn behind the controls so changes are visible immediately. ESC/Done saves.
public class SnowGui extends GuiScreen {
   private final GuiScreen back;
   private GuiButton toggle;
   private GuiTextField hexField;
   private boolean amountDragging;
   private boolean alphaDragging;
   private boolean cursorDragging;
   private int amountX, amountY, alphaX, alphaY, cursorX, cursorY, sliderW;

   public SnowGui(GuiScreen back) {
      this.back = back;
   }

   @Override
   public void initGui() {
      this.buttonList.clear();
      int cx = this.width / 2;
      this.sliderW = 200;
      this.amountX = cx - 100;
      this.amountY = 64;
      this.alphaX = cx - 100;
      this.alphaY = 94;
      this.cursorX = cx - 100;
      this.cursorY = 124;

      this.toggle = new GuiButton(1, cx - 100, 34, 200, 20, toggleLabel());
      this.buttonList.add(this.toggle);

      this.hexField = new GuiTextField(0, this.fontRendererObj, cx - 100, 158, 100, 18);
      this.hexField.setMaxStringLength(6);
      this.hexField.setText(String.format("%06X", SnowSettings.color & 0xFFFFFF));

      this.buttonList.add(new GuiButton(0, cx - 50, 188, 100, 20, "Done"));
   }

   private static String toggleLabel() {
      return "Menu Snow: " + (SnowSettings.menuEnabled ? "§aON" : "§cOFF");
   }

   private void setAmountFromMouse(int mx) {
      float frac = (float) (mx - this.amountX) / (float) Math.max(1, this.sliderW - 6);
      frac = Math.max(0.0F, Math.min(1.0F, frac));
      SnowSettings.amount = SnowSettings.clampAmount(10 + Math.round(frac * (400 - 10)));
   }

   private void setAlphaFromMouse(int mx) {
      float frac = (float) (mx - this.alphaX) / (float) Math.max(1, this.sliderW - 6);
      frac = Math.max(0.0F, Math.min(1.0F, frac));
      SnowSettings.alpha = SnowSettings.clampAlpha(Math.round(frac * 255.0F));
   }

   private void setCursorFromMouse(int mx) {
      float frac = (float) (mx - this.cursorX) / (float) Math.max(1, this.sliderW - 6);
      frac = Math.max(0.0F, Math.min(1.0F, frac));
      SnowSettings.cursorLines = SnowSettings.clampPercent(Math.round(frac * 100.0F));
   }

   private void applyHex() {
      String v = this.hexField.getText().replace("#", "").trim();
      if (v.length() == 6) {
         try {
            SnowSettings.color = Integer.parseInt(v, 16) & 0xFFFFFF;
         } catch (NumberFormatException ignored) {
         }
      }
   }

   @Override
   protected void actionPerformed(GuiButton b) throws IOException {
      switch (b.id) {
         case 1:
            SnowSettings.menuEnabled = !SnowSettings.menuEnabled;
            this.toggle.displayString = toggleLabel();
            break;
         case 0:
            applyHex();
            SnowSettings.save();
            this.mc.displayGuiScreen(this.back);
            break;
      }
   }

   @Override
   protected void mouseClicked(int mx, int my, int btn) throws IOException {
      if (btn == 0 && my >= this.amountY && my <= this.amountY + 10 && mx >= this.amountX && mx <= this.amountX + this.sliderW) {
         this.amountDragging = true;
         setAmountFromMouse(mx);
         return;
      }
      if (btn == 0 && my >= this.alphaY && my <= this.alphaY + 10 && mx >= this.alphaX && mx <= this.alphaX + this.sliderW) {
         this.alphaDragging = true;
         setAlphaFromMouse(mx);
         return;
      }
      if (btn == 0 && my >= this.cursorY && my <= this.cursorY + 10 && mx >= this.cursorX && mx <= this.cursorX + this.sliderW) {
         this.cursorDragging = true;
         setCursorFromMouse(mx);
         return;
      }
      this.hexField.mouseClicked(mx, my, btn);
      super.mouseClicked(mx, my, btn);
   }

   @Override
   protected void mouseClickMove(int mx, int my, int btn, long time) {
      if (this.amountDragging) {
         setAmountFromMouse(mx);
      } else if (this.alphaDragging) {
         setAlphaFromMouse(mx);
      } else if (this.cursorDragging) {
         setCursorFromMouse(mx);
      }
   }

   @Override
   protected void mouseReleased(int mx, int my, int btn) {
      this.amountDragging = false;
      this.alphaDragging = false;
      this.cursorDragging = false;
      super.mouseReleased(mx, my, btn);
   }

   @Override
   protected void keyTyped(char c, int code) throws IOException {
      if (code == 1) { // ESC saves
         applyHex();
         SnowSettings.save();
         this.mc.displayGuiScreen(this.back);
         return;
      }
      if (this.hexField.isFocused()) {
         this.hexField.textboxKeyTyped(c, code);
         applyHex();
         return;
      }
      super.keyTyped(c, code);
   }

   @Override
   public void updateScreen() {
      this.hexField.updateCursorCounter();
   }

   @Override
   public void drawScreen(int mx, int my, float pt) {
      this.drawDefaultBackground();
      // live snow preview (always drawn here so tuning is visible)
      SnowRenderer.render(this.width, this.height, mx, my);

      int cx = this.width / 2;
      this.drawCenteredString(this.fontRendererObj, "§lSnow", cx, 18, -1);

      drawSlider(this.amountX, this.amountY, "Amount", SnowSettings.amount, 10, 400);
      drawSlider(this.alphaX, this.alphaY, "Transparency", SnowSettings.alpha, 0, 255);
      drawSlider(this.cursorX, this.cursorY, "Cursor Lines", SnowSettings.cursorLines, 0, 100);

      this.drawString(this.fontRendererObj, "§7Colour (hex):", this.amountX, 148, -1);
      this.hexField.drawTextBox();
      // colour swatch next to the field
      Gui.drawRect(cx + 8, 158, cx + 28, 176, 0xFF000000 | (SnowSettings.color & 0xFFFFFF));

      super.drawScreen(mx, my, pt);
   }

   private void drawSlider(int x, int y, String name, int value, int min, int max) {
      this.drawString(this.fontRendererObj, "§7" + name + ": §f" + value, x, y - 10, -1);
      Gui.drawRect(x, y, x + this.sliderW, y + 10, 0x80000000);
      float frac = (float) (value - min) / (float) (max - min);
      int knobX = x + (int) (frac * (this.sliderW - 6));
      Gui.drawRect(knobX, y, knobX + 6, y + 10, 0xFFFFFFFF);
   }
}
