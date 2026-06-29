package me.txb1.player.modulesystem.modules.player;

import com.darkmagician6.eventapi.EventManager;
import me.txb1.EsdeathClient;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;

public class Hitbox extends Module {

   public static void renderDebugBoundingBox(Entity var0, double var1, double var3, double var5, float var7, float var8) {
      GlStateManager.depthMask(false);
      GlStateManager.disableTexture2D();
      GlStateManager.disableLighting();
      GlStateManager.disableCull();
      GlStateManager.disableBlend();
      float var9 = var0.width / 2.0F;
      AxisAlignedBB var10 = var0.getEntityBoundingBox();
      AxisAlignedBB var11 = new AxisAlignedBB(
         var10.minX - var0.posX + var1,
         var10.minY - var0.posY + var3,
         var10.minZ - var0.posZ + var5,
         var10.maxX - var0.posX + var1,
         var10.maxY - var0.posY + var3,
         var10.maxZ - var0.posZ + var5
      );
      // TODO(forge-port): re-wire RenderGlobal colored-AABB (was unmapped SRG func_181563_a) to a Mixin/deobf call
      GlStateManager.enableTexture2D();
      GlStateManager.enableLighting();
      GlStateManager.enableCull();
      GlStateManager.disableBlend();
      GlStateManager.depthMask(true);
   }

   public Hitbox() {
      super("Hitbox", "Hitbox", Category.PLAYER, false);
   }

   @Override
   public void onDisable() {
      EventManager.unregister(this);
   }

   @Override
   public void onEnable() {
      EventManager.register(this);
   }

}
