package net.labymod.user.cosmetic.geometry.bedrock;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.UUID;

public class BedrockCube {
   @SerializedName("origin")
   @Expose
   public List<Double> origin = null;
   @SerializedName("size")
   @Expose
   public List<Integer> size = null;
   @SerializedName("uv")
   @Expose
   public List<Integer> uv = null;
   @SerializedName("pivot")
   @Expose
   public List<Double> pivot = null;
   @SerializedName("rotation")
   @Expose
   public List<Double> rotation = null;
   @SerializedName("mirror")
   @Expose
   public Boolean mirror = false;
   @Expose
   public Float inflate = 0.0F;
   public String uuid = UUID.randomUUID().toString();
}
