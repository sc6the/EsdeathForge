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

// Susanoo geometry ported from OldAnimationsMod / OAM-CosmeticsMod (Cosmetic030, displayname
// "Susanoo"): a 3x-stacked chakra ribcage. Texture is OAM's resources.png, bundled as
// susanoo_oam.png (patch-assets). Default colour matches OAM's renderRGB (179/47/196 over /100
// -> ~1.0,0.47,1.0 magenta) and stays recolorable/movable/scalable via CosmeticController.
public class CosmeticSusanoo extends CosmeticBase {
   private static final ResourceLocation TEXTURE = new ResourceLocation("EsdeathClient/susanoo_oam.png");
   private final CosmeticSusanoo.ModelSusanoo model;

   public CosmeticSusanoo(RenderPlayer var1) {
      super(var1, "Susanoo");
      this.model = new CosmeticSusanoo.ModelSusanoo(var1);
   }

   @Override
   public void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if ((CosmeticController.shouldRenderTopHat(var1))) {
         GlStateManager.pushMatrix();
         this.renderPlayer.bindTexture(TEXTURE);
         // OAM default tint (1.0,0.47,1.0 magenta); custom color/transparency override via the controller
         CosmeticController.apply("Susanoo", 1.0F, 0.47F, 1.0F);
         this.model.render(var1, var2, var3, var5, var6, var7, var8);
         GL11.glPopMatrix();
      }
   }

   private class ModelSusanoo extends CosmeticModelBase {
      private final ModelRenderer backStripe;
      private final ModelRenderer ribState;
      private final ModelRenderer rib1;
      private final ModelRenderer rib2;
      private final ModelRenderer cornerleft;
      private final ModelRenderer cornerright;
      private final ModelRenderer ribright;
      private final ModelRenderer ribleft;
      private final ModelRenderer cornerrightFront;
      private final ModelRenderer cornerleftFront;

      public ModelSusanoo(RenderPlayer var2) {
         super(var2);
         this.backStripe = new ModelRenderer(this, 0, 0);
         this.backStripe.mirror = false;
         this.backStripe.addBox(0.0F, 0.0F, 0.0F, 2, 18, 1);
         this.backStripe.setRotationPoint(-1.0F, -2.0F, 8.0F);
         this.backStripe.setTextureSize(32, 32);

         this.ribState = new ModelRenderer(this);

         this.rib1 = new ModelRenderer(this, 0, 0);
         this.rib1.mirror = false;
         this.rib1.addBox(0.0F, 0.0F, 0.0F, 9, 2, 1);
         this.rib1.setRotationPoint(0.0F, 13.0F, 8.0F);
         this.rib1.rotateAngleY = 0.2F;
         this.rib1.setTextureSize(32, 32);
         this.ribState.addChild(this.rib1);

         this.rib2 = new ModelRenderer(this, 0, 0);
         this.rib2.mirror = false;
         this.rib2.addBox(0.0F, 0.0F, 0.0F, 9, 2, 1);
         this.rib2.setRotationPoint(-0.0F, 13.0F, 9.0F);
         this.rib2.rotateAngleY = 3.0F;
         this.rib2.setTextureSize(32, 32);
         this.ribState.addChild(this.rib2);

         this.cornerleft = new ModelRenderer(this, 0, 0);
         this.cornerleft.mirror = false;
         this.cornerleft.addBox(0.0F, 0.0F, 0.0F, 2, 2, 1);
         this.cornerleft.setRotationPoint(-10.0F, 13.0F, 5.76F);
         this.cornerleft.rotateAngleY = -0.6F;
         this.cornerleft.setTextureSize(32, 32);
         this.ribState.addChild(this.cornerleft);

         this.cornerright = new ModelRenderer(this, 0, 0);
         this.cornerright.mirror = false;
         this.cornerright.addBox(0.0F, 0.0F, 0.0F, 2, 2, 1);
         this.cornerright.setRotationPoint(8.4F, 13.0F, 6.39F);
         this.cornerright.rotateAngleY = 0.6F;
         this.cornerright.setTextureSize(32, 32);
         this.ribState.addChild(this.cornerright);

         this.ribright = new ModelRenderer(this, 0, 0);
         this.ribright.mirror = false;
         this.ribright.addBox(0.0F, 0.0F, 0.0F, 1, 2, 9);
         this.ribright.setRotationPoint(9.6F, 13.0F, -2.95F);
         this.ribright.setTextureSize(32, 32);
         this.ribState.addChild(this.ribright);

         this.ribleft = new ModelRenderer(this, 0, 0);
         this.ribleft.mirror = false;
         this.ribleft.addBox(0.0F, 0.0F, 0.0F, 1, 2, 9);
         this.ribleft.setRotationPoint(-10.55F, 13.0F, -2.4F);
         this.ribleft.setTextureSize(32, 32);
         this.ribState.addChild(this.ribleft);

         this.cornerrightFront = new ModelRenderer(this, 0, 0);
         this.cornerrightFront.mirror = false;
         this.cornerrightFront.addBox(0.0F, 0.0F, 0.0F, 5, 2, 1);
         this.cornerrightFront.setRotationPoint(7.15F, 13.0F, -6.5F);
         this.cornerrightFront.rotateAngleY = -0.8F;
         this.cornerrightFront.setTextureSize(32, 32);
         this.ribState.addChild(this.cornerrightFront);

         this.cornerleftFront = new ModelRenderer(this, 0, 0);
         this.cornerleftFront.mirror = false;
         this.cornerleftFront.addBox(0.0F, 0.0F, 0.0F, 5, 2, 1);
         this.cornerleftFront.setRotationPoint(-10.5F, 13.0F, -2.45F);
         this.cornerleftFront.rotateAngleY = 0.8F;
         this.cornerleftFront.setTextureSize(32, 32);
         this.ribState.addChild(this.cornerleftFront);
      }

      @Override
      public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
         if ((EsdeathUtils.isOnline(var1.getName()))) {
            if ((EsdeathClient.getInstance().getPlayer(var1.getUniqueID().toString()).getCosmetics().contains("susanoo"))) {
               // anchor to the torso so the ribcage leans with the body during the 1.8 sneak.
               this.playerModel.bipedBody.postRender(var7);
               // OAM Cosmetic030.render: back stripe, then the ribcage stacked 3x downward.
               GL11.glTranslatef(0.0F, 0.05F, -0.1F);
               if (var1.isSneaking()) {
                  GL11.glTranslatef(0.0F, 0.0F, 0.1F);
               }
               this.backStripe.render(var7);

               for (int i = 0; i < 3; i++) {
                  this.ribState.render(var7);
                  GL11.glTranslatef(0.0F, -0.43F, 0.0F);
               }
            }
         }
      }
   }
}
