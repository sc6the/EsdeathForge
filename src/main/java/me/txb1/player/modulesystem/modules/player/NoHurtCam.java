package me.txb1.player.modulesystem.modules.player;

import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;

// NoHurtCam: suppresses the damage camera tilt/shake. The actual suppression is an
// ASM head-gate injected into EntityRenderer.hurtCameraEffect (patch-asm/gates.tsv)
// that returns early when `active` is true.
public class NoHurtCam extends Module {
   public static boolean active = false;

   public NoHurtCam() {
      super("NoHurtCam", "NoHurtCam", Category.RENDER, false);
   }

   @Override
   public void onEnable() {
      active = true;
   }

   @Override
   public void onDisable() {
      active = false;
   }
}
