package me.txb1.player.modulesystem.modules.render;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import me.txb1.EsdeathClient;
import me.txb1.player.events.EventUpdate;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

// TimeChanger: locks the client-side world time to a value chosen with the in-settings slider
// (0..24000; 6000 = noon, 18000 = midnight). Based on TimeChanger 1.8 — the lock is applied both per
// tick (EventUpdate) AND on each incoming time-update packet (MixinNetHandlerPlayClient), so the
// server's resync can't override it. Replaces 1.8's fixed day/night with a free slider.
public class TimeChanger extends Module {
   private static final long TIME_MAX = 24000L;
   // slider geometry, relative to the settings-panel top-left (x,y)
   private static final int SL_DX = 10;
   private static final int SL_DY = 40;
   private static final int SL_W = 150;
   private static final int SL_H = 12;
   private static final int SL_KNOB = 6;

   // static so MixinNetHandlerPlayClient can read them when a time-update packet arrives
   public static boolean active;
   public static long time = 6000L;
   private boolean loaded;
   private boolean dragging;

   public TimeChanger() {
      super("TimeChanger", "TimeChanger", Category.RENDER, true);
   }

   @Override
   public void onEnable() {
      this.loadTime();
      active = true;
      EventManager.register(this);
   }

   @Override
   public void onDisable() {
      active = false;
      EventManager.unregister(this);
   }

   @EventTarget
   public void onUpdate(EventUpdate var1) {
      if (active && mc.theWorld != null) {
         mc.theWorld.setWorldTime(time);
      }
   }

   @Override
   public void onSettingsDrawScreen(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      int sx = var3 + SL_DX;
      int sy = var4 + SL_DY;

      // drag handling (SettingsGui doesn't forward mouseClickMove, so poll the mouse here)
      boolean down = Mouse.isButtonDown(0);
      boolean inTrack = var1 >= sx && var1 <= sx + SL_W && var2 >= sy - 2 && var2 <= sy + SL_H + 2;
      if (down && (this.dragging || inTrack)) {
         this.dragging = true;
         float frac = (float)(var1 - sx) / (float)(SL_W - SL_KNOB);
         if (frac < 0.0F) {
            frac = 0.0F;
         } else if (frac > 1.0F) {
            frac = 1.0F;
         }
         time = (long)(frac * (float)TIME_MAX);
      } else if (!down && this.dragging) {
         this.dragging = false;
         this.saveTime();
      }

      // label
      Minecraft.getMinecraft().fontRendererObj
         .drawStringWithShadow(String.format("§7Time: §f%d §7(%s)", time, timeLabel(time)), (float)sx, (float)(var4 + 28), -1);
      // track + knob
      float frac = (float)time / (float)TIME_MAX;
      Gui.drawRect(sx, sy, sx + SL_W, sy + SL_H, 0x80000000);
      Gui.drawRect(sx, sy, sx + SL_W, sy + 1, 0xFF555555);
      int knobX = sx + (int)(frac * (SL_W - SL_KNOB));
      Gui.drawRect(knobX, sy, knobX + SL_KNOB, sy + SL_H, 0xFFAAAAAA);

      super.onSettingsDrawScreen(var1, var2, var3, var4, var5, var6, var7, var8);
   }

   private static String timeLabel(long t) {
      // 0 = 06:00 in MC ticks; convert ticks -> HH:MM (24h)
      long mins = (t / 1000L + 6L) % 24L * 60L + (t % 1000L) * 60L / 1000L;
      return String.format("%02d:%02d", mins / 60L, mins % 60L);
   }

   private void loadTime() {
      if (this.loaded) {
         return;
      }
      try {
         Object o = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("timechanger_time");
         if (o != null) {
            time = Long.parseLong(String.valueOf(o));
         }
      } catch (Exception ignored) {
      }
      this.loaded = true;
   }

   private void saveTime() {
      try {
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("timechanger_time", String.valueOf(time));
         EsdeathClient.getInstance().getFireDB().getDataBase().push();
      } catch (Exception ignored) {
      }
   }
}
