package me.txb1.forge.gui.art;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.txb1.EsdeathClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

// Background gallery (opened from the main menu's ART button). Modern layout: category pills
// (Library / Favourites / Wallhaven), an integrated search box, a Random-favourite button, a
// scissor-clipped scrollable thumbnail grid with a draggable scrollbar, and a right-click context
// menu (Rename / Delete / Favourite). Wallhaven category searches wallhaven.cc and lets you download
// + apply a wallpaper in one click. Clicking a local tile sets it as the menu background.
public class EsdeathArtGui extends GuiScreen {
   private static final int COLS = 4;
   private static final String[] CATS = {"Library", "Favourites", "Wallhaven"};
   private static final int LIBRARY = 0, FAVOURITES = 1, WALLHAVEN = 2;

   private final GuiScreen parent;
   private int boxX, boxY, boxRight, boxBottom;
   private int gridX, gridTop, viewBottom, cellW, cellH, gapX, gapY;
   private int barX, barW;
   private int scroll;
   private boolean barDragging;

   private int cat = LIBRARY;
   private GuiTextField searchField;
   private String status = "";

   // category pill hitboxes (recomputed each frame)
   private final int[] pillX = new int[CATS.length];
   private final int[] pillW = new int[CATS.length];
   private int pillY, pillH;
   private int randX, randW, randY, randH;

   // Wallhaven paging state
   private final List<WallhavenClient.Result> whResults = new ArrayList<WallhavenClient.Result>();
   private String whQuery = "";
   private int whPage;
   private boolean whLoading;
   private boolean whMore = true;

   // right-click context menu
   private Tile ctxTile;
   private int ctxX, ctxY;

   // rename modal
   private Tile renameTile;
   private GuiTextField renameField;

   private static final class Tile {
      final int x, y, w, h;
      final BackgroundManager.Entry entry;    // local entry (null for a wallhaven tile)
      final WallhavenClient.Result wh;         // wallhaven result (null for a local tile)
      Tile(int x, int y, int w, int h, BackgroundManager.Entry e, WallhavenClient.Result wh) {
         this.x = x; this.y = y; this.w = w; this.h = h; this.entry = e; this.wh = wh;
      }
      boolean local() { return entry != null; }
      boolean hit(int mx, int my) { return mx >= x && my >= y && mx < x + w && my < y + h; }
   }

   private final List<Tile> tiles = new ArrayList<Tile>();

   public EsdeathArtGui(GuiScreen parent) {
      this.parent = parent;
   }

   @Override
   public void initGui() {
      Keyboard.enableRepeatEvents(true);
      layout();
      this.searchField = new GuiTextField(0, this.fontRendererObj, 0, 0, 10, 14);
      this.searchField.setMaxStringLength(64);
      this.searchField.setEnableBackgroundDrawing(false); // we draw our own single box (no doubled border)
      placeSearchField();
   }

   @Override
   public void onGuiClosed() {
      Keyboard.enableRepeatEvents(false);
   }

   private void layout() {
      this.boxX = this.width / 10;
      this.boxY = this.height / 10;
      this.boxRight = this.width - this.width / 10;
      this.boxBottom = this.height - this.height / 10;

      this.barW = 5;
      this.barX = this.boxRight - 10 - this.barW;
      int pad = 14;
      this.gapX = 10;
      this.gapY = 10;
      this.gridX = this.boxX + pad;
      this.gridTop = this.boxY + 56; // title + pill/search row
      this.viewBottom = this.boxBottom - 18;
      this.cellW = (this.boxRight - this.boxX - pad * 2 - this.gapX * (COLS - 1) - (this.barW + 10)) / COLS;
      this.cellH = 64;

      this.pillY = this.boxY + 30;
      this.pillH = 16;

      buildTiles();
      this.scroll = Math.max(0, Math.min(maxScroll(), this.scroll));
   }

   private void placeSearchField() {
      if (this.searchField == null) {
         return;
      }
      // search box on the right of the header; Random banner to its left
      int sw = 150;
      int sx = this.boxRight - sw - 12;
      this.searchSX = sx; this.searchSW = sw;
      this.searchTop = this.boxY + 30; this.searchBot = this.boxY + 44;
      this.searchField.xPosition = sx + 7;
      this.searchField.yPosition = this.searchTop + 3;
      this.searchField.width = sw - 14;
      this.searchField.height = 10;
      // Random rendered as a dark-slab banner (matches the import tile) to the left of the search box
      this.randW = 96; this.randH = 14;
      this.randX = sx - this.randW - 10; this.randY = this.boxY + 30;
   }

   private int searchSX, searchSW, searchTop, searchBot;

   private List<BackgroundManager.Entry> localSource() {
      return this.cat == FAVOURITES ? BackgroundManager.get().favouriteEntries() : BackgroundManager.get().list();
   }

   private void buildTiles() {
      this.tiles.clear();
      String q = this.searchField == null ? "" : this.searchField.getText().toLowerCase().trim();
      int i = 0;
      if (this.cat == WALLHAVEN) {
         for (WallhavenClient.Result r : this.whResults) {
            addTile(i++, null, r);
         }
      } else {
         for (BackgroundManager.Entry e : localSource()) {
            if (q.isEmpty() || e.id.toLowerCase().contains(q)) {
               addTile(i++, e, null);
            }
         }
         // the "+" import tile only in Library
         if (this.cat == LIBRARY) {
            addTile(i++, null, null);
         }
      }
   }

   private void addTile(int i, BackgroundManager.Entry e, WallhavenClient.Result wh) {
      int col = i % COLS, row = i / COLS;
      int x = this.gridX + col * (this.cellW + this.gapX);
      int y = this.gridTop + row * (this.cellH + this.gapY) - this.scroll;
      this.tiles.add(new Tile(x, y, this.cellW, this.cellH, e, wh));
   }

   private int rowsCount() {
      int n = this.tiles.size();
      return (n + COLS - 1) / COLS;
   }

   private int maxScroll() {
      int content = rowsCount() * (this.cellH + this.gapY);
      return Math.max(0, content - (this.viewBottom - this.gridTop));
   }

   private boolean visible(Tile t) {
      return t.y + t.h > this.gridTop && t.y < this.viewBottom;
   }

   // ---------- rendering ----------

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      layout();
      placeSearchField();

      int accent = EsdeathClient.getInstance().rainbow(800);
      // panel
      Gui.drawRect(this.boxX, this.boxY, this.boxRight, this.boxBottom, new Color(8, 8, 12, 220).getRGB());
      Gui.drawRect(this.boxX - 2, this.boxY - 2, this.boxRight + 2, this.boxY, accent);
      Gui.drawRect(this.boxX - 2, this.boxBottom, this.boxRight + 2, this.boxBottom + 2, accent);
      Gui.drawRect(this.boxX - 2, this.boxY, this.boxX, this.boxBottom, accent);
      Gui.drawRect(this.boxRight, this.boxY, this.boxRight + 2, this.boxBottom, accent);
      this.fontRendererObj.drawStringWithShadow("§lArt Gallery", this.boxX + 12, this.boxY + 10, accent);

      drawPills(mouseX, mouseY, accent);
      drawHeaderControls(mouseX, mouseY, accent);

      // grid (scissor-clipped to the viewport)
      ScaledResolution sr = new ScaledResolution(this.mc);
      int sf = sr.getScaleFactor();
      GL11.glEnable(GL11.GL_SCISSOR_TEST);
      GL11.glScissor(this.boxX * sf, (this.height - this.viewBottom) * sf,
         (this.boxRight - this.boxX) * sf, (this.viewBottom - this.gridTop) * sf);

      String selected = BackgroundManager.get().getSelected();
      for (Tile t : this.tiles) {
         if (!visible(t)) {
            continue;
         }
         drawTile(t, mouseX, mouseY, selected, accent);
      }

      GL11.glDisable(GL11.GL_SCISSOR_TEST);

      drawScrollbar(accent);

      // footer hint
      String hint = this.cat == WALLHAVEN
         ? "§7Type a search + Enter · click a result to download & apply"
         : "§7Left-click apply · right-click for options";
      this.fontRendererObj.drawStringWithShadow(hint, this.boxX + 12, this.boxBottom - 12, -1);
      if (!this.status.isEmpty()) {
         this.fontRendererObj.drawStringWithShadow(this.status, this.boxX + 12 + this.fontRendererObj.getStringWidth(hint.replaceAll("§.", "")) + 16, this.boxBottom - 12, -1);
      }
      if (this.cat == WALLHAVEN && this.whLoading) {
         this.drawCenteredString(this.fontRendererObj, "§7loading…", (this.boxX + this.boxRight) / 2, this.viewBottom - 14, -1);
      }
      if (this.tiles.isEmpty() && this.cat != WALLHAVEN) {
         this.drawCenteredString(this.fontRendererObj, "§7Nothing here yet.", (this.boxX + this.boxRight) / 2, this.gridTop + 20, -1);
      }

      // overlays
      if (this.ctxTile != null) {
         drawContextMenu(mouseX, mouseY, accent);
      }
      if (this.renameTile != null) {
         drawRenameModal(accent);
      }
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   private void drawPills(int mouseX, int mouseY, int accent) {
      int x = this.boxX + 12;
      for (int i = 0; i < CATS.length; i++) {
         int w = this.fontRendererObj.getStringWidth(CATS[i]) + 16;
         boolean active = i == this.cat;
         boolean hov = mouseX >= x && mouseX <= x + w && mouseY >= this.pillY && mouseY <= this.pillY + this.pillH;
         Gui.drawRect(x, this.pillY, x + w, this.pillY + this.pillH, active ? accent : (hov ? 0x55FFFFFF : 0x33000000));
         this.fontRendererObj.drawStringWithShadow((active ? "§f" : "§7") + CATS[i], x + 8, this.pillY + 4, -1);
         this.pillX[i] = x; this.pillW[i] = w;
         x += w + 6;
      }
   }

   private void drawHeaderControls(int mouseX, int mouseY, int accent) {
      // Random — dark-slab banner (same look as the import tile)
      boolean rhov = mouseX >= this.randX && mouseX <= this.randX + this.randW && mouseY >= this.randY && mouseY <= this.randY + this.randH;
      Gui.drawRect(this.randX, this.randY, this.randX + this.randW, this.randY + this.randH, new Color(14, 14, 20, 235).getRGB());
      if (rhov) {
         Gui.drawRect(this.randX, this.randY, this.randX + this.randW, this.randY + this.randH, 0x33FFFFFF);
      }
      Gui.drawRect(this.randX, this.randY + this.randH, this.randX + this.randW, this.randY + this.randH + 1, accent);
      boolean on = BackgroundManager.get().isRandomMode();
      this.drawCenteredString(this.fontRendererObj, (on ? "§a✦ Random: ON" : "§d✦ Random: OFF"), this.randX + this.randW / 2, this.randY + 3, -1);

      // search box — single clean box (the field's own border is disabled)
      Gui.drawRect(this.searchSX, this.searchTop, this.searchSX + this.searchSW, this.searchBot, new Color(14, 14, 20, 235).getRGB());
      Gui.drawRect(this.searchSX, this.searchBot, this.searchSX + this.searchSW, this.searchBot + 1, accent);
      this.searchField.drawTextBox();
      if (this.searchField.getText().isEmpty()) {
         String ph = this.cat == WALLHAVEN ? "Search Wallhaven…" : "Filter…";
         this.fontRendererObj.drawString("§8" + ph, this.searchField.xPosition, this.searchField.yPosition, 0xFF808080);
      }
   }

   private void drawTile(Tile t, int mouseX, int mouseY, String selected, int accent) {
      boolean hov = t.hit(mouseX, mouseY) && mouseY >= this.gridTop && mouseY < this.viewBottom;
      if (!t.local() && t.wh == null) {
         // import tile
         Gui.drawRect(t.x, t.y, t.x + t.w, t.y + t.h, new Color(14, 14, 20, 235).getRGB());
         this.drawCenteredString(this.fontRendererObj, "§a+", t.x + t.w / 2, t.y + t.h / 2 - 8, 0xFFFFFF);
         this.drawCenteredString(this.fontRendererObj, "Import", t.x + t.w / 2, t.y + t.h - 12, 0xAAAAAA);
         if (hov) {
            Gui.drawRect(t.x, t.y, t.x + t.w, t.y + t.h, 0x33FFFFFF);
         }
         return;
      }

      ResourceLocation tex = t.local() ? BackgroundManager.get().texture(t.entry) : WallhavenClient.thumb(t.wh);
      if (tex != null) {
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         this.mc.getTextureManager().bindTexture(tex);
         Gui.drawModalRectWithCustomSizedTexture(t.x, t.y, 0.0F, 0.0F, t.w, t.h, (float) t.w, (float) t.h);
      } else {
         Gui.drawRect(t.x, t.y, t.x + t.w, t.y + t.h, new Color(24, 24, 30, 235).getRGB());
         this.drawCenteredString(this.fontRendererObj, "§7…", t.x + t.w / 2, t.y + t.h / 2 - 4, -1);
      }

      // label strip
      Gui.drawRect(t.x, t.y + t.h - 11, t.x + t.w, t.y + t.h, new Color(0, 0, 0, 190).getRGB());
      String name = t.local() ? t.entry.id : t.wh.resolution;
      if (name == null) {
         name = "";
      }
      if (name.length() > 18) {
         name = name.substring(0, 17) + "…";
      }
      this.drawCenteredString(this.fontRendererObj, name, t.x + t.w / 2, t.y + t.h - 10, 0xFFFFFF);

      // favourite star (local only)
      if (t.local() && BackgroundManager.get().isFavourite(t.entry.id)) {
         this.fontRendererObj.drawStringWithShadow("§e★", t.x + 3, t.y + 3, -1);
      }

      // selected outline (local only)
      if (t.local() && t.entry.id.equals(selected)) {
         Gui.drawRect(t.x - 1, t.y - 1, t.x + t.w + 1, t.y, accent);
         Gui.drawRect(t.x - 1, t.y + t.h, t.x + t.w + 1, t.y + t.h + 1, accent);
         Gui.drawRect(t.x - 1, t.y, t.x, t.y + t.h, accent);
         Gui.drawRect(t.x + t.w, t.y, t.x + t.w + 1, t.y + t.h, accent);
      }
      if (hov) {
         Gui.drawRect(t.x, t.y, t.x + t.w, t.y + t.h, 0x22FFFFFF);
      }
   }

   private void drawScrollbar(int accent) {
      int viewH = this.viewBottom - this.gridTop;
      Gui.drawRect(this.barX, this.gridTop, this.barX + this.barW, this.viewBottom, 0x60000000);
      int max = maxScroll();
      if (max <= 0) {
         return;
      }
      int content = rowsCount() * (this.cellH + this.gapY);
      int thumbH = Math.max(20, (int) ((float) viewH * viewH / content));
      int thumbY = this.gridTop + (int) ((float) this.scroll / max * (viewH - thumbH));
      Gui.drawRect(this.barX, thumbY, this.barX + this.barW, thumbY + thumbH, accent);
   }

   private void drawContextMenu(int mouseX, int mouseY, int accent) {
      String fav = BackgroundManager.get().isFavourite(this.ctxTile.entry.id) ? "Unfavourite" : "Favourite";
      String[] rows = this.ctxTile.entry.builtin ? new String[]{fav} : new String[]{"Rename", "Delete", fav};
      int w = 90, rh = 14;
      int x = Math.min(this.ctxX, this.boxRight - w - 2);
      int y = Math.min(this.ctxY, this.boxBottom - rows.length * rh - 2);
      Gui.drawRect(x - 1, y - 1, x + w + 1, y + rows.length * rh + 1, accent);
      Gui.drawRect(x, y, x + w, y + rows.length * rh, new Color(16, 16, 22, 245).getRGB());
      for (int i = 0; i < rows.length; i++) {
         int ry = y + i * rh;
         boolean hov = mouseX >= x && mouseX <= x + w && mouseY >= ry && mouseY < ry + rh;
         if (hov) {
            Gui.drawRect(x, ry, x + w, ry + rh, 0x33FFFFFF);
         }
         this.fontRendererObj.drawStringWithShadow((rows[i].equals("Delete") ? "§c" : "§f") + rows[i], x + 6, ry + 3, -1);
      }
      this.ctxMenuX = x; this.ctxMenuY = y; this.ctxMenuW = w; this.ctxRowH = rh; this.ctxRows = rows;
   }

   private int ctxMenuX, ctxMenuY, ctxMenuW, ctxRowH;
   private String[] ctxRows = new String[0];

   private void drawRenameModal(int accent) {
      int w = 220, h = 70;
      int x = (this.width - w) / 2, y = (this.height - h) / 2;
      Gui.drawRect(x - 1, y - 1, x + w + 1, y + h + 1, accent);
      Gui.drawRect(x, y, x + w, y + h, new Color(16, 16, 22, 250).getRGB());
      this.fontRendererObj.drawStringWithShadow("Rename", x + 8, y + 8, -1);
      this.renameField.drawTextBox();
      this.fontRendererObj.drawStringWithShadow("§7Enter = save · Esc = cancel", x + 8, y + h - 14, -1);
   }

   // ---------- input ----------

   @Override
   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      int wheel = Mouse.getEventDWheel();
      if (wheel != 0) {
         this.scroll = Math.max(0, Math.min(maxScroll(), this.scroll + (wheel > 0 ? -(this.cellH + this.gapY) : (this.cellH + this.gapY))));
         // Wallhaven: auto-load the next page when near the bottom
         if (this.cat == WALLHAVEN && wheel < 0 && this.scroll >= maxScroll() - (this.cellH + this.gapY) && this.whMore && !this.whLoading) {
            loadWallhaven(this.whQuery, this.whPage + 1, true);
         }
      }
   }

   private void setScrollFromBar(int mouseY) {
      int max = maxScroll();
      if (max <= 0) {
         return;
      }
      float frac = (float) (mouseY - this.gridTop) / (float) (this.viewBottom - this.gridTop);
      this.scroll = (int) (Math.max(0.0F, Math.min(1.0F, frac)) * max);
   }

   @Override
   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      // rename modal swallows everything
      if (this.renameTile != null) {
         this.renameField.mouseClicked(mouseX, mouseY, mouseButton);
         return;
      }
      // context menu: click a row or dismiss
      if (this.ctxTile != null) {
         if (mouseX >= this.ctxMenuX && mouseX <= this.ctxMenuX + this.ctxMenuW
               && mouseY >= this.ctxMenuY && mouseY < this.ctxMenuY + this.ctxRows.length * this.ctxRowH) {
            int row = (mouseY - this.ctxMenuY) / this.ctxRowH;
            handleContext(this.ctxRows[row]);
         }
         this.ctxTile = null;
         return;
      }

      // category pills
      for (int i = 0; i < CATS.length; i++) {
         if (mouseX >= this.pillX[i] && mouseX <= this.pillX[i] + this.pillW[i] && mouseY >= this.pillY && mouseY <= this.pillY + this.pillH) {
            switchCat(i);
            return;
         }
      }
      // Random banner: toggle the "re-roll on each home screen" mode (and roll one now)
      if (mouseX >= this.randX && mouseX <= this.randX + this.randW && mouseY >= this.randY && mouseY <= this.randY + this.randH) {
         boolean on = !BackgroundManager.get().isRandomMode();
         BackgroundManager.get().setRandomMode(on);
         if (on) {
            String id = BackgroundManager.get().selectRandomFavourite();
            this.status = id == null ? "§eRandom on — but no favourites yet." : "§aRandom on · " + id;
         } else {
            this.status = "§7Random off.";
         }
         return;
      }
      // search box focus
      this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
      if (mouseX >= this.searchSX && mouseX <= this.searchSX + this.searchSW && mouseY >= this.searchTop && mouseY <= this.searchBot) {
         return;
      }
      // scrollbar
      if (mouseX >= this.barX && mouseX <= this.barX + this.barW && mouseY >= this.gridTop && mouseY <= this.viewBottom) {
         this.barDragging = true;
         setScrollFromBar(mouseY);
         return;
      }
      // tiles
      for (Tile t : this.tiles) {
         if (!visible(t) || !t.hit(mouseX, mouseY) || mouseY < this.gridTop || mouseY >= this.viewBottom) {
            continue;
         }
         onTileClick(t, mouseButton, mouseX, mouseY);
         return;
      }
      super.mouseClicked(mouseX, mouseY, mouseButton);
   }

   private void onTileClick(Tile t, int mouseButton, int mouseX, int mouseY) {
      if (!t.local() && t.wh == null) { // import tile
         pickCustom();
         return;
      }
      if (t.local()) {
         if (mouseButton == 1) { // right-click -> context menu
            this.ctxTile = t;
            this.ctxX = mouseX;
            this.ctxY = mouseY;
         } else {
            BackgroundManager.get().setSelected(t.entry.id);
         }
         return;
      }
      // wallhaven result -> download + apply
      downloadWallhaven(t.wh);
   }

   private void handleContext(String action) {
      BackgroundManager.Entry e = this.ctxTile.entry;
      if (action.equals("Delete")) {
         if (BackgroundManager.get().delete(e)) {
            this.status = "§7Deleted " + e.id;
         }
      } else if (action.equals("Rename")) {
         openRename(this.ctxTile);
      } else { // Favourite / Unfavourite
         BackgroundManager.get().toggleFavourite(e.id);
      }
   }

   private void openRename(Tile t) {
      this.renameTile = t;
      int w = 220;
      this.renameField = new GuiTextField(1, this.fontRendererObj, (this.width - w) / 2 + 8, (this.height) / 2 - 6, w - 16, 16);
      this.renameField.setMaxStringLength(64);
      String n = t.entry.id;
      int dot = n.lastIndexOf('.');
      this.renameField.setText(dot < 0 ? n : n.substring(0, dot));
      this.renameField.setFocused(true);
   }

   private void switchCat(int i) {
      this.cat = i;
      this.scroll = 0;
      this.searchField.setText("");
      this.status = "";
      if (i == WALLHAVEN && this.whResults.isEmpty() && !this.whLoading) {
         loadWallhaven("", 1, false); // initial toplist
      }
   }

   @Override
   protected void mouseClickMove(int mx, int my, int btn, long time) {
      if (this.barDragging) {
         setScrollFromBar(my);
      }
   }

   @Override
   protected void mouseReleased(int mx, int my, int btn) {
      this.barDragging = false;
   }

   @Override
   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if (this.renameTile != null) {
         if (keyCode == Keyboard.KEY_ESCAPE) {
            this.renameTile = null;
            return;
         }
         if (keyCode == Keyboard.KEY_RETURN) {
            String newId = BackgroundManager.get().rename(this.renameTile.entry, this.renameField.getText());
            this.status = newId == null ? "§cRename failed." : "§aRenamed to " + newId;
            this.renameTile = null;
            return;
         }
         this.renameField.textboxKeyTyped(typedChar, keyCode);
         return;
      }
      if (keyCode == Keyboard.KEY_ESCAPE) {
         if (this.ctxTile != null) {
            this.ctxTile = null;
            return;
         }
         this.mc.displayGuiScreen(this.parent);
         return;
      }
      if (this.searchField.textboxKeyTyped(typedChar, keyCode)) {
         this.scroll = 0;
         if (this.cat != WALLHAVEN) {
            buildTiles(); // live filter for local categories
         }
         return;
      }
      if (keyCode == Keyboard.KEY_RETURN && this.cat == WALLHAVEN) {
         loadWallhaven(this.searchField.getText(), 1, false);
         return;
      }
      super.keyTyped(typedChar, keyCode);
   }

   @Override
   public void updateScreen() {
      this.searchField.updateCursorCounter();
      if (this.renameField != null) {
         this.renameField.updateCursorCounter();
      }
   }

   // ---------- wallhaven ----------

   private void loadWallhaven(final String query, final int page, final boolean append) {
      this.whLoading = true;
      this.whQuery = query == null ? "" : query;
      this.status = "§7Searching Wallhaven…";
      final String q = this.whQuery;
      new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               final List<WallhavenClient.Result> res = WallhavenClient.search(q, page);
               Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                  @Override
                  public void run() {
                     if (!append) {
                        whResults.clear();
                     }
                     whResults.addAll(res);
                     whPage = page;
                     whMore = !res.isEmpty();
                     whLoading = false;
                     status = whResults.isEmpty() ? "§7No results." : "§a" + whResults.size() + " results";
                     buildTiles();
                  }
               });
            } catch (final Throwable t) {
               Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                  @Override
                  public void run() {
                     whLoading = false;
                     status = "§cWallhaven error: " + t.getMessage();
                  }
               });
            }
         }
      }, "Wallhaven-Search").start();
   }

   private void downloadWallhaven(final WallhavenClient.Result r) {
      this.status = "§7Downloading…";
      new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               final byte[] bytes = WallhavenClient.downloadFull(r);
               Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                  @Override
                  public void run() {
                     try {
                        String id = BackgroundManager.get().addDownloaded(bytes, "wallhaven-" + r.id + ".jpg");
                        BackgroundManager.get().setSelected(id);
                        status = "§aApplied " + id;
                     } catch (Throwable t) {
                        status = "§cSave failed: " + t.getMessage();
                     }
                  }
               });
            } catch (final Throwable t) {
               Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                  @Override
                  public void run() {
                     status = "§cDownload failed: " + t.getMessage();
                  }
               });
            }
         }
      }, "Wallhaven-Download").start();
   }

   // ---------- local import ----------

   private void pickCustom() {
      new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               FileDialog fd = new FileDialog((Frame) null, "Select a background image", FileDialog.LOAD);
               fd.setFile("*.png;*.jpg;*.jpeg;*.gif");
               fd.setVisible(true);
               String dir = fd.getDirectory();
               String name = fd.getFile();
               if (dir == null || name == null) {
                  return;
               }
               final File sel = new File(dir, name);
               if (!sel.isFile()) {
                  return;
               }
               Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                  @Override
                  public void run() {
                     try {
                        String id = BackgroundManager.get().addCustom(sel);
                        BackgroundManager.get().setSelected(id);
                     } catch (Throwable ignored) {
                     }
                  }
               });
            } catch (Throwable ignored) {
            }
         }
      }, "EsdeathArt-Picker").start();
   }

   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }
}
