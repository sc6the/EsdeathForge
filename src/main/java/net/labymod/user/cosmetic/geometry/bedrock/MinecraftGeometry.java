package net.labymod.user.cosmetic.geometry.bedrock;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MinecraftGeometry {
   @SerializedName("description")
   @Expose
   public Description description;
   @SerializedName("bones")
   @Expose
   public List<Bone> bones = null;
}
