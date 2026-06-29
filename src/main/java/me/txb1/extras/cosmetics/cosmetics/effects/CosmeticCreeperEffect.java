package me.txb1.extras.cosmetics.cosmetics.effects;

import me.txb1.EsdeathClient;
import me.txb1.extras.cosmetics.CosmeticBase;
import me.txb1.utils.EsdeathUtils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;

public class CosmeticCreeperEffect extends CosmeticBase {
   private static final ResourceLocation LIGHTNING_TEXTURE = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
   private ModelPlayer creeperModel;
   private final RenderPlayer creeperRenderer;

   @Override
   public void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if ((EsdeathUtils.isOnline(var1.getName()))) {
         if ((EsdeathClient.getInstance().getPlayer(var1.getUniqueID().toString()).getCosmetics().contains("creepereffect"))) {
            boolean var9 = var1.isInvisible();
            int var10000;
            if (!(var9)) {
               var10000 = 1;
               
            } else {
               var10000 = 0;
            }

            GlStateManager.depthMask((var10000 != 0));
            this.creeperRenderer.bindTexture(LIGHTNING_TEXTURE);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            float var10 = (float)var1.ticksExisted + var4;
            GlStateManager.translate(var10 * 0.01F, var10 * 0.01F, 0.0F);
            GlStateManager.matrixMode(5888);
            GlStateManager.enableBlend();
            // honor the user's custom color overlay + transparency (default ~0.5 grey brightness)
            me.txb1.extras.cosmetics.CosmeticController.applyEffect("CreeperEffect", 0.5F);
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(1, 1);
            this.creeperModel.setModelAttributes(this.creeperRenderer.getMainModel());
            this.creeperModel.render(var1, var2, var3, var4, var6, var7, var8);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(5888);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.depthMask((boolean)var9);
         }
      }
   }

   public CosmeticCreeperEffect(RenderPlayer var1) {
      super(var1, "CreeperEffect");
      this.creeperModel = new ModelPlayer(2.0F, true);
      this.creeperRenderer = var1;
      this.creeperModel = var1.getMainModel();
   }

}
