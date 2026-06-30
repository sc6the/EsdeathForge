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
   private static final String[] CATS = {"Esdeath", "OAM", "Labymod"};

   private GuiTextField hexField;
   private GuiTextField xField;
   private GuiTextField yField;
   private GuiTextField zField;
   private GuiTextField searchField;
   private boolean scaleDragging;
   private boolean alphaDragging;
   private boolean barDragging;
   private String selected;
   private int lastButton;
   private int colorSlot; // which rgb slot the hex field edits, for multi-colour LabyMod cosmetics

   private int activeCat = 1; // default to OAM (matches the largest set)
   private final List<CosmeticButton> all = new ArrayList<CosmeticButton>();
   private final List<CosmeticButton> view = new ArrayList<CosmeticButton>(); // active category
   private int scroll;

   // layout (computed in initGui)
   private int catY, catH;
   private int[] catTabX = new int[CATS.length];
   private int[] catTabW = new int[CATS.length];
   private int toolbarY, toolbarH;
   private int resetX, refreshX, toolBtnW, toolBtnH;
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

      FontRenderer frInit = Minecraft.getMinecraft().fontRendererObj;
      // Row 1: horizontal category tabs.
      this.catY = 8;
      this.catH = 12;
      int cx = MARGIN;
      for (int i = 0; i < CATS.length; i++) {
         int w = frInit.getStringWidth(CATS[i]) + 16;
         this.catTabX[i] = cx;
         this.catTabW[i] = w;
         cx += w + 4;
      }
      // Row 2: toolbar with search field (left) + themed Reset/Refresh (right).
      this.toolbarY = this.catY + this.catH + 6;
      this.toolbarH = 16;
      this.toolBtnW = 58;
      this.toolBtnH = this.toolbarH;
      this.refreshX = this.width - MARGIN - this.toolBtnW;
      this.resetX = this.refreshX - 6 - this.toolBtnW;
      this.barX = MARGIN;
      this.barW = 5;
      this.gridX = MARGIN + this.barW + 8;
      this.viewTop = this.toolbarY + this.toolbarH + 8;
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

      // make sure the offline LabyMod catalog is registered (if its index has loaded) before we build
      // the button list, so the Labymod category is populated on first open.
      me.txb1.extras.cosmetics.cosmetics.laby.LabyOfflineCatalog.ensureRegistered();
      this.buildButtons();

      FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
      this.hexField = new GuiTextField(1, fr, this.sliderX, this.rowColor, 58, 14);
      this.hexField.setMaxStringLength(7);
      this.xField = new GuiTextField(2, fr, this.rightX + 26, this.fieldRowY, 28, 14);
      this.xField.setMaxStringLength(8);
      this.yField = new GuiTextField(3, fr, this.rightX + 58, this.fieldRowY, 28, 14);
      this.yField.setMaxStringLength(8);
      this.zField = new GuiTextField(4, fr, this.rightX + 90, this.fieldRowY, 28, 14);
      this.zField.setMaxStringLength(8);

      // search field fills the toolbar between the categories and the Reset button
      int searchX = this.gridX;
      int searchW = this.resetX - 8 - searchX;
      this.searchField = new GuiTextField(5, fr, searchX, this.toolbarY, Math.max(60, searchW), this.toolbarH);
      this.searchField.setMaxStringLength(40);
      // Reset/Refresh are drawn + handled manually (themed), so no vanilla GuiButtons here.
      super.initGui();
   }

   // Themed toolbar button (dark panel + accent border on hover), matching the client's GUI style.
   private void drawThemeButton(int x, int y, int w, int h, String label, boolean hovered, int textColor) {
      Gui.drawRect(x, y, x + w, y + h, hovered ? 0xFF2A2A2A : 0xFF1A1A1A);
      int border = hovered ? EsdeathClient.getInstance().rainbow(400) : 0xFF555555;
      this.drawHorizontalLine(x, x + w - 1, y, border);
      this.drawHorizontalLine(x, x + w - 1, y + h - 1, border);
      this.drawVerticalLine(x, y, y + h - 1, border);
      this.drawVerticalLine(x + w - 1, y, y + h - 1, border);
      FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
      this.drawCenteredString(fr, label, x + w / 2, y + (h - 8) / 2, textColor);
   }

   // Number of cosmetic names the buttons were last built for; if the catalog grows (the LabyMod
   // index finishes loading while the menu is open), rebuild so the new entries show without reopening.
   private int builtCount = -1;

   private void buildButtons() {
      this.all.clear();
      this.cosmeticButtonList.clear();
      int id = 0;
      for (String name : CosmeticController.getCosmetics()) {
         CosmeticButton b = new CosmeticButton(id++, 0, 0, 10, BTN_H, name);
         this.cosmeticButtonList.add(b);
         this.all.add(b);
      }
      this.builtCount = CosmeticController.getCosmetics().size();
   }

   private void rebuildView() {
      if (CosmeticController.getCosmetics().size() != this.builtCount) {
         this.buildButtons();
      }
      this.view.clear();
      String cat = CATS[this.activeCat];
      String query = this.searchField == null ? "" : this.searchField.getText().trim().toLowerCase();
      for (CosmeticButton b : this.all) {
         String name = strip(b.displayString);
         boolean match = CosmeticController.categoryOf(name).equals(cat)
            && (query.isEmpty() || name.toLowerCase().contains(query));
         b.visible = match; // only active-category + search-matching buttons participate
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

   // The selected cosmetic's LabyMod Meta if it's a LabyMod cosmetic (else null).
   private me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.Meta selectedLabyMeta() {
      if (this.selected == null || !CosmeticController.isLabymod(this.selected)) {
         return null;
      }
      int id = me.txb1.extras.cosmetics.cosmetics.laby.LabyOfflineCatalog.idOf(this.selected);
      return id <= 0 ? null : me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.meta(id);
   }

   // Number of recolourable slots for the selected cosmetic (0 for non-LabyMod / single-colour).
   private int selectedColorSlots() {
      return me.txb1.extras.cosmetics.cosmetics.laby.CosmeticLabyGeometry.colorSlotCount(this.selectedLabyMeta());
   }

   // The selected LabyMod cosmetic's layer variants ("styles"), empty if none / geometry not loaded.
   private java.util.List<me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.Style> selectedStyles() {
      if (this.selected == null || !CosmeticController.isLabymod(this.selected)) {
         return java.util.Collections.emptyList();
      }
      int id = me.txb1.extras.cosmetics.cosmetics.laby.LabyOfflineCatalog.idOf(this.selected);
      return id <= 0 ? java.util.Collections.<me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.Style>emptyList()
         : me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.styles(id);
   }

   // Default style/texture UUID for the selected cosmetic (the "texture" entry of default_data).
   private String defaultStyleUuid(me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.Meta m) {
      if (m == null || m.options == null || m.defaultData == null) {
         return null;
      }
      for (int i = 0; i < m.options.length && i < m.defaultData.length; i++) {
         if ("texture".equalsIgnoreCase(m.options[i])) {
            return m.defaultData[i];
         }
      }
      return null;
   }

   private String currentStyleUuid(me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.Meta m) {
      return CosmeticController.getOption(this.selected, "labystyle", this.defaultStyleUuid(m));
   }

   private String currentStyleName(me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.Meta m,
                                   java.util.List<me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.Style> styles) {
      String cur = this.currentStyleUuid(m);
      if (cur != null) {
         for (me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.Style s : styles) {
            if (s.uuid.equalsIgnoreCase(cur)) {
               return s.name;
            }
         }
      }
      return styles.isEmpty() ? "Default" : styles.get(0).name;
   }

   private void cycleStyle(boolean backward) {
      me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.Meta m = this.selectedLabyMeta();
      java.util.List<me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.Style> styles = this.selectedStyles();
      if (m == null || styles.isEmpty()) {
         return;
      }
      String cur = this.currentStyleUuid(m);
      int idx = 0;
      for (int i = 0; i < styles.size(); i++) {
         if (styles.get(i).uuid.equalsIgnoreCase(cur)) {
            idx = i;
            break;
         }
      }
      int n = styles.size();
      int next = backward ? (idx - 1 + n) % n : (idx + 1) % n;
      CosmeticController.setOption(this.selected, "labystyle", styles.get(next).uuid);
   }

   // Current RRGGBB for a LabyMod colour slot (user override, else the cosmetic's default).
   private String slotHex(me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.Meta lm, int slot) {
      return CosmeticController.getOption(this.selected, "labycol" + slot,
         me.txb1.extras.cosmetics.cosmetics.laby.CosmeticLabyGeometry.slotDefaultHex(lm, slot));
   }

   private void populate() {
      if (this.selected != null) {
         me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.Meta lm = this.selectedLabyMeta();
         if (lm != null && me.txb1.extras.cosmetics.cosmetics.laby.CosmeticLabyGeometry.colorSlotCount(lm) > 0) {
            this.hexField.setText(this.slotHex(lm, this.colorSlot));
         } else {
            this.hexField.setText(CosmeticController.getColorHex(this.selected));
         }
         this.xField.setText(String.valueOf(CosmeticController.getOffsetX(this.selected)));
         this.yField.setText(String.valueOf(CosmeticController.getOffsetY(this.selected)));
         this.zField.setText(String.valueOf(CosmeticController.getOffsetZ(this.selected)));
      }
   }

   private void applyHex() {
      if (this.selected == null) {
         return;
      }
      me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.Meta lm = this.selectedLabyMeta();
      if (lm != null && me.txb1.extras.cosmetics.cosmetics.laby.CosmeticLabyGeometry.colorSlotCount(lm) > 0) {
         String h = this.hexField.getText().replace("#", "").trim();
         if (h.length() == 6) {
            CosmeticController.setOption(this.selected, "labycol" + this.colorSlot, h.toUpperCase());
         }
      } else {
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

      // horizontal category tabs
      for (int i = 0; i < CATS.length; i++) {
         if (this.inRect(var1, var2, this.catTabX[i], this.catY, this.catTabW[i], this.catH)) {
            if (this.activeCat != i) {
               this.activeCat = i;
               this.scroll = 0;
            }
            return;
         }
      }

      // toolbar: search field + themed Reset/Refresh
      this.searchField.mouseClicked(var1, var2, var3);
      if (this.inRect(var1, var2, this.resetX, this.toolbarY, this.toolBtnW, this.toolBtnH)) {
         this.doReset();
         return;
      }
      if (this.inRect(var1, var2, this.refreshX, this.toolbarY, this.toolBtnW, this.toolBtnH)) {
         this.doRefresh();
         return;
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
         // LabyMod colour swatches: clicking one selects it as the active colour slot.
         int slots = this.selectedColorSlots();
         boolean swatchHit = false;
         if (slots > 0) {
            for (int i = 0; i < slots; i++) {
               int sx = this.blendX + i * (SWATCH_W + SWATCH_GAP);
               if (this.inRect(var1, var2, sx, this.rowColor, SWATCH_W, 14)) {
                  this.colorSlot = i;
                  this.populate();
                  swatchHit = true;
                  break;
               }
            }
         }
         if (swatchHit) {
            // handled
         } else if (this.inRect(var1, var2, this.sliderX, this.rowScale, this.sliderW, 12)) {
            this.scaleDragging = true;
            this.setScaleFromMouse(var1);
         } else if (this.inRect(var1, var2, this.sliderX, this.rowOpacity, this.sliderW, 12)) {
            this.alphaDragging = true;
            this.setAlphaFromMouse(var1);
         } else if (slots == 0 && this.inRect(var1, var2, this.blendX, this.rowColor, this.blendW, 14)) {
            CosmeticController.setOverlay(this.selected, !CosmeticController.isOverlay(this.selected));
         } else if (!this.selectedStyles().isEmpty() && this.inRect(var1, var2, this.sliderX, this.rowStyle, this.sliderW, 12)) {
            // LabyMod cosmetic with multiple layer variants -> the Style row cycles them
            this.cycleStyle(var3 == 1);
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
      // search field is always available (independent of a selected cosmetic)
      if (this.searchField != null && this.searchField.isFocused()) {
         this.searchField.textboxKeyTyped(var1, var2);
         this.scroll = 0;
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
      if (this.searchField != null) {
         this.searchField.updateCursorCounter();
      }
   }

   // Reset the selected cosmetic's scale/colour/opacity/position back to defaults; stay on screen.
   private void doReset() {
      if (this.selected != null) {
         CosmeticController.resetCosmetic(this.selected);
         this.populate(); // refresh the hex + X/Y/Z fields (sliders read live)
      }
   }

   // Reload everyone's equipped cosmetics, then close.
   private void doRefresh() {
      EsdeathClient.getInstance().getPlayerMapList().forEach(var0 -> {
         var0.getCosmetics().clear();
         var0.loadCosmetics();
      });
      Minecraft.getMinecraft().displayGuiScreen((GuiScreen) null);
   }

   @Override
   protected void actionPerformed(CosmeticButton var1) throws IOException {
      String var2 = strip(var1.displayString);
      this.colorSlot = 0; // reset which colour slot is being edited when picking a new cosmetic
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

      // horizontal category tabs
      for (int i = 0; i < CATS.length; i++) {
         boolean active = i == this.activeCat;
         int tx = this.catTabX[i];
         int tw = this.catTabW[i];
         boolean hov = this.inRect(var1, var2, tx, this.catY, tw, this.catH);
         Gui.drawRect(tx, this.catY, tx + tw, this.catY + this.catH, active ? 0xFF2A2A2A : (hov ? 0xFF222222 : 0xFF141414));
         if (active) {
            this.drawHorizontalLine(tx, tx + tw - 1, this.catY + this.catH - 1, EsdeathClient.getInstance().rainbow(400));
         }
         this.drawCenteredString(fr, (active ? "§f" : "§7") + CATS[i], tx + tw / 2, this.catY + 2, -1);
      }

      // toolbar: search field + themed Reset/Refresh
      this.searchField.drawTextBox();
      if (this.searchField.getText().isEmpty() && !this.searchField.isFocused()) {
         fr.drawString("§8Search…", this.searchField.xPosition + 4, this.toolbarY + 4, -1);
      }
      this.drawThemeButton(this.resetX, this.toolbarY, this.toolBtnW, this.toolBtnH, "§cReset",
         this.inRect(var1, var2, this.resetX, this.toolbarY, this.toolBtnW, this.toolBtnH), -1);
      this.drawThemeButton(this.refreshX, this.toolbarY, this.toolBtnW, this.toolBtnH, "§aRefresh",
         this.inRect(var1, var2, this.refreshX, this.toolbarY, this.toolBtnW, this.toolBtnH), -1);

      super.drawScreen(var1, var2, var3); // visible grid buttons

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
         // LabyMod cosmetics with several rgb options get a clickable swatch per colour slot (the hex
         // field edits the selected swatch); others get the Multiply/Overlay blend switch.
         int slots = this.selectedColorSlots();
         if (slots > 0) {
            this.drawColorSwatches(fr, this.selectedLabyMeta(), slots);
         } else {
            this.drawBlendSwitch(fr);
         }
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
      // LabyMod cosmetics: the Style row cycles the cosmetic's layer variants (e.g. katana hip/back/dual)
      java.util.List<me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.Style> styles = this.selectedStyles();
      if (!styles.isEmpty()) {
         Gui.drawRect(this.sliderX, this.rowStyle, this.sliderX + this.sliderW, this.rowStyle + 12, 0xFF333333);
         fr.drawStringWithShadow("§f" + this.currentStyleName(this.selectedLabyMeta(), styles), this.sliderX + 3, this.rowStyle + 2, -1);
         return;
      }
      // a selected LabyMod cosmetic whose geometry is still loading -> styles not known yet
      if (this.selectedLabyMeta() != null && me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.geometryEngine(
            me.txb1.extras.cosmetics.cosmetics.laby.LabyOfflineCatalog.idOf(this.selected)) == null) {
         fr.drawStringWithShadow("§8loading…", this.sliderX, this.rowStyle + 2, -1);
         return;
      }
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

   private static final int SWATCH_W = 14;
   private static final int SWATCH_GAP = 3;

   // One clickable colour swatch per rgb slot of a multi-colour LabyMod cosmetic. The active slot
   // (edited by the hex field) gets an accent border. Click a swatch (handled in mouseClicked) to
   // make it the active slot.
   private void drawColorSwatches(FontRenderer fr, me.txb1.extras.cosmetics.laby.geo.LabyCosmetics.Meta lm, int slots) {
      for (int i = 0; i < slots; i++) {
         int sx = this.blendX + i * (SWATCH_W + SWATCH_GAP);
         int rgb = 0xFF000000 | (parseHexColor(this.slotHex(lm, i)) & 0xFFFFFF);
         Gui.drawRect(sx, this.rowColor, sx + SWATCH_W, this.rowColor + 14, rgb);
         int border = (i == this.colorSlot) ? EsdeathClient.getInstance().rainbow(400) : 0xFF000000;
         this.drawHorizontalLine(sx, sx + SWATCH_W - 1, this.rowColor, border);
         this.drawHorizontalLine(sx, sx + SWATCH_W - 1, this.rowColor + 13, border);
         this.drawVerticalLine(sx, this.rowColor, this.rowColor + 13, border);
         this.drawVerticalLine(sx + SWATCH_W - 1, this.rowColor, this.rowColor + 13, border);
      }
   }

   private static int parseHexColor(String hex) {
      try {
         return Integer.parseInt(hex.replace("#", "").trim(), 16);
      } catch (Exception e) {
         return 0xFFFFFF;
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
