package me.txb1.player.modulesystem.modules.render;

import me.txb1.EsdeathClient;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

// CleanScoreboard: reimplements the sidebar scoreboard (MixinGuiIngame) without the red score
// numbers on the right. The dark per-line background is optional via the "Transparent Background"
// toggle in this module's settings.
public class CleanScoreboard extends Module {
   public static boolean active;
   public static boolean transparentBg; // when true, the sidebar is drawn with no dark backing
   private boolean loaded;
   private boolean wasDown;

   private static final int TOG_DX = 10;
   private static final int TOG_DY = 30;
   private static final int TOG_W = 150;
   private static final int TOG_H = 14;

   public CleanScoreboard() {
      super("CleanScoreboard", "CleanScoreboard", Category.RENDER, true);
   }

   @Override
   public void onEnable() {
      this.load();
      active = true;
   }

   @Override
   public void onDisable() {
      active = false;
   }

   @Override
   public void onSettingsDrawScreen(int mouseX, int mouseY, int x, int y, int var5, int var6, int var7, int var8) {
      Minecraft mc = Minecraft.getMinecraft();
      mc.fontRendererObj.drawStringWithShadow("§7Scoreboard numbers are hidden while enabled.", (float) (x + TOG_DX), (float) (y + 18), -1);

      int tx = x + TOG_DX;
      int ty = y + TOG_DY;
      Gui.drawRect(tx, ty, tx + TOG_W, ty + TOG_H, transparentBg ? 0xFF335533 : 0xFF333333);
      mc.fontRendererObj.drawStringWithShadow(
         (transparentBg ? "§aTransparent Background ✔" : "§7Transparent Background ✘"),
         (float) (tx + 4), (float) (ty + 3), -1);

      // Reposition button -> opens the drag editor for the scoreboard offset
      int rx = x + TOG_DX;
      int ry = y + TOG_DY + TOG_H + 6;
      Gui.drawRect(rx, ry, rx + TOG_W, ry + TOG_H, 0xFF333355);
      mc.fontRendererObj.drawStringWithShadow("§bReposition Scoreboard ⤢", (float) (rx + 4), (float) (ry + 3), -1);

      boolean down = Mouse.isButtonDown(0);
      if (down && !this.wasDown && mouseX >= tx && mouseX <= tx + TOG_W && mouseY >= ty && mouseY <= ty + TOG_H) {
         transparentBg = !transparentBg;
         this.save();
      }
      if (down && !this.wasDown && mouseX >= rx && mouseX <= rx + TOG_W && mouseY >= ry && mouseY <= ry + TOG_H) {
         mc.displayGuiScreen(new me.txb1.extras.settings.anzeige.HudOffsetGui(
            mc.currentScreen, "scoreboard", "Scoreboard", 90, 70));
      }
      this.wasDown = down;

      super.onSettingsDrawScreen(mouseX, mouseY, x, y, var5, var6, var7, var8);
   }

   private void load() {
      if (this.loaded) {
         return;
      }
      try {
         Object o = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("cleanscoreboard_transparent");
         if (o != null) {
            transparentBg = Boolean.parseBoolean(String.valueOf(o));
         }
      } catch (Exception ignored) {
      }
      this.loaded = true;
   }

   private void save() {
      try {
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("cleanscoreboard_transparent", String.valueOf(transparentBg));
         EsdeathClient.getInstance().getFireDB().getDataBase().push();
      } catch (Exception ignored) {
      }
   }
}
