package me.txb1.player.modulesystem.modules.utils;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import me.txb1.extras.settings.anzeige.AnzeigeSettings;
import me.txb1.extras.settings.anzeige.Cordinates;
import me.txb1.player.events.EventRender;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;

// HeightOverlay: HUD showing your current block height (Y). Optional max-height reference colours the
// number as you approach it (handy for BedWars build limits). Positionable like other HUD elements.
public class HeightOverlay extends Module {
   public static boolean active;
   public static boolean showHud = true;
   public static int scale = 100;
   public static int textColor = 0xFFFFFF;
   public static int maxHeight = 0; // 0 = no limit reference

   public HeightOverlay() {
      super("HeightOverlay", "HeightOverlay", Category.UTILS, true);
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
         .slider("Max Height", 0, 256, () -> maxHeight, v -> maxHeight = v)
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
         return; // HUD toggled off -> draw nothing (module logic still runs)
      }
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.thePlayer == null) {
         return;
      }
      int y = (int) Math.floor(mc.thePlayer.posY);
      String text = maxHeight > 0 ? "Height: " + y + " / " + maxHeight : "Height: " + y;
      Cordinates c = AnzeigeSettings.getCords(getName().toLowerCase());
      HudUtil.draw(java.util.Collections.singletonList(text), c.getX(), c.getY(), scale, textColor);
   }
}
