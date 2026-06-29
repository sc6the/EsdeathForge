package me.txb1.player.modulesystem.modules.render;

import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;

// Perspective Mod: front-end for the separately-loaded PerspectiveMod v4
// (me.djtheredstoner.perspectivemod.PerspectiveMod), a free-look third-person camera bound to its
// own "Perspective" key. Toggling this module flips that mod's config.modEnabled master switch and
// persists it via config.save(), all by reflection since PerspectiveMod is its own jar (coremod +
// @Mod) and not a compile dependency. Disabling also drops out of any active perspective.
// No-ops gracefully if the mod isn't installed.
public class PerspectiveModule extends Module {
   private static final String CLS = "me.djtheredstoner.perspectivemod.PerspectiveMod";

   public PerspectiveModule() {
      super("Perspective Mod", "Perspective Mod", Category.RENDER, false);
   }

   // Read/write the bundled mod's live config directly instead of a local `toggled` field, so the
   // displayed state always matches reality and never desyncs across reboots / menu reopens (the
   // old `if (read()) toggle()` constructor flip double-toggled on any reconstruction).
   @Override
   public boolean isEnabled() {
      return read();
   }

   @Override
   public void toggle() {
      set(!read());
   }

   @Override
   public void onEnable() {
      set(true);
   }

   @Override
   public void onDisable() {
      set(false);
   }

   // reflection handles resolved once (isEnabled() runs per frame while the menu is open)
   private static java.lang.reflect.Field instanceF;
   private static java.lang.reflect.Field configF;
   private static java.lang.reflect.Field modEnabledF;
   private static java.lang.reflect.Field toggledF;
   private static java.lang.reflect.Method saveM;
   private static java.lang.reflect.Method resetM;
   private static boolean resolved;
   private static boolean ok;

   private static void resolve() {
      if (resolved) {
         return;
      }
      resolved = true;
      try {
         Class<?> c = Class.forName(CLS);
         instanceF = c.getField("instance");
         configF = c.getField("config");
         toggledF = c.getField("perspectiveToggled");
         resetM = c.getMethod("resetPerspective");
         Class<?> cfgC = configF.getType();
         modEnabledF = cfgC.getField("modEnabled");
         saveM = cfgC.getMethod("save");
         ok = true;
      } catch (Throwable t) {
         ok = false;
      }
   }

   private static boolean read() {
      resolve();
      if (!ok) {
         return false;
      }
      try {
         Object inst = instanceF.get(null);
         if (inst == null) {
            return false;
         }
         return modEnabledF.getBoolean(configF.get(inst));
      } catch (Throwable t) {
         return false;
      }
   }

   private static void set(boolean on) {
      resolve();
      if (!ok) {
         return;
      }
      try {
         Object inst = instanceF.get(null);
         if (inst == null) {
            return;
         }
         Object cfg = configF.get(inst);
         modEnabledF.setBoolean(cfg, on);
         saveM.invoke(cfg);
         // leaving perspective when turned off so the camera doesn't stay stuck in free-look
         if (!on && toggledF.getBoolean(inst)) {
            resetM.invoke(inst);
         }
      } catch (Throwable ignored) {
      }
   }
}
