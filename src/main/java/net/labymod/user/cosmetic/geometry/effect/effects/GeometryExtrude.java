package net.labymod.user.cosmetic.geometry.effect.effects;

import net.labymod.user.cosmetic.animation.MetaEffectFrameParameter;
import net.labymod.user.cosmetic.geometry.effect.GeometryEffect;
import net.labymod.user.cosmetic.geometry.render.Extruded;
import net.labymod.user.cosmetic.geometry.render.GeometryModelRenderer;
import net.labymod.user.cosmetic.remote.objects.data.RemoteData;

// Port of LM3 GeometryExtrude. An "extrude_*" bone's geometry is authored in the geo.json as a single
// flat (near-zero-depth) quad spanning the sprite; LM3 SUPPRESSES that quad (onCubeAdd returns false)
// and rebuilds the sprite as a grid of 1-pixel voxels (one tiny inflated box per texture pixel, each
// UV-mapped to its own pixel). Rendering the flat quad instead causes the z-fighting "venetian blind"
// streaks seen on soul-dagger / aura / wing sprites. We voxelise exactly as LM3 does.
//
// LM3 additionally culls interior voxel faces with a per-texture DepthMap (render/Extruded), but when
// no depth map is present Extruded.isVisible() returns true for every face — identical to rendering all
// voxel faces — so we skip the DepthMap machinery and just emit the full voxel grid (solid sprite).
public class GeometryExtrude extends GeometryEffect {
   private int x = 0;
   private int y = 0;
   private int width = 1;
   private int height = 1;
   private boolean hasCube = false;

   public GeometryExtrude(String name, GeometryModelRenderer model) {
      super(name, model);
   }

   @Override
   protected boolean parse() {
      return true;
   }

   @Override
   protected int getParametersAmount() {
      return 0;
   }

   @Override
   public boolean onCubeAdd(GeometryModelRenderer target, float x, float y, float z,
                            int sizeX, int sizeY, int sizeZ, float inflate, boolean mirror) {
      int originOffsetX = target.textureOffsetX;
      int originOffsetY = target.textureOffsetY;
      for (int relY = 0; relY < sizeY; relY++) {
         for (int relX = 0; relX < sizeX; relX++) {
            target.setTextureOffset(originOffsetX + relX, originOffsetY + relY);
            target.addBox(x + (float) relX + 0.5F, y + (float) relY + 0.5F, z, 0.01F, 0.01F, 0.01F, 0.5F, mirror);
         }
      }
      this.x = originOffsetX;
      this.y = originOffsetY;
      this.width = sizeX;
      this.height = sizeY;
      this.hasCube = true;
      return false; // original flat quad replaced by the voxel grid
   }

   @Override
   public void apply(RemoteData remoteData, MetaEffectFrameParameter meta) {
      // LM3 always wraps the voxel grid in an Extruded so the depth map can cull faces. We guard null
      // depthMap (texture not yet loaded) -> leave extruded null so all faces render until it arrives
      // (Extruded.hashCode would NPE on a null depthMap, and null-depthMap cull == no cull anyway).
      if (!this.hasCube || remoteData.depthMap == null) {
         this.model.extruded = null;
      } else {
         this.model.extruded = new Extruded(this, remoteData.depthMap);
      }
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }
}
