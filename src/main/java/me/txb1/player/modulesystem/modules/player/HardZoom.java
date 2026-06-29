package me.txb1.player.modulesystem.modules.player;

import com.darkmagician6.eventapi.EventManager;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;

// HardZoom: a fixed, instant ("hard", no smooth tween) zoom. While the module is enabled, holding the
// Hard Zoom key (EsdeathForgeMod.HARD_ZOOM, default V — deliberately NOT OptiFine's C) scales the FOV
// down. The actual scaling is applied in ForgeEventBridge via Forge's EntityViewRenderEvent.FOVModifier,
// which fires AFTER OptiFine has applied its own zoom, so the two stack and never fight each other.
public class HardZoom extends Module {
   public static boolean active;
   public static final float DEFAULT = 0.25F; // 4x
   public static float zoom = DEFAULT; // FOV multiplier while the key is held (0.25 = 4x). Lower = closer.

   public HardZoom() {
      super("HardZoom", "HardZoom", Category.PLAYER, false);
   }

   @Override
   public void onEnable() {
      active = true;
      EventManager.register(this);
   }

   @Override
   public void onDisable() {
      active = false;
      EventManager.unregister(this);
   }
}
