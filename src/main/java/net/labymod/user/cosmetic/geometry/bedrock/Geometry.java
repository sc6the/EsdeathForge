package net.labymod.user.cosmetic.geometry.bedrock;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Geometry {
   @SerializedName("format_version")
   @Expose
   public String formatVersion;
   @SerializedName("minecraft:geometry")
   @Expose
   public List<MinecraftGeometry> minecraftGeometry = null;
}
