package me.txb1.player.modulesystem.modules.utils;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.util.HashMap;
import java.util.Map;
import me.txb1.extras.settings.anzeige.AnzeigeSettings;
import me.txb1.extras.settings.anzeige.Cordinates;
import me.txb1.player.events.EventRender;
import me.txb1.player.events.EventTick;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

// BedTracker: finds your bed (the nearest bed block), shows its status/distance on the HUD, and pings
// when an enemy comes within range of it. Positionable. (Bed-location detection is heuristic — the
// nearest bed to you — so re-toggle at your base if it picks the wrong one.)
public class BedTracker extends Module {
   public static boolean active;
   public static boolean showHud = true;
   public static int scale = 100;
   public static int textColor = 0xFFFFFF;
   public static int maxDistance = 40;
   public static boolean pingSound = true;

   private static BlockPos bed;
   private static boolean destroyed;
   private static long lastScan;
   private static long lastAlert;
   private static final Map<String, Long> perPlayer = new HashMap<String, Long>();

   public BedTracker() {
      super("BedTracker", "BedTracker", Category.UTILS, true);
   }

   @Override
   public boolean isHudElement() {
      return true;
   }

   @Override
   public net.minecraft.client.gui.GuiScreen getCustomSettingsGui() {
      return new UtilSettingsGui(this, new me.txb1.forge.gui.EsdeathIngameMenu())
         .toggle("Show HUD", () -> showHud, v -> showHud = v)
         .toggle("Ping Sound", () -> pingSound, v -> pingSound = v)
         .slider("Max Distance", 5, 100, () -> maxDistance, v -> maxDistance = v)
         .slider("Scale", 50, 300, () -> scale, v -> scale = v)
         .color("Text Color", () -> textColor, v -> textColor = v);
   }

   @Override
   public void onEnable() {
      active = true;
      bed = null;
      destroyed = false;
      EventManager.register(this);
   }

   @Override
   public void onDisable() {
      active = false;
      EventManager.unregister(this);
   }

   // find the nearest bed block within ~16 blocks (your base bed when you're standing near it)
   private static void findBed(Minecraft mc) {
      EntityPlayer p = mc.thePlayer;
      BlockPos base = new BlockPos(p);
      BlockPos best = null;
      double bestD = Double.MAX_VALUE;
      for (int dx = -16; dx <= 16; dx++) {
         for (int dy = -8; dy <= 8; dy++) {
            for (int dz = -16; dz <= 16; dz++) {
               BlockPos pos = base.add(dx, dy, dz);
               if (mc.theWorld.getBlockState(pos).getBlock() == Blocks.bed) {
                  double d = pos.distanceSq(base);
                  if (d < bestD) {
                     bestD = d;
                     best = pos;
                  }
               }
            }
         }
      }
      if (best != null) {
         bed = best;
         destroyed = false;
      }
   }

   // Only operate inside an actual BedWars GAME, not the lobby (which also has a "BED WARS" title).
   // The in-game sidebar additionally has team bed-status rows (✔ / ✘ icons) and/or an event-timer
   // line (Diamond/Emerald/Bed… with a MM:SS countdown) — none of which exist in the lobby.
   private static boolean inBedWars() {
      String title = ScoreboardUtil.strip(ScoreboardUtil.getTitle()).toUpperCase();
      if (!title.contains("BED WARS")) {
         return false;
      }
      for (String raw : ScoreboardUtil.getSidebarLines()) {
         String s = ScoreboardUtil.strip(raw);
         // bed-status glyphs: heavy/light check + cross
         if (s.indexOf('✔') >= 0 || s.indexOf('✘') >= 0
            || s.indexOf('✓') >= 0 || s.indexOf('✗') >= 0) {
            return true;
         }
         String l = s.toLowerCase();
         if (l.matches(".*\\d{1,2}:\\d{2}.*")
            && (l.contains("diamond") || l.contains("emerald") || l.contains("bed")
               || l.contains("sudden") || l.contains("game over"))) {
            return true;
         }
      }
      return false;
   }

   @EventTarget
   public void onTick(EventTick e) {
      if (!active) {
         return;
      }
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.thePlayer == null || mc.theWorld == null) {
         return;
      }
      if (!inBedWars()) {
         bed = null; // left the game / not in BedWars -> forget the bed so it re-scans next game
         destroyed = false;
         return;
      }
      long now = System.currentTimeMillis();
      if (bed == null && now - lastScan > 2000L) {
         lastScan = now;
         findBed(mc);
      }
      if (bed == null) {
         return;
      }
      // still a bed there?
      if (mc.theWorld.getBlockState(bed).getBlock() != Blocks.bed) {
         destroyed = true;
         return;
      }
      // enemy proximity alert
      for (Object o : mc.theWorld.playerEntities) {
         EntityPlayer pl = (EntityPlayer) o;
         if (pl == mc.thePlayer || pl.isInvisible()) {
            continue;
         }
         double d = Math.sqrt(pl.getDistanceSq(bed.getX() + 0.5, bed.getY() + 0.5, bed.getZ() + 0.5));
         if (d <= maxDistance) {
            Long last = perPlayer.get(pl.getName());
            if ((last == null || now - last > 4000L) && now - lastAlert > 1200L) {
               perPlayer.put(pl.getName(), now);
               lastAlert = now;
               mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                  "§b[BedWars] §f" + pl.getName() + " §7is near your bed §8(" + (int) d + "m)"));
               if (pingSound) {
                  mc.thePlayer.playSound("note.pling", 1.0F, 1.0F);
               }
            }
         }
      }
   }

   @EventTarget
   public void onRender(EventRender e) {
      if (!showHud) {
         return;
      }
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.thePlayer == null) {
         return;
      }
      // nothing until we're in a BedWars game and your bed has been detected
      if (bed == null || !inBedWars()) {
         return;
      }
      String text;
      if (destroyed || mc.theWorld.getBlockState(bed).getBlock() != Blocks.bed) {
         text = "Bed: Destroyed";
      } else {
         int dist = (int) Math.sqrt(mc.thePlayer.getDistanceSq(bed.getX() + 0.5, bed.getY() + 0.5, bed.getZ() + 0.5));
         text = "Bed: " + dist + "m";
      }
      Cordinates c = AnzeigeSettings.getCords(getName().toLowerCase());
      HudUtil.draw(java.util.Collections.singletonList(text), c.getX(), c.getY(), scale, textColor);
   }
}
