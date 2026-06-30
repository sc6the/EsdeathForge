package net.labymod.user.cosmetic.custom;

import java.awt.image.BufferedImage;
import java.util.Objects;
import net.labymod.user.cosmetic.geometry.effect.effects.GeometryExtrude;

// Verbatim port of LabyMod 3's DepthMap. Built from a cosmetic's texture image: map[x][y] = the pixel
// at (x,y) is opaque (alpha > 0). Used by the extrude effect to cull voxel faces: a face is drawn only
// if its pixel is opaque AND the neighbouring pixel in that face's direction is transparent. This
// removes both the empty-pixel voxels (the thin radiating "streak" slivers) and the interior faces
// between adjacent opaque pixels, leaving a clean solid voxel sprite.
public class DepthMap {
   private final boolean[][] map;
   private final long creationTime = System.currentTimeMillis();
   private final long hashCode;

   public DepthMap(BufferedImage bufferedImage) {
      this.map = new boolean[bufferedImage.getWidth()][bufferedImage.getHeight()];

      for (int x = 0; x < bufferedImage.getWidth(); x++) {
         for (int y = 0; y < bufferedImage.getHeight(); y++) {
            this.map[x][y] = this.hasDepth(bufferedImage.getRGB(x, y));
         }
      }

      this.hashCode = (long) super.hashCode();
   }

   public boolean shouldRenderFace(GeometryExtrude bounds, int x, int y, int face) {
      return this.hasDepthAt(bounds, x, y) && !this.hasDepthInFacing(bounds, x, y, face);
   }

   public boolean hasDepthInFacing(GeometryExtrude bounds, int x, int y, int face) {
      switch (face) {
         case 0:
            return this.hasDepthAt(bounds, x + 1, y);
         case 1:
            return this.hasDepthAt(bounds, x - 1, y);
         case 2:
            return this.hasDepthAt(bounds, x, y - 1);
         case 3:
            return this.hasDepthAt(bounds, x, y + 1);
         default:
            return false;
      }
   }

   public boolean hasDepthAt(GeometryExtrude bounds, int x, int y) {
      return x >= bounds.getX()
         && y >= bounds.getY()
         && x < bounds.getX() + bounds.getWidth()
         && y < bounds.getY() + bounds.getHeight()
         && x < this.map.length
         && y < this.map[0].length
         && this.map[x][y];
   }

   public int getWidth() {
      return this.map.length;
   }

   public int getHeight() {
      return this.map[0].length;
   }

   private boolean hasDepth(int color) {
      return (color >> 24 & 0xFF) > 0;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DepthMap depthMap = (DepthMap) o;
         return this.creationTime == depthMap.creationTime && this.hashCode == depthMap.hashCode;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.creationTime) + Objects.hash(this.hashCode);
   }
}
