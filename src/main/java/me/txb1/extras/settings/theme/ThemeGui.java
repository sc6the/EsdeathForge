package me.txb1.extras.settings.theme;

import java.io.IOException;
import me.txb1.EsdeathClient;
import me.txb1.forge.gui.ButtonStyle;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

// Theme settings: a custom hex colour (no presets) + the menu button style (Fancy / Semi Transparent).
// The hex theme string is resolved by EsdeathUtils.getRainbow ("#RRGGBB" via Color.decode).
public class ThemeGui extends GuiScreen {
   private final GuiScreen parent;
   private GuiTextField hexField;
   private boolean opacityDragging;
   private int opSliderX, opSliderY, opSliderW;

   public ThemeGui(GuiScreen var1) {
      this.parent = var1;
   }

   @Override
   public void initGui() {
      int cx = this.width / 2;
      this.hexField = new GuiTextField(0, this.fontRendererObj, cx - 45, 96, 90, 18);
      this.hexField.setMaxStringLength(7);
      String t = EsdeathClient.getInstance().theme;
      this.hexField.setText(t != null && t.startsWith("#") ? t.substring(1) : "");

      // button-style chooser (Fancy / Semi / Vanilla)
      this.buttonList.add(new GuiButton(20, cx - 100, 130, 63, 20, "Fancy"));
      this.buttonList.add(new GuiButton(21, cx - 32, 130, 63, 20, "Semi"));
      this.buttonList.add(new GuiButton(22, cx + 36, 130, 64, 20, "Vanilla"));

      this.buttonList.add(new GuiButton(30, cx - 100, 154, 98, 20, lowercaseLabel()));
      this.buttonList.add(new GuiButton(31, cx + 2, 154, 98, 20, "Snow..."));

      // opacity slider geometry (drawn/used only when the Semi Transparent style is equipped)
      this.opSliderX = cx - 100;
      this.opSliderY = 190;
      this.opSliderW = 200;

      this.buttonList.add(new GuiButton(10, cx - 50, 214, 100, 20, "Done"));
   }

   private void setOpacityFromMouse(int mouseX) {
      float frac = (float) (mouseX - this.opSliderX) / (float) Math.max(1, this.opSliderW - 6);
      frac = Math.max(0.0F, Math.min(1.0F, frac));
      ButtonStyle.setOpacity(Math.round(frac * 255.0F));
   }

   private void setTheme(String var1) {
      EsdeathClient.getInstance().theme = var1;
      EsdeathClient.getInstance().saveAll();
   }

   private void applyHex() {
      String v = this.hexField.getText().replace("#", "").trim();
      if (v.length() == 6) {
         try {
            Integer.parseInt(v, 16);
            this.setTheme("#" + v.toUpperCase());
         } catch (NumberFormatException ignored) {
         }
      }
   }

   @Override
   protected void actionPerformed(GuiButton var1) throws IOException {
      switch (var1.id) {
         case 20:
            ButtonStyle.setStyle(ButtonStyle.FANCY);
            break;
         case 21:
            ButtonStyle.setStyle(ButtonStyle.SEMI);
            break;
         case 22:
            ButtonStyle.setStyle(ButtonStyle.VANILLA);
            break;
         case 30:
            LowercaseMode.set(!LowercaseMode.enabled);
            var1.displayString = lowercaseLabel();
            break;
         case 31:
            this.mc.displayGuiScreen(new me.txb1.extras.snow.SnowGui(this));
            break;
         case 10:
            this.mc.displayGuiScreen(this.parent);
            break;
      }
   }

   private static String lowercaseLabel() {
      return "All Lowercase: " + (LowercaseMode.enabled ? "§aON" : "§cOFF");
   }

   @Override
   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      if (this.hexField != null) {
         this.hexField.mouseClicked(var1, var2, var3);
      }
      if (ButtonStyle.semiTransparent && var3 == 0
            && var1 >= this.opSliderX && var1 <= this.opSliderX + this.opSliderW
            && var2 >= this.opSliderY && var2 <= this.opSliderY + 10) {
         this.opacityDragging = true;
         setOpacityFromMouse(var1);
         return;
      }
      super.mouseClicked(var1, var2, var3);
   }

   @Override
   protected void mouseClickMove(int mx, int my, int btn, long time) {
      if (this.opacityDragging) {
         setOpacityFromMouse(mx);
      }
   }

   @Override
   protected void mouseReleased(int mx, int my, int btn) {
      this.opacityDragging = false;
      super.mouseReleased(mx, my, btn);
   }

   @Override
   protected void keyTyped(char var1, int var2) throws IOException {
      if (this.hexField != null && this.hexField.isFocused() && var2 != 1) {
         this.hexField.textboxKeyTyped(var1, var2);
         this.applyHex();
         return;
      }
      super.keyTyped(var1, var2);
   }

   @Override
   public void updateScreen() {
      if (this.hexField != null) {
         this.hexField.updateCursorCounter();
      }
   }

   @Override
   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      int cx = this.width / 2;
      this.drawCenteredString(this.fontRendererObj, "§lTheme", cx, 18, -1);
      // live preview swatch (no text on it)
      int color = 0xFF000000 | EsdeathClient.getInstance().rainbow(0);
      Gui.drawRect(cx - 45, 44, cx + 45, 86, color);
      this.hexField.drawTextBox();
      this.drawCenteredString(this.fontRendererObj, "§7Button style:", cx, 120, -1);
      super.drawScreen(var1, var2, var3);

      // Mark the currently-selected button style as "pressed": a darker overlay + a themed outline
      // around the active choice (Fancy id 20 on the left, Semi Transparent id 21 on the right).
      int active = 20 + ButtonStyle.styleIndex();
      for (Object o : this.buttonList) {
         GuiButton b = (GuiButton) o;
         if (b.id == active) {
            int x1 = b.xPosition, y1 = b.yPosition, x2 = x1 + b.width, y2 = y1 + b.height;
            Gui.drawRect(x1, y1, x2, y2, 0x80000000); // pressed-down shading
            int t = 0xFF000000 | EsdeathClient.getInstance().rainbow(0);
            Gui.drawRect(x1 - 1, y1 - 1, x2 + 1, y1, t);   // top
            Gui.drawRect(x1 - 1, y2, x2 + 1, y2 + 1, t);   // bottom
            Gui.drawRect(x1 - 1, y1, x1, y2, t);           // left
            Gui.drawRect(x2, y1, x2 + 1, y2, t);           // right
            // re-draw the label on top of the shading so it stays readable
            this.drawCenteredString(this.fontRendererObj, b.displayString, x1 + b.width / 2, y1 + (b.height - 8) / 2, 0xFFFFFF);
         }
      }

      // Button transparency slider — only when the Semi Transparent style is equipped.
      if (ButtonStyle.semiTransparent) {
         int pct = Math.round(ButtonStyle.opacity / 255.0F * 100.0F);
         this.drawCenteredString(this.fontRendererObj, "§7Button transparency: §f" + pct + "%", cx, this.opSliderY - 12, -1);
         // a live preview slab behind the track at the current opacity
         Gui.drawRect(this.opSliderX, this.opSliderY, this.opSliderX + this.opSliderW, this.opSliderY + 10,
            new java.awt.Color(0, 0, 0, ButtonStyle.opacity).getRGB());
         Gui.drawRect(this.opSliderX, this.opSliderY, this.opSliderX + this.opSliderW, this.opSliderY + 10, 0x40FFFFFF);
         float frac = ButtonStyle.opacity / 255.0F;
         int knobX = this.opSliderX + (int) (frac * (this.opSliderW - 6));
         int t = 0xFF000000 | EsdeathClient.getInstance().rainbow(0);
         Gui.drawRect(knobX, this.opSliderY - 2, knobX + 6, this.opSliderY + 12, t);
      }
   }
}
