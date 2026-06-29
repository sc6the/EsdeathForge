package me.txb1.extras.cosmetics.cosmetics.wings;

import me.txb1.EsdeathClient;
import me.txb1.extras.cosmetics.CosmeticBase;
import me.txb1.utils.EsdeathUtils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;

public class CosmeticWings extends CosmeticBase {
   private WingRenderer renderer;

   @Override
   public void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if ((EsdeathUtils.isOnline(var1.getName()))) {
         if ((EsdeathClient.getInstance().getPlayer(var1.getUniqueID().toString()).getCosmetics().contains("wings"))) {
            this.renderer.renderWings(var1, 1.0F);
         }
      }
   }

   public CosmeticWings(RenderPlayer var1) {
      super(var1, "Wings");
      this.renderer = new WingRenderer("wings.png", 0.9F, 0.3F, 1.0F, 0.8F, 2.0F, 29.0F, 0.0F);
      this.renderer.colorKey = "Wings";
   }

}
