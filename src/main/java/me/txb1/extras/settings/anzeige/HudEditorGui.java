package me.txb1.extras.settings.anzeige;

import com.darkmagician6.eventapi.EventManager;
import java.util.ArrayList;
import java.util.List;
import me.txb1.EsdeathClient;
import me.txb1.player.events.EventRender;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import me.txb1.player.modulesystem.modules.render.CleanChat;
import me.txb1.player.modulesystem.modules.render.CleanScoreboard;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

// Unified HUD editor (opened from the Esc-menu "Display" panel): every live HUD element is shown and
// can be clicked + dragged. Each element is outlined by the actual extent of what it draws (measured
// via HudBoundsRecorder) rather than a placeholder label. Centre guides turn green on alignment.
public class HudEditorGui extends GuiScreen {
   private final GuiScreen back;
   private final List<Element> elements = new ArrayList<Element>();
   private Element selected;
   private boolean dragging;
   private int grabDX;
   private int grabDY;

   public HudEditorGui(GuiScreen back) {
      this.back = back;
   }

   private abstract class Element {
      final String key;
      // current outline box (screen space), recomputed each frame
      int bx;
      int by;
      int bw;
      int bh;

      Element(String key) {
         this.key = key;
      }

      abstract int x(); // anchor (draw origin)

      abstract int y();

      abstract void setPos(int nx, int ny);

      abstract void resetBox(); // fallback box before measured rects are applied
   }

   // absolute-positioned visual modules (cords are the screen position); outline = measured extent
   private final class ModuleElement extends Element {
      ModuleElement(String key) {
         super(key);
      }

      @Override
      int x() {
         return AnzeigeSettings.getCords(this.key).getX();
      }

      @Override
      int y() {
         return AnzeigeSettings.getCords(this.key).getY();
      }

      @Override
      void setPos(int nx, int ny) {
         Cordinates c = AnzeigeSettings.getCords(this.key);
         c.setX(nx);
         c.setY(ny);
      }

      @Override
      void resetBox() {
         // sentinel: no rects yet
         this.bx = x();
         this.by = y();
         this.bw = -1;
         this.bh = -1;
      }
   }

   // offset-positioned elements (scoreboard / chat) — they don't render in a GUI, so use a fixed box
   private final class OffsetElement extends Element {
      final int baseX;
      final int baseY;
      final int fw;
      final int fh;

      OffsetElement(String key, int baseX, int baseY, int fw, int fh) {
         super(key);
         this.baseX = baseX;
         this.baseY = baseY;
         this.fw = fw;
         this.fh = fh;
      }

      @Override
      int x() {
         return this.baseX + AnzeigeSettings.getCords(this.key).getX();
      }

      @Override
      int y() {
         return this.baseY + AnzeigeSettings.getCords(this.key).getY();
      }

      @Override
      void setPos(int nx, int ny) {
         Cordinates c = AnzeigeSettings.getCords(this.key);
         c.setX(nx - this.baseX);
         c.setY(ny - this.baseY);
      }

      @Override
      void resetBox() {
         this.bx = x();
         this.by = y();
         this.bw = this.fw;
         this.bh = this.fh;
      }
   }

   @Override
   public void initGui() {
      this.elements.clear();
      for (Module m : EsdeathClient.getInstance().getModuleManager().getModules()) {
         if (m.isHudElement() && m.isEnabled()) {
            this.elements.add(new ModuleElement(m.getName().toLowerCase()));
         }
      }
      if (CleanScoreboard.active) {
         this.elements.add(new OffsetElement("scoreboard", this.width - 92, this.height / 2 - 30, 90, 70));
      }
      if (CleanChat.active) {
         this.elements.add(new OffsetElement("chat", 2, this.height - 48, 160, 40));
      }
   }

   private void expand(Element e, int rx, int ry, int rw, int rh) {
      if (e.bw < 0) { // first rect
         e.bx = rx;
         e.by = ry;
         e.bw = rw;
         e.bh = rh;
         return;
      }
      int minx = Math.min(e.bx, rx);
      int miny = Math.min(e.by, ry);
      int maxx = Math.max(e.bx + e.bw, rx + rw);
      int maxy = Math.max(e.by + e.bh, ry + rh);
      e.bx = minx;
      e.by = miny;
      e.bw = maxx - minx;
      e.bh = maxy - miny;
   }

   // run the live HUD once, recording string rects, and fold them into each module element's outline
   private void measure() {
      for (Element e : this.elements) {
         e.resetBox();
      }
      HudBoundsRecorder.rects.clear();
      HudBoundsRecorder.recording = true;
      try {
         EventManager.call(new EventRender());
      } finally {
         HudBoundsRecorder.recording = false;
      }
      for (int[] r : HudBoundsRecorder.rects) {
         Element best = null;
         double bd = Double.MAX_VALUE;
         for (Element e : this.elements) {
            if (!(e instanceof ModuleElement)) {
               continue;
            }
            double dx = r[0] - e.x();
            double dy = r[1] - e.y();
            double d = dx * dx + dy * dy;
            if (d < bd) {
               bd = d;
               best = e;
            }
         }
         if (best != null && bd < 80 * 80) {
            expand(best, r[0], r[1], r[2], r[3]);
         }
      }
      // modules that drew nothing get a small fallback box at their anchor
      for (Element e : this.elements) {
         if (e.bw < 0) {
            e.bx = e.x();
            e.by = e.y();
            e.bw = 30;
            e.bh = 9;
         }
      }
   }

   @Override
   protected void mouseClicked(int mx, int my, int btn) {
      for (int i = this.elements.size() - 1; i >= 0; i--) {
         Element e = this.elements.get(i);
         if (mx >= e.bx - 2 && mx <= e.bx + e.bw + 2 && my >= e.by - 2 && my <= e.by + e.bh + 2) {
            this.selected = e;
            this.dragging = true;
            this.grabDX = mx - e.x();
            this.grabDY = my - e.y();
            return;
         }
      }
      this.selected = null;
   }

   @Override
   protected void mouseClickMove(int mx, int my, int btn, long time) {
      if (this.dragging && this.selected != null) {
         this.selected.setPos(mx - this.grabDX, my - this.grabDY);
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
      Gui.drawRect(0, 0, this.width, this.height, 0xA00A0A0A);

      // draw + measure the live HUD
      measure();

      // centre guides
      int cxg = this.width / 2;
      int cyg = this.height / 2;
      boolean vAlign = false;
      boolean hAlign = false;
      if (this.selected != null) {
         vAlign = Math.abs(this.selected.bx + this.selected.bw / 2 - cxg) < 4;
         hAlign = Math.abs(this.selected.by + this.selected.bh / 2 - cyg) < 4;
      }
      Gui.drawRect(cxg, 0, cxg + 1, this.height, vAlign ? 0xFF55FF55 : 0x44FFFFFF);
      Gui.drawRect(0, cyg, this.width, cyg + 1, hAlign ? 0xFF55FF55 : 0x44FFFFFF);

      // outline each element's actual extent (no labels)
      for (Element e : this.elements) {
         int x1 = e.bx - 2;
         int y1 = e.by - 2;
         int x2 = e.bx + e.bw + 2;
         int y2 = e.by + e.bh + 2;
         boolean hover = mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
         boolean sel = e == this.selected;
         if (hover || sel) {
            Gui.drawRect(x1, y1, x2, y2, sel ? 0x553366CC : 0x33FFFFFF);
         }
         int col = sel ? 0xFF55FFFF : (hover ? 0xFFFFFFFF : 0x88FFFFFF);
         Gui.drawRect(x1, y1, x2, y1 + 1, col);
         Gui.drawRect(x1, y2 - 1, x2, y2, col);
         Gui.drawRect(x1, y1, x1 + 1, y2, col);
         Gui.drawRect(x2 - 1, y1, x2, y2, col);
      }

      this.drawCenteredString(this.fontRendererObj, "§7Drag any HUD element — ESC to save", this.width / 2, 6, -1);

      super.drawScreen(mx, my, pt);
   }
}
