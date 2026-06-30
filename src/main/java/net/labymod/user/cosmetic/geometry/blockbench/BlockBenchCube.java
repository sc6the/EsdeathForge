package net.labymod.user.cosmetic.geometry.blockbench;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BlockBenchCube extends Item {
   @SerializedName("from")
   @Expose
   public List<Double> from = null;
   @SerializedName("to")
   @Expose
   public List<Double> to = null;
   @SerializedName("autouv")
   @Expose
   public Integer autouv;
   @SerializedName("color")
   @Expose
   public Integer color;
   @SerializedName("locked")
   @Expose
   public Boolean locked;
   @SerializedName("faces")
   @Expose
   public Faces faces;
   @SerializedName("uuid")
   @Expose
   public String uuid;
   @SerializedName("uv_offset")
   @Expose
   public List<Integer> uvOffset = null;
   @Expose
   public Float inflate = 0.0F;
}
