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

public class CosmeticCrown extends CosmeticBase {
   private final CosmeticCrown.ModelTopHat modelTopHat;
   private static final ResourceLocation TEXTURE = new ResourceLocation("EsdeathClient/crown.png");

   @Override
   public void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if ((CosmeticController.shouldRenderTopHat(var1))) {
         GlStateManager.pushMatrix();
         GlStateManager.color(100.0F, 100.0F, 100.0F);
         this.renderPlayer.bindTexture(TEXTURE);
         if ((var1.isSneaking())) {
            GL11.glTranslated(0.0, 0.225, 0.0);
         }

         CosmeticController.apply("Crown");
         this.modelTopHat.render(var1, var2, var3, var5, var6, var7, var8);
         GL11.glPopMatrix();
      }
   }

   public CosmeticCrown(RenderPlayer var1) {
      super(var1, "Crown");
      this.modelTopHat = new CosmeticCrown.ModelTopHat(var1);
   }

   private class ModelTopHat extends CosmeticModelBase {
      private ModelRenderer rim;

      @Override
      public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
         if ((EsdeathUtils.isOnline(var1.getName()))) {
            if ((EsdeathClient.getInstance().getPlayer(var1.getUniqueID().toString()).getCosmetics().contains("crown"))) {
               this.rim.rotateAngleX = this.playerModel.bipedHead.rotateAngleX;
               this.rim.rotateAngleY = this.playerModel.bipedHead.rotateAngleY;
               this.rim.rotationPointX = 0.0F;
               this.rim.rotationPointY = 0.0F;
               this.rim.rotateAngleY += 0.004F;
               this.rim.render(var7);
            }
         }
      }

      public ModelTopHat(RenderPlayer var2) {
         super(var2);
         this.rim = new ModelRenderer(this.playerModel, 32, 32);
         this.rim.addBox(-4.5F, -8.4F, -4.75F, 9, 1, 9);
         this.rim.addBox(-4.5F, -10.4F, -4.75F, 1, 2, 1);
         this.rim.addBox(-3.5F, -9.4F, -4.75F, 1, 1, 1);
         this.rim.addBox(-4.5F, -9.4F, -3.75F, 1, 1, 1);
         this.rim.addBox(3.5F, -10.4F, -4.75F, 1, 2, 1);
         this.rim.addBox(2.5F, -9.4F, -4.75F, 1, 1, 1);
         this.rim.addBox(3.5F, -9.4F, -3.75F, 1, 1, 1);
         this.rim.addBox(-1.5F, -9.4F, -4.75F, 3, 1, 1);
         this.rim.addBox(-0.5F, -10.4F, -4.75F, 1, 1, 1);
         this.rim.addBox(-4.5F, -10.4F, 3.25F, 1, 2, 1);
         this.rim.addBox(-3.5F, -9.4F, 3.25F, 1, 1, 1);
         this.rim.addBox(-4.5F, -9.4F, 2.25F, 1, 1, 1);
         this.rim.addBox(3.5F, -10.4F, 3.25F, 1, 2, 1);
         this.rim.addBox(2.5F, -9.4F, 3.25F, 1, 1, 1);
         this.rim.addBox(3.5F, -9.4F, 2.25F, 1, 1, 1);
      }

   }
}
