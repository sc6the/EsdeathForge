package me.txb1.player.modulesystem.modules.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Team;

// BedBreakSound: plays a custom sound (wav / ogg / mp3) when YOU break a bed, and a separate one when
// YOUR bed gets broken — each cut off after a configurable number of seconds. Detects the Hypixel
// "BED DESTRUCTION > <Team> Bed was destroyed by <player>!" broadcast: breaker == you -> break sound;
// destroyed team's colour == your team's colour -> bed-lost sound.
public class BedBreakSound extends Module {
   public static boolean active;
   public static String breakSound = "";
   public static String bedLostSound = "";
   public static int cutoffSeconds = 5;
   public static int fadeMs = 250;

   private static final Pattern BREAKER = Pattern.compile("destroyed by\\s+([A-Za-z0-9_]{1,16})", Pattern.CASE_INSENSITIVE);

   public BedBreakSound() {
      super("BedBreakSound", "BedBreakSound", Category.UTILS, true);
   }

   @Override
   public void onEnable() {
      active = true;
   }

   @Override
   public void onDisable() {
      active = false;
   }

   @Override
   public net.minecraft.client.gui.GuiScreen getCustomSettingsGui() {
      return new UtilSettingsGui(this, new me.txb1.forge.gui.EsdeathIngameMenu())
         .file("Break Sound", () -> breakSound, v -> breakSound = v)
         .button("Preview Break", () -> BedSoundPlayer.play(breakSound, cutoffSeconds, fadeMs))
         .file("Bed Lost Sound", () -> bedLostSound, v -> bedLostSound = v)
         .button("Preview Bed Lost", () -> BedSoundPlayer.play(bedLostSound, cutoffSeconds, fadeMs))
         .slider("Cutoff (s)", 1, 30, () -> cutoffSeconds, v -> cutoffSeconds = v)
         .slider("Fade (ms)", 0, 2000, () -> fadeMs, v -> fadeMs = v);
   }

   // formatted = the chat line WITH § colour codes (needed to read the destroyed team's colour)
   public static void onChat(String formatted) {
      if (!active || formatted == null) {
         return;
      }
      String plain = formatted.replaceAll("§.", "");
      String lower = plain.toLowerCase();
      if (!lower.contains("destroyed by") || !lower.contains("bed")) {
         return;
      }
      Matcher m = BREAKER.matcher(plain);
      if (!m.find()) {
         return;
      }
      String breaker = m.group(1);
      Minecraft mc = Minecraft.getMinecraft();
      String you = mc.getSession() != null ? mc.getSession().getUsername() : "";

      if (breaker.equalsIgnoreCase(you)) {
         BedSoundPlayer.play(breakSound, cutoffSeconds, fadeMs);
      } else if (isMyTeam(formatted)) {
         BedSoundPlayer.play(bedLostSound, cutoffSeconds, fadeMs);
      }
   }

   // colour of the team whose bed was destroyed (from the message) vs your own team colour
   private static boolean isMyTeam(String formatted) {
      char msgColor = colorAfter(formatted, "DESTRUCTION");
      char myColor = myTeamColor();
      return msgColor != 0 && msgColor == myColor;
   }

   private static char myTeamColor() {
      try {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.thePlayer == null) {
            return 0;
         }
         Team team = mc.thePlayer.getTeam();
         if (team == null) {
            return 0;
         }
         return colorIn(team.formatString("")); // applies the team's coloured prefix to an empty string
      } catch (Throwable t) {
         return 0;
      }
   }

   // first colour code (§ + 0-9a-f) appearing after the given marker
   private static char colorAfter(String s, String marker) {
      int i = s.indexOf(marker);
      return i < 0 ? 0 : colorIn(s.substring(i + marker.length()));
   }

   private static char colorIn(String s) {
      if (s == null) {
         return 0;
      }
      for (int i = 0; i + 1 < s.length(); i++) {
         if (s.charAt(i) == '§') {
            char c = Character.toLowerCase(s.charAt(i + 1));
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')) {
               return c;
            }
         }
      }
      return 0;
   }
}
