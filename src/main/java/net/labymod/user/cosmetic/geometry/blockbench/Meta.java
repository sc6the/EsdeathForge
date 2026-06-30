package net.labymod.user.cosmetic.geometry.blockbench;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Meta {
   @SerializedName("format_version")
   @Expose
   public String formatVersion;
   @SerializedName("creation_time")
   @Expose
   public Integer creationTime;
   @SerializedName("model_format")
   @Expose
   public String modelFormat;
   @SerializedName("box_uv")
   @Expose
   public Boolean boxUv;
}
