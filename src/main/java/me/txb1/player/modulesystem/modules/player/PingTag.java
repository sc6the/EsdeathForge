package me.txb1.player.modulesystem.modules.player;

import com.darkmagician6.eventapi.EventManager;
import java.util.HashMap;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.network.NetworkPlayerInfo;

public class PingTag extends Module {
   public static HashMap<NetworkPlayerInfo, Integer> ping = new HashMap<>();

   @Override
   public void onEnable() {
      EventManager.register(this);
   }

   @Override
   public void onDisable() {
      EventManager.unregister(this);
   }

   public PingTag() {
      super("PingTag", "PingTag", Category.PLAYER, false);
   }
}
