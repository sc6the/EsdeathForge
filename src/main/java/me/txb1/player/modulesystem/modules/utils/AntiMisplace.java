package me.txb1.player.modulesystem.modules.utils;

import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

// AntiMisplace: cancels your block placement if you try to place obsidian somewhere that isn't next to
// a bed — so a misclick can't waste your obsidian away from your base. The placement is intercepted in
// ForgeEventBridge#onInteract (PlayerInteractEvent), which calls shouldCancel().
public class AntiMisplace extends Module {
   public static boolean active;

   public AntiMisplace() {
      super("AntiMisplace", "AntiMisplace", Category.UTILS, false);
   }

   @Override
   public void onEnable() {
      active = true;
   }

   @Override
   public void onDisable() {
      active = false;
   }

   // true if this right-click block placement should be blocked (holding obsidian, not near a bed)
   public static boolean shouldCancel(BlockPos clicked, EnumFacing face) {
      if (!active || clicked == null || face == null) {
         return false;
      }
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.thePlayer == null || mc.theWorld == null) {
         return false;
      }
      ItemStack held = mc.thePlayer.getHeldItem();
      if (held == null || held.getItem() != Item.getItemFromBlock(Blocks.obsidian)) {
         return false;
      }
      BlockPos place = clicked.offset(face);
      return !nearBed(mc, place);
   }

   private static boolean nearBed(Minecraft mc, BlockPos p) {
      for (int dx = -2; dx <= 2; dx++) {
         for (int dy = -2; dy <= 2; dy++) {
            for (int dz = -2; dz <= 2; dz++) {
               if (mc.theWorld.getBlockState(p.add(dx, dy, dz)).getBlock() == Blocks.bed) {
                  return true;
               }
            }
         }
      }
      return false;
   }
}
