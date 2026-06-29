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
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class CosmeticTopHat extends CosmeticBase {
   private static final ResourceLocation TEXTURE = new ResourceLocation("EsdeathClient/hat.png");
   private final CosmeticTopHat.ModelTopHat modelTopHat;

   @Override
   public void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if ((CosmeticController.shouldRenderTopHat(var1))) {
         GlStateManager.pushMatrix();
         this.renderPlayer.bindTexture(TEXTURE);
         if ((var1.isSneaking())) {
            GL11.glTranslated(0.0, 0.225, 0.0);
         }

         CosmeticController.apply("Hat");
         this.modelTopHat.render(var1, var2, var3, var5, var6, var7, var8);
         GL11.glPopMatrix();
      }
   }

   public CosmeticTopHat(RenderPlayer var1) {
      super(var1, "Hat");
      this.modelTopHat = new CosmeticTopHat.ModelTopHat(var1);
   }

   private class ModelTopHat extends CosmeticModelBase {
      private ModelRenderer rim;
      private ModelRenderer pointy;

      @Override
      public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
         if ((EsdeathUtils.isOnline(var1.getName()))) {
            if ((EsdeathClient.getInstance().getPlayer(var1.getUniqueID().toString()).getCosmetics().contains("hat"))) {
               this.rim.rotateAngleX = this.playerModel.bipedHead.rotateAngleX;
               this.rim.rotateAngleY = this.playerModel.bipedHead.rotateAngleY;
               this.rim.rotationPointX = 0.0F;
               this.rim.rotationPointY = 0.0F;
               this.rim.render(var7);
               this.pointy.rotateAngleX = this.playerModel.bipedHead.rotateAngleX;
               this.pointy.rotateAngleY = this.playerModel.bipedHead.rotateAngleY;
               this.pointy.rotationPointX = 0.0F;
               this.pointy.rotationPointY = 0.0F;
               this.pointy.render(var7);
            }
         }
      }

      public ModelTopHat(RenderPlayer var2) {
         super(var2);
         this.rim = new ModelRenderer(this.playerModel, 0, 0);
         this.rim.addBox(-5.5F, -8.0F, -5.5F, 11, 2, 11);
         this.pointy = new ModelRenderer(this.playerModel, 0, 13);
         this.pointy.addBox(-3.5F, -12.0F, -3.5F, 7, 4, 7);
      }
   }
}
