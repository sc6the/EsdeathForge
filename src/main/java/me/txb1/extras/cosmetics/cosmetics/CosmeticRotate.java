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

public class CosmeticRotate extends CosmeticBase {
   private static final ResourceLocation TEXTURE = new ResourceLocation("EsdeathClient/halo.png");
   private final CosmeticRotate.ModelTopHat modelTopHat;

   @Override
   public void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if ((CosmeticController.shouldRenderTopHat(var1))) {
         GlStateManager.pushMatrix();
         this.renderPlayer.bindTexture(TEXTURE);
         if ((var1.isSneaking())) {
            GL11.glTranslated(0.0, 0.225, 0.0);
         }

         CosmeticController.apply("Rotate");
         this.modelTopHat.render(var1, var2, var3, var5, var6, var7, var8);
         GL11.glPopMatrix();
      }
   }

   public CosmeticRotate(RenderPlayer var1) {
      super(var1, "Rotate");
      this.modelTopHat = new CosmeticRotate.ModelTopHat(var1);
   }

   private class ModelTopHat extends CosmeticModelBase {
      private ModelRenderer rim;
      private Integer up;

      @Override
      public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
         if ((EsdeathUtils.isOnline(var1.getName()))) {
            if ((EsdeathClient.getInstance().getPlayer(var1.getUniqueID().toString()).getCosmetics().contains("rotate"))) {
               this.rim.rotationPointX = 0.0F;
               this.rim.rotationPointY = 0.0F;
               this.up = this.up + 1;
               this.rim.rotateAngleY = (float)(this.up / 15);
               if (!(var1.isSneaking())) {
                  this.rim.render(var7);
               }
            }
         }
      }

      public ModelTopHat(RenderPlayer var2) {
         super(var2);
         this.up = 0;
         this.rim = new ModelRenderer(this.playerModel, 0, 0);
         this.rim.addBox(-5.5F, -12.3F, -4.25F, 2, 2, 3);
      }

   }
}
