package net.labymod.user.cosmetic.geometry.render;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Faithful port of LabyMod 3's GeometryModelRenderer (its own ModelRenderer-equivalent for
 * BlockBench/Bedrock cosmetics). The niche "extrude effect" path is removed (it pulled in the
 * effects/DepthMap framework); everything else renders as in LabyMod, via a compiled display list.
 */
public class GeometryModelRenderer {
   public float textureWidth = 64.0F;
   public float textureHeight = 32.0F;
   public int textureOffsetX;
   public int textureOffsetY;
   public float rotationPointX;
   public float rotationPointY;
   public float rotationPointZ;
   public float rotateAngleX;
   public float rotateAngleY;
   public float rotateAngleZ;
   public boolean compiled;
   private int displayList;
   public boolean mirror;
   public boolean showModel = true;
   public boolean isHidden;
   public List<GeometryModelBox> cubeList = Lists.newArrayList();
   public List<GeometryModelRenderer> childModels = Lists.newArrayList();
   public float offsetX;
   public float offsetY;
   public float offsetZ;
   public float scaleX = 1.0F;
   public float scaleY = 1.0F;
   public float scaleZ = 1.0F;
   public GeometryModelRenderer parent;
   public Color color = null;
   public Extruded extruded = null;
   public boolean glow;
   public Map<Integer, Integer> extrudedCompileList = new HashMap<>();

   public void addChild(GeometryModelRenderer renderer) {
      if (this.childModels == null) {
         this.childModels = Lists.newArrayList();
      }

      renderer.parent = this;
      this.childModels.add(renderer);
   }

   public GeometryModelRenderer setTextureOffset(int x, int y) {
      this.textureOffsetX = x;
      this.textureOffsetY = y;
      return this;
   }

   public void addBox(float x, float y, float z, float width, float height, float depth, float delta, boolean mirror) {
      GeometryModelBox box = new GeometryModelBox(
         this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, delta, mirror, this.textureWidth, this.textureHeight
      );
      this.cubeList.add(box);
   }

   public void setRotationPoint(float rotationPointXIn, float rotationPointYIn, float rotationPointZIn) {
      this.rotationPointX = rotationPointXIn;
      this.rotationPointY = rotationPointYIn;
      this.rotationPointZ = rotationPointZIn;
   }

   public void render(float scale) {
      // Extrude bones recompile their display list per depth-map (texture) so face culling stays
      // correct; cache the compiled list per Extruded.hashCode so we don't rebuild every frame.
      if (this.extruded != null) {
         int code = this.extruded.hashCode();
         if (this.extrudedCompileList.containsKey(code)) {
            this.displayList = this.extrudedCompileList.get(code);
            this.compiled = true;
         } else {
            this.compiled = false;
         }
      }

      if (!this.isHidden && this.showModel) {
         if (!this.compiled) {
            this.compileDisplayList(scale);
         }

         GlStateManager.translate(this.offsetX, this.offsetY, this.offsetZ);
         GlStateManager.pushMatrix();
         if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F) {
            GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
         }

         if (this.rotateAngleZ != 0.0F) {
            GlStateManager.rotate(this.rotateAngleZ * (180.0F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
         }

         if (this.rotateAngleY != 0.0F) {
            GlStateManager.rotate(this.rotateAngleY * (180.0F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
         }

         if (this.rotateAngleX != 0.0F) {
            GlStateManager.rotate(this.rotateAngleX * (180.0F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
         }

         if (this.scaleX != 1.0F || this.scaleY != 1.0F || this.scaleZ != 1.0F) {
            GlStateManager.scale(this.scaleX, this.scaleY, this.scaleZ);
         }

         if (this.color != null) {
            GL11.glColor4f(
               (float)this.color.getRed() / 255.0F,
               (float)this.color.getGreen() / 255.0F,
               (float)this.color.getBlue() / 255.0F,
               (float)this.color.getAlpha() / 255.0F
            );
         }

         if (this.glow) {
            GlStateManager.disableLighting();
            Minecraft.getMinecraft().entityRenderer.disableLightmap();
         }

         GlStateManager.callList(this.displayList);
         if (this.childModels != null) {
            for (int i = 0; i < this.childModels.size(); i++) {
               this.childModels.get(i).render(scale);
            }
         }

         if (this.color != null) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         }

         if (this.glow) {
            GlStateManager.enableLighting();
            Minecraft.getMinecraft().entityRenderer.enableLightmap();
         }

         GlStateManager.popMatrix();
         GlStateManager.translate(-this.offsetX, -this.offsetY, -this.offsetZ);
      }
   }

   private void compileDisplayList(float scale) {
      this.displayList = GLAllocation.generateDisplayLists(1);
      GL11.glNewList(this.displayList, 4864);
      if (this.extruded != null) {
         this.extrudedCompileList.put(this.extruded.hashCode(), this.displayList);
      }

      WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();

      for (int cubeIndex = 0; cubeIndex < this.cubeList.size(); cubeIndex++) {
         GeometryModelBox box = this.cubeList.get(cubeIndex);

         for (int quadIndex = 0; quadIndex < box.quadList.length; quadIndex++) {
            // For extrude bones, cull voxel faces via the texture depth map (transparent pixels and
            // interior faces between adjacent opaque pixels) -> clean solid sprite, no slivers.
            if (this.extruded == null || this.extruded.isVisible(cubeIndex, quadIndex)) {
               box.quadList[quadIndex].draw(worldRenderer, scale);
            }
         }
      }

      GL11.glEndList();
      this.compiled = true;
   }

   public GeometryModelRenderer setTextureSize(int textureWidthIn, int textureHeightIn) {
      this.textureWidth = (float)textureWidthIn;
      this.textureHeight = (float)textureHeightIn;
      return this;
   }

   public void setScale(float scaleX, float scaleY, float scaleZ) {
      this.scaleX = scaleX;
      this.scaleY = scaleY;
      this.scaleZ = scaleZ;
   }
}
