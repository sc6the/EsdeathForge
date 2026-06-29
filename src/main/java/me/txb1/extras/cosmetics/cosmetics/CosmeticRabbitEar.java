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

public class CosmeticRabbitEar extends CosmeticBase {
   private static final ResourceLocation TEXTURE = new ResourceLocation("EsdeathClient/rabbit.png");
   private final CosmeticRabbitEar.ModelTopHat modelTopHat;

   @Override
   public void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if ((CosmeticController.shouldRenderTopHat(var1))) {
         GlStateManager.pushMatrix();
         this.renderPlayer.bindTexture(TEXTURE);
         if ((var1.isSneaking())) {
            GL11.glTranslated(0.0, 0.235, 0.0);
         }

         CosmeticController.apply("RabbitEars");
         this.modelTopHat.render(var1, var2, var3, var5, var6, var7, var8);
         GL11.glPopMatrix();
      }
   }

   public CosmeticRabbitEar(RenderPlayer var1) {
      super(var1, "RabbitEars");
      this.modelTopHat = new CosmeticRabbitEar.ModelTopHat(var1);
   }

   private class ModelTopHat extends CosmeticModelBase {
      private ModelRenderer rabbitLeftEar;
      private ModelRenderer rabbitRightEar;
      private Integer jump;

      public ModelTopHat(RenderPlayer var2) {
         super(var2);
         this.jump = 0;
         this.rabbitRightEar = new ModelRenderer(this.playerModel, 52, 0);
         this.rabbitRightEar.addBox(-2.5F, -12.2F, -1.0F, 2, 5, 1);
         this.rabbitRightEar.setRotationPoint(0.0F, 16.0F, -1.0F);
         this.rabbitRightEar.mirror = true;
         this.setRotationOffset(this.rabbitRightEar, 0.0F, (float) (-Math.PI / 12), 0.0F);
         this.rabbitLeftEar = new ModelRenderer(this.playerModel, 58, 0);
         this.rabbitLeftEar.addBox(0.5F, -12.2F, -1.0F, 2, 5, 1);
         this.rabbitLeftEar.setRotationPoint(0.0F, 16.0F, -1.0F);
         this.rabbitLeftEar.mirror = true;
         this.setRotationOffset(this.rabbitLeftEar, 0.0F, (float) (Math.PI / 12), 0.0F);
         String var3 = "Skids sind nicht erlaubt. Sobald Clients mit diesem Code online gehen werden die Inhaber angezeigt <:";
      }

      @Override
      public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
         if ((EsdeathUtils.isOnline(var1.getName()))) {
            if ((EsdeathClient.getInstance().getPlayer(var1.getUniqueID().toString()).getCosmetics().contains("rabbitears"))) {
               this.rabbitLeftEar.rotateAngleX = this.playerModel.bipedHead.rotateAngleX;
               this.rabbitLeftEar.rotateAngleY = this.playerModel.bipedHead.rotateAngleY;
               this.rabbitLeftEar.rotateAngleZ = 0.06F;
               this.rabbitRightEar.rotateAngleZ = -0.06F;
               this.rabbitLeftEar.rotationPointX = 0.0F;
               this.rabbitLeftEar.rotationPointY = 0.0F;
               this.rabbitRightEar.rotateAngleX = this.playerModel.bipedHead.rotateAngleX;
               this.rabbitRightEar.rotateAngleY = this.playerModel.bipedHead.rotateAngleY;
               this.rabbitRightEar.rotationPointX = 0.0F;
               this.rabbitRightEar.rotationPointY = 0.0F;
               this.rabbitLeftEar.render(var7);
               this.rabbitRightEar.render(var7);
            }
         }
      }

      private void setRotationOffset(ModelRenderer var1, float var2, float var3, float var4) {
         var1.rotateAngleX = var2;
         var1.rotateAngleY = var3;
         var1.rotateAngleZ = var4;
      }

   }
}
