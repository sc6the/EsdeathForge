package net.labymod.user.cosmetic.geometry.blockbench;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Group extends Item {
   @SerializedName("uuid")
   @Expose
   public String uuid;
   @SerializedName("export")
   @Expose
   public Boolean export;
   @SerializedName("isOpen")
   @Expose
   public Boolean isOpen;
   @SerializedName("locked")
   @Expose
   public Boolean locked;
   @SerializedName("visibility")
   @Expose
   public Boolean visibility;
   @SerializedName("autouv")
   @Expose
   public Integer autouv;
   @SerializedName("children")
   @Expose
   public JsonArray children = null;
}
