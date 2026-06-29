package me.txb1.extras.cosmetics;

import me.txb1.EsdeathClient;
import me.txb1.utils.EsdeathUtils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import org.lwjgl.opengl.GL11;

public abstract class CosmeticBase implements LayerRenderer<AbstractClientPlayer> {
   protected final RenderPlayer renderPlayer;

   public CosmeticBase(RenderPlayer var1, String var2) {
      this.renderPlayer = var1;
      CosmeticController.addCos(var2);
   }

   @Override
   public boolean shouldCombineTextures() {
      return false;
   }

   public void doRenderLayer(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      // OFFLINE PATCH: dropped the isConnected()/Connector gates; the per-cosmetic
      // render() still checks isOnline + the player's cosmetic list.
      if (!((var1) == null)) {
         if ((var1.hasPlayerInfo()) && !(var1.isInvisible())) {
            // Cosmetics always win the depth test over the player's 2nd skin layer (and stack cleanly
            // over each other), at ANY scale: the overlay boxes are inflated ~0.25px past the base
            // body, so a cosmetic hugging — or scaled down into — the body would z-fight / sink under
            // the overlay. A large negative polygon offset pulls the cosmetic's fragments toward the
            // camera in depth by a constant amount that's comfortably bigger than the overlay's
            // sub-pixel proudness (so it always draws on top of it) yet far smaller than the depth gap
            // of behind-the-body cosmetics like tails/wings (≥0.25 block back), so those stay correctly
            // occluded by the body and don't punch through it. Restored immediately after.
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(-1.5F, -1500.0F);
            this.render(var1, var2, var3, var4, var5, var6, var7, var8);
            GL11.glPolygonOffset(0.0F, 0.0F);
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
            // restore color/blend so per-cosmetic tint+transparency doesn't leak to the next layer
            CosmeticController.resetRenderState();
         }
      }
   }

   public abstract void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8);

}
