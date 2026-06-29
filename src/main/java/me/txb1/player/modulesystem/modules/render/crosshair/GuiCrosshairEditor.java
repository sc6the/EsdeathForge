package me.txb1.player.modulesystem.modules.render.crosshair;

import java.io.IOException;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

// Ported from CrosshairMod-1.0.0 (com.customxhair.gui.GuiCrosshairEditor). Grid editor:
// left-click paints, right-click erases, drag to fill; ARGB hex field; grid/blend/scale buttons.
public final class GuiCrosshairEditor extends GuiScreen {
   private static final int CANVAS_MAX_PX = 320;
   private final CrosshairMod mod;
   private final CrosshairConfig cfg;
   private int canvasX;
   private int canvasY;
   private int canvasSize;
   private int cellSize;
   private int btnY;
   private boolean dragging = false;
   private boolean dragValue = true;
   private GuiButton gridBtn;
   private GuiButton blendBtn;
   private GuiButton scaleBtn;
   private GuiButton clearBtn;
   private GuiButton doneBtn;
   private GuiTextField hexField;

   public GuiCrosshairEditor(CrosshairMod mod) {
      this.mod = mod;
      this.cfg = mod.config;
   }

   @Override
   public void initGui() {
      this.buttonList.clear();
      int n = this.cfg.gridSize;
      this.canvasY = 24;
      int verticalBudget = this.height - this.canvasY - 60;
      int horizontalBudget = this.width - 160;
      int targetByBudget = Math.min(verticalBudget, Math.max(horizontalBudget, n * 4));
      int target = Math.min(320, targetByBudget);
      this.cellSize = Math.max(4, target / n);
      this.canvasSize = this.cellSize * n;
      int sidebarW = 130;
      this.canvasX = Math.max(8, (this.width - sidebarW - 16 - this.canvasSize) / 2);
      this.btnY = Math.min(this.canvasY + this.canvasSize + 8, this.height - 48);
      int btnW = 60;
      int btnH = 20;
      int gap = 4;
      int totalW = btnW * 5 + gap * 4;
      int btnX = (this.width - totalW) / 2;
      this.gridBtn = new GuiButton(0, btnX, this.btnY, btnW, btnH, n + "x" + n);
      this.blendBtn = new GuiButton(1, btnX + (btnW + gap), this.btnY, btnW, btnH, this.cfg.vanillaBlending ? "Vanilla" : "Normal");
      this.scaleBtn = new GuiButton(4, btnX + (btnW + gap) * 2, this.btnY, btnW, btnH, scaleLabel(this.cfg.scale));
      this.clearBtn = new GuiButton(2, btnX + (btnW + gap) * 3, this.btnY, btnW, btnH, "Clear");
      this.doneBtn = new GuiButton(3, btnX + (btnW + gap) * 4, this.btnY, btnW, btnH, "Done");
      this.buttonList.add(this.gridBtn);
      this.buttonList.add(this.blendBtn);
      this.buttonList.add(this.scaleBtn);
      this.buttonList.add(this.clearBtn);
      this.buttonList.add(this.doneBtn);
      int sidebarX = this.canvasX + this.canvasSize + 16;
      int sidebarY = this.canvasY;
      this.hexField = new GuiTextField(99, this.fontRendererObj, sidebarX, sidebarY + 60, 100, 18);
      this.hexField.setMaxStringLength(8);
      this.hexField.setText(String.format("%08X", this.cfg.colorARGB));
   }

   @Override
   protected void actionPerformed(GuiButton button) throws IOException {
      if (button == this.gridBtn) {
         this.cfg.gridSize = nextGridSize(this.cfg.gridSize);
         this.cfg.save();
         this.mod.invalidate();
         this.initGui();
      } else if (button == this.blendBtn) {
         this.cfg.vanillaBlending = !this.cfg.vanillaBlending;
         this.cfg.save();
         this.blendBtn.displayString = this.cfg.vanillaBlending ? "Vanilla" : "Normal";
      } else if (button == this.scaleBtn) {
         this.cfg.scale = nextScale(this.cfg.scale);
         this.cfg.save();
         this.scaleBtn.displayString = scaleLabel(this.cfg.scale);
      } else if (button == this.clearBtn) {
         this.cfg.clearActive();
         this.cfg.save();
         this.mod.invalidate();
      } else if (button == this.doneBtn) {
         this.commitHex();
         this.mc.displayGuiScreen(null);
      }
   }

   @Override
   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if (this.hexField.textboxKeyTyped(typedChar, keyCode)) {
         this.commitHex();
      } else {
         super.keyTyped(typedChar, keyCode);
      }
   }

   @Override
   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      this.hexField.mouseClicked(mouseX, mouseY, mouseButton);
      if (mouseButton == 0 || mouseButton == 1) {
         int cell = this.pickCell(mouseX, mouseY);
         if (cell >= 0) {
            int cx = cell & 65535;
            int cy = cell >>> 16;
            this.dragValue = mouseButton == 0;
            this.cfg.activeGrid()[cy][cx] = this.dragValue;
            this.dragging = true;
            this.mod.invalidate();
         }
      }
   }

   @Override
   protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long time) {
      if (this.dragging) {
         int cell = this.pickCell(mouseX, mouseY);
         if (cell >= 0) {
            int cx = cell & 65535;
            int cy = cell >>> 16;
            this.cfg.activeGrid()[cy][cx] = this.dragValue;
            this.mod.invalidate();
         }
      }
   }

   @Override
   protected void mouseReleased(int mouseX, int mouseY, int state) {
      super.mouseReleased(mouseX, mouseY, state);
      if (this.dragging) {
         this.dragging = false;
         this.cfg.save();
      }
   }

   private static int nextGridSize(int current) {
      switch (current) {
         case 15:
            return 16;
         case 16:
            return 32;
         case 32:
            return 33;
         case 33:
         default:
            return 15;
      }
   }

   private static int nextScale(int current) {
      switch (current) {
         case 0:
            return 1;
         case 1:
            return 2;
         case 2:
            return 3;
         case 3:
         default:
            return 0;
      }
   }

   private static String scaleLabel(int scale) {
      switch (scale) {
         case 1:
            return "Small";
         case 2:
            return "Normal";
         case 3:
            return "Large";
         case 0:
         default:
            return "Auto";
      }
   }

   private int pickCell(int mx, int my) {
      if (mx >= this.canvasX && my >= this.canvasY) {
         int dx = mx - this.canvasX;
         int dy = my - this.canvasY;
         if (dx < this.canvasSize && dy < this.canvasSize) {
            int cx = dx / this.cellSize;
            int cy = dy / this.cellSize;
            return cx < this.cfg.gridSize && cy < this.cfg.gridSize ? cy << 16 | cx & 65535 : -1;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private void commitHex() {
      try {
         String s = this.hexField.getText().trim();
         if (s.startsWith("0x") || s.startsWith("0X")) {
            s = s.substring(2);
         }
         if (s.length() == 0) {
            return;
         }
         long v = Long.parseLong(s, 16);
         int color = (int)(v & 4294967295L);
         if (s.length() <= 6) {
            color |= -16777216;
         }
         if (color != this.cfg.colorARGB) {
            this.cfg.colorARGB = color;
            this.cfg.save();
            this.mod.invalidate();
         }
      } catch (NumberFormatException var5) {
      }
   }

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      int n = this.cfg.gridSize;
      boolean[][] g = this.cfg.activeGrid();
      Gui.drawRect(this.canvasX - 1, this.canvasY - 1, this.canvasX + this.canvasSize + 1, this.canvasY + this.canvasSize + 1, -14671840);
      Gui.drawRect(this.canvasX, this.canvasY, this.canvasX + this.canvasSize, this.canvasY + this.canvasSize, -15724528);
      int centerColor = -7667712;
      if ((n & 1) == 1) {
         int center = n / 2;
         int ccx = this.canvasX + center * this.cellSize;
         int ccy = this.canvasY + center * this.cellSize;
         Gui.drawRect(ccx, ccy, ccx + this.cellSize, ccy + this.cellSize, centerColor);
      } else {
         int c0 = n / 2 - 1;
         int ccx = this.canvasX + c0 * this.cellSize;
         int ccy = this.canvasY + c0 * this.cellSize;
         Gui.drawRect(ccx, ccy, ccx + this.cellSize * 2, ccy + this.cellSize * 2, centerColor);
      }

      int paintColor = this.cfg.vanillaBlending ? -1 : 0xFF000000 | this.cfg.colorARGB & 16777215;
      for (int y = 0; y < n; y++) {
         for (int x = 0; x < n; x++) {
            if (g[y][x]) {
               int x0 = this.canvasX + x * this.cellSize;
               int y0 = this.canvasY + y * this.cellSize;
               Gui.drawRect(x0, y0, x0 + this.cellSize, y0 + this.cellSize, paintColor);
            }
         }
      }

      int lineColor = 1090519039;
      for (int i = 0; i <= n; i++) {
         int v = this.canvasX + i * this.cellSize;
         Gui.drawRect(v, this.canvasY, v + 1, this.canvasY + this.canvasSize, lineColor);
         int h = this.canvasY + i * this.cellSize;
         Gui.drawRect(this.canvasX, h, this.canvasX + this.canvasSize, h + 1, lineColor);
      }

      int cell = this.pickCell(mouseX, mouseY);
      if (cell >= 0) {
         int cx = cell & 65535;
         int cy = cell >>> 16;
         int x0 = this.canvasX + cx * this.cellSize;
         int y0 = this.canvasY + cy * this.cellSize;
         Gui.drawRect(x0, y0, x0 + this.cellSize, y0 + this.cellSize, 1090519039);
      }

      int sidebarX = this.canvasX + this.canvasSize + 16;
      int sidebarY = this.canvasY;
      this.fontRendererObj.drawStringWithShadow("Preview:", (float)sidebarX, (float)sidebarY, -1);
      this.mod.ensureTexture(n);
      this.drawPreview(sidebarX, sidebarY + 12, n);
      this.fontRendererObj.drawStringWithShadow("Color (ARGB hex):", (float)sidebarX, (float)(sidebarY + 50), -1);
      this.hexField.drawTextBox();
      int infoY = this.btnY + 24;
      if (infoY + 10 <= this.height) {
         this.fontRendererObj.drawString("Left-click: paint  |  Right-click: erase  |  Drag to fill", this.canvasX, infoY, -6250336);
      }

      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   private void drawPreview(int x, int y, int size) {
      Gui.drawRect(x - 1, y - 1, x + size + 1, y + size + 1, -14671840);
      Gui.drawRect(x, y, x + size, y + size, -10461088);
      boolean[][] g = this.cfg.activeGrid();
      int color = this.cfg.vanillaBlending ? -1 : this.cfg.colorARGB;
      for (int gy = 0; gy < size; gy++) {
         for (int gx = 0; gx < size; gx++) {
            if (g[gy][gx]) {
               if (this.cfg.vanillaBlending) {
                  Gui.drawRect(x + gx, y + gy, x + gx + 1, y + gy + 1, -10461088);
               } else {
                  Gui.drawRect(x + gx, y + gy, x + gx + 1, y + gy + 1, color);
               }
            }
         }
      }
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }

   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }

   @Override
   public void onGuiClosed() {
      this.cfg.save();
   }

   @Override
   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      if (!Mouse.isButtonDown(0) && !Mouse.isButtonDown(1) && this.dragging) {
         this.dragging = false;
         this.cfg.save();
      }
   }
}
