package net.labymod.user.cosmetic.geometry.blockbench;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class East {
   @SerializedName("uv")
   @Expose
   public List<Integer> uv = null;
   @SerializedName("texture")
   @Expose
   public Integer texture;
}
