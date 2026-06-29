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

public class CosmeticDevilHorns extends CosmeticBase {
   private static final ResourceLocation TEXTURE = new ResourceLocation("EsdeathClient/susanoo.png");
   private final CosmeticDevilHorns.ModelTopHat modelTopHat;

   @Override
   public void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if ((CosmeticController.shouldRenderTopHat(var1))) {
         GlStateManager.pushMatrix();
         GlStateManager.color(100.0F, 100.0F, 100.0F);
         this.renderPlayer.bindTexture(TEXTURE);
         if ((var1.isSneaking())) {
            GL11.glTranslated(0.0, 0.225, 0.0);
         }

         CosmeticController.apply("DevilHorns");
         this.modelTopHat.render(var1, var2, var3, var5, var6, var7, var8);
         GL11.glPopMatrix();
      }
   }

   public CosmeticDevilHorns(RenderPlayer var1) {
      super(var1, "DevilHorns");
      this.modelTopHat = new CosmeticDevilHorns.ModelTopHat(var1);
   }

   private class ModelTopHat extends CosmeticModelBase {
      public ModelRenderer Shape23;
      public ModelRenderer Shape2;
      public ModelRenderer Shape21;
      public ModelRenderer Shape27;
      public ModelRenderer Shape24;
      public ModelRenderer Shape17;
      public ModelRenderer Shape10;
      public ModelRenderer Shape7;
      public ModelRenderer Shape1 = new ModelRenderer(this);
      public ModelRenderer Shape16;
      public ModelRenderer Shape8;
      public ModelRenderer Shape12;
      public ModelRenderer Shape13;
      public ModelRenderer Shape28;
      public ModelRenderer Shape4;
      public ModelRenderer Shape5;
      public ModelRenderer Shape15;
      public ModelRenderer Shape11;
      public ModelRenderer Shape9;
      public ModelRenderer Shape19;
      public ModelRenderer Shape22;
      public ModelRenderer Shape18;
      public ModelRenderer Shape25;
      public ModelRenderer Shape6;
      public ModelRenderer Shape26;
      public ModelRenderer Shape3;
      public ModelRenderer Shape20;
      public ModelRenderer Shape14;

      @Override
      public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
         if ((EsdeathUtils.isOnline(var1.getName()))) {
            if ((EsdeathClient.getInstance().getPlayer(var1.getUniqueID().toString()).getCosmetics().contains("devilhorns"))) {
               this.Shape1.rotateAngleX = this.playerModel.bipedHead.rotateAngleX;
               this.Shape1.rotateAngleY = this.playerModel.bipedHead.rotateAngleY;
               this.Shape1.rotationPointX = 0.0F;
               this.Shape1.render(var7);
            }
         }
      }

      public ModelTopHat(RenderPlayer var2) {
         super(var2);
         this.Shape1.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape1.setRotationPoint(4.0F, -4.0F, 0.0F);
         this.Shape1.rotateAngleX = 0.0F;
         this.Shape1.rotateAngleY = 0.0F;
         this.Shape1.rotateAngleZ = 0.0F;
         this.Shape1.mirror = false;
         this.Shape2 = new ModelRenderer(this);
         this.Shape2.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape2.setRotationPoint(-6.0F, -5.0F, 0.0F);
         this.Shape2.rotateAngleX = 0.0F;
         this.Shape2.rotateAngleY = 0.0F;
         this.Shape2.rotateAngleZ = 0.0F;
         this.Shape2.mirror = false;
         this.Shape1.addChild(this.Shape2);
         this.Shape3 = new ModelRenderer(this);
         this.Shape3.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape3.setRotationPoint(-5.0F, -3.0F, 0.0F);
         this.Shape3.rotateAngleX = 0.0F;
         this.Shape3.rotateAngleY = 0.0F;
         this.Shape3.rotateAngleZ = 0.0F;
         this.Shape3.mirror = false;
         this.Shape1.addChild(this.Shape3);
         this.Shape4 = new ModelRenderer(this);
         this.Shape4.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape4.setRotationPoint(-5.0F, -5.0F, 0.0F);
         this.Shape4.rotateAngleX = 0.0F;
         this.Shape4.rotateAngleY = 0.0F;
         this.Shape4.rotateAngleZ = 0.0F;
         this.Shape4.mirror = false;
         this.Shape1.addChild(this.Shape4);
         this.Shape5 = new ModelRenderer(this);
         this.Shape5.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape5.setRotationPoint(-6.0F, -4.0F, 0.0F);
         this.Shape5.rotateAngleX = 0.0F;
         this.Shape5.rotateAngleY = 0.0F;
         this.Shape5.rotateAngleZ = 0.0F;
         this.Shape5.mirror = false;
         this.Shape1.addChild(this.Shape5);
         this.Shape6 = new ModelRenderer(this);
         this.Shape6.addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
         this.Shape6.setRotationPoint(-7.0F, -6.0F, 0.0F);
         this.Shape6.rotateAngleX = 0.0F;
         this.Shape6.rotateAngleY = 0.0F;
         this.Shape6.rotateAngleZ = 0.0F;
         this.Shape6.mirror = false;
         this.Shape1.addChild(this.Shape6);
         this.Shape7 = new ModelRenderer(this);
         this.Shape7.addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
         this.Shape7.setRotationPoint(-6.0F, -8.0F, 0.0F);
         this.Shape7.rotateAngleX = 0.0F;
         this.Shape7.rotateAngleY = 0.0F;
         this.Shape7.rotateAngleZ = 0.0F;
         this.Shape7.mirror = false;
         this.Shape1.addChild(this.Shape7);
         this.Shape8 = new ModelRenderer(this);
         this.Shape8.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape8.setRotationPoint(-5.0F, -4.0F, -1.0F);
         this.Shape8.rotateAngleX = 0.0F;
         this.Shape8.rotateAngleY = 0.0F;
         this.Shape8.rotateAngleZ = 0.0F;
         this.Shape8.mirror = false;
         this.Shape1.addChild(this.Shape8);
         this.Shape9 = new ModelRenderer(this);
         this.Shape9.addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
         this.Shape9.setRotationPoint(-6.0F, -6.0F, -1.0F);
         this.Shape9.rotateAngleX = 0.0F;
         this.Shape9.rotateAngleY = 0.0F;
         this.Shape9.rotateAngleZ = 0.0F;
         this.Shape9.mirror = false;
         this.Shape1.addChild(this.Shape9);
         this.Shape10 = new ModelRenderer(this);
         this.Shape10.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape10.setRotationPoint(-5.533333F, -8.4F, 0.0F);
         this.Shape10.rotateAngleX = 0.0F;
         this.Shape10.rotateAngleY = 0.0F;
         this.Shape10.rotateAngleZ = 0.0F;
         this.Shape10.mirror = false;
         this.Shape1.addChild(this.Shape10);
         this.Shape11 = new ModelRenderer(this);
         this.Shape11.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape11.setRotationPoint(-6.0F, -6.0F, 0.0F);
         this.Shape11.rotateAngleX = 0.0F;
         this.Shape11.rotateAngleY = 0.0F;
         this.Shape11.rotateAngleZ = 0.0F;
         this.Shape11.mirror = false;
         this.Shape1.addChild(this.Shape11);
         this.Shape12 = new ModelRenderer(this);
         this.Shape12.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape12.setRotationPoint(-6.6F, -6.6F, 0.0F);
         this.Shape12.rotateAngleX = 0.0F;
         this.Shape12.rotateAngleY = 0.0F;
         this.Shape12.rotateAngleZ = 0.0F;
         this.Shape12.mirror = false;
         this.Shape1.addChild(this.Shape12);
         this.Shape13 = new ModelRenderer(this);
         this.Shape13.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape13.setRotationPoint(-5.466667F, -5.666667F, 0.0F);
         this.Shape13.rotateAngleX = 0.0F;
         this.Shape13.rotateAngleY = 0.0F;
         this.Shape13.rotateAngleZ = 0.0F;
         this.Shape13.mirror = false;
         this.Shape1.addChild(this.Shape13);
         this.Shape14 = new ModelRenderer(this);
         this.Shape14.addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
         this.Shape14.setRotationPoint(-6.0F, -6.0F, 1.0F);
         this.Shape14.rotateAngleX = 0.0F;
         this.Shape14.rotateAngleY = 0.0F;
         this.Shape14.rotateAngleZ = 0.0F;
         this.Shape14.mirror = false;
         this.Shape1.addChild(this.Shape14);
         this.Shape15 = new ModelRenderer(this);
         this.Shape15.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape15.setRotationPoint(-5.0F, -4.0F, 1.0F);
         this.Shape15.rotateAngleX = 0.0F;
         this.Shape15.rotateAngleY = 0.0F;
         this.Shape15.rotateAngleZ = 0.0F;
         this.Shape15.mirror = false;
         this.Shape1.addChild(this.Shape15);
         this.Shape20 = new ModelRenderer(this);
         this.Shape20.addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
         this.Shape20.setRotationPoint(5.0F, -6.0F, -1.0F);
         this.Shape20.rotateAngleX = 0.0F;
         this.Shape20.rotateAngleY = 0.0F;
         this.Shape20.rotateAngleZ = 0.0F;
         this.Shape20.mirror = false;
         this.Shape1.addChild(this.Shape20);
         this.Shape21 = new ModelRenderer(this);
         this.Shape21.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape21.setRotationPoint(4.0F, -4.0F, -1.0F);
         this.Shape21.rotateAngleX = 0.0F;
         this.Shape21.rotateAngleY = 0.0F;
         this.Shape21.rotateAngleZ = 0.0F;
         this.Shape21.mirror = false;
         this.Shape1.addChild(this.Shape21);
         this.Shape22 = new ModelRenderer(this);
         this.Shape22.addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
         this.Shape22.setRotationPoint(5.0F, -6.0F, 1.0F);
         this.Shape22.rotateAngleX = 0.0F;
         this.Shape22.rotateAngleY = 0.0F;
         this.Shape22.rotateAngleZ = 0.0F;
         this.Shape22.mirror = false;
         this.Shape1.addChild(this.Shape22);
         this.Shape23 = new ModelRenderer(this);
         this.Shape23.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape23.setRotationPoint(4.0F, -4.0F, 1.0F);
         this.Shape23.rotateAngleX = 0.0F;
         this.Shape23.rotateAngleY = 0.0F;
         this.Shape23.rotateAngleZ = 0.0F;
         this.Shape23.mirror = false;
         this.Shape1.addChild(this.Shape23);
         this.Shape24 = new ModelRenderer(this);
         this.Shape24.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape24.setRotationPoint(4.533333F, -5.666667F, 0.0F);
         this.Shape24.rotateAngleX = 0.0F;
         this.Shape24.rotateAngleY = 0.0F;
         this.Shape24.rotateAngleZ = 0.0F;
         this.Shape24.mirror = false;
         this.Shape1.addChild(this.Shape24);
         this.Shape25 = new ModelRenderer(this);
         this.Shape25.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape25.setRotationPoint(5.533333F, -6.533333F, 0.0F);
         this.Shape25.rotateAngleX = 0.0F;
         this.Shape25.rotateAngleY = 0.0F;
         this.Shape25.rotateAngleZ = 0.0F;
         this.Shape25.mirror = false;
         this.Shape1.addChild(this.Shape25);
         this.Shape16 = new ModelRenderer(this);
         this.Shape16.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape16.setRotationPoint(-5.0F, -4.0F, 0.0F);
         this.Shape16.rotateAngleX = 0.0F;
         this.Shape16.rotateAngleY = 0.0F;
         this.Shape16.rotateAngleZ = 0.0F;
         this.Shape16.mirror = false;
         this.Shape1.addChild(this.Shape16);
         this.Shape17 = new ModelRenderer(this);
         this.Shape17.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape17.setRotationPoint(4.0F, -3.0F, 0.0F);
         this.Shape17.rotateAngleX = 0.0F;
         this.Shape17.rotateAngleY = 0.0F;
         this.Shape17.rotateAngleZ = 0.0F;
         this.Shape17.mirror = false;
         this.Shape1.addChild(this.Shape17);
         this.Shape18 = new ModelRenderer(this);
         this.Shape18.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape18.setRotationPoint(4.0F, -5.0F, 0.0F);
         this.Shape18.rotateAngleX = 0.0F;
         this.Shape18.rotateAngleY = 0.0F;
         this.Shape18.rotateAngleZ = 0.0F;
         this.Shape18.mirror = false;
         this.Shape1.addChild(this.Shape18);
         this.Shape19 = new ModelRenderer(this);
         this.Shape19.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape19.setRotationPoint(5.0F, -4.0F, 0.0F);
         this.Shape19.rotateAngleX = 0.0F;
         this.Shape19.rotateAngleY = 0.0F;
         this.Shape19.rotateAngleZ = 0.0F;
         this.Shape19.mirror = false;
         this.Shape1.addChild(this.Shape19);
         this.Shape26 = new ModelRenderer(this);
         this.Shape26.addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
         this.Shape26.setRotationPoint(6.0F, -6.0F, 0.0F);
         this.Shape26.rotateAngleX = 0.0F;
         this.Shape26.rotateAngleY = 0.0F;
         this.Shape26.rotateAngleZ = 0.0F;
         this.Shape26.mirror = false;
         this.Shape1.addChild(this.Shape26);
         this.Shape27 = new ModelRenderer(this);
         this.Shape27.addBox(0.0F, 0.0F, 0.0F, 1, 4, 1, 0.0F);
         this.Shape27.setRotationPoint(5.0F, -8.0F, 0.0F);
         this.Shape27.rotateAngleX = 0.0F;
         this.Shape27.rotateAngleY = 0.0F;
         this.Shape27.rotateAngleZ = 0.0F;
         this.Shape27.mirror = false;
         this.Shape1.addChild(this.Shape27);
         this.Shape28 = new ModelRenderer(this);
         this.Shape28.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
         this.Shape28.setRotationPoint(4.733333F, -8.266666F, 0.0F);
         this.Shape28.rotateAngleX = 0.0F;
         this.Shape28.rotateAngleY = 0.0F;
         this.Shape28.rotateAngleZ = 0.0F;
         this.Shape28.mirror = false;
         this.Shape1.addChild(this.Shape28);
      }
   }
}
