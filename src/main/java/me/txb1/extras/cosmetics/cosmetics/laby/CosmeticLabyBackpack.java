package me.txb1.extras.cosmetics.cosmetics.laby;

import me.txb1.extras.cosmetics.CosmeticBase;
import me.txb1.extras.cosmetics.CosmeticController;
import me.txb1.extras.cosmetics.CosmeticModelBase;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

// Ported from LabyMod 3's CosmeticBackPack (net.labymod.user.cosmetic.cosmetics.shop.body). A static
// backpack sitting on the player's upper back; geometry/texture-offsets are the LabyMod originals,
// re-rooted onto the body. Shown in the Labymod category; equip from the cosmetics GUI. Position is
// tunable via the X/Y/Z offset editor if it needs nudging on a given skin.
public class CosmeticLabyBackpack extends CosmeticBase {
   public static final String NAME = "Backpack";
   public static final int ID = 20; // LabyMod cosmetic id
   private static final ResourceLocation TEXTURE = new ResourceLocation("EsdeathClient/laby_backpack.png");
   private final Model model;

   public CosmeticLabyBackpack(RenderPlayer renderPlayer) {
      super(renderPlayer, NAME);
      CosmeticController.markLabymod(NAME);
      this.model = new Model(renderPlayer);
   }

   @Override
   public void render(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks,
                      float ageInTicks, float headYaw, float headPitch, float scale) {
      // Render from the player's real LabyMod equipment (userdata id 20); also allow the GUI equip as
      // a self-preview so you can see it without owning it on LabyMod.
      boolean isSelf = player == net.minecraft.client.Minecraft.getMinecraft().thePlayer;
      boolean show = me.txb1.extras.cosmetics.laby.LabyUserData.owns(player.getUniqueID(), ID)
         || (isSelf && CosmeticController.isActive(NAME));
      if (!show) {
         return;
      }
      GlStateManager.pushMatrix();
      this.renderPlayer.bindTexture(TEXTURE);
      if (player.isSneaking()) {
         GL11.glTranslated(0.0, 0.2, 0.0);
      }
      CosmeticController.apply(NAME);
      this.model.render(player, scale);
      GlStateManager.popMatrix();
   }

   private final class Model extends CosmeticModelBase {
      private final ModelRenderer root;

      Model(RenderPlayer renderPlayer) {
         super(renderPlayer);
         // LabyMod backpack texture sheet is 38x16; boxes below mirror its addBox calls, re-parented
         // under a single root anchored to the upper back (z+ is behind the player, y+ is down).
         this.root = box(0, 0, -3.5F, 0.0F, -0.5F, 7, 9, 2);
         this.root.setRotationPoint(0.0F, 1.6F, 2.4F);

         this.root.addChild(rp(box(34, 0, -0.5F, 0.0F, -0.5F, 1, 2, 1), 0.0F, 2.2F, 1.7F));     // buckle
         this.root.addChild(rp(box(18, 0, -3.5F, 0.0F, -0.5F, 7, 4, 1), 0.0F, 5.8F, 1.9F));     // flap top
         this.root.addChild(rp(box(18, 5, -3.5F, 0.0F, -0.5F, 7, 3, 1), 0.0F, 0.3F, 1.5F));     // flap face
         this.root.addChild(rp(box(0, 11, -4.0F, 0.0F, -0.5F, 8, 3, 2), 0.0F, 6.7F, 0.1F));     // base
         this.root.addChild(rp(box(34, 3, -4.0F, 0.0F, -0.5F, 1, 3, 1), 0.5F, -1.3F, -0.2F));   // strap
         this.root.addChild(rp(box(20, 12, -4.0F, 0.0F, -0.5F, 1, 1, 3), 0.6F, -1.6F, -2.4F));  // strap clip
      }

      private ModelRenderer box(int u, int v, float ox, float oy, float oz, int sx, int sy, int sz) {
         ModelRenderer m = new ModelRenderer(this.playerModel, u, v);
         m.setTextureSize(38, 16);
         m.addBox(ox, oy, oz, sx, sy, sz);
         return m;
      }

      private ModelRenderer rp(ModelRenderer m, float x, float y, float z) {
         m.setRotationPoint(x, y, z);
         return m;
      }

      void render(Entity entity, float scale) {
         // follow the body's lean so the pack stays on the back
         this.root.rotateAngleX = this.playerModel.bipedBody.rotateAngleX;
         this.root.rotateAngleY = this.playerModel.bipedBody.rotateAngleY;
         this.root.render(scale);
      }
   }
}
