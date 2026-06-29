package me.txb1.player.modulesystem.modules.player;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import me.txb1.EsdeathClient;
import me.txb1.player.events.EventTick;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;

public class Connector extends Module {
   int dreset;
   int i;
   int d;

   @EventTarget
   public void onTick(EventTick var1) {
      this.d = this.d + 1;
      if (((this.d) > (350))) {
         this.d = 0;
         this.dostuff();
      }
   }

   @Override
   public void onDisable() {
      this.i = 0;
      EventManager.unregister(this);
   }

   private void dostuff() {
      EsdeathClient.getInstance().getThreadHelper().getThreadpool().submit(() -> {
         if (((mc.theWorld) != null) && ((mc.thePlayer) != null)) {
            EsdeathClient.getInstance().getServer().refreshOnline();
            label38:
            if ((mc.thePlayer.isServerWorld())) {
               try {
                  String var0 = mc.getCurrentServerData().serverIP.toLowerCase();
                  EsdeathClient.getInstance().getServer().sendServer(var0);
               } catch (Exception var5) {
                  break label38;
               }

            }
         }

         EsdeathClient.getInstance().getValuesInteger().put("online", EsdeathClient.getInstance().getServer().getOnlinePlayers());
         EsdeathClient.getInstance().getPlayers().clear();
         String var6 = EsdeathClient.getInstance().getServer().getOnlinePlayerString();
         if ((var6.contains(","))) {
            String[] var1 = var6.split(",");
            int var2 = var1.length;
            int var3 = 0;

            while (((var3) < (var2))) {
               String var4 = var1[var3];
               EsdeathClient.getInstance().getPlayers().add(var4);
               var3++;
               
            }

         } else if (!(var6.equalsIgnoreCase(""))) {
            EsdeathClient.getInstance().getPlayers().add(var6);
         }
      });
   }

   @Override
   public void onEnable() {
      this.i = 1;
      this.dostuff();
      EventManager.register(this);
   }

   public Connector() {
      super("Connector", "Connector", Category.PLAYER, false);
      this.i = 0;
      this.d = 0;
      this.dreset = 0;
   }

}
