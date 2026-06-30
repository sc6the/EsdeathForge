package me.txb1.player.modulesystem.modules.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

// Reads the sidebar scoreboard (used by the BedWars HUD utility modules to source live game state
// that Hypixel already exposes there). Lines keep their colour codes; index 0 = top line.
public final class ScoreboardUtil {
   private ScoreboardUtil() {
   }

   public static List<String> getSidebarLines() {
      List<String> out = new ArrayList<String>();
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.theWorld == null) {
         return out;
      }
      try {
         Scoreboard sb = mc.theWorld.getScoreboard();
         ScoreObjective obj = sb.getObjectiveInDisplaySlot(1); // 1 = sidebar
         if (obj == null) {
            return out;
         }
         Collection<Score> coll = sb.getSortedScores(obj);
         List<Score> scores = new ArrayList<Score>(coll);
         // Hypixel hides lines whose name starts with '#'; the sidebar renders highest score at top.
         for (int i = scores.size() - 1; i >= 0; i--) {
            Score s = scores.get(i);
            String name = s.getPlayerName();
            if (name != null && name.startsWith("#")) {
               continue;
            }
            ScorePlayerTeam team = sb.getPlayersTeam(name);
            out.add(ScorePlayerTeam.formatPlayerName(team, name));
         }
      } catch (Throwable ignored) {
      }
      return out;
   }

   public static String getTitle() {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.theWorld == null) {
         return "";
      }
      try {
         ScoreObjective obj = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
         return obj == null ? "" : obj.getDisplayName();
      } catch (Throwable t) {
         return "";
      }
   }

   public static String strip(String s) {
      return s == null ? "" : s.replaceAll("§.", "");
   }
}
