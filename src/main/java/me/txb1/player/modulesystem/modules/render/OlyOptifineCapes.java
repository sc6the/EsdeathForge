package me.txb1.player.modulesystem.modules.render;

import com.darkmagician6.eventapi.EventManager;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;

public class OlyOptifineCapes extends Module {

   @Override
   public void onDisable() {
      EventManager.unregister(this);
   }

   public OlyOptifineCapes() {
      super("OnlyOptifineCapes", "OnlyOptifineCapes", Category.RENDER, false);
   }

   @Override
   public void onEnable() {
      EventManager.register(this);
   }

}
