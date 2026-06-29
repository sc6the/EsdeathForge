package me.txb1.player.modulesystem.modules.player;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import me.txb1.player.events.EventUpdate;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import org.lwjgl.input.Keyboard;

public class ToggleSprint extends Module {

   public ToggleSprint() {
      super("ToggleSprint", "ToggleSprint", Category.PLAYER, false);
   }

   @Override
   public void onDisable() {
      EventManager.unregister(this);
   }

   @EventTarget
   public void onUpdate(EventUpdate var1) {
      if ((Keyboard.isKeyDown(17))) {
         mc.thePlayer.setSprinting(true);
      }
   }

   @Override
   public void onEnable() {
      EventManager.register(this);
   }
}
