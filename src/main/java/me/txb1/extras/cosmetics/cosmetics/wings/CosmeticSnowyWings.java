package me.txb1.extras.cosmetics.cosmetics.wings;

import me.txb1.EsdeathClient;
import me.txb1.extras.cosmetics.CosmeticBase;
import me.txb1.utils.EsdeathUtils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;

public class CosmeticSnowyWings extends CosmeticBase {
   private WingRenderer renderer;

   @Override
   public void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if ((EsdeathUtils.isOnline(var1.getName()))) {
         if ((EsdeathClient.getInstance().getPlayer(var1.getUniqueID().toString()).getCosmetics().contains("snowywings"))) {
            this.renderer.renderWings(var1, 1.0F);
         }
      }
   }

   public CosmeticSnowyWings(RenderPlayer var1) {
      super(var1, "SnowyWings");
      this.renderer = new WingRenderer("wings.png", 255.0F, 255.0F, 255.0F, 0.75F, 4.0F, 10.0F, 0.0F);
      this.renderer.colorKey = "SnowyWings";
   }

}
