package me.txb1.forge.gui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;
import me.txb1.EsdeathClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.ResourcePackRepository.Entry;
import org.lwjgl.input.Mouse;

// Esdeath resource-pack organizer (built-in, not a module). Replaces the vanilla GuiScreenResourcePacks
// via GuiOpenEvent. Improvements over vanilla:
//   - recursive scan: folders that aren't packs themselves are descended into to find nested packs
//   - search bar filtering the available column by name
//   - pinning (PinnedPacks): pinned packs float to the top of the available column
//   - a static scan cache keyed on the resourcepacks dir signature, so re-opening is instant
//   - skips the (expensive) resource refresh on Done when the selection is unchanged
// All pack loading/applying goes through the vanilla ResourcePackRepository; the only reflection is
// the package-private Entry(File) constructor for nested packs the repository never scans itself.
public class EsdeathResourcePackGui extends GuiScreen {
   private static final int B_DONE = 200, B_REFRESH = 201, B_FOLDER = 202;
   private static final int ROW_H = 36;
   private static final int MAX_DEPTH = 3;
   private static final int BAR_W = 6; // draggable scrollbar width

   // which column's scrollbar is being dragged: 0 none, 1 available (left), 2 selected (right)
   private int draggingBar;

   private final GuiScreen parent;
   private ResourcePackRepository repo;
   private GuiTextField search;

   // available = packs not currently applied; selected = applied (top of column = highest priority).
   private final List<Entry> available = new ArrayList<Entry>();
   private final List<Entry> selected = new ArrayList<Entry>();
   private final List<Entry> availableView = new ArrayList<Entry>();
   private List<String> originalSelection;

   private int leftX, rightX, colW, listTop, listBottom;
   private int availScroll, selScroll;

   // drag-reorder state for the selected (active) column
   private int selDragFrom = -1;
   private int selPressY;
   private int selDragY;
   private boolean selDragging;

   // cached recursive scan, reused while the resourcepacks dir signature is unchanged.
   private static List<Entry> cachedAll;
   private static String cacheSig;
   private static Constructor<?> entryCtor;

   // DISK cache of "this file is a valid pack" verdicts (absPath -> lastModified), persisted to
   // EsdeathClient/resourcepack_cache.txt. After a reboot the in-memory cache is empty, so the first
   // open would otherwise re-validate every zip (open + read pack.mcmeta) — this skips that for
   // files whose mtime is unchanged. The expensive pack.png/description load is deferred (lazy,
   // per visible row) so opening is fast regardless of pack count.
   private static final File CACHE_FILE = new File(Minecraft.getMinecraft().mcDataDir, "EsdeathClient/resourcepack_cache.txt");
   private static Map<String, Long> validCache;
   // entries whose updateResourcePack() (mcmeta + icon) has been loaded; cleared on each full rescan.
   private static final Set<Entry> loaded = new HashSet<Entry>();
   // file-based pack name per scanned Entry. Entry.getResourcePackName() NPEs before updateResourcePack
   // (it builds reResourcePack lazily), so for our deferred entries we use the file name (which is
   // exactly what getResourcePackName would return) for all sorting / filtering / comparison.
   private static final Map<Entry, String> entryNames = new java.util.HashMap<Entry, String>();

   public EsdeathResourcePackGui(GuiScreen parent) {
      this.parent = parent;
   }

   @Override
   public void initGui() {
      this.repo = this.mc.getResourcePackRepository();
      layout();

      this.search = new GuiTextField(0, this.fontRendererObj, this.leftX, 22, this.colW, 16);
      this.search.setMaxStringLength(64);

      // bottom bar: Refresh | Done (centered) | Open Folder, three evenly spaced buttons.
      this.buttonList.clear();
      int margin = 16, gap = 8, by = this.height - 26;
      int bw = (this.width - margin * 2 - gap * 2) / 3;
      this.buttonList.add(new GuiButton(B_REFRESH, margin, by, bw, 20, "Refresh"));
      this.buttonList.add(new GuiButton(B_DONE, margin + bw + gap, by, bw, 20, "Done"));
      this.buttonList.add(new GuiButton(B_FOLDER, margin + (bw + gap) * 2, by, bw, 20, "Open Folder"));

      loadEntries(false);
   }

   private void layout() {
      int margin = 16, gap = 16;
      this.colW = (this.width - margin * 2 - gap) / 2;
      this.leftX = margin;
      this.rightX = margin + this.colW + gap;
      this.listTop = 58;             // below title (8) + search box (22..38) + column header (47)
      this.listBottom = this.height - 34;
   }

   // x of a column's scrollbar track (just inside the column's right edge)
   private int barX(int columnX) {
      return columnX + this.colW - BAR_W;
   }

   private int colMax(int count) {
      return Math.max(0, count * ROW_H - (this.listBottom - this.listTop));
   }

   // draw a draggable scrollbar for a column; thumb size/position reflect the scroll.
   private void drawScrollbar(int columnX, int scroll, int count) {
      int bx = barX(columnX);
      int viewH = this.listBottom - this.listTop;
      Gui.drawRect(bx, this.listTop, bx + BAR_W, this.listBottom, 0x60000000);
      int max = colMax(count);
      if (max <= 0) {
         return; // nothing to scroll — no thumb
      }
      int content = count * ROW_H;
      int thumbH = Math.max(20, (int) ((float) viewH * viewH / content));
      int thumbY = this.listTop + (int) ((float) scroll / max * (viewH - thumbH));
      Gui.drawRect(bx, thumbY, bx + BAR_W, thumbY + thumbH, EsdeathClient.getInstance().rainbow(800));
   }

   // map a drag on a column's scrollbar to a scroll offset (thumb-center follows the cursor)
   private void setScrollFromBar(int which, int mouseY) {
      int count = which == 1 ? this.availableView.size() : this.selected.size();
      int max = colMax(count);
      if (max <= 0) {
         return;
      }
      int viewH = this.listBottom - this.listTop;
      int content = count * ROW_H;
      int thumbH = Math.max(20, (int) ((float) viewH * viewH / content));
      float frac = (float) (mouseY - this.listTop - thumbH / 2) / (float) Math.max(1, viewH - thumbH);
      frac = Math.max(0.0F, Math.min(1.0F, frac));
      int s = (int) (frac * max);
      if (which == 1) {
         this.availScroll = s;
      } else {
         this.selScroll = s;
      }
   }

   // (re)build the selected/available lists. forceRescan ignores the scan cache (used by Refresh).
   private void loadEntries(boolean forceRescan) {
      this.selected.clear();
      this.available.clear();
      // selected, top = highest priority (repositoryEntries stores it the other way round).
      List<Entry> sel = new ArrayList<Entry>(this.repo.getRepositoryEntries());
      Collections.reverse(sel);
      this.selected.addAll(sel);
      this.originalSelection = names(this.selected);

      List<Entry> all = scanAll(forceRescan);
      for (Entry e : all) {
         if (!containsByName(this.selected, e)) {
            this.available.add(e);
         }
      }
      sortAvailable();
      rebuildView();
      clampScroll();
   }

   // pinned packs first (then alphabetical), so the user's favourites are always on top. Done with a
   // hand-written sort rather than a Comparator: an anonymous/inner class here gets a synthetic name
   // (EsdeathResourcePackGui$1) that the co-loaded raven transformer fails to load (NoClassDefFound).
   private void sortAvailable() {
      List<Entry> pinned = new ArrayList<Entry>();
      List<Entry> rest = new ArrayList<Entry>();
      for (Entry e : this.available) {
         (PinnedPacks.isPinned(nameOf(e)) ? pinned : rest).add(e);
      }
      sortByName(pinned);
      sortByName(rest);
      this.available.clear();
      this.available.addAll(pinned);
      this.available.addAll(rest);
   }

   private static void sortByName(List<Entry> list) {
      for (int i = 1; i < list.size(); i++) {
         Entry e = list.get(i);
         String n = nameOf(e);
         int j = i - 1;
         while (j >= 0 && nameOf(list.get(j)).compareToIgnoreCase(n) > 0) {
            list.set(j + 1, list.get(j));
            j--;
         }
         list.set(j + 1, e);
      }
   }

   private void rebuildView() {
      this.availableView.clear();
      String q = this.search == null ? "" : this.search.getText().trim().toLowerCase();
      for (Entry e : this.available) {
         if (q.isEmpty() || nameOf(e).toLowerCase().contains(q)) {
            this.availableView.add(e);
         }
      }
   }

   // recursive scan of the resourcepacks dir. .zip files and folders that contain pack.mcmeta are
   // packs; a folder without pack.mcmeta is descended into to find packs nested inside it. Cached on
   // the dir signature (paths + last-modified) so re-opening the screen doesn't re-read every pack.
   private List<Entry> scanAll(boolean forceRescan) {
      File dir = new File(this.mc.mcDataDir, "resourcepacks");
      List<File> files = new ArrayList<File>();
      collect(dir, files, 0);
      Collections.sort(files);
      String sig = signature(files);
      if (!forceRescan && sig.equals(cacheSig) && cachedAll != null) {
         return cachedAll;
      }
      loadValidCache();
      Map<String, Long> fresh = new LinkedHashMap<String, Long>();
      List<Entry> result = new ArrayList<Entry>();
      entryNames.clear();
      for (File f : files) {
         String path = f.getAbsolutePath();
         long mtime = f.lastModified();
         Long cached = validCache.get(path);
         boolean valid;
         if (!forceRescan && cached != null && cached == mtime) {
            valid = true; // trusted from the disk cache — no zip open / mcmeta read needed
         } else {
            valid = cheapValid(f); // cheap: folder has pack.mcmeta, or zip contains a pack.mcmeta entry
         }
         if (valid) {
            Entry e = newEntry(f);
            if (e != null) {
               result.add(e); // NOTE: pack.png/description loaded lazily (ensureLoaded) when drawn
               entryNames.put(e, f.getName()); // file-based name, safe to read before updateResourcePack
               fresh.put(path, mtime);
            }
         }
      }
      cachedAll = result;
      cacheSig = sig;
      loaded.clear();
      if (!fresh.equals(validCache)) {
         validCache = fresh;
         saveValidCache();
      }
      return result;
   }

   // cheap pack check — much lighter than updateResourcePack (no pack.png decode / GL upload).
   private static boolean cheapValid(File f) {
      if (f.isDirectory()) {
         return new File(f, "pack.mcmeta").isFile();
      }
      try (ZipFile z = new ZipFile(f)) {
         return z.getEntry("pack.mcmeta") != null;
      } catch (Throwable t) {
         return false;
      }
   }

   // lazily load a pack's mcmeta + icon the first time it's actually shown.
   private static void ensureLoaded(Entry e) {
      if (e != null && loaded.add(e)) {
         try {
            e.updateResourcePack();
         } catch (Throwable ignored) {
         }
      }
   }

   private static void loadValidCache() {
      if (validCache != null) {
         return;
      }
      validCache = new LinkedHashMap<String, Long>();
      try {
         if (CACHE_FILE.isFile()) {
            for (String line : Files.readAllLines(CACHE_FILE.toPath(), StandardCharsets.UTF_8)) {
               int sep = line.lastIndexOf('|');
               if (sep <= 0) {
                  continue;
               }
               try {
                  validCache.put(line.substring(0, sep), Long.parseLong(line.substring(sep + 1).trim()));
               } catch (NumberFormatException ignored) {
               }
            }
         }
      } catch (Throwable ignored) {
      }
   }

   private static void saveValidCache() {
      try {
         CACHE_FILE.getParentFile().mkdirs();
         StringBuilder sb = new StringBuilder();
         for (Map.Entry<String, Long> en : validCache.entrySet()) {
            sb.append(en.getKey()).append('|').append(en.getValue()).append('\n');
         }
         Files.write(CACHE_FILE.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
      } catch (Throwable ignored) {
      }
   }

   private static void collect(File dir, List<File> out, int depth) {
      if (depth > MAX_DEPTH || dir == null || !dir.isDirectory()) {
         return;
      }
      File[] kids = dir.listFiles();
      if (kids == null) {
         return;
      }
      for (File f : kids) {
         if (f.isFile()) {
            if (f.getName().toLowerCase().endsWith(".zip")) {
               out.add(f);
            }
         } else if (f.isDirectory()) {
            if (new File(f, "pack.mcmeta").isFile()) {
               out.add(f); // folder pack
            } else if (!new File(f, "assets").isDirectory()) {
               // a non-pack folder that doesn't look like an exploded pack -> it's a category folder,
               // descend to find packs inside. (folders with assets/ are broken/unzipped packs whose
               // deep asset trees would otherwise be walked needlessly.)
               collect(f, out, depth + 1);
            }
         }
      }
   }

   private static String signature(List<File> files) {
      StringBuilder sb = new StringBuilder();
      for (File f : files) {
         sb.append(f.getAbsolutePath()).append('@').append(f.lastModified()).append(';');
      }
      return sb.toString();
   }

   // build a repository Entry for a file the repository itself never scanned (nested pack). The
   // Entry(File) constructor is package-private and Entry is a non-static inner class, so the real
   // constructor takes (ResourcePackRepository, File).
   private Entry newEntry(File f) {
      try {
         if (entryCtor == null) {
            entryCtor = Entry.class.getDeclaredConstructor(ResourcePackRepository.class, File.class);
            entryCtor.setAccessible(true);
         }
         return (Entry) entryCtor.newInstance(this.repo, f);
      } catch (Throwable t) {
         return null;
      }
   }

   // ---- rendering ----

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      drawDefaultBackground();
      int accent = EsdeathClient.getInstance().rainbow(800);

      drawCenteredString(this.fontRendererObj, "§lResource Packs", this.width / 2, 8, accent);
      this.search.drawTextBox();
      if (this.search.getText().isEmpty() && !this.search.isFocused()) {
         this.fontRendererObj.drawString("Search...", this.leftX + 4, 26, 0xFF808080);
      }

      this.fontRendererObj.drawStringWithShadow("Available (" + this.availableView.size() + ")", this.leftX, this.listTop - 11, 0xFFBBBBBB);
      this.fontRendererObj.drawStringWithShadow("Selected (" + this.selected.size() + ")", this.rightX, this.listTop - 11, 0xFFBBBBBB);

      drawColumn(this.availableView, this.leftX, this.availScroll, mouseX, mouseY, true);
      drawColumn(this.selected, this.rightX, this.selScroll, mouseX, mouseY, false);

      // draggable scrollbars (one per column)
      drawScrollbar(this.leftX, this.availScroll, this.availableView.size());
      drawScrollbar(this.rightX, this.selScroll, this.selected.size());

      // floating dragged row (active column reorder)
      if (this.selDragging && this.selDragFrom >= 0 && this.selDragFrom < this.selected.size()) {
         Entry e = this.selected.get(this.selDragFrom);
         int ry = this.selDragY - ROW_H / 2;
         Gui.drawRect(this.rightX, ry, this.rightX + this.colW, ry + ROW_H, 0x803366CC);
         this.fontRendererObj.drawStringWithShadow(trim(nameOf(e), this.colW - 6), this.rightX + 4, ry + ROW_H / 2 - 9, 0xFFFFFFFF);
         this.fontRendererObj.drawStringWithShadow(trim(safeDesc(e), this.colW - 6), this.rightX + 4, ry + ROW_H / 2 + 3, 0xFF909090);
      }

      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   private void drawColumn(List<Entry> list, int x, int scroll, int mouseX, int mouseY, boolean availableCol) {
      Gui.drawRect(x - 2, this.listTop - 2, x + this.colW + 2, this.listBottom + 2, 0x66000000);
      boolean hoverCol = mouseX >= x && mouseX <= x + this.colW && mouseY >= this.listTop && mouseY <= this.listBottom;
      int icon = ROW_H - 4;
      for (int i = 0; i < list.size(); i++) {
         int ry = this.listTop - scroll + i * ROW_H;
         if (ry + ROW_H <= this.listTop || ry >= this.listBottom) {
            continue; // off-screen
         }
         if (!availableCol && this.selDragging && i == this.selDragFrom) {
            continue; // the dragged row is drawn under the cursor instead
         }
         Entry e = list.get(i);
         ensureLoaded(e); // load mcmeta + icon now that this row is on-screen
         boolean hov = hoverCol && mouseY >= ry && mouseY < ry + ROW_H;
         if (hov) {
            Gui.drawRect(x, Math.max(ry, this.listTop), x + this.colW, Math.min(ry + ROW_H, this.listBottom), 0x55FFFFFF);
         }
         // icon (full pack.png scaled to the current icon size)
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         try {
            e.bindTexturePackIcon(this.mc.getTextureManager());
            Gui.drawModalRectWithCustomSizedTexture(x + 2, ry + 2, 0.0F, 0.0F, icon, icon, (float) icon, (float) icon);
         } catch (Throwable ignored) {
         }
         boolean pinned = availableCol && PinnedPacks.isPinned(nameOf(e));
         String name = (pinned ? "§e* §r" : "") + nameOf(e);
         String desc = safeDesc(e);
         int tx = x + icon + 6;
         int tw = this.colW - icon - 8 - BAR_W; // leave room for the scrollbar
         // vertically center the two text lines within the row
         int textTop = ry + ROW_H / 2 - 9;
         this.fontRendererObj.drawStringWithShadow(trim(name, tw), tx, textTop, 0xFFFFFFFF);
         this.fontRendererObj.drawStringWithShadow(trim(desc, tw), tx, textTop + 12, 0xFF909090);
      }
   }

   private String safeDesc(Entry e) {
      try {
         ensureLoaded(e);
         String d = e.getTexturePackDescription();
         return d == null ? "" : d.replace('\n', ' ');
      } catch (Throwable t) {
         return "";
      }
   }

   private String trim(String s, int max) {
      return this.fontRendererObj.trimStringToWidth(s, max);
   }

   // ---- input ----

   @Override
   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      this.search.mouseClicked(mouseX, mouseY, mouseButton);

      // scrollbars — grab if the click landed on a column's bar (and it actually scrolls)
      if (mouseButton == 0 && mouseY >= this.listTop && mouseY <= this.listBottom) {
         if (mouseX >= barX(this.leftX) && mouseX <= barX(this.leftX) + BAR_W && colMax(this.availableView.size()) > 0) {
            this.draggingBar = 1;
            setScrollFromBar(1, mouseY);
            return;
         }
         if (mouseX >= barX(this.rightX) && mouseX <= barX(this.rightX) + BAR_W && colMax(this.selected.size()) > 0) {
            this.draggingBar = 2;
            setScrollFromBar(2, mouseY);
            return;
         }
      }

      int ai = rowAt(this.leftX, this.availScroll, this.availableView, mouseX, mouseY);
      int si = rowAt(this.rightX, this.selScroll, this.selected, mouseX, mouseY);

      if (ai >= 0) {
         Entry e = this.availableView.get(ai);
         if (mouseButton == 1) { // right-click pins/unpins
            PinnedPacks.toggle(nameOf(e));
            sortAvailable();
            rebuildView();
         } else { // move to selected (top = highest priority)
            this.available.remove(e);
            this.selected.add(0, e);
            rebuildView();
            clampScroll();
         }
         return;
      }
      if (si >= 0 && mouseButton == 0) {
         // defer: a plain click moves the pack back to available, a drag reorders the active column
         this.selDragFrom = si;
         this.selPressY = mouseY;
         this.selDragY = mouseY;
         this.selDragging = false;
         return;
      }
      super.mouseClicked(mouseX, mouseY, mouseButton);
   }

   @Override
   protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long time) {
      if (this.draggingBar != 0) {
         setScrollFromBar(this.draggingBar, mouseY);
         return;
      }
      this.selDragY = mouseY;
      if (this.selDragFrom >= 0 && Math.abs(mouseY - this.selPressY) > 4) {
         this.selDragging = true;
      }
      super.mouseClickMove(mouseX, mouseY, mouseButton, time);
   }

   @Override
   protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
      if (this.draggingBar != 0) {
         this.draggingBar = 0;
         return;
      }
      if (this.selDragFrom >= 0) {
         if (this.selDragging) {
            int target = (mouseY - this.listTop + this.selScroll) / ROW_H;
            target = Math.max(0, Math.min(this.selected.size() - 1, target));
            if (target != this.selDragFrom) {
               Entry moved = this.selected.remove(this.selDragFrom);
               this.selected.add(target, moved);
            }
         } else {
            // no drag -> treat as a click: send the pack back to available
            Entry e = this.selected.remove(this.selDragFrom);
            this.available.add(e);
            sortAvailable();
            rebuildView();
         }
         clampScroll();
         this.selDragFrom = -1;
         this.selDragging = false;
      }
      super.mouseReleased(mouseX, mouseY, mouseButton);
   }

   private int rowAt(int x, int scroll, List<Entry> list, int mouseX, int mouseY) {
      if (mouseX < x || mouseX > x + this.colW || mouseY < this.listTop || mouseY > this.listBottom) {
         return -1;
      }
      int i = (mouseY - this.listTop + scroll) / ROW_H;
      return i >= 0 && i < list.size() ? i : -1;
   }

   @Override
   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      int d = Mouse.getEventDWheel();
      if (d == 0) {
         return;
      }
      int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
      int step = d > 0 ? -ROW_H : ROW_H;
      if (mx < this.rightX) {
         this.availScroll += step;
      } else {
         this.selScroll += step;
      }
      clampScroll();
   }

   private void clampScroll() {
      int visible = this.listBottom - this.listTop;
      int availMax = Math.max(0, this.availableView.size() * ROW_H - visible);
      int selMax = Math.max(0, this.selected.size() * ROW_H - visible);
      this.availScroll = Math.max(0, Math.min(this.availScroll, availMax));
      this.selScroll = Math.max(0, Math.min(this.selScroll, selMax));
   }

   @Override
   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if (this.search.isFocused() && keyCode != 1) {
         this.search.textboxKeyTyped(typedChar, keyCode);
         rebuildView();
         clampScroll();
         return;
      }
      super.keyTyped(typedChar, keyCode);
   }

   @Override
   public void updateScreen() {
      this.search.updateCursorCounter();
   }

   @Override
   protected void actionPerformed(GuiButton button) throws IOException {
      switch (button.id) {
         case B_DONE:
            apply();
            break;
         case B_REFRESH:
            loadEntries(true);
            break;
         case B_FOLDER:
            openFolder();
            break;
      }
   }

   private void apply() {
      // repositoryEntries order is the reverse of the selected column (top = highest priority).
      List<Entry> ordered = new ArrayList<Entry>(this.selected);
      Collections.reverse(ordered);
      for (Entry e : ordered) {
         ensureLoaded(e); // make sure mcmeta/format is loaded before the pack is committed
      }
      this.repo.setRepositories(ordered);

      this.mc.gameSettings.resourcePacks.clear();
      this.mc.gameSettings.incompatibleResourcePacks.clear();
      for (Entry e : this.repo.getRepositoryEntries()) {
         this.mc.gameSettings.resourcePacks.add(nameOf(e));
         try {
            if (e.func_183027_f() != 1) {
               this.mc.gameSettings.incompatibleResourcePacks.add(nameOf(e));
            }
         } catch (Throwable ignored) {
         }
      }
      this.mc.gameSettings.saveOptions();

      boolean changed = !names(this.selected).equals(this.originalSelection);
      this.mc.displayGuiScreen(this.parent);
      if (changed) {
         this.mc.refreshResources(); // only pay the reload cost when the selection actually changed
      }
   }

   private void openFolder() {
      try {
         File dir = new File(this.mc.mcDataDir, "resourcepacks");
         dir.mkdirs();
         java.awt.Desktop.getDesktop().open(dir);
      } catch (Throwable ignored) {
      }
   }

   // pack name that's safe before updateResourcePack: prefer the cached file name, else fall back to
   // the repository's name (selected entries from the repo are already loaded).
   private static String nameOf(Entry e) {
      String n = entryNames.get(e);
      if (n != null) {
         return n;
      }
      try {
         return e.getResourcePackName();
      } catch (Throwable t) {
         return "";
      }
   }

   private static List<String> names(List<Entry> list) {
      List<String> out = new ArrayList<String>();
      for (Entry e : list) {
         out.add(nameOf(e));
      }
      return out;
   }

   private static boolean containsByName(List<Entry> list, Entry e) {
      String n = nameOf(e);
      for (Entry x : list) {
         if (nameOf(x).equals(n)) {
            return true;
         }
      }
      return false;
   }
}
