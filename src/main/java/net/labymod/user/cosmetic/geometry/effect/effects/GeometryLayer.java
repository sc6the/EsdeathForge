package net.labymod.user.cosmetic.geometry.effect.effects;

import java.util.UUID;
import net.labymod.user.cosmetic.animation.MetaEffectFrameParameter;
import net.labymod.user.cosmetic.geometry.effect.GeometryEffect;
import net.labymod.user.cosmetic.geometry.render.GeometryModelRenderer;
import net.labymod.user.cosmetic.remote.objects.data.RemoteData;

// Verbatim port of LM3 GeometryLayer: a bone "layer_<uuid>" is shown ONLY when the user's selected
// textureUUID matches <uuid> (this is what hides all the non-selected texture variants). "layer_slim"
// / "layer_right" gate on skin model / arm side; "_negate" inverts.
public class GeometryLayer extends GeometryEffect {
   private UUID uuid = null;
   private boolean negate = false;
   private boolean filterSlim = false;
   private boolean filterRightSide = false;

   public GeometryLayer(String name, GeometryModelRenderer model) {
      super(name, model);
   }

   @Override
   protected boolean parse() {
      String id = this.getParameter(0);
      if (id.equals("slim")) {
         this.filterSlim = true;
      } else if (id.equals("right")) {
         this.filterRightSide = true;
      } else {
         try {
            this.uuid = UUID.fromString(
               id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32)
            );
         } catch (Exception ignored) {
         }
      }

      this.negate = this.hasParameter(1) && this.getParameter(1).equals("negate");
      return true;
   }

   @Override
   protected int getParametersAmount() {
      return 1;
   }

   @Override
   public void apply(RemoteData remoteData, MetaEffectFrameParameter meta) {
      if (this.uuid != null) {
         this.model.showModel = remoteData.textureUUID != null && this.negate != remoteData.textureUUID.equals(this.uuid);
      }

      if (this.filterSlim) {
         this.model.showModel = this.negate != meta.isSlim;
      }

      if (this.filterRightSide) {
         this.model.showModel = this.negate != meta.rightSide;
      }
   }

   public UUID getUuid() {
      return this.uuid;
   }
}
