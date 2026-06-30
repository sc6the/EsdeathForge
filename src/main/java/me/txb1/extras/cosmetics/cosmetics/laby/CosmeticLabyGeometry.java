package me.txb1.extras.cosmetics.cosmetics.laby;

import java.util.regex.Pattern;
import me.txb1.extras.cosmetics.CosmeticController;
import me.txb1.extras.cosmetics.laby.geo.LabyCosmetics;
import net.labymod.user.cosmetic.geometry.render.GeometryModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

// OFFLINE LabyMod cosmetic renderer. Renders the LOCAL player's selected LabyMod cosmetics only —
// the ones equipped in EsdeathForge's cosmetic menu (Labymod category), each drawn with the
// cosmetic's own `default_data` from the index. It deliberately does NOT read laby.net ownership and
// does NOT render other players' real cosmetics; LabyMod cosmetics behave like every other Esdeath
// offline cosmetic (pick from the menu, see it on yourself). Geometry/textures come from
// dl.labymod.net via LabyCosmetics; the catalog of selectable names is built by LabyOfflineCatalog.
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
      // Offline: only ever render on the local player (like the OAM cosmetics).
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.thePlayer == null || player != mc.thePlayer) {
         return;
      }
      LabyOfflineCatalog.ensureRegistered();
      java.util.ArrayList<String> active = CosmeticController.getActive();
      if (active.isEmpty()) {
         return;
      }
      ModelBiped pm = (ModelBiped) this.renderPlayer.getMainModel();
      for (String name : active) {
         if (!CosmeticController.isLabymod(name)) {
            continue;
         }
         int id = LabyOfflineCatalog.idOf(name);
         if (id <= 0) {
            continue;
         }
         LabyCosmetics.Meta m = LabyCosmetics.meta(id);
         if (m == null || !"COSMETIC".equalsIgnoreCase(m.type) || m.defaultData == null) {
            continue;
         }
         net.labymod.user.cosmetic.geometry.BlockBenchLoader loader = LabyCosmetics.geometryEngine(id);
         if (loader == null || loader.getModel() == null) {
            continue; // geometry still loading / failed
         }
         net.labymod.user.cosmetic.animation.AnimationLoader anim = LabyCosmetics.animationEngine(id);
         String[] data = effectiveData(name, m);
         ResourceLocation tex = resolveTexture(m, id, data, player);
         boolean canMirror = "ARM".equalsIgnoreCase(m.attachedTo) || "LEG".equalsIgnoreCase(m.attachedTo);
         // apply the per-cosmetic position/scale tweaks from the menu, around the whole cosmetic
         GlStateManager.pushMatrix();
         CosmeticController.translate(name);
         // LM3: a mirrored ARM/LEG cosmetic renders on BOTH limbs (right then left-mirrored).
         if (m.mirror && canMirror) {
            renderOne(pm, player, loader, anim, tex, m, data, false, partialTicks);
            renderOne(pm, player, loader, anim, tex, m, data, true, partialTicks);
         } else {
            renderOne(pm, player, loader, anim, tex, m, data, false, partialTicks);
         }
         GlStateManager.popMatrix();
      }
      CosmeticController.resetRenderState();
   }

   // Build the data array used to render this cosmetic: the index default_data, with each rgb slot
   // overridden by the user's chosen colour from the cosmetic menu (CosmeticController option
   // "labycol<slot>" = RRGGBB) when set. Lets a cosmetic with multiple colour options be recoloured.
   private String[] effectiveData(String name, LabyCosmetics.Meta m) {
      String[] data = m.defaultData.clone();
      if (m.options == null) {
         return data;
      }
      String style = CosmeticController.getOption(name, "labystyle", null);
      int slot = 0;
      for (int i = 0; i < m.options.length && i < data.length; i++) {
         if ("texture".equalsIgnoreCase(m.options[i])) {
            // the chosen style sets the texture/variant UUID (GeometryLayer shows that variant only)
            if (style != null && !style.isEmpty()) {
               data[i] = style;
            }
         } else if ("rgb".equalsIgnoreCase(m.options[i])) {
            String ov = CosmeticController.getOption(name, "labycol" + slot, null);
            if (ov != null && ov.length() == 6) {
               data[i] = ov;
            }
            slot++;
         }
      }
      return data;
   }

   // How many rgb colour slots a cosmetic exposes (for the menu's per-slot colour pickers).
   public static int colorSlotCount(LabyCosmetics.Meta m) {
      if (m == null || m.options == null) {
         return 0;
      }
      int n = 0;
      for (String o : m.options) {
         if ("rgb".equalsIgnoreCase(o)) {
            n++;
         }
      }
      return n;
   }

   // The default RRGGBB hex for a given rgb slot (from default_data), or "FFFFFF".
   public static String slotDefaultHex(LabyCosmetics.Meta m, int slot) {
      if (m == null || m.options == null || m.defaultData == null) {
         return "FFFFFF";
      }
      int s = 0;
      for (int i = 0; i < m.options.length && i < m.defaultData.length; i++) {
         if ("rgb".equalsIgnoreCase(m.options[i])) {
            if (s == slot) {
               String v = m.defaultData[i];
               return v != null && v.length() == 6 ? v : "FFFFFF";
            }
            s++;
         }
      }
      return "FFFFFF";
   }

   private ResourceLocation resolveTexture(LabyCosmetics.Meta m, int id, String[] data, AbstractClientPlayer player) {
      if ("MOJANG_BOUND".equalsIgnoreCase(m.textureType)) {
         return player.getLocationSkin();
      }
      // USER_BOUND: the texture file is the wearer's own UUID; TYPE_BOUND: the texture UUID the user
      // picked, which is the first UUID-looking entry in the cosmetic data array (data[0]).
      String textureUuid;
      if ("USER_BOUND".equalsIgnoreCase(m.textureType)) {
         textureUuid = player.getUniqueID() == null ? firstUuid(data) : player.getUniqueID().toString();
      } else {
         textureUuid = firstUuid(data);
      }
      return LabyCosmetics.cosmeticTexture(id, m.textureDirectory, textureUuid);
   }

   // The texture UUID used to key this cosmetic's texture (and thus its depth map). Mirrors
   // resolveTexture: MOJANG_BOUND has no per-cosmetic texture (player skin) so no depth map.
   private String extrudeTextureUuid(LabyCosmetics.Meta m, String[] data, AbstractClientPlayer player) {
      if ("MOJANG_BOUND".equalsIgnoreCase(m.textureType)) {
         return null;
      }
      if ("USER_BOUND".equalsIgnoreCase(m.textureType)) {
         return player.getUniqueID() == null ? firstUuid(data) : player.getUniqueID().toString();
      }
      return firstUuid(data);
   }

   private static String firstUuid(String[] data) {
      if (data == null) {
         return null;
      }
      for (String d : data) {
         if (d != null && UUID_RE.matcher(d).matches()) {
            return d;
         }
      }
      return null;
   }

   private void renderOne(ModelBiped pm, AbstractClientPlayer player, net.labymod.user.cosmetic.geometry.BlockBenchLoader loader,
                          net.labymod.user.cosmetic.animation.AnimationLoader anim, ResourceLocation tex,
                          LabyCosmetics.Meta m, String[] data, boolean mirrored, float partialTicks) {
      GeometryModelRenderer geo = loader.getModel();
      // Parse the user's data into a RemoteData (textureUUID/colors/rightSide/offset).
      net.labymod.user.cosmetic.remote.objects.data.RemoteData rd =
         new net.labymod.user.cosmetic.remote.objects.data.RemoteData();
      rd.loadData(m.options, data);
      // Texture depth map for the extrude effect (null until the texture loads); resolved by the same
      // (dir, textureUuid) the texture was fetched with.
      rd.depthMap = LabyCosmetics.depthMap(m.id, m.textureDirectory, extrudeTextureUuid(m, data, player));
      // LM3 gives every USER their own geometry model instance, so it only resets the bones an
      // animation touched (AnimationController) and lets effects re-set their own bones each frame.
      // We cache ONE model per cosmetic id and share it across every wearer, so any bone left dirty
      // by the previously-rendered player (animation scale/offset, physics rotation, a hidden LAYER
      // variant, a COLOR tint, glow) would leak onto this player -> smears / exploding geometry.
      // Reset the whole model to its bind pose first so the shared model behaves per-user-fresh.
      resetModel(loader);
      // LM3 order each frame: animation transforms first, then geometry effects, then render.
      applyAnimation(loader, anim);
      applyEffects(loader, rd, player, mirrored, partialTicks);

      GlStateManager.pushMatrix();
      GlStateManager.enableRescaleNormal();
      // Cull MUST stay enabled (vanilla entity-render default; LM3 never disables it). Many cosmetics
      // are flat zero-depth quads (bat wings, katana blades) whose front+back faces are coplanar with
      // opposite winding: with culling on, exactly the camera-facing one draws -> clean. With culling
      // OFF both draw coincident -> z-fighting that turns into thin "streak"/venetian-blind artifacts,
      // worst at grazing angles and when animation swings them edge-on.
      GlStateManager.enableCull();
      GlStateManager.color(1F, 1F, 1F, 1F);
      GlStateManager.enableTexture2D();
      this.renderPlayer.bindTexture(tex != null ? tex : whiteTexture());

      // LM3 RemoteCosmetic.render wraps the whole cosmetic in GlStateManager.scale(data.scale): the
      // attachment translates below use 0.0625/data.scale and the model renders at 0.0625, so this
      // enclosing scale rebalances both -> the cosmetic sits at its limb at its authored size.
      float cosScale = m.scale <= 0.0D ? 1.0F : (float) m.scale;
      GlStateManager.scale(cosScale, cosScale, cosScale);

      // LM3: attachment translations use 0.0625/data.scale; the model itself always renders at the
      // base model scale 0.0625 (data.scale never scales the geometry vertices).
      float scale = 0.0625F / cosScale;
      boolean slim = "slim".equals(player.getSkinType());
      String att = m.attachedTo == null ? "BODY" : m.attachedTo.toUpperCase();
      if ("HEAD".equals(att)) {
         if (player.isSneaking()) {
            GlStateManager.translate(0F, 1.0F * scale, 0F);
         }
         GlStateManager.rotate((float) Math.toDegrees(pm.bipedHead.rotateAngleY), 0F, 1F, 0F);
         GlStateManager.rotate((float) Math.toDegrees(pm.bipedHead.rotateAngleX), 1F, 0F, 0F);
      } else if ("ARM".equals(att) || "LEG".equals(att)) {
         boolean isArm = "ARM".equals(att);
         // Mirrored side uses the LEFT limb; otherwise the RIGHT limb (LM3 modelCosmetics h/i/j/k).
         ModelRenderer limb = isArm ? (mirrored ? pm.bipedLeftArm : pm.bipedRightArm)
                                    : (mirrored ? pm.bipedLeftLeg : pm.bipedRightLeg);
         GlStateManager.translate(limb.rotationPointX * scale, limb.rotationPointY * scale, limb.rotationPointZ * scale);
         GlStateManager.rotate((float) Math.toDegrees(limb.rotateAngleZ), 0F, 0F, 1F);
         GlStateManager.rotate((float) Math.toDegrees(limb.rotateAngleY), 0F, 1F, 0F);
         GlStateManager.rotate((float) Math.toDegrees(limb.rotateAngleX), 1F, 0F, 0F);
         if (mirrored) {
            if ("DUPLICATE".equalsIgnoreCase(m.mirrorType)) {
               if (isArm) {
                  GlStateManager.translate(slim ? -scale : -scale * 2.0F, 0F, 0F);
               }
            } else if ("MIRROR".equalsIgnoreCase(m.mirrorType)) {
               GlStateManager.scale(-1.0F, 1.0F, 1.0F);
            } else {
               GlStateManager.rotate(180.0F, 0F, 1F, 0F);
            }
         }
         if (isArm) {
            GlStateManager.translate(slim ? scale / 2.0F : scale, -2.0F * scale, 0F);
         }
      } else { // BODY (default)
         GlStateManager.rotate((float) Math.toDegrees(pm.bipedBody.rotateAngleY), 0F, 1F, 0F);
         GlStateManager.rotate((float) Math.toDegrees(pm.bipedBody.rotateAngleX), 1F, 0F, 0F);
      }

      // Per-user positional offset (LM3 divides by data.scale, applied inside the scaled frame).
      if (rd.offset != null) {
         GlStateManager.translate((float) (rd.offset.x / 16.0 / cosScale),
                                  (float) (-rd.offset.y / 16.0 / cosScale),
                                  (float) (rd.offset.z / 16.0 / cosScale));
      }

      geo.render(0.0625F);
      GlStateManager.enableCull();
      GlStateManager.popMatrix();
   }

   // Restores every named bone to its BIND pose, matching LM3 GeometryCosmetic.resetTransformation
   // (rotation back to the bone's fixed geometry rotation, offset 0, scale 1) and additionally clearing
   // the state geometry effects mutate (showModel / color / glow). LM3 calls resetTransformation per
   // animated bone after each render because each user owns its model; since our model is a shared
   // cached singleton we reset ALL bones up-front so no per-player state leaks to the next wearer.
   private void resetModel(net.labymod.user.cosmetic.geometry.BlockBenchLoader loader) {
      for (java.util.Map.Entry<String, net.labymod.user.cosmetic.geometry.blockbench.Item> e : loader.getItems().entrySet()) {
         GeometryModelRenderer model = loader.getModel(e.getKey());
         if (model == null) {
            continue;
         }
         net.labymod.user.cosmetic.geometry.blockbench.Item item = e.getValue();
         if (item != null && item.rotation != null) {
            model.rotateAngleX = (float) Math.toRadians(-item.rotation.get(0));
            model.rotateAngleY = (float) Math.toRadians(-item.rotation.get(1));
            model.rotateAngleZ = (float) Math.toRadians(item.rotation.get(2));
         } else {
            model.rotateAngleX = 0.0F;
            model.rotateAngleY = 0.0F;
            model.rotateAngleZ = 0.0F;
         }
         model.offsetX = 0.0F;
         model.offsetY = 0.0F;
         model.offsetZ = 0.0F;
         model.scaleX = 1.0F;
         model.scaleY = 1.0F;
         model.scaleZ = 1.0F;
         model.showModel = true;
         model.color = null;
         model.glow = false;
         model.extruded = null;
      }
   }

   // Plays a cosmetic's looping idle animation (LM3 AnimationController + GeometryCosmetic.transform):
   // for each animated bone, evaluate rotation/position/scale at the current looped time and write
   // them onto the bone's model (rotation is relative to the bone's fixed geometry rotation).
   private void applyAnimation(net.labymod.user.cosmetic.geometry.BlockBenchLoader loader,
                               net.labymod.user.cosmetic.animation.AnimationLoader anim) {
      if (anim == null) {
         return;
      }
      net.labymod.user.cosmetic.animation.model.Animation animation = anim.getIdleAnimation();
      if (animation == null) {
         return;
      }
      long length = animation.getLength();
      long offset = length <= 0L ? 0L : System.currentTimeMillis() % length;
      for (java.util.Map.Entry<String, net.labymod.user.cosmetic.animation.model.BoneAnimation> e : animation.getBoneAnimations().entrySet()) {
         GeometryModelRenderer model = loader.getModel(e.getKey());
         if (model == null) {
            continue;
         }
         net.labymod.user.cosmetic.geometry.blockbench.Item item = loader.getItem(e.getKey());
         net.labymod.user.cosmetic.animation.model.BoneAnimation ba = e.getValue();
         net.labymod.user.cosmetic.animation.model.KeyframeVector rot = ba.rotation.get(offset);
         net.labymod.user.cosmetic.animation.model.KeyframeVector pos = ba.position.get(offset);
         net.labymod.user.cosmetic.animation.model.KeyframeVector scl = ba.scale.get(offset);
         double frx = item != null && item.rotation != null ? item.rotation.get(0) : 0.0;
         double fry = item != null && item.rotation != null ? item.rotation.get(1) : 0.0;
         double frz = item != null && item.rotation != null ? item.rotation.get(2) : 0.0;
         model.rotateAngleX = (float) Math.toRadians(rot.x - frx);
         model.rotateAngleY = (float) Math.toRadians(rot.y - fry);
         model.rotateAngleZ = (float) Math.toRadians(rot.z + frz);
         model.offsetX = (float) (pos.x / 16.0);
         model.offsetY = (float) (-pos.y / 16.0);
         model.offsetZ = (float) (pos.z / 16.0);
         model.scaleX = (float) scl.x;
         model.scaleY = (float) scl.y;
         model.scaleZ = (float) scl.z;
      }
   }

   // Shared per-frame effect parameters (LM3 keeps a single instance too). Rendering is single
   // threaded, so reusing one instance is safe.
   private static final net.labymod.user.cosmetic.animation.MetaEffectFrameParameter META =
      new net.labymod.user.cosmetic.animation.MetaEffectFrameParameter();

   // Builds a RemoteData from the cosmetic's options + the user's data array, then runs every loaded
   // geometry effect against it (LM3 IModelTransformer.applyEffects). The movement-driven physics
   // inputs (forward/gravity/strafe) are left at 0 here — we don't compute per-frame motion — so
   // physics bones rest in their neutral pose; LAYER/COLOR/GLOW/HEAD_GRAVITY/ORIENTATION/CURRENT_TIME
   // all behave exactly as in LabyMod.
   private void applyEffects(net.labymod.user.cosmetic.geometry.BlockBenchLoader loader,
                             net.labymod.user.cosmetic.remote.objects.data.RemoteData rd,
                             AbstractClientPlayer player, boolean mirrored, float partialTicks) {
      if (loader.getEffects().isEmpty()) {
         return;
      }
      // Verbatim port of LM3 IModelTransformer.applyEffects: derives the movement-driven physics
      // inputs (forward / gravity / strafe) from the player's cape-physics motion so physics bones
      // (tails, scythes, hanging parts) sway naturally instead of collapsing.
      double motionX = player.prevChasingPosX + (player.chasingPosX - player.prevChasingPosX) * partialTicks
         - (player.prevPosX + (player.posX - player.prevPosX) * partialTicks);
      double motionY = player.prevChasingPosY + (player.chasingPosY - player.prevChasingPosY) * partialTicks
         - (player.prevPosY + (player.posY - player.prevPosY) * partialTicks);
      double motionZ = player.prevChasingPosZ + (player.chasingPosZ - player.prevChasingPosZ) * partialTicks
         - (player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks);
      float motionYaw = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
      double yawSin = Math.sin(motionYaw * (float) Math.PI / 180.0F);
      double yawCos = -Math.cos(motionYaw * (float) Math.PI / 180.0F);
      float cameraMotionYaw = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
      float gravity = clamp((float) motionY * 10.0F, -6.0F, 32.0F);
      gravity += Math.sin((player.prevDistanceWalkedModified
         + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * cameraMotionYaw;
      float forward = clamp((float) (motionX * yawSin + motionZ * yawCos) * 100.0F, 0.0F, 150.0F);
      float strafe = clamp((float) (motionX * yawCos - motionZ * yawSin) * 100.0F, -20.0F, 20.0F);
      float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;

      META.forward = (float) Math.toRadians(forward / 2.0F);
      META.gravity = (float) Math.toRadians(gravity);
      META.strafe = (float) Math.toRadians(strafe / 2.0F);
      META.renderYawOffset = motionYaw;
      META.pitch = pitch;
      META.isSlim = "slim".equals(player.getSkinType());
      META.rightSide = mirrored; // current rendered side (LM3 passes `mirrored` as rightSide)

      for (net.labymod.user.cosmetic.geometry.effect.GeometryEffect eff : loader.getEffects()) {
         try {
            eff.apply(rd, META);
         } catch (Throwable ignored) {
         }
      }
   }

   private static float clamp(float v, float min, float max) {
      return v < min ? min : (v > max ? max : v);
   }

   private static ResourceLocation WHITE;

   // 2x2 fully-white texture, used when a cosmetic's real UV texture can't be fetched. With white
   // texels the geometry renders as a flat solid that the per-bone colours tint.
   private static ResourceLocation whiteTexture() {
      if (WHITE == null) {
         try {
            net.minecraft.client.renderer.texture.DynamicTexture dt =
               new net.minecraft.client.renderer.texture.DynamicTexture(2, 2);
            int[] px = dt.getTextureData();
            for (int i = 0; i < px.length; i++) {
               px[i] = 0xFFFFFFFF;
            }
            dt.updateDynamicTexture();
            WHITE = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("labygeo_white", dt);
         } catch (Throwable t) {
            WHITE = new ResourceLocation("textures/misc/unknown_pack.png");
         }
      }
      return WHITE;
   }
}
