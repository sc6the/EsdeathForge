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

public class CosmeticHalo extends CosmeticBase {
   private static final ResourceLocation TEXTURE = new ResourceLocation("EsdeathClient/halo.png");
   private final CosmeticHalo.ModelTopHat modelTopHat;

   public CosmeticHalo(RenderPlayer var1) {
      super(var1, "Halo");
      this.modelTopHat = new CosmeticHalo.ModelTopHat(var1);
   }

   @Override
   public void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if ((CosmeticController.shouldRenderTopHat(var1))) {
         GlStateManager.pushMatrix();
         this.renderPlayer.bindTexture(TEXTURE);
         if ((var1.isSneaking())) {
            GL11.glTranslated(0.0, 0.225, 0.0);
         }

         Color var9 = new Color(249, 255, 68);
         GL11.glColor3f((float)var9.getRed(), (float)var9.getGreen(), (float)var9.getBlue());
         CosmeticController.apply("Halo");
         this.modelTopHat.render(var1, var2, var3, var5, var6, var7, var8);
         GL11.glPopMatrix();
      }
   }

   private class ModelTopHat extends CosmeticModelBase {
      private Integer updown;
      private ModelRenderer rim;
      private String info;

      public ModelTopHat(RenderPlayer var2) {
         super(var2);
         this.updown = 0;
         this.info = "up";
         String var3 = "Skids sind nicht erlaubt. Sobald Clients mit diesem Code online gehen werden die Inhaber angezeigt <:";
         this.rim = new ModelRenderer(this.playerModel, 0, 0);
         this.rim.addBox(-3.5F, -13.5F, -1.5F, 1, 1, 7);
         this.rim.addBox(-3.5F, -13.5F, -1.5F, 7, 1, 1);
         this.rim.addBox(2.5F, -13.5F, -1.5F, 1, 1, 7);
         this.rim.addBox(-3.5F, -13.5F, 4.5F, 7, 1, 1);
      }

      @Override
      public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
         if ((EsdeathUtils.isOnline(var1.getName()))) {
            if ((EsdeathClient.getInstance().getPlayer(var1.getUniqueID().toString()).getCosmetics().contains("halo"))) {
               this.rim.rotationPointX = 0.0F;
               this.rim.rotateAngleY = 0.0F;
               this.rim.rotateAngleY += 0.2F;
               if ((this.info.equals("up"))) {
                  Integer var8 = this.updown;
                  Integer var9 = this.updown = this.updown + 1;
                  if (((this.updown) > (150))) {
                     this.info = "down";
                     
                  }
               } else {
                  Integer var10 = this.updown;
                  Integer var11 = this.updown = this.updown - 1;
                  if (((this.updown) < (1))) {
                     this.info = "up";
                  }
               }

               this.rim.rotationPointY = (float)this.updown.intValue() / 80.0F + 1.4F;
               this.rim.render(var7);
            }
         }
      }

   }
}
