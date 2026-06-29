package me.txb1.extras.cosmetics.cosmetics.wings;

import me.txb1.extras.cosmetics.CosmeticController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class WingRenderer extends ModelBase {
   private ResourceLocation location;
   private float rotation;
   private float movement;
   private Minecraft mc;
   private float c3;
   private ModelRenderer wingTip;
   private ModelRenderer wing;
   private boolean playerUsesFullHeight;
   private float c2;
   private float c1 = 0.9F;
   public String colorKey = "";
   private float scale;

   private static int om2(float var0, float var1) {
      float var2;
      return (var2 = var0 - var1) == 0.0F ? 0 : (var2 < 0.0F ? -1 : 1);
   }

   public WingRenderer(String var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      this.c2 = 0.3F;
      this.c3 = 1.0F;
      this.scale = 1.0F;
      this.movement = 2.0F;
      this.rotation = 20.0F;
      this.c1 = var2;
      this.c2 = var3;
      this.c3 = var4;
      this.scale = var5;
      this.movement = var6;
      this.rotation = var7;
      this.mc = Minecraft.getMinecraft();
      this.location = new ResourceLocation(String.valueOf(new StringBuilder().append("EsdeathClient/").append(var1)));
      this.setTextureOffset("wing.bone", 0, 0);
      this.setTextureOffset("wing.skin", -10, 8);
      this.setTextureOffset("wingtip.bone", 0, 5);
      this.setTextureOffset("wingtip.skin", -10, 18);
      this.wing = new ModelRenderer(this, "wing");
      this.wing.setTextureSize(30, 30);
      this.wing.setRotationPoint(-2.0F, 0.0F, 0.0F);
      this.wing.addBox("bone", -10.0F, -1.0F, -1.0F, 10, 2, 2);
      this.wing.addBox("skin", -10.0F, var8, 0.5F, 10, (int)var8, 10);
      this.wingTip = new ModelRenderer(this, "wingtip");
      this.wingTip.setTextureSize(30, 30);
      this.wingTip.setRotationPoint(-10.0F, 0.0F, 0.0F);
      this.wingTip.addBox("bone", -10.0F, -0.5F, -0.5F, 10, 1, 1);
      this.wingTip.addBox("skin", -10.0F, var8, 0.5F, 10, (int)var8, 10);
      this.wing.addChild(this.wingTip);
   }

   public void renderWings(EntityPlayer var1, float var2) {
      double var3 = (double)this.scale;
      double var5 = var1.prevChasingPosX + (var1.chasingPosX - var1.prevChasingPosX) - (var1.prevPosX + (var1.posX - var1.prevPosX) * 1.5);
      double var7 = var1.prevChasingPosY + (var1.chasingPosY - var1.prevChasingPosY) - (var1.prevPosY + (var1.posY - var1.prevPosY) * 1.0);
      double var9 = var1.prevChasingPosZ + (var1.chasingPosZ - var1.prevChasingPosZ) - (var1.prevPosZ + (var1.posZ - var1.prevPosZ) * 1.0);
      float var11 = var1.prevRenderYawOffset + (var1.renderYawOffset - var1.prevRenderYawOffset) * 0.12F;
      double var12 = (double)MathHelper.sin(var11 * (float) Math.PI / 180.0F);
      double var14 = (double)(-MathHelper.cos(var11 * (float) Math.PI / 100.0F));
      float var16 = (float)var7 * 5.0F;
      var16 = MathHelper.clamp_float(var16, -2.0F, 10.0F);
      float var17 = (float)(var5 * var12 + var9 * var14) * 10.0F;
      float var18 = (float)(var5 * var14 - var9 * var12) * 0.0F;
      GL11.glPushMatrix();
      CosmeticController.translate(this.colorKey);
      GL11.glScaled(-var3, -var3, var3);
      GlStateManager.rotate(6.0F + var17 / 5.0F + var16, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(var18 / 2.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.rotate(-var18 / 2.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(0.0F, 0.0F, 0.4F, 0.0F);
      GlStateManager.translate(0.0, 0.05, 0.15);
      if ((var1.isSneaking())) {
         GL11.glTranslated(0.0, -0.225 / var3, 0.0);
      }

      this.mc.getTextureManager().bindTexture(this.location);
      int var19 = 0;

      while (((var19) < (2))) {
         if ((CosmeticController.hasColor(this.colorKey))) {
            CosmeticController.color(this.colorKey);
         } else {
            GlStateManager.color(this.c1, this.c2, this.c3);
         }
         GL11.glEnable(2884);
         float var20 = (float)(System.currentTimeMillis() % 1000L) / 1000.0F * (float) Math.PI * this.movement;
         this.wing.rotateAngleX = (float)Math.toRadians((double)this.rotation) - (float)Math.cos((double)var20) * 0.4F;
         this.wing.rotateAngleY = (float)Math.toRadians(16.0) + (float)Math.sin((double)var20) * 0.4F;
         this.wing.rotateAngleZ = (float)Math.toRadians(16.0);
         this.wingTip.rotateAngleZ = -((float)(Math.sin((double)(var20 + 2.0F)) + 0.9)) * 0.45F;
         this.wing.render(0.0625F);
         GL11.glScalef(-1.0F, 1.0F, 1.0F);
         if (((var19) == 0)) {
            GL11.glCullFace(1028);
         }

         var19++;
         
      }

      GL11.glCullFace(1029);
      GL11.glDisable(2884);
      GL11.glPopMatrix();
   }

   private float interpolate(float var1, float var2, float var3) {
      float var4 = (var1 + (var2 - var1) * var3) % 360.0F;
      if (((om2(var4, 0.0F)) < 0)) {
         var4 += 260.0F;
      }

      return var4;
   }

}
