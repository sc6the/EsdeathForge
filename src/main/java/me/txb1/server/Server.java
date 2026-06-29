package me.txb1.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import me.txb1.EsdeathClient;
import net.minecraft.client.Minecraft;

public class Server {
   private final int port;
   private String allcapes;
   private final String ip;

   public Integer getOnlinePlayers() {
      String var1 = this.getOnlinePlayerString();
      if (((var1) == null)) {
         return 0;
      } else {
         Integer var2 = 0;
         if (!(var1.equalsIgnoreCase(""))) {
            if ((var1.contains(","))) {
               var2 = var1.split(",").length;
               
            } else {
               var2 = 1;
            }
         }

         return var2;
      }
   }

   public String getPlayersOnServer(String var1) {
      return this.get(String.valueOf(new StringBuilder().append("getPlayersOnServer ").append(var1)));
   }

   public void refreshOnline() {
      this.info(
         String.valueOf(
            new StringBuilder()
               .append("join ")
               .append(Minecraft.getMinecraft().getSession().getToken())
               .append(" ")
               .append(Minecraft.getMinecraft().thePlayer.getName())
               .append(" ")
               .append(Minecraft.getMinecraft().thePlayer.getUniqueID().toString())
         )
      );
   }

   public void refreshStatus(String var1) {
      System.out.println("UPDATED STATUS");
      this.info(
         String.valueOf(
            new StringBuilder()
               .append("setStatus ")
               .append(Minecraft.getMinecraft().thePlayer.getUniqueID().toString())
               .append(" ")
               .append(var1)
         )
      );
      EsdeathClient.getInstance().getPlayer(Minecraft.getMinecraft().thePlayer.getUniqueID().toString()).loadStatus();
   }

   public void unlink() {
      EsdeathClient.getInstance()
         .getThreadHelper()
         .getThreadpool()
         .submit(
            () -> this.info(String.valueOf(new StringBuilder().append("unlink ").append(Minecraft.getMinecraft().thePlayer.getUniqueID().toString())))
         );
   }

   public void setCape(String var1) {
      this.info(
         String.valueOf(
            new StringBuilder()
               .append("setCape ")
               .append(Minecraft.getMinecraft().thePlayer.getUniqueID().toString())
               .append(" ")
               .append(var1)
         )
      );
   }

   public String getRank() {
      return this.getRank(Minecraft.getMinecraft().thePlayer.getUniqueID().toString());
   }

   public void sendServer(String var1) {
      if (!((Minecraft.getMinecraft().thePlayer.getUniqueID()) == null)) {
         String var2 = Minecraft.getMinecraft().thePlayer.getUniqueID().toString();
         this.info(String.valueOf(new StringBuilder().append("setPlayingOnServer ").append(var1.toLowerCase()).append(" ").append(var2)));
      }
   }

   public String getRank(String var1) {
      try {
         return this.get(String.valueOf(new StringBuilder().append("getRank ").append(var1)));
      } catch (Exception var3) {
         return "";
      }
   }

   public boolean hasCosmetic(String var1, String var2) {
      // OFFLINE PATCH: server is dead; cosmetics were free -> grant everything.
      return true;
   }

   public String getOnlinePlayerString() {
      try {
         String var1 = this.get("getPlayers");
         return ((var1) == null) ? "" : var1;
      } catch (Exception var2) {
         return "";
      }
   }

   public void removeCape() {
      this.info(String.valueOf(new StringBuilder().append("removeCape ").append(Minecraft.getMinecraft().thePlayer.getUniqueID().toString())));
   }

   public String getStatus(String var1) {
      String var2 = "";

      try {
         var2 = this.get(String.valueOf(new StringBuilder().append("getStatus ").append(var1)));
      } catch (Exception var4) {
         return var2;
      }

      return ((49 ^ 54) & ~(50 ^ 53)) != 0 ? null : var2;
   }

   // $VF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private void info(String var1) {
      // OFFLINE PATCH: no outbound calls. (was: socket to esdeath.de)
   }

   public boolean addCosmetic(String var1) {
      var1 = var1.toLowerCase();
      return Boolean.parseBoolean(
         this.get(
            String.valueOf(
               new StringBuilder()
                  .append("addCosmetic ")
                  .append(Minecraft.getMinecraft().thePlayer.getUniqueID().toString())
                  .append(" ")
                  .append(var1)
            )
         )
      );
   }

   public Server(String var1, Integer var2) {
      this.allcapes = "";
      this.ip = var1;
      this.port = var2;
   }

   public String linkAccount(String var1) {
      return this.get(
         String.valueOf(
            new StringBuilder()
               .append("link ")
               .append(Minecraft.getMinecraft().thePlayer.getUniqueID().toString())
               .append(" ")
               .append(var1)
         )
      );
   }

   public void setAnimatedCape(String var1) {
      this.info(
         String.valueOf(
            new StringBuilder()
               .append("setAnimatedCape ")
               .append(Minecraft.getMinecraft().thePlayer.getUniqueID().toString())
               .append(" ")
               .append(var1)
         )
      );
   }

   public ArrayList<String> getMyAnimatedCapes() {
      ArrayList var1 = new ArrayList();
      String var2 = this.get(String.valueOf(new StringBuilder().append("getAnimatedCapes ").append(Minecraft.getMinecraft().thePlayer.getUniqueID().toString())));
      System.out.println(var2);
      if (!(var2.equalsIgnoreCase(""))) {
         if ((var2.contains(","))) {
            var1.addAll(Arrays.asList(var2.split(",")));
            
         } else {
            var1.add(var2);
         }
      }

      return var1;
   }

   // $VF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public String getVersion() {
      // OFFLINE PATCH: report our own version so the client considers itself up to date.
      return EsdeathClient.getInstance().version;
   }

   public void removeCosmetic(String var1) {
      var1 = var1.toLowerCase();
      this.get(
         String.valueOf(
            new StringBuilder()
               .append("removeCosmetic ")
               .append(Minecraft.getMinecraft().thePlayer.getUniqueID().toString())
               .append(" ")
               .append(var1)
         )
      );
   }

   public String getAllCapes() {
      if ((this.allcapes.equalsIgnoreCase(""))) {
         this.allcapes = this.get("getAllCapes ");
      }

      return this.allcapes;
   }

   // $VF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private String get(String var1) {
      // OFFLINE PATCH: no network round-trip; callers handle the empty response.
      return "";
   }

}
