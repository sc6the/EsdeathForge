package net.labymod.user.cosmetic.geometry.blockbench;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Item {
   @SerializedName("name")
   @Expose
   public String name;
   @SerializedName("origin")
   @Expose
   public List<Double> origin = null;
   @SerializedName("rotation")
   @Expose
   public List<Double> rotation = null;
   @SerializedName("mirror")
   @Expose
   public boolean mirror = false;
}
