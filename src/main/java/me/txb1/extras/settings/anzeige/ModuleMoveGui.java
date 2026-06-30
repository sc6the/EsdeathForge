package me.txb1.extras.settings.anzeige;

import com.darkmagician6.eventapi.EventManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.txb1.EsdeathClient;
import me.txb1.player.events.EventRender;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

// Single-element move editor: drag ONE visual module's HUD element to reposition it, showing ONLY
// that element (scissor-clipped) and no other HUD elements. Opened by the module button's Move
// handle; the pencil opens config instead. The element's extent is measured live (via
// HudBoundsRecorder), assigning each drawn string to its nearest module anchor so neighbouring
// elements' text isn't mistaken for this one. ESC saves.
public class ModuleMoveGui extends GuiScreen {
   private final GuiScreen back;
   private final String key;
   private final String label;
   private boolean dragging;
   private int grabDX;
   private int grabDY;
   private int bx, by, bw, bh;

   private final List<int[]> anchors = new ArrayList<int[]>(); // {x, y, isTarget}

   public ModuleMoveGui(Module m, GuiScreen back) {
      this.back = back;
      this.key = m.getName().toLowerCase();
      this.label = m.getDisplayName();
   }

   private int ax() {
      return AnzeigeSettings.getCords(this.key).getX();
   }

   private int ay() {
      return AnzeigeSettings.getCords(this.key).getY();
   }

   private void setPos(int nx, int ny) {
      Cordinates c = AnzeigeSettings.getCords(this.key);
      c.setX(nx);
      c.setY(ny);
   }

   private void rebuildAnchors() {
      this.anchors.clear();
      for (Module m : EsdeathClient.getInstance().getModuleManager().getModules()) {
         if (m.isHudElement() && m.isEnabled()) {
            String k = m.getName().toLowerCase();
            Cordinates c = AnzeigeSettings.getCords(k);
            this.anchors.add(new int[]{c.getX(), c.getY(), k.equals(this.key) ? 1 : 0});
         }
      }
   }

   private void expand(int rx, int ry, int rw, int rh) {
      if (this.bw < 0) {
         this.bx = rx;
         this.by = ry;
         this.bw = rw;
         this.bh = rh;
         return;
      }
      int minx = Math.min(this.bx, rx);
      int miny = Math.min(this.by, ry);
      int maxx = Math.max(this.bx + this.bw, rx + rw);
      int maxy = Math.max(this.by + this.bh, ry + rh);
      this.bx = minx;
      this.by = miny;
      this.bw = maxx - minx;
      this.bh = maxy - miny;
   }

   // run the live HUD once, recording string rects, and keep only the ones nearest THIS module
   private void measure() {
      rebuildAnchors();
      this.bx = ax();
      this.by = ay();
      this.bw = -1;
      this.bh = -1;
      HudBoundsRecorder.rects.clear();
      HudBoundsRecorder.recording = true;
      try {
         EventManager.call(new EventRender());
      } finally {
         HudBoundsRecorder.recording = false;
      }
      for (int[] r : HudBoundsRecorder.rects) {
         int[] best = null;
         double bd = Double.MAX_VALUE;
         for (int[] a : this.anchors) {
            double dx = r[0] - a[0];
            double dy = r[1] - a[1];
            double d = dx * dx + dy * dy;
            if (d < bd) {
               bd = d;
               best = a;
            }
         }
         if (best != null && best[2] == 1 && bd < 80 * 80) {
            expand(r[0], r[1], r[2], r[3]);
         }
      }
      if (this.bw < 0) {
         this.bx = ax();
         this.by = ay();
         this.bw = 30;
         this.bh = 9;
      }
   }

   @Override
   public void drawScreen(int mx, int my, float pt) {
      // 1) measure (this also draws the full HUD onto the framebuffer)
      measure();
      // 2) cover the whole screen with a dark backdrop (hides every element)
      Gui.drawRect(0, 0, this.width, this.height, 0xC00A0A0A);

      int x1 = this.bx - 2, y1 = this.by - 2, x2 = this.bx + this.bw + 2, y2 = this.by + this.bh + 2;

      // 3) re-render the HUD, scissor-clipped to this element's box -> only this element shows
      ScaledResolution sr = new ScaledResolution(this.mc);
      int sf = sr.getScaleFactor();
      GL11.glEnable(GL11.GL_SCISSOR_TEST);
      GL11.glScissor(x1 * sf, (this.height - y2) * sf, Math.max(0, x2 - x1) * sf, Math.max(0, y2 - y1) * sf);
      EventManager.call(new EventRender());
      GL11.glDisable(GL11.GL_SCISSOR_TEST);

      // 4) outline + centre guides + hint
      int accent = EsdeathClient.getInstance().rainbow(500);
      boolean hover = mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
      if (hover || this.dragging) {
         Gui.drawRect(x1, y1, x2, y2, 0x33FFFFFF);
      }
      Gui.drawRect(x1, y1, x2, y1 + 1, accent);
      Gui.drawRect(x1, y2 - 1, x2, y2, accent);
      Gui.drawRect(x1, y1, x1 + 1, y2, accent);
      Gui.drawRect(x2 - 1, y1, x2, y2, accent);

      int cx = this.width / 2, cy = this.height / 2;
      boolean va = Math.abs(this.bx + this.bw / 2 - cx) < 4;
      boolean ha = Math.abs(this.by + this.bh / 2 - cy) < 4;
      Gui.drawRect(cx, 0, cx + 1, this.height, va ? 0xFF55FF55 : 0x33FFFFFF);
      Gui.drawRect(0, cy, this.width, cy + 1, ha ? 0xFF55FF55 : 0x33FFFFFF);

      this.drawCenteredString(this.fontRendererObj, "§7Drag §f" + this.label + " §7— ESC to save", this.width / 2, 6, -1);
      super.drawScreen(mx, my, pt);
   }

   @Override
   protected void mouseClicked(int mx, int my, int btn) throws IOException {
      int x1 = this.bx - 2, y1 = this.by - 2, x2 = this.bx + this.bw + 2, y2 = this.by + this.bh + 2;
      if (btn == 0 && mx >= x1 && mx <= x2 && my >= y1 && my <= y2) {
         this.dragging = true;
         this.grabDX = mx - ax();
         this.grabDY = my - ay();
      }
      super.mouseClicked(mx, my, btn);
   }

   @Override
   protected void mouseClickMove(int mx, int my, int btn, long time) {
      if (this.dragging) {
         setPos(mx - this.grabDX, my - this.grabDY);
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
   protected void keyTyped(char c, int code) throws IOException {
      if (code == 1) { // ESC saves + returns
         AnzeigeSettings.save();
         this.mc.displayGuiScreen(this.back);
         return;
      }
      super.keyTyped(c, code);
   }

   @Override
   public boolean doesGuiPauseGame() {
      return true;
   }
}
