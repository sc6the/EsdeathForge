package net.labymod.user.cosmetic.geometry.bedrock;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.UUID;

public class Bone {
   @SerializedName("name")
   @Expose
   public String name;
   @SerializedName("pivot")
   @Expose
   public List<Double> pivot = null;
   @SerializedName("parent")
   @Expose
   public String parent;
   @SerializedName("cubes")
   @Expose
   public List<BedrockCube> cubes = null;
   @SerializedName("rotation")
   @Expose
   public List<Double> rotation = null;
   public String uuid = UUID.randomUUID().toString();
}
