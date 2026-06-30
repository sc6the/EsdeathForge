package net.labymod.user.cosmetic.geometry.render;

public class GeometryModelBox {
   private GeometryPositionTextureVertex[] vertexPositions;
   public GeometryTexturedQuad[] quadList;
   public final float posX1;
   public final float posY1;
   public final float posZ1;
   public final float posX2;
   public final float posY2;
   public final float posZ2;
   public String boxName;

   public GeometryModelBox(
      int textureX,
      int textureY,
      float x,
      float y,
      float z,
      float width,
      float height,
      float depth,
      float delta,
      boolean mirror,
      float texWidth,
      float texHeight
   ) {
      this.posX1 = x;
      this.posY1 = y;
      this.posZ1 = z;
      this.posX2 = x + width;
      this.posY2 = y + height;
      this.posZ2 = z + depth;
      this.vertexPositions = new GeometryPositionTextureVertex[8];
      this.quadList = new GeometryTexturedQuad[6];
      float f = x + width;
      float f1 = y + height;
      float f2 = z + depth;
      x -= delta;
      y -= delta;
      z -= delta;
      f += delta;
      f1 += delta;
      f2 += delta;
      if (mirror) {
         float f3 = f;
         f = x;
         x = f3;
      }

      GeometryPositionTextureVertex positiontexturevertex7 = new GeometryPositionTextureVertex(x, y, z, 0.0F, 0.0F);
      GeometryPositionTextureVertex positiontexturevertex = new GeometryPositionTextureVertex(f, y, z, 0.0F, 8.0F);
      GeometryPositionTextureVertex positiontexturevertex1 = new GeometryPositionTextureVertex(f, f1, z, 8.0F, 8.0F);
      GeometryPositionTextureVertex positiontexturevertex2 = new GeometryPositionTextureVertex(x, f1, z, 8.0F, 0.0F);
      GeometryPositionTextureVertex positiontexturevertex3 = new GeometryPositionTextureVertex(x, y, f2, 0.0F, 0.0F);
      GeometryPositionTextureVertex positiontexturevertex4 = new GeometryPositionTextureVertex(f, y, f2, 0.0F, 8.0F);
      GeometryPositionTextureVertex positiontexturevertex5 = new GeometryPositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
      GeometryPositionTextureVertex positiontexturevertex6 = new GeometryPositionTextureVertex(x, f1, f2, 8.0F, 0.0F);
      this.vertexPositions[0] = positiontexturevertex7;
      this.vertexPositions[1] = positiontexturevertex;
      this.vertexPositions[2] = positiontexturevertex1;
      this.vertexPositions[3] = positiontexturevertex2;
      this.vertexPositions[4] = positiontexturevertex3;
      this.vertexPositions[5] = positiontexturevertex4;
      this.vertexPositions[6] = positiontexturevertex5;
      this.vertexPositions[7] = positiontexturevertex6;
      this.quadList[0] = new GeometryTexturedQuad(
         new GeometryPositionTextureVertex[]{positiontexturevertex4, positiontexturevertex, positiontexturevertex1, positiontexturevertex5},
         (float)textureX + depth + width,
         (float)textureY + depth,
         (float)textureX + depth + width + depth,
         (float)textureY + depth + height,
         texWidth,
         texHeight
      );
      this.quadList[1] = new GeometryTexturedQuad(
         new GeometryPositionTextureVertex[]{positiontexturevertex7, positiontexturevertex3, positiontexturevertex6, positiontexturevertex2},
         (float)textureX,
         (float)textureY + depth,
         (float)textureX + depth,
         (float)textureY + depth + height,
         texWidth,
         texHeight
      );
      this.quadList[2] = new GeometryTexturedQuad(
         new GeometryPositionTextureVertex[]{positiontexturevertex4, positiontexturevertex3, positiontexturevertex7, positiontexturevertex},
         (float)textureX + depth,
         (float)textureY,
         (float)textureX + depth + width,
         (float)textureY + depth,
         texWidth,
         texHeight
      );
      this.quadList[3] = new GeometryTexturedQuad(
         new GeometryPositionTextureVertex[]{positiontexturevertex1, positiontexturevertex2, positiontexturevertex6, positiontexturevertex5},
         (float)textureX + depth + width,
         (float)textureY + depth,
         (float)textureX + depth + width + width,
         (float)textureY,
         texWidth,
         texHeight
      );
      this.quadList[4] = new GeometryTexturedQuad(
         new GeometryPositionTextureVertex[]{positiontexturevertex, positiontexturevertex7, positiontexturevertex2, positiontexturevertex1},
         (float)textureX + depth,
         (float)textureY + depth,
         (float)textureX + depth + width,
         (float)textureY + depth + height,
         texWidth,
         texHeight
      );
      this.quadList[5] = new GeometryTexturedQuad(
         new GeometryPositionTextureVertex[]{positiontexturevertex3, positiontexturevertex4, positiontexturevertex5, positiontexturevertex6},
         (float)textureX + depth + width + depth,
         (float)textureY + depth,
         (float)textureX + depth + width + depth + width,
         (float)textureY + depth + height,
         texWidth,
         texHeight
      );
      if (mirror) {
         for (int i = 0; i < this.quadList.length; i++) {
            this.quadList[i].flipFace();
         }
      }
   }

   public GeometryModelBox setBoxName(String name) {
      this.boxName = name;
      return this;
   }
}
