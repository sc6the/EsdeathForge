package me.txb1.player.modulesystem.modules.render;

import me.txb1.EsdeathClient;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

// CleanChat: smooth chat — when a new message arrives the chat slides in from the left instead of
// popping. MixinGuiNewChat reads `active`, `slideMs` (animation duration) and `lastMessage`.
public class CleanChat extends Module {
   public static boolean active;
   public static boolean removeBackground;  // hide the per-line chat background
   public static long lastMessage;        // ms timestamp of the last real (non-refresh) chat line
   public static int slideMs = 250;        // animation duration; lower = faster
   public static final int SLIDE_MIN = 60;
   public static final int SLIDE_MAX = 800;

   private boolean loaded;
   private boolean dragging;
   private boolean wasDown;
   private static final int TOG_DY = 50;
   private static final int TOG_W = 150;
   private static final int TOG_H = 14;

   private static final int SL_DX = 10;
   private static final int SL_DY = 30;
   private static final int SL_W = 150;
   private static final int SL_H = 10;
   private static final int SL_KNOB = 6;

   public CleanChat() {
      super("CleanChat", "CleanChat", Category.RENDER, true);
   }

   @Override
   public void onEnable() {
      ensureConfig();
      active = true;
   }

   @Override
   public void onDisable() {
      active = false;
   }

   @Override
   public void onSettingsDrawScreen(int mouseX, int mouseY, int x, int y, int var5, int var6, int var7, int var8) {
      Minecraft mc = Minecraft.getMinecraft();
      mc.fontRendererObj.drawStringWithShadow("§7Chat slide-in speed (lower = faster)", (float) (x + SL_DX), (float) (y + 18), -1);

      int sx = x + SL_DX;
      int sy = y + SL_DY;
      float frac = (float) (slideMs - SLIDE_MIN) / (float) (SLIDE_MAX - SLIDE_MIN);
      boolean down = Mouse.isButtonDown(0);
      boolean inTrack = mouseX >= sx && mouseX <= sx + SL_W && mouseY >= sy - 2 && mouseY <= sy + SL_H + 2;
      if (down && (this.dragging || inTrack)) {
         this.dragging = true;
         frac = (float) (mouseX - sx) / (float) (SL_W - SL_KNOB);
         frac = Math.max(0.0F, Math.min(1.0F, frac));
         slideMs = (int) (SLIDE_MIN + frac * (SLIDE_MAX - SLIDE_MIN));
      } else if (!down && this.dragging) {
         this.dragging = false;
         this.save();
      }
      frac = Math.max(0.0F, Math.min(1.0F, frac));

      Gui.drawRect(sx, sy, sx + SL_W, sy + SL_H, 0x80000000);
      Gui.drawRect(sx, sy, sx + SL_W, sy + 1, 0xFF555555);
      int knobX = sx + (int) (frac * (SL_W - SL_KNOB));
      Gui.drawRect(knobX, sy, knobX + SL_KNOB, sy + SL_H, 0xFFAAAAAA);
      mc.fontRendererObj.drawStringWithShadow(String.format("§7%dms", slideMs), (float) (sx + SL_W + 8), (float) (sy + 1), -1);

      // Remove Background toggle
      int tx = x + SL_DX;
      int ty = y + TOG_DY;
      Gui.drawRect(tx, ty, tx + TOG_W, ty + TOG_H, removeBackground ? 0xFF335533 : 0xFF333333);
      mc.fontRendererObj.drawStringWithShadow(
         (removeBackground ? "§aRemove Background ✔" : "§7Remove Background ✘"), (float) (tx + 4), (float) (ty + 3), -1);
      if (down && !this.wasDown && mouseX >= tx && mouseX <= tx + TOG_W && mouseY >= ty && mouseY <= ty + TOG_H) {
         removeBackground = !removeBackground;
         this.save();
      }

      // Reposition button -> opens the drag editor for the chat offset
      int rx = x + SL_DX;
      int ry = y + TOG_DY + TOG_H + 6;
      Gui.drawRect(rx, ry, rx + TOG_W, ry + TOG_H, 0xFF333355);
      mc.fontRendererObj.drawStringWithShadow("§bReposition Chat ⤢", (float) (rx + 4), (float) (ry + 3), -1);
      if (down && !this.wasDown && mouseX >= rx && mouseX <= rx + TOG_W && mouseY >= ry && mouseY <= ry + TOG_H) {
         mc.displayGuiScreen(new me.txb1.extras.settings.anzeige.HudOffsetGui(
            mc.currentScreen, "chat", "Chat", 160, 40));
      }
      this.wasDown = down;

      super.onSettingsDrawScreen(mouseX, mouseY, x, y, var5, var6, var7, var8);
   }

   // Loaded once (lazily) regardless of whether the module is enabled, so removeBackground/slideMs
   // reflect the saved settings even before/without enabling CleanChat.
   private static boolean configLoaded;

   public static void ensureConfig() {
      if (configLoaded) {
         return;
      }
      configLoaded = true;
      try {
         Object o = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("cleanchat_slidems");
         if (o != null) {
            slideMs = Integer.parseInt(String.valueOf(o));
         }
         Object b = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("cleanchat_nobg");
         if (b != null) {
            removeBackground = Boolean.parseBoolean(String.valueOf(b));
         }
      } catch (Exception ignored) {
      }
   }

   private void save() {
      try {
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("cleanchat_slidems", String.valueOf(slideMs));
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("cleanchat_nobg", String.valueOf(removeBackground));
         EsdeathClient.getInstance().getFireDB().getDataBase().push();
      } catch (Exception ignored) {
      }
   }
}
