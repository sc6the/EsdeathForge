package net.labymod.user.cosmetic.geometry.bedrock;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Description {
   @SerializedName("identifier")
   @Expose
   public String identifier;
   @SerializedName("texture_width")
   @Expose
   public Integer textureWidth;
   @SerializedName("texture_height")
   @Expose
   public Integer textureHeight;
   @SerializedName("visible_bounds_width")
   @Expose
   public Integer visibleBoundsWidth;
   @SerializedName("visible_bounds_height")
   @Expose
   public Double visibleBoundsHeight;
   @SerializedName("visible_bounds_offset")
   @Expose
   public List<Double> visibleBoundsOffset = null;
}
