package me.txb1.extras.cosmetics.cosmetics;

import java.awt.Color;
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

public class CosmeticKagune extends CosmeticBase {
   private CosmeticKagune.ModelTopHat modelTopHat;
   private static final ResourceLocation TEXTURE = new ResourceLocation("EsdeathClient/kagune2.png");

   private void rim5(ModelRenderer var1, float var2, float var3, float var4) {
   }

   @Override
   public void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if ((EsdeathUtils.isOnline(var1.getName()))) {
         if ((EsdeathClient.getInstance().getPlayer(var1.getUniqueID().toString()).getCosmetics().contains("kagune"))) {
            if ((CosmeticController.shouldRenderTopHat(var1))) {
               GlStateManager.pushMatrix();
               this.renderPlayer.bindTexture(TEXTURE);
               if ((var1.isSneaking())) {
                  GL11.glTranslated(0.0, 0.225, 0.0);
               }

               Color var9 = new Color(186, 186, 186, 255);
               GL11.glColor3f((float)var9.getRed(), (float)var9.getGreen(), (float)var9.getBlue());
               CosmeticController.apply("Kagune");
               this.modelTopHat.render(var1, var2, var3, var5, var6, var7, var8);
               GL11.glColor3f(1.0F, 1.0F, 1.0F);
               GL11.glPopMatrix();
            }
         }
      }
   }

   public CosmeticKagune(RenderPlayer var1) {
      super(var1, "Kagune");
      this.modelTopHat = new CosmeticKagune.ModelTopHat(var1);
   }

   private void rim2(ModelRenderer var1, float var2, float var3, float var4) {
   }

   private void rim6(ModelRenderer var1, float var2, float var3, float var4) {
   }

   private class ModelTopHat extends CosmeticModelBase {
      private ModelRenderer rim3;
      private ModelRenderer rim4;
      private ModelRenderer rim;
      private ModelRenderer rim5;
      private ModelRenderer rim6;
      private String info;
      private ModelRenderer rim2;
      private Integer updown;
      private ModelRenderer rim8;
      private ModelRenderer rim7;

      public ModelTopHat(RenderPlayer var2) {
         super(var2);
         this.updown = 0;
         this.info = "up";
         this.rim = new ModelRenderer(this.playerModel, 0, 0);
         this.rim2 = new ModelRenderer(this.playerModel, 0, 0);
         this.rim3 = new ModelRenderer(this.playerModel, 0, 0);
         this.rim4 = new ModelRenderer(this.playerModel, 0, 0);
         this.rim5 = new ModelRenderer(this.playerModel, 0, 0);
         this.rim6 = new ModelRenderer(this.playerModel, 0, 0);
         this.rim7 = new ModelRenderer(this.playerModel, 0, 0);
         this.rim8 = new ModelRenderer(this.playerModel, 0, 0);
         this.rim.addBox(-2.0F, 8.0F, -10.0F, 3, 3, 11);
         this.rim2.addBox(0.0F, 8.0F, -6.0F, 3, 3, 11);
         this.rim3.addBox(-9.0F, 5.0F, -6.0F, 2, 2, 14);
         this.rim4.addBox(6.0F, 2.0F, -6.0F, 2, 2, 14);
         this.rim5.addBox(-5.0F, 8.0F, 1.0F, 3, 3, 4);
         this.rim5.addBox(1.0F, 12.0F, 0.0F, 3, 3, 3);
      }

      private void rim4(ModelRenderer var1, float var2, float var3, float var4) {
      }

      @Override
      public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
         this.rim.rotateAngleY = 1.0F;
         this.rim.rotateAngleX = 1.0F;
         this.rim2.rotateAngleY = -1.0F;
         this.rim2.rotateAngleX = 1.0F;
         this.rim3.rotateAngleX = 2.0F;
         this.rim4.rotateAngleX = 2.0F;
         this.rim.render(var7);
         this.rim2.render(var7);
         this.rim3.render(var7);
         this.rim4.render(var7);
         this.rim5.render(var7);
         this.rim6.render(var7);
         this.rim7.render(var7);
         this.rim8.render(var7);
      }
   }
}
