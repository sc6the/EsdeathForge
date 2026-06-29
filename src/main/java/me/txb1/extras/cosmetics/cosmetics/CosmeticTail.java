package me.txb1.extras.cosmetics.cosmetics;

import me.txb1.EsdeathClient;
import me.txb1.extras.cosmetics.CosmeticBase;
import me.txb1.extras.cosmetics.CosmeticController;
import me.txb1.extras.cosmetics.CosmeticModelBase;
import me.txb1.utils.EsdeathUtils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class CosmeticTail extends CosmeticBase {
   private final CosmeticTail.ModelTopHat modelTopHat;
   private static final ResourceLocation TEXTURE = new ResourceLocation("EsdeathClient/stripes.png");

   @Override
   public void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if ((CosmeticController.shouldRenderTopHat(var1))) {
         GlStateManager.pushMatrix();
         this.renderPlayer.bindTexture(TEXTURE);
         if ((var1.isSneaking())) {
            GL11.glTranslated(0.0, 0.225, 0.0);
         }

         CosmeticController.apply("Tail");
         this.modelTopHat.render(var1, var2, var3, var5, var6, var7, var8);
         GL11.glPopMatrix();
      }
   }

   public CosmeticTail(RenderPlayer var1) {
      super(var1, "Tail");
      this.modelTopHat = new CosmeticTail.ModelTopHat(var1);
   }

   private class ModelTopHat extends CosmeticModelBase {
      private ModelRenderer rim;

      public ModelTopHat(RenderPlayer var2) {
         super(var2);
         this.rim = new ModelRenderer(this.playerModel, 0, 0);
         this.rim = new ModelRenderer(this.playerModel, 12, 11);
         this.rim.addBox(-0.5F, 0.0F, -0.5F, 2, 7, 2, 0.0F);
         this.rim.setRotationPoint(-0.2F, 10.0F, 3.0F);
      }

      @Override
      public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
         if ((EsdeathUtils.isOnline(var1.getName()))) {
            if ((EsdeathClient.getInstance().getPlayer(var1.getUniqueID().toString()).getCosmetics().contains("tail"))) {
               float var8 = 1.0F;
               AbstractClientPlayer var9 = (AbstractClientPlayer)var1;
               double var10 = var9.prevChasingPosX
                  + (var9.chasingPosX - var9.prevChasingPosX) * (double)var8
                  - (var9.prevPosX + (var9.posX - var9.prevPosX) * (double)var8);
               double var12 = var9.prevChasingPosZ
                  + (var9.chasingPosZ - var9.prevChasingPosZ) * (double)var8
                  - (var9.prevPosZ + (var9.posZ - var9.prevPosZ) * (double)var8);
               float var14 = var9.prevRenderYawOffset + (var9.renderYawOffset - var9.prevRenderYawOffset) * var8;
               double var15 = (double)MathHelper.sin(var14 * (float) Math.PI / 180.0F);
               double var17 = (double)(-MathHelper.cos(var14 * (float) Math.PI / 180.0F));
               float var19 = (float)(var10 * var15 + var12 * var17) * 100.0F;
               if (((om1(var19, 100.0F)) > 0)) {
                  var19 = 100.0F;
               }

               this.rim.render(var7);
               this.rim.rotateAngleX = var19 / 70.0F;
            }
         }
      }

      private int om1(float var0, float var1) {
         float var2;
         return (var2 = var0 - var1) == 0.0F ? 0 : (var2 < 0.0F ? -1 : 1);
      }

   }
}
