package me.txb1.extras.cosmetics.cosmetics.laby;

import me.txb1.extras.cosmetics.CosmeticBase;
import me.txb1.extras.cosmetics.CosmeticController;
import me.txb1.extras.cosmetics.CosmeticModelBase;
import me.txb1.extras.cosmetics.laby.LabyTextures;
import me.txb1.extras.cosmetics.laby.LabyUserData;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;

// Ported from LabyMod 3 CosmeticCap (id 19). A cap on the head whose texture is the player's actual
// uploaded LabyMod cap image, fetched per-user from the LabyMod CDN (cosmetics/19/textures/<uuid>.png
// via LabyTextures). Only renders for players who really have it equipped (laby.net userdata id 19).
public class CosmeticLabyCap extends CosmeticBase {
   public static final String NAME = "Cap";
   public static final int ID = 19;
   // LabyMod's built-in default cap texture id (used when the user's data is "1").
   private static final String DEFAULT_TEX = "7a9c8635-d64f-47ee-a373-5faceffc1915";
   private final Model model;

   public CosmeticLabyCap(RenderPlayer renderPlayer) {
      super(renderPlayer, NAME);
      CosmeticController.markLabymod(NAME);
      this.model = new Model(renderPlayer);
   }

   @Override
   public void render(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks,
                      float ageInTicks, float headYaw, float headPitch, float scale) {
      if (!LabyUserData.owns(player.getUniqueID(), ID)) {
         return;
      }
      String[] data = LabyUserData.data(player.getUniqueID(), ID);
      String texUuid = (data.length > 1 && data[1] != null && !data[1].equals("1")) ? data[1] : DEFAULT_TEX;
      ResourceLocation tex = LabyTextures.get(ID, texUuid);
      if (tex == null) {
         return; // texture not ready / missing
      }
      boolean snapBack = data.length > 0 && "1".equals(data[0]);

      GlStateManager.pushMatrix();
      this.renderPlayer.bindTexture(tex);
      CosmeticController.apply(NAME);
      this.model.render(player, scale, snapBack);
      GlStateManager.popMatrix();
   }

   private final class Model extends CosmeticModelBase {
      private final ModelRenderer cap;

      Model(RenderPlayer renderPlayer) {
         super(renderPlayer);
         // texture sheet 168x180; geometry from LabyMod CosmeticCap.addModels
         this.cap = new ModelRenderer(this.playerModel, 0, 0);
         this.cap.setTextureSize(168, 180);
         this.cap.rotateAngleX = (float) Math.toRadians(-90.0D);
         this.cap.setTextureOffset(0, 0).addBox(-4.0F, -4.0F, -6.0F, 8, 11, 1);
         this.cap.setTextureOffset(18, 0).addBox(-4.0F, -4.0F, -8.0F, 8, 8, 2);
         this.cap.setTextureOffset(38, 0).addBox(-4.0F, -3.0F, -9.001F, 8, 6, 1);
         this.cap.setTextureOffset(38, 7).addBox(-3.0F, -4.0F, -9.001F, 6, 1, 1);
         this.cap.setTextureOffset(38, 9).addBox(-3.0F, 3.0F, -9.001F, 6, 1, 1);
         this.cap.setTextureOffset(56, 0).addBox(-3.0F, 7.0F, -6.0F, 6, 1, 1);
      }

      void render(AbstractClientPlayer player, float scale, boolean snapBack) {
         if (player.isSneaking()) {
            GlStateManager.translate(0.0F, scale, 0.0F);
         }
         GlStateManager.rotate((float) Math.toDegrees(this.playerModel.bipedHead.rotateAngleY), 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate((float) Math.toDegrees(this.playerModel.bipedHead.rotateAngleX), 1.0F, 0.0F, 0.0F);
         float up = 1.1252F;
         GlStateManager.scale(up, up, up);
         if (snapBack) {
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         }
         this.cap.render(scale);
      }
   }
}
