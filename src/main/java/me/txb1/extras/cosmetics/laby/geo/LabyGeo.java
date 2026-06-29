package me.txb1.extras.cosmetics.laby.geo;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

// Parses a LabyMod Bedrock cosmetic geometry (dl.labymod.net/cosmetics/<id>/geo.json) into a vanilla
// ModelRenderer tree. The Bedrock->BlockBench->model coordinate math is replicated from LabyMod 3's
// GeometryLoader + BlockBenchLoader; LabyMod's GeometryModelBox uses the exact vanilla box-UV unwrap,
// so vanilla ModelRenderer.addBox + setTextureOffset reproduces the geometry 1:1. Box-UV only
// (per-face UV cosmetics fail to parse here and are skipped) and no animation (static pose).
public final class LabyGeo {

   public final ModelRenderer root;
   public final int texWidth;
   public final int texHeight;

   private LabyGeo(ModelRenderer root, int texWidth, int texHeight) {
      this.root = root;
      this.texWidth = texWidth;
      this.texHeight = texHeight;
   }

   public static LabyGeo parse(String json) {
      try {
         Geometry g = new Gson().fromJson(json, Geometry.class);
         if (g == null || g.minecraftGeometry == null || g.minecraftGeometry.isEmpty()) {
            return null;
         }
         MC mc = g.minecraftGeometry.get(0);
         int tw = (mc.description != null && mc.description.textureWidth != null) ? mc.description.textureWidth : 64;
         int th = (mc.description != null && mc.description.textureHeight != null) ? mc.description.textureHeight : 64;
         List<Bone> bones = mc.bones == null ? new ArrayList<Bone>() : mc.bones;

         ModelBase base = new ModelBase() {};
         ModelRenderer rootR = new ModelRenderer(base, 0, 0);
         // build each top-level bone (parent == null); children recursed in buildBone
         for (Bone b : bones) {
            if (b.parent == null || b.parent.isEmpty()) {
               buildBone(b, bones, base, rootR, 0f, 0f, 0f, tw, th);
            }
         }
         return new LabyGeo(rootR, tw, th);
      } catch (Throwable t) {
         return null;
      }
   }

   private static void buildBone(Bone bone, List<Bone> all, ModelBase base, ModelRenderer parent,
                                 float parentPivotX, float parentPivotY, float parentPivotZ, int tw, int th) {
      float px = at(bone.pivot, 0), py = at(bone.pivot, 1), pz = at(bone.pivot, 2);
      ModelRenderer m = new ModelRenderer(base, 0, 0);
      m.setRotationPoint(px - parentPivotX, -(py - parentPivotY), pz - parentPivotZ);
      if (bone.rotation != null) {
         m.rotateAngleX = (float) Math.toRadians(at(bone.rotation, 0));
         m.rotateAngleY = (float) Math.toRadians(at(bone.rotation, 1));
         m.rotateAngleZ = (float) Math.toRadians(at(bone.rotation, 2));
      }
      parent.addChild(m);

      if (bone.cubes != null) {
         for (Cube c : bone.cubes) {
            addCube(m, base, c, px, py, pz, tw, th);
         }
      }

      // recurse into children
      for (Bone child : all) {
         if (bone.name != null && bone.name.equals(child.parent)) {
            buildBone(child, all, base, m, px, py, pz, tw, th);
         }
      }
   }

   private static void addCube(ModelRenderer bone, ModelBase base, Cube c, float bx, float by, float bz, int tw, int th) {
      float ox = at(c.origin, 0), oy = at(c.origin, 1), oz = at(c.origin, 2);
      int sx = Math.round(at(c.size, 0)), sy = Math.round(at(c.size, 1)), sz = Math.round(at(c.size, 2));
      // BlockBenchLoader placement (relative to the bone's pivot)
      float x = ox - bx;
      float y = -oy - sy + by;
      float z = oz - bz;
      int uvX = c.uv != null && c.uv.size() > 0 ? (int) Math.round(c.uv.get(0)) : 0;
      int uvY = c.uv != null && c.uv.size() > 1 ? (int) Math.round(c.uv.get(1)) : 0;

      ModelRenderer target = bone;
      if (c.rotation != null) {
         // per-cube rotation -> its own child renderer pivoted on the cube pivot
         float cpx = at(c.pivot, 0), cpy = at(c.pivot, 1), cpz = at(c.pivot, 2);
         ModelRenderer rot = new ModelRenderer(base, 0, 0);
         rot.setRotationPoint(cpx - bx, -(cpy - by), cpz - bz);
         rot.rotateAngleX = (float) Math.toRadians(at(c.rotation, 0));
         rot.rotateAngleY = (float) Math.toRadians(at(c.rotation, 1));
         rot.rotateAngleZ = (float) Math.toRadians(at(c.rotation, 2));
         bone.addChild(rot);
         target = rot;
         x -= rot.rotationPointX;
         y -= rot.rotationPointY;
         z -= rot.rotationPointZ;
      }

      target.mirror = c.mirror != null && c.mirror;
      target.setTextureSize(tw, th);
      target.setTextureOffset(uvX, uvY);
      target.addBox(x, y, z, sx, sy, sz, c.inflate == null ? 0f : c.inflate);
      target.mirror = false;
   }

   private static float at(List<Double> l, int i) {
      return (l != null && l.size() > i && l.get(i) != null) ? l.get(i).floatValue() : 0f;
   }

   // ---- Bedrock geo.json POJOs ----
   private static final class Geometry {
      @SerializedName("minecraft:geometry") List<MC> minecraftGeometry;
   }

   private static final class MC {
      Description description;
      List<Bone> bones;
   }

   private static final class Description {
      @SerializedName("texture_width") Integer textureWidth;
      @SerializedName("texture_height") Integer textureHeight;
   }

   private static final class Bone {
      String name;
      String parent;
      List<Double> pivot;
      List<Double> rotation;
      List<Cube> cubes;
   }

   private static final class Cube {
      List<Double> origin;
      List<Double> size;
      List<Double> uv;
      List<Double> pivot;
      List<Double> rotation;
      Boolean mirror;
      Float inflate;
   }
}
