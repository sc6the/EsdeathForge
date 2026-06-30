package net.labymod.user.cosmetic.geometry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import net.labymod.user.cosmetic.geometry.bedrock.BedrockCube;
import net.labymod.user.cosmetic.geometry.bedrock.Bone;
import net.labymod.user.cosmetic.geometry.bedrock.Geometry;
import net.labymod.user.cosmetic.geometry.bedrock.MinecraftGeometry;
import net.labymod.user.cosmetic.geometry.blockbench.BlockBench;
import net.labymod.user.cosmetic.geometry.blockbench.BlockBenchCube;
import net.labymod.user.cosmetic.geometry.render.GeometryModelRenderer;

public class GeometryLoader {
   private static final Gson GSON = new Gson();
   private final Geometry geometry;

   public GeometryLoader(Geometry geometry) throws IOException {
      this.geometry = geometry;
   }

   public GeometryLoader(String json) throws IOException {
      this((Geometry)GSON.fromJson(json, Geometry.class));
   }

   public GeometryLoader(InputStream inputStream) throws IOException {
      this((Geometry)GSON.fromJson(new InputStreamReader(inputStream), Geometry.class));
      inputStream.close();
   }

   public GeometryLoader(File file) throws IOException {
      this(new FileInputStream(file));
   }

   public Geometry getGeometry() {
      return this.geometry;
   }

   public GeometryModelRenderer toModelRenderer() throws Exception {
      return this.toBlockBenchLoader().getModel();
   }

   public BlockBenchLoader toBlockBenchLoader() throws Exception {
      return new BlockBenchLoader(this.toBlockBench());
   }

   public BlockBench toBlockBench() {
      BlockBench blockBench = new BlockBench();
      if (this.geometry.minecraftGeometry != null && !this.geometry.minecraftGeometry.isEmpty()) {
         MinecraftGeometry geometry = this.geometry.minecraftGeometry.get(0);
         blockBench.resolution.width = geometry.description.textureWidth;
         blockBench.resolution.height = geometry.description.textureHeight;

         for (Bone bone : geometry.bones) {
            for (int i = 0; i < (bone.cubes == null ? 0 : bone.cubes.size()); i++) {
               BedrockCube cube = bone.cubes.get(i);
               blockBench.elements.add(this.toBlockBenchCube(cube, bone.name + "_" + i));
            }
         }

         for (Bone bone : geometry.bones) {
            if (bone.parent == null) {
               blockBench.outliner.add(this.toBlockBenchChild(geometry, bone));
            }
         }

         return blockBench;
      } else {
         return blockBench;
      }
   }

   private JsonObject toBlockBenchChild(MinecraftGeometry geometry, Bone bone) {
      JsonObject child = new JsonObject();
      child.addProperty("name", bone.name);
      if (bone.pivot != null) {
         JsonArray arrayOrigin = new JsonArray();
         arrayOrigin.add(new JsonPrimitive(-bone.pivot.get(0)));
         arrayOrigin.add(new JsonPrimitive(bone.pivot.get(1)));
         arrayOrigin.add(new JsonPrimitive(bone.pivot.get(2)));
         child.add("origin", arrayOrigin);
      }

      if (bone.rotation != null) {
         JsonArray arrayRotation = new JsonArray();
         arrayRotation.add(new JsonPrimitive(-bone.rotation.get(0)));
         arrayRotation.add(new JsonPrimitive(-bone.rotation.get(1)));
         arrayRotation.add(new JsonPrimitive(bone.rotation.get(2)));
         child.add("rotation", arrayRotation);
      }

      child.addProperty("uuid", bone.uuid);
      child.add("children", this.findChildren(geometry, bone));
      return child;
   }

   private JsonArray findChildren(MinecraftGeometry geometry, Bone targetBone) {
      JsonArray array = new JsonArray();

      for (Bone bone : geometry.bones) {
         if (bone.parent != null && bone.parent.equals(targetBone.name)) {
            array.add(this.toBlockBenchChild(geometry, bone));
         }
      }

      if (targetBone.cubes != null) {
         for (BedrockCube cube : targetBone.cubes) {
            array.add(new JsonPrimitive(cube.uuid));
         }
      }

      return array;
   }

   private BlockBenchCube toBlockBenchCube(BedrockCube cube, String name) {
      BlockBenchCube bbCube = new BlockBenchCube();
      bbCube.name = name;
      bbCube.from = Arrays.asList(-cube.origin.get(0) - (double)cube.size.get(0).intValue(), cube.origin.get(1), cube.origin.get(2));
      bbCube.to = Arrays.asList(
         -cube.origin.get(0), cube.origin.get(1) + (double)cube.size.get(1).intValue(), cube.origin.get(2) + (double)cube.size.get(2).intValue()
      );
      if (cube.rotation != null) {
         bbCube.rotation = Arrays.asList(-cube.rotation.get(0), -cube.rotation.get(1), cube.rotation.get(2));
      }

      if (cube.pivot != null) {
         bbCube.origin = Arrays.asList(-cube.pivot.get(0), cube.pivot.get(1), cube.pivot.get(2));
      }

      bbCube.inflate = cube.inflate;
      bbCube.uvOffset = cube.uv;
      bbCube.uuid = cube.uuid;
      bbCube.mirror = cube.mirror;
      return bbCube;
   }
}
