package me.txb1.forge.gui.server;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;

// Starts a connection while stashing the target server name + address so MixinGuiConnecting can show
// "Connecting to <name> (<address>)" with a shortened address.
public final class EsdeathConnect {
   public static volatile String pendingName;
   public static volatile String pendingIp;

   private EsdeathConnect() {
   }

   public static void connect(GuiScreen parent, String name, String ip) {
      pendingName = name;
      pendingIp = ip;
      Minecraft mc = Minecraft.getMinecraft();
      mc.displayGuiScreen(new GuiConnecting(parent, mc, new ServerData(name, ip, false)));
   }

   // Shorten a long address: keep the first chars + the TLD (.net/.com…) + the few chars right before
   // it, with the cut-out middle shown as "...". e.g. play.jaraxxor.network -> play.jar...rk.network
   public static String truncate(String address) {
      if (address == null) {
         return "";
      }
      if (address.length() < 16) {
         return address;
      }
      int dot = address.lastIndexOf('.');
      if (dot <= 0) {
         return address.substring(0, 8) + "..." + address.substring(address.length() - 4);
      }
      String tld = address.substring(dot);       // ".net" (may include :port)
      String main = address.substring(0, dot);   // "play.jaraxxor"
      int first = Math.min(8, main.length());
      int last = 2;
      if (main.length() <= first + last) {
         return address;
      }
      return main.substring(0, first) + "..." + main.substring(main.length() - last) + tld;
   }
}
