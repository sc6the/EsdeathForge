package me.txb1.player.modulesystem.modules.utils;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import me.txb1.extras.settings.anzeige.AnzeigeSettings;
import me.txb1.extras.settings.anzeige.Cordinates;
import me.txb1.player.events.EventRender;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;

// UpgradeHud: HUD listing your team's purchased BedWars upgrades. Detected from the team-broadcast
// "<player> purchased <upgrade>" chat lines; reset at the start of each game. Positionable.
public class UpgradeHud extends Module {
   public static boolean active;
   public static boolean showHud = true;
   public static boolean shortNames = false;
   public static int scale = 100;
   public static int textColor = 0x55FFFF;
   public static boolean showSharp = true, showProt = true, showTraps = true, showForge = true, showHeal = true;

   private static boolean sharp;
   private static int prot;          // 0..4
   private static String forge = "";  // Iron / Golden / Emerald / Molten
   private static boolean healPool;
   private static boolean haste;
   private static final Set<String> traps = new LinkedHashSet<String>();

   public UpgradeHud() {
      super("UpgradeHud", "UpgradeHud", Category.UTILS, true);
   }

   @Override
   public boolean isHudElement() {
      return true;
   }

   @Override
   public net.minecraft.client.gui.GuiScreen getCustomSettingsGui() {
      return new UtilSettingsGui(this, new me.txb1.forge.gui.EsdeathIngameMenu())
         .toggle("Show HUD", () -> showHud, v -> showHud = v)
         .toggle("Short Names", () -> shortNames, v -> shortNames = v)
         .slider("Scale", 50, 300, () -> scale, v -> scale = v)
         .toggle("Sharpness", () -> showSharp, v -> showSharp = v)
         .toggle("Protection", () -> showProt, v -> showProt = v)
         .toggle("Traps", () -> showTraps, v -> showTraps = v)
         .toggle("Heal Pool", () -> showHeal, v -> showHeal = v)
         .toggle("Forge", () -> showForge, v -> showForge = v)
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

   private static void reset() {
      sharp = false;
      prot = 0;
      forge = "";
      healPool = false;
      haste = false;
      traps.clear();
   }

   public static void onChat(String msg) {
      if (!active || msg == null) {
         return;
      }
      String l = msg.toLowerCase();
      if (l.contains("protect your bed")) { // new game
         reset();
         return;
      }
      if (!l.contains("purchased")) {
         return;
      }
      if (l.contains("sharpened swords")) {
         sharp = true;
      }
      if (l.contains("reinforced armor")) {
         prot = Math.max(prot, romanLevel(l));
      }
      if (l.contains("iron forge")) {
         forge = "Iron";
      } else if (l.contains("golden forge")) {
         forge = "Golden";
      } else if (l.contains("emerald forge")) {
         forge = "Emerald";
      } else if (l.contains("molten forge")) {
         forge = "Molten";
      }
      if (l.contains("heal pool")) {
         healPool = true;
      }
      if (l.contains("maniac miner")) {
         haste = true;
      }
      if (l.contains("trap")) {
         if (l.contains("it's a trap") || l.contains("its a trap")) {
            traps.add("Trap");
         } else if (l.contains("counter")) {
            traps.add("Counter");
         } else if (l.contains("alarm")) {
            traps.add("Alarm");
         } else if (l.contains("miner fatigue")) {
            traps.add("MinerFatigue");
         }
      }
   }

   private static int romanLevel(String l) {
      if (l.contains(" iv")) {
         return 4;
      }
      if (l.contains(" iii")) {
         return 3;
      }
      if (l.contains(" ii")) {
         return 2;
      }
      if (l.contains(" i")) {
         return 1;
      }
      return 1;
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
      List<String> lines = new ArrayList<String>();
      if (showSharp && sharp) {
         lines.add(shortNames ? "Sharp" : "Sharpness");
      }
      if (showProt && prot > 0) {
         lines.add((shortNames ? "Prot " : "Protection ") + roman(prot));
      }
      if (showHeal && haste) {
         lines.add(shortNames ? "Haste" : "Maniac Miner");
      }
      if (showHeal && healPool) {
         lines.add(shortNames ? "Heal" : "Heal Pool");
      }
      if (showForge && !forge.isEmpty()) {
         lines.add((shortNames ? "Forge " : "Forge: ") + forge);
      }
      if (showTraps) {
         for (String t : traps) {
            lines.add(t);
         }
      }
      Cordinates c = AnzeigeSettings.getCords(getName().toLowerCase());
      HudUtil.draw(lines, c.getX(), c.getY(), scale, textColor);
   }

   private static String roman(int n) {
      switch (n) {
         case 4: return "IV";
         case 3: return "III";
         case 2: return "II";
         default: return "I";
      }
   }
}
