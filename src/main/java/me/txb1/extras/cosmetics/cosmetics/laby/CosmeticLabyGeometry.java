package me.txb1.extras.cosmetics.cosmetics.laby;

import java.util.Map;
import java.util.regex.Pattern;
import me.txb1.extras.cosmetics.CosmeticController;
import me.txb1.extras.cosmetics.laby.LabyTextures;
import me.txb1.extras.cosmetics.laby.LabyUserData;
import me.txb1.extras.cosmetics.laby.geo.LabyCosmetics;
import me.txb1.extras.cosmetics.laby.geo.LabyGeo;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

// Generic LabyMod cosmetic renderer. For EVERY player, reads which cosmetics they actually have
// equipped (LabyUserData -> laby.net), fetches each one's Bedrock geometry + texture
// (LabyCosmetics -> dl.labymod.net) and renders it at the right attachment point. One layer covers
// the whole catalog and always shows (no toggle / no GUI entry).
public class CosmeticLabyGeometry implements LayerRenderer<AbstractClientPlayer> {

   private static final Pattern UUID_RE =
      Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

   private final RenderPlayer renderPlayer;

   public CosmeticLabyGeometry(RenderPlayer renderPlayer) {
      this.renderPlayer = renderPlayer;
   }

   @Override
   public boolean shouldCombineTextures() {
      return false;
   }

   @Override
   public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks,
                             float ageInTicks, float netHeadYaw, float headPitch, float scale) {
      if (player == null || !player.hasPlayerInfo() || player.isInvisible()) {
         return;
      }
      Map<Integer, String[]> owned = LabyUserData.get(player.getUniqueID());
      if (owned == null || owned.isEmpty()) {
         return;
      }
      ModelBiped pm = (ModelBiped) this.renderPlayer.getMainModel();
      for (Map.Entry<Integer, String[]> e : owned.entrySet()) {
         int id = e.getKey();
         if (id == 0) {
            continue; // cape handled by the cape system / priority
         }
         LabyCosmetics.Meta m = LabyCosmetics.meta(id);
         if (m == null || !"COSMETIC".equalsIgnoreCase(m.type)) {
            continue; // index not loaded yet, or a pet (not supported here)
         }
         LabyGeo geo = LabyCosmetics.geometry(id);
         if (geo == null) {
            continue; // geometry still loading / failed
         }
         ResourceLocation tex = resolveTexture(m, id, e.getValue(), player);
         if (tex == null) {
            continue;
         }
         renderOne(pm, player, geo, tex, m);
      }
      CosmeticController.resetRenderState();
   }

   private ResourceLocation resolveTexture(LabyCosmetics.Meta m, int id, String[] data, AbstractClientPlayer player) {
      if ("MOJANG_BOUND".equalsIgnoreCase(m.textureType)) {
         return player.getLocationSkin();
      }
      if ("USER_BOUND".equalsIgnoreCase(m.textureType) && data != null) {
         for (String d : data) {
            if (d != null && UUID_RE.matcher(d).matches()) {
               ResourceLocation rl = LabyTextures.get(id, d);
               if (rl != null) {
                  return rl;
               }
            }
         }
      }
      return LabyCosmetics.typeTexture(id); // TYPE_BOUND default (also USER_BOUND fallback)
   }

   private void renderOne(ModelBiped pm, AbstractClientPlayer player, LabyGeo geo, ResourceLocation tex, LabyCosmetics.Meta m) {
      GlStateManager.pushMatrix();
      GlStateManager.enableRescaleNormal();
      GlStateManager.color(1F, 1F, 1F, 1F);
      GlStateManager.enableTexture2D();
      this.renderPlayer.bindTexture(tex);

      String att = m.attachedTo == null ? "BODY" : m.attachedTo.toUpperCase();
      if ("HEAD".equals(att)) {
         if (player.isSneaking()) {
            GlStateManager.translate(0F, 0.0625F, 0F);
         }
         GlStateManager.rotate((float) Math.toDegrees(pm.bipedHead.rotateAngleY), 0F, 1F, 0F);
         GlStateManager.rotate((float) Math.toDegrees(pm.bipedHead.rotateAngleX), 1F, 0F, 0F);
      } else if ("ARM".equals(att) || "LEG".equals(att)) {
         ModelRenderer limb = "ARM".equals(att) ? pm.bipedRightArm : pm.bipedRightLeg;
         GlStateManager.translate(limb.rotationPointX * 0.0625F, limb.rotationPointY * 0.0625F, limb.rotationPointZ * 0.0625F);
         GlStateManager.rotate((float) Math.toDegrees(limb.rotateAngleZ), 0F, 0F, 1F);
         GlStateManager.rotate((float) Math.toDegrees(limb.rotateAngleY), 0F, 1F, 0F);
         GlStateManager.rotate((float) Math.toDegrees(limb.rotateAngleX), 1F, 0F, 0F);
      } else { // BODY (default)
         GlStateManager.rotate((float) Math.toDegrees(pm.bipedBody.rotateAngleY), 0F, 1F, 0F);
         GlStateManager.rotate((float) Math.toDegrees(pm.bipedBody.rotateAngleX), 1F, 0F, 0F);
      }

      geo.root.render(0.0625F);
      GlStateManager.popMatrix();
   }
}
