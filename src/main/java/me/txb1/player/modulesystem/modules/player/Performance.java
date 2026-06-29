package me.txb1.player.modulesystem.modules.player;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import me.txb1.player.events.EventTick;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;

public class Performance extends Module {

   @Override
   public void onEnable() {
      EventManager.register(this);
   }

   private static int om6(double var0, double var2) {
      double var4;
      return (var4 = var0 - var2) == 0.0 ? 0 : (var4 < 0.0 ? -1 : 1);
   }

   public Performance() {
      super("Performance+", "Performance+", Category.PLAYER, false);
   }

   @EventTarget
   public void onTick(EventTick var1) {
      mc.thePlayer.worldObj.loadedEntityList.forEach(var0 -> {
         if (((om6(var0.getPosition().distanceSq(mc.thePlayer.getPosition()), 10.0)) > 0)) {
            mc.thePlayer.worldObj.removeEntity(var0);
         }
      });
   }

   @Override
   public void onDisable() {
      EventManager.unregister(this);
   }
}
