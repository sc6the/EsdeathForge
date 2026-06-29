package me.txb1.player.modulesystem.modules.render;

import com.darkmagician6.eventapi.EventManager;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;

public class ScoreBoard extends Module {

   @Override
   public void onEnable() {
      EventManager.register(this);
   }

   public ScoreBoard() {
      super("ScoreBoard", "ScoreBoard", Category.RENDER, false);
   }

   @Override
   public void onDisable() {
      EventManager.unregister(this);
   }

}
