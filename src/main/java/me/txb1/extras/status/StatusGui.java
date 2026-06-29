package me.txb1.extras.status;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

// Set the local status shown above your nametag. Text supports '&' colour codes. Size is a multiplier
// on the nametag scale; Y is a vertical offset (world units). Both Size and Y accept typed decimal
// values (e.g. 1.25) as well as the -/+ steppers.
public class StatusGui extends GuiScreen {
   private GuiTextField statusField;
   private GuiTextField sizeField;
   private GuiTextField yField;

   private static final float SIZE_MIN = 0.5F, SIZE_MAX = 3.0F;
   private static final float Y_MIN = -2.0F, Y_MAX = 2.0F;

   @Override
   public void initGui() {
      LocalStatus.ensure();
      int cx = this.width / 2;
      this.statusField = new GuiTextField(0, this.fontRendererObj, cx - 105, 60, 210, 20);
      this.statusField.setMaxStringLength(64);
      this.statusField.setText(LocalStatus.text.replace("§", "&"));
      this.statusField.setFocused(true);

      // Size row: [-] [ field ] [+]
      this.buttonList.add(new GuiButton(1, cx - 110, 96, 30, 20, "-"));
      this.sizeField = new GuiTextField(1, this.fontRendererObj, cx - 72, 98, 144, 16);
      this.sizeField.setMaxStringLength(8);
      this.sizeField.setText(fmt(LocalStatus.size));
      this.buttonList.add(new GuiButton(2, cx + 80, 96, 30, 20, "+"));

      // Y row: [-] [ field ] [+]
      this.buttonList.add(new GuiButton(3, cx - 110, 130, 30, 20, "-"));
      this.yField = new GuiTextField(2, this.fontRendererObj, cx - 72, 132, 144, 16);
      this.yField.setMaxStringLength(8);
      this.yField.setText(fmt(LocalStatus.y));
      this.buttonList.add(new GuiButton(4, cx + 80, 130, 30, 20, "+"));

      this.buttonList.add(new GuiButton(0, cx - 50, 168, 100, 20, "Save"));
   }

   private static String fmt(float v) {
      String s = String.format(java.util.Locale.US, "%.2f", v);
      return s;
   }

   private static float parse(String s, float def) {
      try {
         return Float.parseFloat(s.trim());
      } catch (Exception e) {
         return def;
      }
   }

   private static float clamp(float v, float min, float max) {
      return Math.max(min, Math.min(max, v));
   }

   private void applyText() {
      LocalStatus.text = this.statusField.getText().replace("&", "§");
   }

   // pull Size/Y from the typed fields (clamped), without rewriting what the user is typing
   private void applyFields() {
      LocalStatus.size = clamp(parse(this.sizeField.getText(), LocalStatus.size), SIZE_MIN, SIZE_MAX);
      LocalStatus.y = clamp(parse(this.yField.getText(), LocalStatus.y), Y_MIN, Y_MAX);
   }

   @Override
   protected void actionPerformed(GuiButton b) throws IOException {
      switch (b.id) {
         case 1:
            LocalStatus.size = clamp(LocalStatus.size - 0.1F, SIZE_MIN, SIZE_MAX);
            this.sizeField.setText(fmt(LocalStatus.size));
            break;
         case 2:
            LocalStatus.size = clamp(LocalStatus.size + 0.1F, SIZE_MIN, SIZE_MAX);
            this.sizeField.setText(fmt(LocalStatus.size));
            break;
         case 3:
            LocalStatus.y = clamp(LocalStatus.y - 0.1F, Y_MIN, Y_MAX);
            this.yField.setText(fmt(LocalStatus.y));
            break;
         case 4:
            LocalStatus.y = clamp(LocalStatus.y + 0.1F, Y_MIN, Y_MAX);
            this.yField.setText(fmt(LocalStatus.y));
            break;
         case 0:
            applyText();
            applyFields();
            LocalStatus.save();
            this.mc.displayGuiScreen(null);
            break;
      }
   }

   @Override
   protected void mouseClicked(int mx, int my, int btn) throws IOException {
      this.statusField.mouseClicked(mx, my, btn);
      this.sizeField.mouseClicked(mx, my, btn);
      this.yField.mouseClicked(mx, my, btn);
      super.mouseClicked(mx, my, btn);
   }

   @Override
   protected void keyTyped(char c, int code) throws IOException {
      if (code == 1) { // ESC saves + closes
         applyText();
         applyFields();
         LocalStatus.save();
         this.mc.displayGuiScreen(null);
         return;
      }
      if (this.sizeField.textboxKeyTyped(c, code)) {
         applyFields();
         return;
      }
      if (this.yField.textboxKeyTyped(c, code)) {
         applyFields();
         return;
      }
      if (this.statusField.textboxKeyTyped(c, code)) {
         applyText();
         return;
      }
      super.keyTyped(c, code);
   }

   @Override
   public void updateScreen() {
      this.statusField.updateCursorCounter();
      this.sizeField.updateCursorCounter();
      this.yField.updateCursorCounter();
   }

   @Override
   public void drawScreen(int mx, int my, float pt) {
      this.drawDefaultBackground();
      int cx = this.width / 2;
      this.drawCenteredString(this.fontRendererObj, "§lStatus", cx, 22, -1);
      this.drawString(this.fontRendererObj, "§7Status text (use & for colours):", cx - 105, 48, -1);
      this.statusField.drawTextBox();

      this.drawString(this.fontRendererObj, "§7Size", cx - 105, 86, -1);
      this.sizeField.drawTextBox();
      this.drawString(this.fontRendererObj, "§7Y offset", cx - 105, 120, -1);
      this.yField.drawTextBox();

      // live preview
      String preview = this.statusField.getText().replace("&", "§");
      if (!preview.isEmpty()) {
         this.drawCenteredString(this.fontRendererObj, "§7Preview:", cx, 196, -1);
         this.drawCenteredString(this.fontRendererObj, preview, cx, 206, -1);
      }
      super.drawScreen(mx, my, pt);
   }
}
