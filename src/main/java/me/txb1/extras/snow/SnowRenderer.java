package me.txb1.extras.snow;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

// Falling-snow overlay: drifting snowflakes that fall and wrap to the top, drawn as small soft squares
// (no textures) tinted by SnowSettings (colour + alpha). Snowflakes also draw fading "constellation"
// lines to nearby flakes and to the cursor — the cursor lines are controlled by SnowSettings.cursorLines
// (0 = off). One shared instance is fine; the main menu and inventory are never on screen at once.
public final class SnowRenderer {
   private SnowRenderer() {
   }

   private static final class Flake {
      float x, y, size, vy, drift, phase, a;
      float dx; // last drawn x (x + sway), so connecting lines line up with the flake
   }

   private static Flake[] flakes;
   private static int builtN = -1;
   private static int builtW = -1;
   private static int builtH = -1;
   private static long lastNs;

   public static void render(int w, int h) {
      render(w, h, Integer.MIN_VALUE, Integer.MIN_VALUE);
   }

   public static void render(int w, int h, int mouseX, int mouseY) {
      int n = SnowSettings.clampAmount(SnowSettings.amount);
      if (flakes == null || builtN != n || builtW != w || builtH != h) {
         build(n, w, h);
      }

      long now = System.nanoTime();
      float dt = lastNs == 0L ? 0.016F : (now - lastNs) / 1.0E9F;
      lastNs = now;
      if (dt < 0F) {
         dt = 0F;
      } else if (dt > 0.1F) {
         dt = 0.1F; // clamp after a pause / lag spike
      }

      int rgb = SnowSettings.color & 0xFFFFFF;
      int baseA = SnowSettings.clampAlpha(SnowSettings.alpha);

      for (Flake f : flakes) {
         f.phase += dt * 1.5F;
         f.y += f.vy * dt;
         if (f.y > h + 4) {
            f.y = -4F;
            f.x = (float) (Math.random() * w);
         }
         float x = f.x + (float) Math.sin(f.phase) * f.drift;
         f.dx = x;
         int a = Math.max(0, Math.min(255, (int) (baseA * f.a)));
         int col = (a << 24) | rgb;
         int s = Math.max(1, (int) f.size);
         Gui.drawRect((int) x, (int) f.y, (int) x + s, (int) f.y + s, col);
      }

      drawLines(mouseX, mouseY);
   }

   // fading "constellation" lines: each flake links to nearby flakes, and (when SnowSettings.cursorLines
   // > 0) to the cursor. Line alpha fades with distance. Immediate-mode GL (there's no Gui.drawLine).
   private static void drawLines(int mouseX, int mouseY) {
      int rgb = SnowSettings.color & 0xFFFFFF;
      float rf = ((rgb >> 16) & 0xFF) / 255.0F;
      float gf = ((rgb >> 8) & 0xFF) / 255.0F;
      float bf = (rgb & 0xFF) / 255.0F;
      float baseA = SnowSettings.clampAlpha(SnowSettings.alpha) / 255.0F;
      boolean linkFlakes = SnowSettings.amount <= 200; // cap the O(n^2) flake-flake links

      // cursor line range scales with the slider (0 = off, 100% = max reach)
      float cursorR = 160.0F * (SnowSettings.cursorLines / 100.0F);
      boolean cursorLines = SnowSettings.cursorLines > 0 && mouseX != Integer.MIN_VALUE;
      float cursorR2 = cursorR * cursorR;

      final float FLAKE_R = 70.0F, FLAKE_R2 = FLAKE_R * FLAKE_R;

      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.disableAlpha();
      GL11.glEnable(GL11.GL_LINE_SMOOTH);
      GL11.glLineWidth(1.0F);
      GL11.glBegin(GL11.GL_LINES);
      for (int i = 0; i < flakes.length; i++) {
         Flake fi = flakes[i];
         if (cursorLines) {
            float dx = fi.dx - mouseX, dy = fi.y - mouseY, d2 = dx * dx + dy * dy;
            if (d2 < cursorR2) {
               float a = (1.0F - (float) Math.sqrt(d2) / cursorR) * baseA;
               GL11.glColor4f(rf, gf, bf, a);
               GL11.glVertex2f(fi.dx, fi.y);
               GL11.glVertex2f(mouseX, mouseY);
            }
         }
         if (linkFlakes) {
            for (int j = i + 1; j < flakes.length; j++) {
               Flake fj = flakes[j];
               float dx = fi.dx - fj.dx, dy = fi.y - fj.y, d2 = dx * dx + dy * dy;
               if (d2 < FLAKE_R2) {
                  float a = (1.0F - (float) Math.sqrt(d2) / FLAKE_R) * baseA * 0.5F;
                  GL11.glColor4f(rf, gf, bf, a);
                  GL11.glVertex2f(fi.dx, fi.y);
                  GL11.glVertex2f(fj.dx, fj.y);
               }
            }
         }
      }
      GL11.glEnd();
      GL11.glDisable(GL11.GL_LINE_SMOOTH);
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private static void build(int n, int w, int h) {
      flakes = new Flake[n];
      builtN = n;
      builtW = w;
      builtH = h;
      for (int i = 0; i < n; i++) {
         Flake f = new Flake();
         f.x = (float) (Math.random() * Math.max(1, w));
         f.y = (float) (Math.random() * Math.max(1, h));
         f.size = 1.0F + (float) (Math.random() * 2.5F);
         f.vy = 18.0F + (float) (Math.random() * 42.0F);  // px/sec fall speed
         f.drift = 3.0F + (float) (Math.random() * 11.0F); // horizontal sway amplitude
         f.phase = (float) (Math.random() * Math.PI * 2.0);
         f.a = 0.5F + (float) (Math.random() * 0.5F);      // per-flake alpha for depth
         flakes[i] = f;
      }
   }
}
