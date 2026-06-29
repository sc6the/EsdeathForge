package me.txb1.extras.cosmetics.oam;

import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.WorldRenderer;

// OAM cosmetic model box (clone of vanilla ModelBox) — ported verbatim.
public class CosmeticModelBox {
   private PositionTextureVertex[] vertexPositions;
   private TexturedQuad[] quadList;
   public final float posX1;
   public final float posY1;
   public final float posZ1;
   public final float posX2;
   public final float posY2;
   public final float posZ2;
   public String boxName;

   public CosmeticModelBox(CosmeticModelRenderer renderer, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float expand) {
      this(renderer, texU, texV, x, y, z, dx, dy, dz, expand, renderer.mirror);
   }

   public CosmeticModelBox(CosmeticModelRenderer renderer, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float expand, boolean mirror) {
      this.posX1 = x;
      this.posY1 = y;
      this.posZ1 = z;
      this.posX2 = x + (float)dx;
      this.posY2 = y + (float)dy;
      this.posZ2 = z + (float)dz;
      this.vertexPositions = new PositionTextureVertex[8];
      this.quadList = new TexturedQuad[6];
      float f4 = x + (float)dx;
      float f5 = y + (float)dy;
      float f6 = z + (float)dz;
      x -= expand;
      y -= expand;
      z -= expand;
      f4 += expand;
      f5 += expand;
      f6 += expand;
      if (mirror) {
         float f7 = f4;
         f4 = x;
         x = f7;
      }
      PositionTextureVertex v7 = new PositionTextureVertex(x, y, z, 0.0f, 0.0f);
      PositionTextureVertex v0 = new PositionTextureVertex(f4, y, z, 0.0f, 8.0f);
      PositionTextureVertex v1 = new PositionTextureVertex(f4, f5, z, 8.0f, 8.0f);
      PositionTextureVertex v2 = new PositionTextureVertex(x, f5, z, 8.0f, 0.0f);
      PositionTextureVertex v3 = new PositionTextureVertex(x, y, f6, 0.0f, 0.0f);
      PositionTextureVertex v4 = new PositionTextureVertex(f4, y, f6, 0.0f, 8.0f);
      PositionTextureVertex v5 = new PositionTextureVertex(f4, f5, f6, 8.0f, 8.0f);
      PositionTextureVertex v6 = new PositionTextureVertex(x, f5, f6, 8.0f, 0.0f);
      this.vertexPositions[0] = v7;
      this.vertexPositions[1] = v0;
      this.vertexPositions[2] = v1;
      this.vertexPositions[3] = v2;
      this.vertexPositions[4] = v3;
      this.vertexPositions[5] = v4;
      this.vertexPositions[6] = v5;
      this.vertexPositions[7] = v6;
      this.quadList[0] = new TexturedQuad(new PositionTextureVertex[]{v4, v0, v1, v5}, texU + dz + dx, texV + dz, texU + dz + dx + dz, texV + dz + dy, renderer.textureWidth, renderer.textureHeight);
      this.quadList[1] = new TexturedQuad(new PositionTextureVertex[]{v7, v3, v6, v2}, texU, texV + dz, texU + dz, texV + dz + dy, renderer.textureWidth, renderer.textureHeight);
      this.quadList[2] = new TexturedQuad(new PositionTextureVertex[]{v4, v3, v7, v0}, texU + dz, texV, texU + dz + dx, texV + dz, renderer.textureWidth, renderer.textureHeight);
      this.quadList[3] = new TexturedQuad(new PositionTextureVertex[]{v1, v2, v6, v5}, texU + dz + dx, texV + dz, texU + dz + dx + dx, texV, renderer.textureWidth, renderer.textureHeight);
      this.quadList[4] = new TexturedQuad(new PositionTextureVertex[]{v0, v7, v2, v1}, texU + dz, texV + dz, texU + dz + dx, texV + dz + dy, renderer.textureWidth, renderer.textureHeight);
      this.quadList[5] = new TexturedQuad(new PositionTextureVertex[]{v3, v4, v5, v6}, texU + dz + dx + dz, texV + dz, texU + dz + dx + dz + dx, texV + dz + dy, renderer.textureWidth, renderer.textureHeight);
      if (mirror) {
         for (int j1 = 0; j1 < this.quadList.length; ++j1) {
            this.quadList[j1].flipFace();
         }
      }
   }

   public void render(WorldRenderer worldRenderer, float scale) {
      for (int i = 0; i < this.quadList.length; ++i) {
         this.quadList[i].draw(worldRenderer, scale);
      }
   }

   public CosmeticModelBox setBoxName(String name) {
      this.boxName = name;
      return this;
   }
}
