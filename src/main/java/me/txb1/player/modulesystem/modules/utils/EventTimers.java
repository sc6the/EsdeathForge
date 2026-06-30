package me.txb1.player.modulesystem.modules.utils;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.util.List;
import java.util.regex.Pattern;
import me.txb1.extras.settings.anzeige.AnzeigeSettings;
import me.txb1.extras.settings.anzeige.Cordinates;
import me.txb1.player.events.EventRender;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;

// EventTimers: HUD showing the next BedWars event countdown. Hypixel already exposes the upcoming
// event + timer on the sidebar scoreboard; we pull the line that contains a MM:SS countdown next to a
// known event keyword and render it cleanly. Positionable like other HUD elements.
public class EventTimers extends Module {
   public static boolean active;
   public static boolean showHud = true;
   public static int scale = 100;
   public static int textColor = 0x55FFFF;
   private static final Pattern TIME = Pattern.compile("\\d{1,2}:\\d{2}");

   public EventTimers() {
      super("EventTimers", "EventTimers", Category.UTILS, true);
   }

   @Override
   public boolean isHudElement() {
      return true;
   }

   @Override
   public net.minecraft.client.gui.GuiScreen getCustomSettingsGui() {
      return new UtilSettingsGui(this, new me.txb1.forge.gui.EsdeathIngameMenu())
         .toggle("Show HUD", () -> showHud, v -> showHud = v)
         .slider("Scale", 50, 300, () -> scale, v -> scale = v)
         .color("Text Color", () -> textColor, v -> textColor = v);
   }

   @Override
   public void onEnable() {
      active = true;
      EventManager.register(this);
   }

   @Override
   public void onDisable() {
      active = false;
      EventManager.unregister(this);
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
      String line = findEventLine();
      if (line == null) {
         return;
      }
      Cordinates c = AnzeigeSettings.getCords(getName().toLowerCase());
      HudUtil.draw(java.util.Collections.singletonList(line), c.getX(), c.getY(), scale, textColor);
   }

   // the sidebar line describing the next event (Diamond/Emerald upgrade, bed destruction, game end…)
   private static String findEventLine() {
      List<String> lines = ScoreboardUtil.getSidebarLines();
      for (String raw : lines) {
         String s = ScoreboardUtil.strip(raw).trim();
         String l = s.toLowerCase();
         if (!TIME.matcher(s).find()) {
            continue;
         }
         if (l.contains("diamond") || l.contains("emerald") || l.contains("bed")
            || l.contains("game") || l.contains("sudden") || l.contains("death") || l.contains("event")) {
            return s;
         }
      }
      return null;
   }
}
