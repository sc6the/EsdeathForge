package me.txb1.player.modulesystem.modules.render;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import me.txb1.player.events.EventUpdate;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.gui.GuiScreen;

// Fullbright: front-end for the bundled io.armandukx.fullbright mod. Toggling this module flips that
// mod's config.enabled (its GUI/config persist) AND forces the gamma directly here every tick —
// the bundled mod's own RenderTickEvent listener doesn't reliably win against OptiFine, so we drive
// the gamma ourselves too. Restores the previous gamma on disable.
public class Fullbright extends Module {
   private float previous = 1.0F;

   public Fullbright() {
      super("Fullbright", "Fullbright", Category.RENDER, true);
   }

   @Override
   public void onEnable() {
      this.previous = mc.gameSettings.gammaSetting;
      setBundledEnabled(true);
      EventManager.register(this);
   }

   @Override
   public void onDisable() {
      setBundledEnabled(false);
      mc.gameSettings.gammaSetting = this.previous;
      EventManager.unregister(this);
   }

   @EventTarget
   public void onUpdate(EventUpdate var1) {
      mc.gameSettings.gammaSetting = 10000000.0F; // matches the bundled mod's fullbright gamma
   }

   private static void setBundledEnabled(boolean on) {
      try {
         if (io.armandukx.fullbright.Fullbright.config != null) {
            io.armandukx.fullbright.Fullbright.config.enabled = on;
         }
      } catch (Throwable ignored) {
      }
   }

   // Called once after module toggle-states are restored at boot. The bundled io.armandukx.fullbright
   // mod persists its OWN config.enabled (defaults ON), so without this it forces fullbright every
   // tick until the module is toggled on then off. Reconcile the bundled flag to the module's real
   // state so a disabled module means fullbright is actually off from boot.
   public static void reconcileBundled(boolean moduleEnabled) {
      setBundledEnabled(moduleEnabled);
   }

   @Override
   public GuiScreen getCustomSettingsGui() {
      try {
         return new io.armandukx.fullbright.gui.SettingsGui();
      } catch (Throwable t) {
         return null;
      }
   }
}
