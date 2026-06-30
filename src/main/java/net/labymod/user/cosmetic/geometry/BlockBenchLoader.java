package net.labymod.user.cosmetic.geometry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.labymod.user.cosmetic.geometry.blockbench.BlockBench;
import net.labymod.user.cosmetic.geometry.blockbench.BlockBenchCube;
import net.labymod.user.cosmetic.geometry.blockbench.Group;
import net.labymod.user.cosmetic.geometry.blockbench.Item;
import net.labymod.user.cosmetic.geometry.effect.EnumEffectType;
import net.labymod.user.cosmetic.geometry.effect.GeometryEffect;
import net.labymod.user.cosmetic.geometry.render.GeometryModelRenderer;

public class BlockBenchLoader {
   private static final Gson GSON = new Gson();
   private BlockBench blockBench;
   private GeometryModelRenderer model;
   private final Map<String, BlockBenchCube> cubes = new HashMap<>();
   private final List<GeometryEffect> effects = new ArrayList<>();
   private final Map<String, Item> nameToItemMappings = new HashMap<>();
   private final Map<String, GeometryModelRenderer> nameToModelMappings = new HashMap<>();
   private final Map<String, Integer> nameToIdMappings = new HashMap<>();
   private final Map<Integer, String> idToNameMappings = new HashMap<>();

   public BlockBenchLoader(BlockBench blockBench) throws IOException {
      this.model = new GeometryModelRenderer();
      this.blockBench = blockBench;

      for (BlockBenchCube cube : this.blockBench.elements) {
         this.cubes.put(cube.uuid, cube);
      }

      this.addGroup(this.blockBench.outliner, this.model, new Group());
   }

   public BlockBenchLoader(InputStream inputStream) throws IOException {
      this((BlockBench)GSON.fromJson(new InputStreamReader(inputStream), BlockBench.class));
      inputStream.close();
   }

   public BlockBenchLoader(String json) throws IOException {
      this((BlockBench)GSON.fromJson(json, BlockBench.class));
   }

   public BlockBenchLoader(File file) throws IOException {
      this(new FileInputStream(file));
   }

   private void addGroup(JsonArray outliner, GeometryModelRenderer parent, Group group) {
      for (int i = 0; i < outliner.size(); i++) {
         JsonElement element = outliner.get(i);
         if (element.isJsonObject()) {
            Group child = (Group)GSON.fromJson(element, Group.class);
            GeometryModelRenderer model = this.addModel(parent, child, group);
            this.addGroup(child.children, model, child);
         } else {
            BlockBenchCube cube = this.cubes.get(element.getAsString());
            float originX = group.origin.get(0).floatValue();
            float originY = group.origin.get(1).floatValue();
            float originZ = group.origin.get(2).floatValue();
            float fromX = cube.from.get(0).floatValue();
            float fromY = cube.from.get(1).floatValue();
            float fromZ = cube.from.get(2).floatValue();
            float toX = cube.to.get(0).floatValue();
            float toY = cube.to.get(1).floatValue();
            float toZ = cube.to.get(2).floatValue();
            int sizeX = Math.round(Math.abs(fromX - toX));
            int sizeY = Math.round(Math.abs(fromY - toY));
            int sizeZ = Math.round(Math.abs(fromZ - toZ));
            float x = originX - toX;
            float y = -fromY - (float)sizeY + originY;
            float z = fromZ - originZ;
            int textureOffsetX = cube.uvOffset == null ? 0 : cube.uvOffset.get(0);
            int textureOffsetZ = cube.uvOffset == null ? 0 : cube.uvOffset.get(1);
            GeometryModelRenderer target = cube.rotation == null ? parent : this.addModel(parent, cube, group);
            if (cube.rotation != null) {
               x -= target.rotationPointX;
               y -= target.rotationPointY;
               z -= target.rotationPointZ;
            }

            target.setTextureOffset(textureOffsetX, textureOffsetZ);
            target.setTextureSize(this.blockBench.resolution.width, this.blockBench.resolution.height);
            boolean shouldAdd = true;
            GeometryEffect effect = this.getEffectByModel(target);
            if (effect != null) {
               shouldAdd = effect.onCubeAdd(target, x, y, z, sizeX, sizeY, sizeZ, cube.inflate, cube.mirror);
            }

            if (shouldAdd) {
               target.addBox(x, y, z, (float)sizeX, (float)sizeY, (float)sizeZ, cube.inflate, cube.mirror);
            }
         }
      }
   }

   private GeometryModelRenderer addModel(GeometryModelRenderer parentModel, Item item, Item group) {
      GeometryModelRenderer model = new GeometryModelRenderer();
      this.rotateGroup(model, item, group);
      parentModel.addChild(model);
      this.addMapping(model, item);
      return model;
   }

   private void rotateGroup(GeometryModelRenderer model, Item child, Item group) {
      float originX = child.origin == null ? 0.0F : child.origin.get(0).floatValue();
      float originY = child.origin == null ? 0.0F : child.origin.get(1).floatValue();
      float originZ = child.origin == null ? 0.0F : child.origin.get(2).floatValue();
      if (group.origin != null) {
         originX -= group.origin.get(0).floatValue();
         originY -= group.origin.get(1).floatValue();
         originZ -= group.origin.get(2).floatValue();
      }

      model.setRotationPoint(-originX, -originY, originZ);
      if (child.rotation != null) {
         float rotationX = child.rotation.get(0).floatValue();
         float rotationY = child.rotation.get(1).floatValue();
         float rotationZ = child.rotation.get(2).floatValue();
         model.rotateAngleX = (float)Math.toRadians((double)(-rotationX));
         model.rotateAngleY = (float)Math.toRadians((double)(-rotationY));
         model.rotateAngleZ = (float)Math.toRadians((double)rotationZ);
      }
   }

   private void addMapping(GeometryModelRenderer model, Item item) {
      int id = this.nameToIdMappings.size();
      String name = item.name;
      this.nameToItemMappings.put(name, item);
      this.nameToModelMappings.put(name, model);
      this.nameToIdMappings.put(name, id);
      this.idToNameMappings.put(id, name);

      try {
         GeometryEffect effect = EnumEffectType.createEffect(name, model);
         if (effect != null) {
            this.effects.add(effect);
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }
   }

   public GeometryModelRenderer getModel(String name) {
      return this.nameToModelMappings.get(name);
   }

   public Item getItem(String name) {
      return this.nameToItemMappings.get(name);
   }

   public Map<String, Item> getItems() {
      return this.nameToItemMappings;
   }

   public GeometryModelRenderer getModel(int id) {
      return this.getModel(this.idToNameMappings.get(id));
   }

   public int getModelId(String name) {
      return this.nameToIdMappings.get(name);
   }

   public Collection<GeometryModelRenderer> getModels() {
      return this.nameToModelMappings.values();
   }

   public Map<String, BlockBenchCube> getCubes() {
      return this.cubes;
   }

   public BlockBench getBlockBench() {
      return this.blockBench;
   }

   public GeometryModelRenderer getModel() {
      return this.model;
   }

   public List<GeometryEffect> getEffects() {
      return this.effects;
   }

   public int getTotalPoseCount() {
      return this.nameToModelMappings.size();
   }

   public GeometryEffect getEffectByModel(GeometryModelRenderer model) {
      for (GeometryEffect effect : this.effects) {
         if (effect.getModel() == model) {
            return effect;
         }
      }

      return null;
   }
}
