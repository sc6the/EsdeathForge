package net.labymod.user.cosmetic.geometry.blockbench;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Faces {
   @SerializedName("north")
   @Expose
   public North north;
   @SerializedName("east")
   @Expose
   public East east;
   @SerializedName("south")
   @Expose
   public South south;
   @SerializedName("west")
   @Expose
   public West west;
   @SerializedName("up")
   @Expose
   public Up up;
   @SerializedName("down")
   @Expose
   public Down down;
}
