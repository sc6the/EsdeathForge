package me.txb1.extras.cosmetics.oam;

import me.txb1.EsdeathClient;
import me.txb1.extras.cosmetics.CosmeticBase;
import me.txb1.extras.cosmetics.CosmeticController;
import me.txb1.utils.EsdeathUtils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.lwjgl.opengl.GL11;

// Bridges a ported OAM cosmetic into EsdeathForge's LayerRenderer/equip pipeline. Gates on the
// player's equipped set, applies the user's offset/scale/colour/transparency, then delegates to
// the OAM model's own renderCosmetic (preserving its exact geometry + movement).
public class OamCosmeticLayer extends CosmeticBase {
   private final CosmeticModelRenderer model;
   private final String displayName;
   private final String key;
   private final Cosmetic shim = new Cosmetic();

   public OamCosmeticLayer(RenderPlayer renderPlayer, String displayName, CosmeticModelRenderer model) {
      super(renderPlayer, displayName);
      this.model = model;
      this.displayName = displayName;
      this.key = displayName.toLowerCase();
      this.model.cosmeticName = displayName;
      this.shim.cosmeticName = displayName;
      CosmeticController.markOam(displayName);
      CosmeticController.markOam(displayName);
   }

   @Override
   public void render(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      // offline: cosmetics only render on the local player. Cheap identity + set check instead of
      // isOnline() scan + getPlayer() map lookup + getActive() ArrayList copy (this runs per layer
      // per frame; with ~50 OAM layers the old path allocated/scanned heavily every frame).
      if (var1 != net.minecraft.client.Minecraft.getMinecraft().thePlayer) {
         return;
      }
      if (!CosmeticController.isActive(this.key)) {
         return;
      }
      this.model.alpha = CosmeticController.getAlpha(this.displayName);
      GL11.glPushMatrix();
      // match the vanilla entity render state: alpha-test discards transparent skin/texture texels
      // (so Mini Me's base head shows through its hat layer instead of an opaque shell). Cull stays
      // ENABLED — OAM's flat planes (e.g. Raven's feathers) are already two-sided (two opposing
      // coincident quads), and disabling cull instead exposes the back faces of solid parts.
      GL11.glEnable(3008);            // GL_ALPHA_TEST
      GL11.glAlphaFunc(516, 0.1F);    // GL_GREATER, 0.1
      // Some cosmetics (Mini Me, Deadmau5) toggle glCullFace(GL_FRONT) / glDisable(GL_CULL_FACE)
      // internally and don't fully restore it, so the leaked state would cull the FRONT faces of the
      // next cosmetic (you'd "see through" a face to the back). Force the vanilla cull state before
      // each cosmetic and restore it after so they can't leak into one another or the next entity.
      GL11.glEnable(2884);            // GL_CULL_FACE
      GL11.glCullFace(1029);          // GL_BACK
      // user offset/scale tweaks (shared with the hand-ported cosmetics)
      CosmeticController.translate(this.displayName);
      // for cosmetics that never call renderRGB, still honor transparency
      if (this.model.alpha < 1.0f) {
         GL11.glEnable(3042);
         GL11.glBlendFunc(770, 771);
         GL11.glColor4f(1.0f, 1.0f, 1.0f, this.model.alpha);
      }
      // ticks_2=limbSwing, ticks_3=limbSwingAmount, ticks_4=ageInTicks, ticks_5=netHeadYaw, ticks_6=headPitch
      this.model.renderCosmetic(var1, var2, var3, var5, var6, var7, var8, var1, var4, this.shim);

      // Restore the GL state to the vanilla entity-render baseline. The OAM models set blend + a
      // tinted/alpha glColor via raw GL11 (applyColor/renderMultiColor) and never undo it, so without
      // this every cosmetic's colour + transparency bled into the next layer and into the player model
      // (opaque cosmetics rendering see-through, the player going translucent, etc.). The baseline
      // here matches what GlStateManager's cache expects (blend off, white colour), so it stays synced.
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F); // clear leaked colour/alpha
      GL11.glDisable(3042);                   // GL_BLEND off
      GL11.glDepthMask(true);                 // some cosmetics leave the depth mask off
      GL11.glEnable(3008);                    // GL_ALPHA_TEST
      GL11.glAlphaFunc(516, 0.1F);            // GL_GREATER, 0.1
      GL11.glEnable(2884);                    // GL_CULL_FACE
      GL11.glCullFace(1029);                  // GL_BACK
      GL11.glPopMatrix();

      // keep GlStateManager's cache in sync with the raw resets above
      net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }
}
