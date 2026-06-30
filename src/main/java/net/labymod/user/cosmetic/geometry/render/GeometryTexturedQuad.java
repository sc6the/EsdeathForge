package net.labymod.user.cosmetic.geometry.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class GeometryTexturedQuad {
   public GeometryPositionTextureVertex[] vertexPositions;
   public int nVertices;
   private boolean invertNormal;

   public GeometryTexturedQuad(GeometryPositionTextureVertex[] vertices) {
      this.vertexPositions = vertices;
      this.nVertices = vertices.length;
   }

   public GeometryTexturedQuad(
      GeometryPositionTextureVertex[] vertices, float texcoordU1, float texcoordV1, float texcoordU2, float texcoordV2, float textureWidth, float textureHeight
   ) {
      this(vertices);
      float f = 0.0F / textureWidth;
      float f1 = 0.0F / textureHeight;
      vertices[0] = vertices[0].setTexturePosition(texcoordU2 / textureWidth - f, texcoordV1 / textureHeight + f1);
      vertices[1] = vertices[1].setTexturePosition(texcoordU1 / textureWidth + f, texcoordV1 / textureHeight + f1);
      vertices[2] = vertices[2].setTexturePosition(texcoordU1 / textureWidth + f, texcoordV2 / textureHeight - f1);
      vertices[3] = vertices[3].setTexturePosition(texcoordU2 / textureWidth - f, texcoordV2 / textureHeight - f1);
   }

   public void flipFace() {
      GeometryPositionTextureVertex[] apositiontexturevertex = new GeometryPositionTextureVertex[this.vertexPositions.length];

      for (int i = 0; i < this.vertexPositions.length; i++) {
         apositiontexturevertex[i] = this.vertexPositions[this.vertexPositions.length - i - 1];
      }

      this.vertexPositions = apositiontexturevertex;
   }

   public void draw(WorldRenderer renderer, float scale) {
      GeometryVector3 vec3 = this.vertexPositions[1].vector3D.subtractReverse(this.vertexPositions[0].vector3D);
      GeometryVector3 vec31 = this.vertexPositions[1].vector3D.subtractReverse(this.vertexPositions[2].vector3D);
      GeometryVector3 vec32 = vec31.crossProduct(vec3).normalize();
      float f = (float)vec32.xCoord;
      float f1 = (float)vec32.yCoord;
      float f2 = (float)vec32.zCoord;
      if (this.invertNormal) {
         f = -f;
         f1 = -f1;
         f2 = -f2;
      }

      renderer.begin(7, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);

      for (int i = 0; i < 4; i++) {
         GeometryPositionTextureVertex positiontexturevertex = this.vertexPositions[i];
         renderer.pos(
               positiontexturevertex.vector3D.xCoord * (double)scale,
               positiontexturevertex.vector3D.yCoord * (double)scale,
               positiontexturevertex.vector3D.zCoord * (double)scale
            )
            .tex((double)positiontexturevertex.texturePositionX, (double)positiontexturevertex.texturePositionY)
            .normal(f, f1, f2)
            .endVertex();
      }

      Tessellator.getInstance().draw();
   }
}
