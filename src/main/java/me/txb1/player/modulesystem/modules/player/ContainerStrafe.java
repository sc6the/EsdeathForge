package me.txb1.player.modulesystem.modules.player;

import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;

// ContainerStrafe: front-end for the separately-loaded ContainerStrafe mod
// (com.example.containerstrafe.ContainerStrafeForge), which lets you keep strafing while a container /
// inventory GUI is open. Toggling this module flips that mod's INSTANCE.enabled flag and persists it
// via saveConfig(), all by reflection since ContainerStrafe is its own jar (not a compile dependency).
// No-ops gracefully if the mod isn't installed.
public class ContainerStrafe extends Module {
   private static final String CLS = "com.example.containerstrafe.ContainerStrafeForge";

   public ContainerStrafe() {
      super("ContainerStrafe", "ContainerStrafe", Category.PLAYER, false);
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
   private static java.lang.reflect.Field enabledF;
   private static java.lang.reflect.Method saveM;
   private static boolean resolved;
   private static boolean ok;

   private static void resolve() {
      if (resolved) {
         return;
      }
      resolved = true;
      try {
         Class<?> c = Class.forName(CLS);
         instanceF = c.getField("INSTANCE");
         enabledF = c.getField("enabled");
         saveM = c.getMethod("saveConfig");
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
         return enabledF.getBoolean(instanceF.get(null));
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
         enabledF.setBoolean(inst, on);
         saveM.invoke(inst);
      } catch (Throwable ignored) {
      }
   }
}
