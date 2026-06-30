package me.txb1.player.modulesystem.modules.utils;

import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

// AutoTip: runs "/tip all" on Hypixel every few minutes for network XP. Optionally hides the tip
// confirmation messages from chat.
public class AutoTip extends Module {
   public static boolean active;
   public static int delayMinutes = 5;
   public static boolean hideMessages = true;
   private static long lastTip;

   public AutoTip() {
      super("AutoTip", "AutoTip", Category.UTILS, true);
   }

   @Override
   public net.minecraft.client.gui.GuiScreen getCustomSettingsGui() {
      return new me.txb1.player.modulesystem.modules.utils.UtilSettingsGui(this, new me.txb1.forge.gui.EsdeathIngameMenu())
         .toggle("Hide Messages", () -> hideMessages, v -> hideMessages = v)
         .slider("Delay (min)", 1, 30, () -> delayMinutes, v -> delayMinutes = v);
   }

   @Override
   public void onEnable() {
      active = true;
      lastTip = System.currentTimeMillis(); // wait a full interval before the first tip
   }

   @Override
   public void onDisable() {
      active = false;
   }

   public static void tick() {
      if (!active) {
         return;
      }
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.thePlayer == null) {
         return;
      }
      ServerData sd = mc.getCurrentServerData();
      if (sd == null || sd.serverIP == null || !sd.serverIP.toLowerCase().contains("hypixel")) {
         return;
      }
      long now = System.currentTimeMillis();
      if (now - lastTip >= Math.max(1, delayMinutes) * 60_000L) {
         lastTip = now;
         mc.thePlayer.sendChatMessage("/tip all");
      }
   }

   public static boolean shouldHide() {
      return active && hideMessages;
   }

   public static boolean isTipMessage(String s) {
      if (s == null) {
         return false;
      }
      String l = s.toLowerCase();
      return l.contains("you tipped") || l.contains("tipped you")
         || (l.contains("tip") && l.contains("network experience"));
   }
}
