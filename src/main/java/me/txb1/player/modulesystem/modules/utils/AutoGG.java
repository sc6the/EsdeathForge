package me.txb1.player.modulesystem.modules.utils;

import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;

// AutoGG: sends "gg" shortly after a game ends on Hypixel (detected from the end-of-game chat lines).
// Debounced so it only fires once per game.
public class AutoGG extends Module {
   public static boolean active;
   public static String message = "gg";
   public static int delaySeconds = 1;
   private static boolean pending;
   private static long fireAt;
   private static long lastFired;

   public AutoGG() {
      super("AutoGG", "AutoGG", Category.UTILS, true);
   }

   @Override
   public net.minecraft.client.gui.GuiScreen getCustomSettingsGui() {
      return new UtilSettingsGui(this, new me.txb1.forge.gui.EsdeathIngameMenu())
         .text("Message", () -> message, v -> message = v)
         .slider("Delay (s)", 0, 10, () -> delaySeconds, v -> delaySeconds = v);
   }

   @Override
   public void onEnable() {
      active = true;
   }

   @Override
   public void onDisable() {
      active = false;
      pending = false;
   }

   // Hypixel end-of-game lines (BedWars / SkyWars / Duels / general).
   public static void onChat(String msg) {
      if (!active || msg == null) {
         return;
      }
      String l = msg.toLowerCase();
      boolean gameOver = l.contains("1st killer") || l.contains("winner:") || l.contains("game over")
         || l.contains(" won the game") || l.contains("1st place")
         || l.contains("victory!") || l.contains("game over!");
      if (gameOver) {
         long now = System.currentTimeMillis();
         if (now - lastFired > 8000L) { // debounce: one message per game
            pending = true;
            fireAt = now + Math.max(0, delaySeconds) * 1000L;
            lastFired = now;
         }
      }
   }

   public static void tick() {
      if (pending && System.currentTimeMillis() >= fireAt) {
         pending = false;
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.thePlayer != null && message != null && !message.trim().isEmpty()) {
            mc.thePlayer.sendChatMessage(message.trim());
         }
      }
   }
}
