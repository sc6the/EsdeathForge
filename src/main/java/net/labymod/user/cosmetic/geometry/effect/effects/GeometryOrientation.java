package net.labymod.user.cosmetic.geometry.effect.effects;

import net.labymod.user.cosmetic.animation.MetaEffectFrameParameter;
import net.labymod.user.cosmetic.geometry.effect.GeometryEffect;
import net.labymod.user.cosmetic.geometry.render.GeometryModelRenderer;
import net.labymod.user.cosmetic.remote.objects.data.RemoteData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;

// Port of LM3 GeometryOrientation (obfuscated MC refs deobfuscated to MCP): keeps a bone facing the
// camera or north regardless of the player's body rotation.
public class GeometryOrientation extends GeometryEffect {
   private OrientationTarget target;

   public GeometryOrientation(String name, GeometryModelRenderer model) {
      super(name, model);
   }

   @Override
   protected boolean parse() {
      this.target = OrientationTarget.getById(this.getParameter(0));
      return true;
   }

   @Override
   protected int getParametersAmount() {
      return 1;
   }

   @Override
   public void apply(RemoteData remoteData, MetaEffectFrameParameter meta) {
      RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
      float x = 0.0F;
      float y = 0.0F;
      float z = 0.0F;
      if (this.target == OrientationTarget.CAMERA) {
         x = renderManager.playerViewX * (float)(Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? -1 : 1);
         y = renderManager.playerViewY - meta.renderYawOffset;
      }

      if (this.target == OrientationTarget.NORTH) {
         y = -meta.renderYawOffset;
      }

      for (GeometryModelRenderer t = this.model.parent; t != null; t = t.parent) {
         x = (float)((double)x - Math.toDegrees((double)t.rotateAngleX));
         y = (float)((double)y - Math.toDegrees((double)t.rotateAngleY));
         z = (float)((double)z - Math.toDegrees((double)t.rotateAngleZ));
      }

      x += 180.0F;
      this.model.rotateAngleX = (float)Math.toRadians((double)x);
      this.model.rotateAngleY = (float)Math.toRadians((double)y);
      this.model.rotateAngleZ = (float)Math.toRadians((double)z);
   }

   public static enum OrientationTarget {
      CAMERA,
      NORTH;

      public static OrientationTarget getById(String id) {
         for (OrientationTarget target : values()) {
            if (target.name().equalsIgnoreCase(id)) {
               return target;
            }
         }

         return null;
      }
   }
}
