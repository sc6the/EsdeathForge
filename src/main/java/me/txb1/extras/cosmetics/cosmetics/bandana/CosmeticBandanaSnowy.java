package me.txb1.extras.cosmetics.cosmetics.bandana;

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

public class CosmeticBandanaSnowy extends CosmeticBase {
   private static final ResourceLocation TEXTURE = new ResourceLocation("EsdeathClient/snowybandana.png");
   private final CosmeticBandanaSnowy.ModelTopHat modelTopHat;

   public CosmeticBandanaSnowy(RenderPlayer var1) {
      super(var1, "SnowyBandana");
      this.modelTopHat = new CosmeticBandanaSnowy.ModelTopHat(var1);
   }

   @Override
   public void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if ((CosmeticController.shouldRenderTopHat(var1))) {
         GlStateManager.pushMatrix();
         this.renderPlayer.bindTexture(TEXTURE);
         CosmeticController.apply("SnowyBandana");
         // OAM CosmeticModelRenderer.setHeadRotations: sneak lift + head yaw/pitch so the
         // bandana tracks the head exactly like vanilla headwear (var6=netHeadYaw, var7=headPitch).
         if (var1.isSneaking()) {
            GL11.glTranslatef(0.0F, 0.29375F, 0.0F);
         }
         GL11.glRotatef(var6, 0.0F, 1.0F, 0.0F);
         GL11.glRotatef(var7, 1.0F, 0.0F, 0.0F);
         this.modelTopHat.render(var1, var2, var3, var5, var6, var7, var8);
         GL11.glPopMatrix();
      }
   }

   private class ModelTopHat extends CosmeticModelBase {
      private ModelRenderer rim;
      private ModelRenderer pointy;

      @Override
      public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
         if ((EsdeathUtils.isOnline(var1.getName()))) {
            if ((EsdeathClient.getInstance().getPlayer(var1.getUniqueID().toString()).getCosmetics().contains("snowybandana"))) {
               // head transform is applied by the outer render (OAM setHeadRotations); render flat.
               this.rim.rotateAngleX = 0.0F;
               this.rim.rotateAngleY = 0.0F;
               this.rim.rotationPointX = 0.0F;
               this.rim.rotationPointY = 0.0F;
               this.rim.render(var7);
            }
         }
      }

      public ModelTopHat(RenderPlayer var2) {
         super(var2);
         this.rim = new ModelRenderer(this.playerModel, 0, 0);
         this.rim.addBox(-4.5F, -7.3F, -4.75F, 9, 3, 9);
      }

   }
}
