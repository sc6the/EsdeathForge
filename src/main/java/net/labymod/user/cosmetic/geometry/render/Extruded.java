package net.labymod.user.cosmetic.geometry.render;

import net.labymod.user.cosmetic.custom.DepthMap;
import net.labymod.user.cosmetic.geometry.effect.effects.GeometryExtrude;

// Verbatim port of LabyMod 3's Extruded: pairs an extrude effect with the texture depth map so the
// model renderer can cull each voxel face (isVisible). hashCode/equals identify a depth-map instance
// so the compiled display list can be cached + invalidated when the texture (depth map) changes.
public class Extruded {
   private final GeometryExtrude geometryExtrude;
   private final DepthMap depthMap;

   public Extruded(GeometryExtrude geometryExtrude, DepthMap depthMap) {
      this.geometryExtrude = geometryExtrude;
      this.depthMap = depthMap;
   }

   public boolean isVisible(int cubeIndex, int quadIndex) {
      int x = cubeIndex % this.geometryExtrude.getWidth() + this.geometryExtrude.getX();
      int y = cubeIndex / this.geometryExtrude.getWidth() + this.geometryExtrude.getY();
      return this.depthMap == null || this.depthMap.shouldRenderFace(this.geometryExtrude, x, y, quadIndex);
   }

   public boolean hasChanged(DepthMap depthMap) {
      return this.depthMap != null && !this.depthMap.equals(depthMap);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Extruded extruded = (Extruded) o;
         return this.depthMap.equals(extruded.depthMap);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.depthMap.hashCode();
   }
}
