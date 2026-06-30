package me.txb1.player.modulesystem.modules.utils;

import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;

// NullMove ("snap tap"): when two opposite movement keys are held at once, don't cancel out — move in
// the direction of the most recently pressed key. The decision is applied in MixinMovementInputFromOptions.
public class NullMove extends Module {
   public static boolean active;
   public static boolean forwardLatest = true; // W pressed more recently than S
   public static boolean leftLatest = true;    // A pressed more recently than D
   private static boolean prevF, prevB, prevL, prevR;

   public NullMove() {
      super("NullMove", "NullMove", Category.UTILS, false);
   }

   @Override
   public void onEnable() {
      active = true;
   }

   @Override
   public void onDisable() {
      active = false;
   }

   // fed the current key states each movement tick; tracks which key of each opposite pair was the
   // latest to transition to "down".
   public static void update(boolean f, boolean b, boolean l, boolean r) {
      if (f && !prevF) {
         forwardLatest = true;
      }
      if (b && !prevB) {
         forwardLatest = false;
      }
      if (l && !prevL) {
         leftLatest = true;
      }
      if (r && !prevR) {
         leftLatest = false;
      }
      prevF = f;
      prevB = b;
      prevL = l;
      prevR = r;
   }
}
