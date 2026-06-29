package me.proxycracked.universalaccountmanager.skin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;

// Standalone 3D player preview. Renders a ModelPlayer with a given skin
// texture in idle pose at an arbitrary screen position. Doesn't go through
// EntityRenderer / NetworkPlayerInfo, so it works on any GUI screen and
// doesn't require swapping the local player's skin.
public final class SkinPreview3D {
  private static final ModelPlayer MODEL_CLASSIC = new ModelPlayer(0.0f, false);
  private static final ModelPlayer MODEL_SLIM    = new ModelPlayer(0.0f, true);
  private static final float UNIT = 0.0625F; // 1/16 — ModelRenderer scale

  private SkinPreview3D() {}

  // posX/posY: screen-space anchor (the player's feet).
  // scale: roughly the on-screen height of the player in pixels (45 = small).
  // bodyYaw: degrees, 0=facing camera.
  // headPitch: degrees, head tilt only (body stays upright).
  public static void draw(int posX, int posY, int scale, float bodyYaw, float headPitch,
                          ResourceLocation skin, boolean slim) {
    draw(posX, posY, scale, bodyYaw, headPitch, skin, slim, false);
  }

  // fullBright: skip directional lighting and render the skin at uniform full brightness. Use this
  // for a spinning/turntable preview — model-relative directional lighting makes the model flicker
  // dark as it rotates through the fixed light, and the large GUI scale leaves the normals un-rescaled
  // (so even the static lighting comes out very dim). Full-bright shows the true skin colours at every
  // angle.
  public static void draw(int posX, int posY, int scale, float bodyYaw, float headPitch,
                          ResourceLocation skin, boolean slim, boolean fullBright) {
    if (skin == null) return;
    Minecraft mc = Minecraft.getMinecraft();
    ModelPlayer model = slim ? MODEL_SLIM : MODEL_CLASSIC;

    // Idle pose (no animation). Reset every frame so consecutive calls don't
    // accumulate rotations.
    resetPose(model);
    model.bipedHead.rotateAngleX = (float) Math.toRadians(headPitch);
    model.bipedHeadwear.rotateAngleX = model.bipedHead.rotateAngleX;

    GlStateManager.enableColorMaterial();
    GlStateManager.pushMatrix();
    GlStateManager.translate((float) posX, (float) posY, 50.0F);
    GlStateManager.scale((float) scale, (float) scale, (float) scale);

    if (fullBright) {
      // no directional lighting — the texture is drawn at full brightness, consistent at every spin
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableLighting();
    } else {
      // Standard MC lighting setup: apply +135° Y, enable lighting (which
      // captures the current orientation as the light direction), then
      // restore so the geometry isn't actually rotated.
      GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
      RenderHelper.enableStandardItemLighting();
      GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
    }

    GlStateManager.rotate(bodyYaw, 0.0F, 1.0F, 0.0F);

    // ModelPlayer's intrinsic origin is at the shoulders. Model coords run
    // head (Y=-0.5 after UNIT) → feet (Y=+1.5). Shifting origin by -1.5
    // in model Y puts the feet at the (posX, posY) anchor. Note we use a
    // plain positive scale and skip the rotate(180,Z) trick GuiInventory
    // uses — that trick only works because RenderLivingBase.doRender
    // adds a compensating rotate(180,Y) downstream, which we don't have
    // when rendering ModelRenderers directly.
    GlStateManager.translate(0.0F, -1.5F, 0.0F);

    GlStateManager.enableDepth();
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    GlStateManager.color(1f, 1f, 1f, 1f);
    mc.getTextureManager().bindTexture(skin);

    // Bases first, then overlays. Keeps geometry simple and avoids the
    // disableCull() path used by MC's RenderLivingBase (which can flip
    // the perceived facing for raw ModelRenderer rendering).
    model.bipedHead.render(UNIT);
    model.bipedBody.render(UNIT);
    model.bipedRightArm.render(UNIT);
    model.bipedLeftArm.render(UNIT);
    model.bipedRightLeg.render(UNIT);
    model.bipedLeftLeg.render(UNIT);
    model.bipedHeadwear.render(UNIT);
    model.bipedBodyWear.render(UNIT);
    model.bipedRightArmwear.render(UNIT);
    model.bipedLeftArmwear.render(UNIT);
    model.bipedRightLegwear.render(UNIT);
    model.bipedLeftLegwear.render(UNIT);

    GlStateManager.disableBlend();
    GlStateManager.popMatrix();
    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableRescaleNormal();
    GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
    GlStateManager.disableTexture2D();
    GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
  }

  private static void resetPose(ModelPlayer m) {
    m.bipedHead.rotateAngleX = m.bipedHead.rotateAngleY = m.bipedHead.rotateAngleZ = 0;
    m.bipedHeadwear.rotateAngleX = m.bipedHeadwear.rotateAngleY = m.bipedHeadwear.rotateAngleZ = 0;
    m.bipedBody.rotateAngleX = m.bipedBody.rotateAngleY = m.bipedBody.rotateAngleZ = 0;
    m.bipedRightArm.rotateAngleX = m.bipedRightArm.rotateAngleY = m.bipedRightArm.rotateAngleZ = 0;
    m.bipedLeftArm.rotateAngleX = m.bipedLeftArm.rotateAngleY = m.bipedLeftArm.rotateAngleZ = 0;
    m.bipedRightLeg.rotateAngleX = m.bipedRightLeg.rotateAngleY = m.bipedRightLeg.rotateAngleZ = 0;
    m.bipedLeftLeg.rotateAngleX = m.bipedLeftLeg.rotateAngleY = m.bipedLeftLeg.rotateAngleZ = 0;
    m.bipedBodyWear.rotateAngleX = m.bipedBodyWear.rotateAngleY = m.bipedBodyWear.rotateAngleZ = 0;
    m.bipedRightArmwear.rotateAngleX = m.bipedRightArmwear.rotateAngleY = m.bipedRightArmwear.rotateAngleZ = 0;
    m.bipedLeftArmwear.rotateAngleX = m.bipedLeftArmwear.rotateAngleY = m.bipedLeftArmwear.rotateAngleZ = 0;
    m.bipedRightLegwear.rotateAngleX = m.bipedRightLegwear.rotateAngleY = m.bipedRightLegwear.rotateAngleZ = 0;
    m.bipedLeftLegwear.rotateAngleX = m.bipedLeftLegwear.rotateAngleY = m.bipedLeftLegwear.rotateAngleZ = 0;
    m.isSneak = false;
    m.isChild = false;
    m.isRiding = false;
  }
}
