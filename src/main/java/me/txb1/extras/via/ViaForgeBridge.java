package me.txb1.extras.via;

import net.minecraft.client.gui.GuiScreen;

// Reflective bridge to the (separately-installed) ViaForge mod. ViaForge normally injects its
// protocol-selector button into the vanilla multiplayer screen via a mixin, but Esdeath replaces
// that screen with EsdeathServerListGui, so the button never appears. We re-add it ourselves here.
// All access is reflective so EsdeathForge builds and runs whether or not ViaForge is installed.
public final class ViaForgeBridge {
   private ViaForgeBridge() {
   }

   private static Boolean present;

   public static boolean isPresent() {
      if (present == null) {
         try {
            Class.forName("viaforge.gui.GuiProtocolSelector");
            Class.forName("de.florianmichael.vialoadingbase.ViaLoadingBase");
            present = Boolean.TRUE;
         } catch (Throwable t) {
            present = Boolean.FALSE;
         }
      }
      return present;
   }

   // Current target protocol version name (e.g. "1.20.4"), or "" if unavailable.
   public static String targetVersionName() {
      if (!isPresent()) {
         return "";
      }
      try {
         Class<?> vlb = Class.forName("de.florianmichael.vialoadingbase.ViaLoadingBase");
         Object inst = vlb.getMethod("getInstance").invoke(null);
         if (inst == null) {
            return "";
         }
         Object ver = vlb.getMethod("getTargetVersion").invoke(inst);
         if (ver == null) {
            return "";
         }
         Object name = ver.getClass().getMethod("getName").invoke(ver);
         return name == null ? "" : String.valueOf(name);
      } catch (Throwable t) {
         return "";
      }
   }

   // Open ViaForge's protocol selector with the given parent screen. Returns false if it couldn't.
   public static boolean openSelector(GuiScreen parent) {
      if (!isPresent()) {
         return false;
      }
      try {
         Class<?> gui = Class.forName("viaforge.gui.GuiProtocolSelector");
         GuiScreen screen = (GuiScreen) gui.getConstructor(GuiScreen.class).newInstance(parent);
         net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(screen);
         return true;
      } catch (Throwable t) {
         return false;
      }
   }
}
