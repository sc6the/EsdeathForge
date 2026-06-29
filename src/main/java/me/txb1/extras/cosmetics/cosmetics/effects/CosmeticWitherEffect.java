package me.txb1.extras.cosmetics.cosmetics.effects;

import me.txb1.EsdeathClient;
import me.txb1.extras.cosmetics.CosmeticBase;
import me.txb1.utils.EsdeathUtils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class CosmeticWitherEffect extends CosmeticBase {
   private RenderPlayer witherRenderer = null;
   private static final ResourceLocation WITHER_ARMOR = new ResourceLocation("textures/entity/wither/wither_armor.png");
   private ModelPlayer witherModel;

   @Override
   public void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if ((EsdeathUtils.isOnline(var1.getName()))) {
         if ((EsdeathClient.getInstance().getPlayer(var1.getUniqueID().toString()).getCosmetics().contains("withereffect"))) {
            this.witherRenderer.bindTexture(WITHER_ARMOR);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            float var9 = (float)var1.ticksExisted + var4;
            float var10 = MathHelper.cos(var9 * 0.02F) * 3.0F;
            float var11 = var9 * 0.01F;
            GlStateManager.translate(var10, var11, 0.0F);
            GlStateManager.matrixMode(5888);
            GlStateManager.enableBlend();
            // honor the user's custom color overlay + transparency (default ~0.5 grey brightness)
            me.txb1.extras.cosmetics.CosmeticController.applyEffect("WitherEffect", 0.5F);
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(1, 1);
            this.witherModel.setLivingAnimations(var1, var2, var3, var4);
            this.witherModel.setModelAttributes(this.witherRenderer.getMainModel());
            this.witherModel.render(var1, var2, var3, var4, var6, var7, var8);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(5888);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
         }
      }
   }

   public CosmeticWitherEffect(RenderPlayer var1) {
      super(var1, "WitherEffect");
      this.witherModel = new ModelPlayer(0.5F, true);
      this.witherRenderer = var1;
   }

}
