package me.txb1.extras.cosmetics.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.txb1.EsdeathClient;
import me.txb1.MessageHelper;
import me.txb1.extras.cosmetics.CosmeticController;
import me.txb1.extras.cosmetics.oam.CosmeticOptions;
import me.txb1.player.buttons.CosmeticButton;
import me.txb1.utils.EsdeathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;

// Cosmetic browser: a vertical category breadcrumb (Esdeath / Labymod / OAM) on the top-left, a
// scrollable thumbnail grid for the active category with a draggable left scrollbar, and a fixed
// editing panel across the bottom (Color / Opacity / Scale / Style, with X-Y-Z + multicolor + any
// extra OAM variant options in a right-hand column).
public class CosmeticGui extends me.txb1.EsdeathGuiScreen {
   private static final int MARGIN = 12;
   private static final int COLS = 4;
   private static final int BTN_H = 18;
   private static final int ROW_GAP = 5;
   private static final int EDIT_H = 84;
   private static final int BTN_RESET = 1;
   private static final String[] CATS = {"Esdeath", "OAM"};

   private GuiButton button_refresh;
   private GuiTextField hexField;
   private GuiTextField xField;
   private GuiTextField yField;
   private GuiTextField zField;
   private boolean scaleDragging;
   private boolean alphaDragging;
   private boolean barDragging;
   private String selected;
   private int lastButton;

   private int activeCat = 1; // default to OAM (matches the largest set)
   private final List<CosmeticButton> all = new ArrayList<CosmeticButton>();
   private final List<CosmeticButton> view = new ArrayList<CosmeticButton>(); // active category
   private int scroll;

   // layout (computed in initGui)
   private int catX, catY, catH;
   private int gridX, gridW, viewTop, viewBottom;
   private int barX, barW;
   private int editTop;
   private int colLabelW, sliderX, sliderW;
   private int rowColor, rowOpacity, rowScale, rowStyle;
   private int blendX, blendW;
   private int rightX, fieldRowY, optStartY;

   public static void fail(String var0) {
      FontRenderer var1 = Minecraft.getMinecraft().fontRendererObj;
      var1.drawString(var0, 30.0F, 5.0F, Color.RED.getRGB(), true);
   }

   @Override
   public void initGui() {
      if (!(EsdeathUtils.isOnline(this.mc.thePlayer.getName()))) {
         this.mc.displayGuiScreen(null);
         MessageHelper.sendMessage("loading");
         return;
      }

      this.catX = MARGIN;
      this.catY = 16;
      this.catH = 13;
      this.barX = MARGIN;
      this.barW = 5;
      this.gridX = MARGIN + this.barW + 8;
      this.viewTop = this.catY + CATS.length * this.catH + 8;
      this.gridW = this.width - this.gridX - MARGIN;
      this.editTop = this.height - EDIT_H;
      this.viewBottom = this.editTop - 4;

      // bottom edit panel geometry
      this.colLabelW = 52;
      this.sliderX = MARGIN + this.colLabelW;
      this.sliderW = Math.min(150, this.width / 2 - this.sliderX - 30);
      this.rowColor = this.editTop + 14;
      this.rowOpacity = this.editTop + 30;
      this.rowScale = this.editTop + 46;
      this.rowStyle = this.editTop + 62;
      // blend switch sits directly to the right of the colour hex field
      this.blendX = this.sliderX + 62;
      this.blendW = 70;
      this.rightX = this.width / 2 + 10;
      this.fieldRowY = this.editTop + 14;
      this.optStartY = this.editTop + 32;

      // build all cosmetic buttons once, bucketed by category at layout time
      this.all.clear();
      int id = 0;
      for (String name : CosmeticController.getCosmetics()) {
         CosmeticButton b = new CosmeticButton(id++, 0, 0, 10, BTN_H, name);
         this.cosmeticButtonList.add(b);
         this.all.add(b);
      }

      FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
      this.hexField = new GuiTextField(1, fr, this.sliderX, this.rowColor, 58, 14);
      this.hexField.setMaxStringLength(7);
      this.xField = new GuiTextField(2, fr, this.rightX + 26, this.fieldRowY, 28, 14);
      this.xField.setMaxStringLength(8);
      this.yField = new GuiTextField(3, fr, this.rightX + 58, this.fieldRowY, 28, 14);
      this.yField.setMaxStringLength(8);
      this.zField = new GuiTextField(4, fr, this.rightX + 90, this.fieldRowY, 28, 14);
      this.zField.setMaxStringLength(8);

      this.button_refresh = new GuiButton(0, this.width - 92, this.editTop - 22, 80, 18, "Refresh");
      this.buttonList.add(this.button_refresh);
      // resets the selected cosmetic's scale / colour / opacity / position back to defaults
      this.buttonList.add(new GuiButton(BTN_RESET, this.width - 92 - 86, this.editTop - 22, 80, 18, "Reset"));
      super.initGui();
   }

   private void rebuildView() {
      this.view.clear();
      String cat = CATS[this.activeCat];
      for (CosmeticButton b : this.all) {
         String name = strip(b.displayString);
         boolean match = CosmeticController.categoryOf(name).equals(cat);
         b.visible = match; // only active-category buttons participate
         if (match) {
            this.view.add(b);
         }
      }
   }

   private static String strip(String s) {
      return s.replace("§a", "").replace("§c", "");
   }

   private int maxScroll() {
      int rows = (this.view.size() + COLS - 1) / COLS;
      int content = rows * (BTN_H + ROW_GAP);
      return Math.max(0, content - (this.viewBottom - this.viewTop));
   }

   private void layoutGrid() {
      int bw = (this.gridW - (COLS - 1) * 6) / COLS;
      for (int i = 0; i < this.view.size(); i++) {
         CosmeticButton b = this.view.get(i);
         int col = i % COLS;
         int row = i / COLS;
         int x = this.gridX + col * (bw + 6);
         int y = this.viewTop + row * (BTN_H + ROW_GAP) - this.scroll;
         b.xPosition = x;
         b.yPosition = y;
         b.setWidth(bw);
         b.visible = y >= this.viewTop && y + BTN_H <= this.viewBottom;
      }
   }

   private void populate() {
      if (this.selected != null) {
         this.hexField.setText(CosmeticController.getColorHex(this.selected));
         this.xField.setText(String.valueOf(CosmeticController.getOffsetX(this.selected)));
         this.yField.setText(String.valueOf(CosmeticController.getOffsetY(this.selected)));
         this.zField.setText(String.valueOf(CosmeticController.getOffsetZ(this.selected)));
      }
   }

   private void applyHex() {
      if (this.selected != null) {
         CosmeticController.setColorHex(this.selected, this.hexField.getText());
      }
   }

   private float parseF(String s, float def) {
      try {
         return Float.parseFloat(s.trim());
      } catch (Exception e) {
         return def;
      }
   }

   private void applyXYZ() {
      if (this.selected != null) {
         float x = this.parseF(this.xField.getText(), CosmeticController.getOffsetX(this.selected));
         float y = this.parseF(this.yField.getText(), CosmeticController.getOffsetY(this.selected));
         float z = this.parseF(this.zField.getText(), CosmeticController.getOffsetZ(this.selected));
         CosmeticController.setOffset(this.selected, x, y, z);
      }
   }

   private void setScaleFromMouse(int mouseX) {
      if (this.selected == null) {
         return;
      }
      float frac = (float) (mouseX - this.sliderX) / (float) (this.sliderW - 6);
      frac = Math.max(0.0F, Math.min(1.0F, frac));
      CosmeticController.setScale(this.selected, CosmeticController.SCALE_MIN + frac * (CosmeticController.SCALE_MAX - CosmeticController.SCALE_MIN));
   }

   private void setAlphaFromMouse(int mouseX) {
      if (this.selected == null) {
         return;
      }
      float frac = (float) (mouseX - this.sliderX) / (float) (this.sliderW - 6);
      frac = Math.max(0.0F, Math.min(1.0F, frac));
      CosmeticController.setAlpha(this.selected, CosmeticController.ALPHA_MIN + frac * (1.0F - CosmeticController.ALPHA_MIN));
   }

   private boolean inRect(int mx, int my, int x, int y, int w, int h) {
      return mx >= x && mx <= x + w && my >= y && my <= y + h;
   }

   @Override
   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      int wheel = Mouse.getEventDWheel();
      if (wheel == 0) {
         return;
      }
      int step = wheel > 0 ? -(BTN_H + ROW_GAP) : (BTN_H + ROW_GAP);
      this.scroll = Math.max(0, Math.min(this.maxScroll(), this.scroll + step));
   }

   private void setScrollFromBar(int mouseY) {
      int viewH = this.viewBottom - this.viewTop;
      int max = this.maxScroll();
      if (max <= 0) {
         return;
      }
      float frac = (float) (mouseY - this.viewTop) / (float) viewH;
      frac = Math.max(0.0F, Math.min(1.0F, frac));
      this.scroll = (int) (frac * max);
   }

   @Override
   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      this.lastButton = var3;

      // category breadcrumb
      for (int i = 0; i < CATS.length; i++) {
         int cy = this.catY + i * this.catH;
         if (this.inRect(var1, var2, this.catX, cy, 90, this.catH)) {
            if (this.activeCat != i) {
               this.activeCat = i;
               this.scroll = 0;
            }
            return;
         }
      }

      // left scrollbar
      if (this.inRect(var1, var2, this.barX, this.viewTop, this.barW, this.viewBottom - this.viewTop)) {
         this.barDragging = true;
         this.setScrollFromBar(var2);
         return;
      }

      if (this.selected != null && this.hexField != null) {
         this.hexField.mouseClicked(var1, var2, var3);
         this.xField.mouseClicked(var1, var2, var3);
         this.yField.mouseClicked(var1, var2, var3);
         this.zField.mouseClicked(var1, var2, var3);
         if (this.inRect(var1, var2, this.sliderX, this.rowScale, this.sliderW, 12)) {
            this.scaleDragging = true;
            this.setScaleFromMouse(var1);
         } else if (this.inRect(var1, var2, this.sliderX, this.rowOpacity, this.sliderW, 12)) {
            this.alphaDragging = true;
            this.setAlphaFromMouse(var1);
         } else if (this.inRect(var1, var2, this.blendX, this.rowColor, this.blendW, 14)) {
            CosmeticController.setOverlay(this.selected, !CosmeticController.isOverlay(this.selected));
         } else {
            List<CosmeticOptions.Opt> opts = CosmeticOptions.get(this.selected);
            // first option is the "Style" row on the left; the rest stack in the right column
            if (!opts.isEmpty() && this.inRect(var1, var2, this.sliderX, this.rowStyle, this.sliderW, 12)) {
               this.cycleOption(opts.get(0), var3 == 1);
            } else {
               for (int i = 1; i < opts.size(); i++) {
                  int oy = this.optStartY + (i - 1) * 14;
                  if (this.inRect(var1, var2, this.rightX, oy, 150, 12)) {
                     this.cycleOption(opts.get(i), var3 == 1);
                     break;
                  }
               }
            }
         }
      }
      super.mouseClicked(var1, var2, var3);
   }

   @Override
   protected void mouseClickMove(int var1, int var2, int var3, long var4) {
      if (this.scaleDragging) {
         this.setScaleFromMouse(var1);
      } else if (this.alphaDragging) {
         this.setAlphaFromMouse(var1);
      } else if (this.barDragging) {
         this.setScrollFromBar(var2);
      }
      super.mouseClickMove(var1, var2, var3, var4);
   }

   @Override
   protected void mouseReleased(int var1, int var2, int var3) {
      this.scaleDragging = false;
      this.alphaDragging = false;
      this.barDragging = false;
      super.mouseReleased(var1, var2, var3);
   }

   @Override
   protected void keyTyped(char var1, int var2) throws IOException {
      if (var2 == 1) {
         super.keyTyped(var1, var2);
         return;
      }
      if (this.selected != null && this.hexField != null) {
         if (this.hexField.isFocused()) {
            this.hexField.textboxKeyTyped(var1, var2);
            this.applyHex();
            return;
         }
         if (this.xField.isFocused()) {
            this.xField.textboxKeyTyped(var1, var2);
            this.applyXYZ();
            return;
         }
         if (this.yField.isFocused()) {
            this.yField.textboxKeyTyped(var1, var2);
            this.applyXYZ();
            return;
         }
         if (this.zField.isFocused()) {
            this.zField.textboxKeyTyped(var1, var2);
            this.applyXYZ();
            return;
         }
      }
      super.keyTyped(var1, var2);
   }

   @Override
   public void updateScreen() {
      super.updateScreen();
      if (this.hexField != null) {
         this.hexField.updateCursorCounter();
         this.xField.updateCursorCounter();
         this.yField.updateCursorCounter();
         this.zField.updateCursorCounter();
      }
   }

   @Override
   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.id == BTN_RESET) {
         // reset the selected cosmetic's scale/colour/opacity/position; stay on the screen
         if (this.selected != null) {
            CosmeticController.resetCosmetic(this.selected);
            this.populate(); // refresh the hex + X/Y/Z fields (sliders read live)
         }
         return;
      }
      // Refresh (id 0): reload everyone's equipped cosmetics, then close.
      EsdeathClient.getInstance().getPlayerMapList().forEach(var0 -> {
         var0.getCosmetics().clear();
         var0.loadCosmetics();
      });
      Minecraft.getMinecraft().displayGuiScreen((GuiScreen) null);
      super.actionPerformed(var1);
   }

   @Override
   protected void actionPerformed(CosmeticButton var1) throws IOException {
      String var2 = strip(var1.displayString);
      if (this.lastButton == 1) {
         this.selected = var2;
         this.populate();
      } else {
         CosmeticController.toggle(var2);
         this.selected = var2;
         this.populate();
      }
      super.actionPerformed(var1);
   }

   @Override
   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      this.rebuildView();
      this.scroll = Math.min(this.scroll, this.maxScroll());
      this.layoutGrid();

      FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

      // breadcrumb categories (active gets "v", others ">")
      for (int i = 0; i < CATS.length; i++) {
         int cy = this.catY + i * this.catH;
         boolean active = i == this.activeCat;
         String arrow = active ? " §7v" : " §7>";
         fr.drawStringWithShadow((active ? "§b§l" : "§7") + CATS[i] + arrow, this.catX, cy, -1);
      }

      super.drawScreen(var1, var2, var3); // visible grid buttons + refresh

      this.drawScrollbar();

      if (this.view.isEmpty()) {
         this.drawCenteredString(fr, "§7No cosmetics in this category.", this.gridX + this.gridW / 2, this.viewTop + 20, -1);
      }

      // edit panel
      Gui.drawRect(0, this.editTop - 2, this.width, this.height, 0xC0000000);
      Gui.drawRect(0, this.editTop - 2, this.width, this.editTop - 1, 0xFF555555);

      if (this.selected != null) {
         fr.drawStringWithShadow("§eEdit: §f" + this.selected, MARGIN, this.editTop + 1, -1);
         fr.drawStringWithShadow("§7Color:", MARGIN, this.rowColor + 3, -1);
         this.hexField.drawTextBox();
         this.drawBlendSwitch(fr);
         this.drawAlphaSlider(fr);
         this.drawScaleSlider(fr);
         this.drawStyleRow(fr);
         // right column: position (X/Y/Z) then extra options
         fr.drawStringWithShadow("§7Pos:", this.rightX, this.fieldRowY + 3, -1);
         this.xField.drawTextBox();
         this.yField.drawTextBox();
         this.zField.drawTextBox();
         this.drawExtraOptions(fr);
      } else {
         fr.drawStringWithShadow("§7Left-click a cosmetic to equip + edit, right-click to edit only", MARGIN, this.editTop + 24, -1);
      }
   }

   private void drawScrollbar() {
      int viewH = this.viewBottom - this.viewTop;
      Gui.drawRect(this.barX, this.viewTop, this.barX + this.barW, this.viewBottom, 0x60000000);
      int max = this.maxScroll();
      if (max <= 0) {
         Gui.drawRect(this.barX, this.viewTop, this.barX + this.barW, this.viewBottom, 0x40FFFFFF);
         return;
      }
      int rows = (this.view.size() + COLS - 1) / COLS;
      int content = rows * (BTN_H + ROW_GAP);
      int thumbH = Math.max(20, (int) ((float) viewH * viewH / content));
      int thumbY = this.viewTop + (int) ((float) this.scroll / max * (viewH - thumbH));
      Gui.drawRect(this.barX, thumbY, this.barX + this.barW, thumbY + thumbH, 0xFFAAAAAA);
   }

   private void cycleOption(CosmeticOptions.Opt o, boolean backward) {
      String cur = CosmeticController.getOption(this.selected, o.key, o.values[0]);
      int idx = 0;
      for (int i = 0; i < o.values.length; i++) {
         if (o.values[i].equalsIgnoreCase(cur)) {
            idx = i;
            break;
         }
      }
      int n = o.values.length;
      int next = backward ? (idx - 1 + n) % n : (idx + 1) % n;
      CosmeticController.setOption(this.selected, o.key, o.values[next]);
   }

   private String optLabel(CosmeticOptions.Opt o) {
      String cur = CosmeticController.getOption(this.selected, o.key, o.values[0]);
      for (int j = 0; j < o.values.length; j++) {
         if (o.values[j].equalsIgnoreCase(cur)) {
            return o.labels[j];
         }
      }
      return o.labels[0];
   }

   // "Style:" row on the left column = the cosmetic's first OAM variant option, or static "Default".
   private void drawStyleRow(FontRenderer fr) {
      fr.drawStringWithShadow("§7Style:", MARGIN, this.rowStyle + 2, -1);
      List<CosmeticOptions.Opt> opts = CosmeticOptions.get(this.selected);
      if (opts.isEmpty()) {
         fr.drawStringWithShadow("§fDefault", this.sliderX, this.rowStyle + 2, -1);
         return;
      }
      CosmeticOptions.Opt o = opts.get(0);
      Gui.drawRect(this.sliderX, this.rowStyle, this.sliderX + this.sliderW, this.rowStyle + 12, 0xFF333333);
      fr.drawStringWithShadow("§f" + this.optLabel(o), this.sliderX + 3, this.rowStyle + 2, -1);
   }

   private void drawExtraOptions(FontRenderer fr) {
      List<CosmeticOptions.Opt> opts = CosmeticOptions.get(this.selected);
      for (int i = 1; i < opts.size(); i++) {
         CosmeticOptions.Opt o = opts.get(i);
         int oy = this.optStartY + (i - 1) * 14;
         Gui.drawRect(this.rightX, oy, this.rightX + 150, oy + 12, 0xFF333333);
         fr.drawStringWithShadow("§7" + o.label + ": §f" + this.optLabel(o), this.rightX + 3, oy + 2, -1);
      }
   }

   // colour blend switch next to the hex field — cycles Multiply / Overlay (like the Style switch)
   private void drawBlendSwitch(FontRenderer fr) {
      boolean overlay = CosmeticController.isOverlay(this.selected);
      Gui.drawRect(this.blendX, this.rowColor, this.blendX + this.blendW, this.rowColor + 14, 0xFF333333);
      this.drawCenteredString(fr, overlay ? "§bOverlay" : "§fMultiply", this.blendX + this.blendW / 2, this.rowColor + 3, -1);
   }

   private void drawScaleSlider(FontRenderer fr) {
      fr.drawStringWithShadow("§7Scale:", MARGIN, this.rowScale + 2, -1);
      float scale = CosmeticController.getScale(this.selected);
      float frac = (scale - CosmeticController.SCALE_MIN) / (CosmeticController.SCALE_MAX - CosmeticController.SCALE_MIN);
      frac = Math.max(0.0F, Math.min(1.0F, frac));
      Gui.drawRect(this.sliderX, this.rowScale, this.sliderX + this.sliderW, this.rowScale + 12, 0x80000000);
      int knobX = this.sliderX + (int) (frac * (this.sliderW - 6));
      Gui.drawRect(knobX, this.rowScale, knobX + 6, this.rowScale + 12, 0xFFAAAAAA);
      fr.drawStringWithShadow(String.format("§f%.2fx", scale), this.sliderX + this.sliderW + 6, this.rowScale + 2, -1);
   }

   private void drawAlphaSlider(FontRenderer fr) {
      fr.drawStringWithShadow("§7Opacity:", MARGIN, this.rowOpacity + 2, -1);
      float alpha = CosmeticController.getAlpha(this.selected);
      float frac = (alpha - CosmeticController.ALPHA_MIN) / (1.0F - CosmeticController.ALPHA_MIN);
      frac = Math.max(0.0F, Math.min(1.0F, frac));
      Gui.drawRect(this.sliderX, this.rowOpacity, this.sliderX + this.sliderW, this.rowOpacity + 12, 0x80000000);
      int knobX = this.sliderX + (int) (frac * (this.sliderW - 6));
      Gui.drawRect(knobX, this.rowOpacity, knobX + 6, this.rowOpacity + 12, 0xFFAAAAAA);
      fr.drawStringWithShadow(String.format("§f%d%%", (int) (alpha * 100.0F)), this.sliderX + this.sliderW + 6, this.rowOpacity + 2, -1);
   }
}
