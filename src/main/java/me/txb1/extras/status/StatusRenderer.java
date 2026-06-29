package me.txb1.extras.status;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

// Draws a billboard label (the local status) above an entity's head — same transform/look as the
// vanilla nametag, sat one line higher, with a configurable size multiplier. Called from a
// RenderLivingEvent.Specials.Post handler so x/y/z are the entity's render offsets.
public final class StatusRenderer {
   private StatusRenderer() {
   }

   public static void render(Entity entity, double x, double y, double z, String text, float sizeMul, float yOffset) {
      if (text == null || text.isEmpty()) {
         return;
      }
      Minecraft mc = Minecraft.getMinecraft();
      RenderManager rm = mc.getRenderManager();
      FontRenderer fr = mc.fontRendererObj;
      if (rm == null || fr == null) {
         return;
      }

      float scale = 0.016666668F * 1.6F * sizeMul;
      GlStateManager.pushMatrix();
      // above the head and a bit higher than the nametag (which sits at height + 0.5), plus the
      // user's vertical offset (Y slider)
      GlStateManager.translate((float) x, (float) y + entity.height + 0.5F + 0.30F + yOffset, (float) z);
      GL11.glNormal3f(0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(rm.playerViewX, 1.0F, 0.0F, 0.0F);
      GlStateManager.scale(-scale, -scale, scale);
      GlStateManager.disableLighting();
      GlStateManager.depthMask(false);
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

      int half = fr.getStringWidth(text) / 2;

      // no background — just the text (shadowed)
      GlStateManager.disableDepth();
      fr.drawString(text, -half, 0, 553648127);
      GlStateManager.enableDepth();
      GlStateManager.depthMask(true);
      fr.drawString(text, -half, 0, -1);

      GlStateManager.enableLighting();
      GlStateManager.disableBlend();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
   }
}
