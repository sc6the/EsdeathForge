package me.txb1.player.modulesystem.modules.visuals;

import java.io.IOException;
import me.txb1.EsdeathClient;
import me.txb1.extras.settings.anzeige.HudEditorGui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

// Editor for the HUD module's two-part label (part1 = rainbow, part2 = black). Two text lines plus a
// Reposition button (opens the standard drag editor) and Done (saves to FireDB + returns).
public class HudSettingsGui extends GuiScreen {
   private final HUD module;
   private final GuiScreen parent;
   private GuiTextField field1;
   private GuiTextField field2;

   public HudSettingsGui(HUD module, GuiScreen parent) {
      this.module = module;
      this.parent = parent;
   }

   @Override
   public void initGui() {
      int cx = this.width / 2;
      this.field1 = new GuiTextField(0, this.fontRendererObj, cx - 100, 60, 200, 18);
      this.field2 = new GuiTextField(1, this.fontRendererObj, cx - 100, 100, 200, 18);
      this.field1.setMaxStringLength(64);
      this.field2.setMaxStringLength(64);
      this.field1.setText(HUD.part1);
      this.field2.setText(HUD.part2);
      this.field1.setFocused(true);

      this.buttonList.add(new GuiButton(20, cx - 100, 134, 95, 20, "Reposition"));
      this.buttonList.add(new GuiButton(10, cx + 5, 134, 95, 20, "Done"));
   }

   private void apply() {
      HUD.part1 = this.field1.getText();
      HUD.part2 = this.field2.getText();
   }

   @Override
   protected void actionPerformed(GuiButton b) throws IOException {
      switch (b.id) {
         case 20:
            this.apply();
            HUD.save();
            this.mc.displayGuiScreen(new HudEditorGui(this));
            break;
         case 10:
            this.apply();
            HUD.save();
            this.mc.displayGuiScreen(this.parent);
            break;
      }
   }

   @Override
   protected void mouseClicked(int mx, int my, int btn) throws IOException {
      this.field1.mouseClicked(mx, my, btn);
      this.field2.mouseClicked(mx, my, btn);
      super.mouseClicked(mx, my, btn);
   }

   @Override
   protected void keyTyped(char c, int code) throws IOException {
      if (code == 1) { // ESC saves + returns
         this.apply();
         HUD.save();
         this.mc.displayGuiScreen(this.parent);
         return;
      }
      if (this.field1.textboxKeyTyped(c, code) || this.field2.textboxKeyTyped(c, code)) {
         this.apply();
         return;
      }
      super.keyTyped(c, code);
   }

   @Override
   public void updateScreen() {
      this.field1.updateCursorCounter();
      this.field2.updateCursorCounter();
   }

   @Override
   public void drawScreen(int mx, int my, float pt) {
      this.drawDefaultBackground();
      int cx = this.width / 2;
      this.drawCenteredString(this.fontRendererObj, "§lHUD Text", cx, 22, -1);
      this.drawString(this.fontRendererObj, "§7First part (theme colour):", cx - 100, 50, -1);
      this.drawString(this.fontRendererObj, "§7Second part (black):", cx - 100, 90, -1);
      this.field1.drawTextBox();
      this.field2.drawTextBox();

      // live preview, rendered exactly as the HUD draws it (part1 hud colour, part2 black)
      String p1 = this.field1.getText();
      String p2 = this.field2.getText();
      this.drawCenteredString(this.fontRendererObj, "§7Preview:", cx, 162, -1);
      int total = this.fontRendererObj.getStringWidth(p1) + this.fontRendererObj.getStringWidth(p2);
      int px = cx - total / 2;
      this.fontRendererObj.drawStringWithShadow(p1, px, 176, EsdeathClient.getInstance().rainbow(500));
      this.fontRendererObj.drawStringWithShadow(p2, px + this.fontRendererObj.getStringWidth(p1), 176, 0x000000);

      super.drawScreen(mx, my, pt);
   }
}
