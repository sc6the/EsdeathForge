package me.txb1.player.modulesystem.modules.player;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.util.HashMap;
import me.txb1.EsdeathClient;
import me.txb1.MessageHelper;
import me.txb1.player.events.EventRender;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

public class MLGHelper extends Module {
   private static HashMap<Integer, String> check = new HashMap<>();

   @Override
   public void onEnable() {
      EventManager.register(this);
      MessageHelper.sendMessage("mlghelper");
   }

   static {
      String var0 = "Jump";
      String var1 = "Run";
      check.put(16, var0);
      check.put(17, var0);
      check.put(18, var1);
      check.put(19, var1);
      check.put(20, var0);
      check.put(21, var1);
      check.put(22, var1);
      check.put(23, var0);
      check.put(28, var0);
      check.put(30, var0);
      check.put(31, var0);
      check.put(35, var0);
      check.put(39, var0);
      check.put(41, var0);
      check.put(43, var0);
      check.put(45, var0);
      check.put(50, var0);
      check.put(52, var0);
      check.put(54, var0);
      check.put(56, var0);
      check.put(59, var0);
      check.put(61, var0);
      check.put(66, var0);
      check.put(68, var0);
      check.put(71, var0);
      check.put(73, var0);
      check.put(76, var0);
      check.put(78, var0);
      check.put(81, var0);
      check.put(83, var0);
      check.put(86, var0);
      check.put(88, var0);
      check.put(89, var0);
      check.put(91, var0);
      check.put(94, var0);
      check.put(97, var0);
      check.put(100, var0);
      check.put(102, var0);
      check.put(27, var1);
      check.put(29, var1);
      check.put(34, var1);
      check.put(36, var1);
      check.put(38, var1);
      check.put(40, var1);
      check.put(42, var1);
      check.put(44, var1);
      check.put(49, var1);
      check.put(51, var1);
      check.put(53, var1);
      check.put(55, var1);
      check.put(58, var1);
      check.put(60, var1);
      check.put(62, var1);
      check.put(65, var1);
      check.put(67, var1);
      check.put(69, var1);
      check.put(72, var1);
      check.put(74, var1);
      check.put(77, var1);
      check.put(79, var1);
      check.put(85, var1);
      check.put(87, var1);
      check.put(90, var1);
      check.put(93, var1);
      check.put(95, var1);
      check.put(98, var1);
      check.put(101, var1);
      check.put(104, var1);
   }

   @EventTarget
   public void OnTick(EventRender var1) {
      if ((mc.thePlayer.isSneaking())) {
         MovingObjectPosition var2 = mc.thePlayer.rayTrace(200.0, 1.0F);
         int var3 = var2.getBlockPos().getY();
         if ((mc.theWorld.getBlockState(var2.getBlockPos()).getBlock().getMaterial().equals(Material.air))) {
            return;
         }

         if (!(mc.theWorld.getBlockState(new BlockPos(var2.getBlockPos())).getBlock().getMaterial().equals(Material.web))
            && (mc.theWorld.getBlockState(new BlockPos(var2.getBlockPos())).getBlock().getMaterial().isSolid())) {
            var3++;
         }

         int var4 = mc.thePlayer.getPosition().getY() - var3;
         if (((var4) < (10))) {
            return;
         }

         String var5 = "§cUnbekannt!";
         if ((check.containsKey(var4))) {
            var5 = String.valueOf(new StringBuilder().append("§f").append(check.get(var4)));
            
         } else if ((check.containsKey(var4 + 1))) {
            var5 = String.valueOf(new StringBuilder().append("§cEinen Block höher: §f§l").append(check.get(var4 + 1)));
            
         } else if ((check.containsKey(var4 + 2))) {
            var5 = String.valueOf(new StringBuilder().append("§cZwei Blöcke höher: §f§l").append(check.get(var4 + 2)));
         }

         ScaledResolution var6 = new ScaledResolution(mc);
         int var7 = var6.getScaledWidth();
         int var8 = var6.getScaledHeight();
         mc.fontRendererObj.drawStringWithShadow(var5, 2, var8 - 10, EsdeathClient.getInstance().rainbow(200));
      }
   }

   public MLGHelper() {
      super("MLGHelper", "MLGHelper", Category.PLAYER, false);
   }

   @Override
   public void onDisable() {
      EventManager.unregister(this);
   }
}
