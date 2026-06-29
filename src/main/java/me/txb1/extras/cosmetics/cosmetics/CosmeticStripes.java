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
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class CosmeticStripes extends CosmeticBase {
   private final CosmeticStripes.ModelTopHat modelTopHat;
   private static final ResourceLocation TEXTURE = new ResourceLocation("EsdeathClient/stripes.png");

   @Override
   public void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if ((CosmeticController.shouldRenderTopHat(var1))) {
         GlStateManager.pushMatrix();
         this.renderPlayer.bindTexture(TEXTURE);
         CosmeticController.apply("Stripes");
         this.modelTopHat.render(var1, var2, var3, var5, var6, var7, var8);
         GL11.glPopMatrix();
      }
   }

   public CosmeticStripes(RenderPlayer var1) {
      super(var1, "Stripes");
      this.modelTopHat = new CosmeticStripes.ModelTopHat(var1);
   }

   private class ModelTopHat extends CosmeticModelBase {
      int add;
      private Integer updown;
      private ModelRenderer[] blazeSticks;
      private String info;

      @Override
      public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
         if ((EsdeathUtils.isOnline(var1.getName()))) {
            if ((EsdeathClient.getInstance().getPlayer(var1.getUniqueID().toString()).getCosmetics().contains("stripes"))) {
               int var8 = 0;

               while (((var8) < (6))) {
                  this.blazeSticks[var8].render(var7);
                  var8++;
                  
               }

               float var13 = (float)this.add;
               float var9 = var13 * (float) Math.PI * -0.05F;
               if ((this.info.equals("up"))) {
                  Integer var10 = this.updown;
                  Integer var11 = this.updown = this.updown + 1;
                  if (((this.updown) > (350))) {
                     this.info = "down";
                     
                  }
               } else {
                  Integer var14 = this.updown;
                  Integer var16 = this.updown = this.updown - 1;
                  if (((this.updown) < (1))) {
                     this.info = "up";
                  }
               }

               int var15 = 0;

               while (((var15) < (6))) {
                  this.blazeSticks[var15].rotationPointY = (float)this.updown.intValue() / 65.0F - 4.4F;
                  this.blazeSticks[var15].rotationPointX = MathHelper.cos(var9) * 11.0F;
                  this.blazeSticks[var15].rotationPointZ = MathHelper.sin(var9) * 11.0F;
                  var9++;
                  var15++;
                  
               }
            }
         }
      }

      public ModelTopHat(RenderPlayer var2) {
         super(var2);
         this.blazeSticks = new ModelRenderer[6];
         this.updown = 0;
         this.info = "up";
         this.add = 0;
         String var3 = "Skids sind nicht erlaubt. Sobald Clients mit diesem Code online gehen werden die Inhaber angezeigt <:";
         int var4 = 0;

         while (((var4) < (6))) {
            this.blazeSticks[var4] = new ModelRenderer(this, 0, 16);
            this.blazeSticks[var4].addBox(0.0F, 0.0F, 0.0F, 1, 22, 1);
            var4++;
            
         }
      }

   }
}
