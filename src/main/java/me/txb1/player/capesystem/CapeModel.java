package me.txb1.player.capesystem;

import java.util.Random;
import me.txb1.EsdeathClient;
import me.txb1.player.PlayerObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

public class CapeModel implements LayerRenderer {
   private Long last = 0L;
   public final RenderPlayer playerRenderer;
   private int i;

   @Override
   public boolean shouldCombineTextures() {
      return false;
   }

   private static int om3(float var0, float var1) {
      float var2;
      return (var2 = var0 - var1) == 0.0F ? 0 : (var2 < 0.0F ? -1 : 1);
   }

   private static int om4(double var0, double var2) {
      double var4;
      return (var4 = var0 - var2) == 0.0 ? 0 : (var4 < 0.0 ? -1 : 1);
   }

   public void doRenderLayer(AbstractClientPlayer var1, float var2) {
      if ((Minecraft.getMinecraft().thePlayer.canEntityBeSeen(var1))) {
         PlayerObject var3 = EsdeathClient.getInstance().getPlayer(var1.getUniqueID().toString());

         try {
            if ((var1.hasPlayerInfo()) && !(var1.isInvisible()) && ((var3) != null) && ((var3.getCapes().size()) > 0)) {
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               this.playerRenderer.bindTexture(var3.getCurrentCape());
               GlStateManager.pushMatrix();
               GlStateManager.translate(0.0F, 0.0F, 0.125F);
               GlStateManager.rotate(0.0F, 20.0F, 20.0F, 20.0F);
               double var4 = var1.prevChasingPosX
                  + (var1.chasingPosX - var1.prevChasingPosX) * (double)var2
                  - (var1.prevPosX + (var1.posX - var1.prevPosX) * (double)var2);
               double var6 = var1.prevChasingPosY
                  + (var1.chasingPosY - var1.prevChasingPosY) * (double)var2
                  - (var1.prevPosY + (var1.posY - var1.prevPosY) * (double)var2);
               double var8 = var1.prevChasingPosZ
                  + (var1.chasingPosZ - var1.prevChasingPosZ) * (double)var2
                  - (var1.prevPosZ + (var1.posZ - var1.prevPosZ) * (double)var2);
               float var10 = var1.prevRenderYawOffset + (var1.renderYawOffset - var1.prevRenderYawOffset) * var2;
               double var11 = (double)MathHelper.sin(var10 * (float) Math.PI / 180.0F);
               double var13 = (double)(-MathHelper.cos(var10 * (float) Math.PI / 180.0F));
               float var15 = (float)var6 * 10.0F;
               var15 = MathHelper.clamp_float(var15, -6.0F, 32.0F);
               float var16 = (float)(var4 * var11 + var8 * var13) * 100.0F;
               float var17 = (float)(var4 * var13 - var8 * var11) * 100.0F;
               if (((om5(var16, 0.0F)) < 0)) {
                  var16 = 0.0F;
               }

               if (((om3(var16, 165.0F)) > 0)) {
                  var16 -= 10.0F;
               }

               float var18 = var1.prevCameraYaw + (var1.cameraYaw - var1.prevCameraYaw) * var2;
               var15 += MathHelper.sin((var1.prevDistanceWalkedModified + (var1.distanceWalkedModified - var1.prevDistanceWalkedModified) * var2) * 6.0F)
                  * 32.0F
                  * var18;
               if ((var1.isSneaking())) {
                  var15 += 20.0F;
                  GlStateManager.translate(0.0F, 0.142F + (float)(new Random().nextInt(10) / 10), -0.0178F);
               }

               if ((var1.isSprinting()) && ((om4(var1.motionY, 0.0)) <= 0)) {
                  if (((this.i) < (60))) {
                     if (((this.i) < (20))) {
                        this.i = this.i + 1;
                        
                     } else {
                        this.i = this.i + 2;
                        
                     }
                  }
               } else if (((this.i) > 0)) {
                  this.i = this.i - 1;
               }

               GlStateManager.rotate((float)(this.i / 2) + 6.0F + var16 / 3.0F / 2.0F + var15, 1.0F, 0.0F, 0.0F);
               GlStateManager.rotate(var17 / 2.0F, 0.0F, 0.0F, 1.0F);
               GlStateManager.rotate(-var17 / 2.0F, 0.0F, 1.0F, 0.0F);
               GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
               this.playerRenderer.getMainModel().renderCape(0.0625F);
               GlStateManager.popMatrix();
            }
         } catch (Exception var19) {
            var19.printStackTrace();
            return;
         }

                     ;
         
      }
   }

   public CapeModel(RenderPlayer var1) {
      this.i = 0;
      this.playerRenderer = var1;
   }

   @Override
   public void doRenderLayer(EntityLivingBase var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if (!(EsdeathClient.getInstance().getModuleManager().getModuleByName("OnlyOptifineCapes").isEnabled())) {
         this.doRenderLayer((AbstractClientPlayer)var1, var4);
      }
   }

   private static int om5(float var0, float var1) {
      float var2;
      return (var2 = var0 - var1) == 0.0F ? 0 : (var2 < 0.0F ? -1 : 1);
   }
}
