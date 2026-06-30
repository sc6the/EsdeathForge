package net.labymod.user.cosmetic.geometry.blockbench;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class BlockBench {
   @SerializedName("meta")
   @Expose
   public Meta meta;
   @SerializedName("name")
   @Expose
   public String name;
   @SerializedName("geometry_name")
   @Expose
   public String geometryName;
   @SerializedName("modded_entity_version")
   @Expose
   public String moddedEntityVersion;
   @SerializedName("visible_box")
   @Expose
   public List<Double> visibleBox = null;
   @SerializedName("layered_textures")
   @Expose
   public Boolean layeredTextures;
   @SerializedName("resolution")
   @Expose
   public Resolution resolution = new Resolution();
   @SerializedName("elements")
   @Expose
   public List<BlockBenchCube> elements = new ArrayList<>();
   @SerializedName("outliner")
   @Expose
   public JsonArray outliner = new JsonArray();
   @SerializedName("textures")
   @Expose
   public List<Texture> textures = null;
}
