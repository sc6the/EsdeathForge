package me.txb1.extras.cosmetics.cosmetics.laby;

import me.txb1.extras.cosmetics.CosmeticBase;
import me.txb1.extras.cosmetics.CosmeticController;
import me.txb1.extras.cosmetics.CosmeticModelBase;
import me.txb1.extras.cosmetics.laby.LabyUserData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

// Ported from LabyMod 3 CosmeticWolfTail (id 1). A wolf tail on the lower back that sways with walking
// and droops with the player's health, exactly like LabyMod. Renders from the player's real equipped
// LabyMod cosmetics (laby.net userdata id 1) with their chosen colour; self GUI-equip previews it.
public class CosmeticLabyWolfTail extends CosmeticBase {
   public static final String NAME = "Wolf Tail";
   public static final int ID = 1;
   private static final ResourceLocation TEXTURE = new ResourceLocation("EsdeathClient/laby_wolftail.png");
   private final Model model;

   public CosmeticLabyWolfTail(RenderPlayer renderPlayer) {
      super(renderPlayer, NAME);
      CosmeticController.markLabymod(NAME);
      this.model = new Model(renderPlayer);
   }

   @Override
   public void render(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks,
                      float ageInTicks, float headYaw, float headPitch, float scale) {
      boolean isSelf = player == Minecraft.getMinecraft().thePlayer;
      boolean owns = LabyUserData.owns(player.getUniqueID(), ID);
      if (!owns && !(isSelf && CosmeticController.isActive(NAME))) {
         return;
      }
      int color = owns ? parseColor(LabyUserData.data(player.getUniqueID(), ID), 0) : 0xFFFFFF;

      GlStateManager.pushMatrix();
      this.renderPlayer.bindTexture(TEXTURE);
      CosmeticController.apply(NAME, ((color >> 16) & 255) / 255F, ((color >> 8) & 255) / 255F, (color & 255) / 255F);
      this.model.render(player, limbSwing, limbSwingAmount, scale);
      GlStateManager.popMatrix();
   }

   private static int parseColor(String[] data, int idx) {
      if (data != null && data.length > idx && data[idx] != null) {
         try {
            return Integer.parseInt(data[idx].replace("#", ""), 16);
         } catch (NumberFormatException ignored) {
         }
      }
      return 0xFFFFFF;
   }

   private final class Model extends CosmeticModelBase {
      private final ModelRenderer tail;

      Model(RenderPlayer renderPlayer) {
         super(renderPlayer);
         this.tail = new ModelRenderer(this.playerModel, 0, 0);
         this.tail.setTextureSize(8, 10);
         this.tail.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2);
         this.tail.setRotationPoint(-0.2F, 10.0F, 3.0F);
      }

      void render(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float scale) {
         // walk sway (LabyMod setRotationAngles)
         this.tail.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
         this.tail.rotateAngleY = limbSwingAmount;

         if (player.isSneaking()) {
            GlStateManager.translate(0.0F, 0.2F, -0.25F);
            GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
         } else {
            GlStateManager.translate(0.0F, 0.1F, -0.25F);
            GlStateManager.rotate(15.0F, 1.0F, 0.0F, 0.0F);
         }

         float health = player.getHealth();
         if (health > 20.0F || Float.isNaN(health)) {
            health = 20.0F;
         }
         if (health < 0.0F) {
            health = 0.0F;
         }
         GlStateManager.translate(0.0F, health / 80.0F, health / 50.0F * -1.0F);
         GlStateManager.rotate(health * 2.0F, 1.0F, 0.0F, 0.0F);

         this.tail.render(scale);
      }
   }
}
