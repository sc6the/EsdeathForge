package me.txb1.forge.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import me.txb1.extras.settings.anzeige.AnzeigeSettings;
import me.txb1.extras.settings.anzeige.Cordinates;
import me.txb1.player.modulesystem.modules.render.CleanScoreboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScorePlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// CleanScoreboard: replace the vanilla sidebar render with one that omits the red score numbers
// (and, optionally, the dark per-line background). Faithful to the vanilla layout otherwise.
@Mixin(GuiIngame.class)
public class MixinGuiIngame {

   @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
   private void esdeath$cleanScoreboard(ScoreObjective objective, ScaledResolution res, CallbackInfo ci) {
      if (!CleanScoreboard.active) {
         return;
      }
      ci.cancel();
      Minecraft mc = Minecraft.getMinecraft();
      FontRenderer font = mc.fontRendererObj;
      Scoreboard scoreboard = objective.getScoreboard();

      // draggable offset (set via HudOffsetGui, key "scoreboard")
      Cordinates sbOff = AnzeigeSettings.getCords("scoreboard");
      GlStateManager.pushMatrix();
      GlStateManager.translate((float) sbOff.getX(), (float) sbOff.getY(), 0.0F);

      Collection<Score> all = scoreboard.getSortedScores(objective);
      List<Score> filtered = new ArrayList<Score>();
      for (Score s : all) {
         if (s.getPlayerName() != null && !s.getPlayerName().startsWith("#")) {
            filtered.add(s);
         }
      }
      List<Score> scores = filtered.size() > 15
         ? filtered.subList(filtered.size() - 15, filtered.size())
         : filtered;

      // width = widest of (title, each name) — note: no ": <number>" included.
      int width = font.getStringWidth(objective.getDisplayName());
      for (Score s : scores) {
         ScorePlayerTeam team = scoreboard.getPlayersTeam(s.getPlayerName());
         width = Math.max(width, font.getStringWidth(ScorePlayerTeam.formatPlayerName(team, s.getPlayerName())));
      }

      int count = scores.size();
      int totalH = count * font.FONT_HEIGHT;
      int yMid = res.getScaledHeight() / 2 + totalH / 2;
      int pad = 3;
      int xLeft = res.getScaledWidth() - width - pad;
      int bgRight = res.getScaledWidth() - pad + 2;
      boolean drawBg = !CleanScoreboard.transparentBg;

      int line = 0;
      for (Score s : scores) {
         ++line;
         ScorePlayerTeam team = scoreboard.getPlayersTeam(s.getPlayerName());
         String name = ScorePlayerTeam.formatPlayerName(team, s.getPlayerName());
         int y = yMid - line * font.FONT_HEIGHT;
         if (drawBg) {
            Gui.drawRect(xLeft - 2, y, bgRight, y + font.FONT_HEIGHT, 0x33000000);
         }
         font.drawString(name, xLeft, y, 0xFFFFFF);
         if (line == count) {
            String title = objective.getDisplayName();
            if (drawBg) {
               Gui.drawRect(xLeft - 2, y - font.FONT_HEIGHT - 1, bgRight, y - 1, 0x55000000);
               Gui.drawRect(xLeft - 2, y - 1, bgRight, y, 0x33000000);
            }
            font.drawString(title, xLeft + width / 2 - font.getStringWidth(title) / 2, y - font.FONT_HEIGHT, 0xFFFFFF);
         }
      }
      GlStateManager.popMatrix();
   }
}
