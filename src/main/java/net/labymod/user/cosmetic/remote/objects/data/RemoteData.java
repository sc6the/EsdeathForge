package net.labymod.user.cosmetic.remote.objects.data;

import java.awt.Color;
import java.util.Arrays;
import java.util.UUID;

// Minimal port of LabyMod 3's RemoteData — just the fields the geometry effects read (textureUUID +
// colors), plus rightSide/offset which the renderer uses. The full RemoteData extended CosmeticData
// and carried DepthMap/RemoteObject; those aren't needed for the LAYER/COLOR/GLOW/PHYSICS effects.
// loadData() parses a cosmetic's data array exactly as LM3 did, driven by the cosmetic's `options`.
public class RemoteData {
   public OffsetVector offset;
   public boolean rightSide;
   public UUID textureUUID;
   public Color[] colors = new Color[0];
   // Texture alpha depth-map, used by the extrude effect to cull voxel faces (set by the renderer
   // from the cosmetic's texture image once it loads).
   public net.labymod.user.cosmetic.custom.DepthMap depthMap;

   // options[i] tells what data[i] means: "texture"/"mojang_uuid" -> textureUUID, "rgb" -> a colour,
   // "offset" -> x;y;z, "side"/"shoulder_side" -> right side flag. (LM3 RemoteData.loadData.)
   public void loadData(String[] options, String[] data) {
      if (options == null || data == null) {
         return;
      }
      int min = Math.min(options.length, data.length);
      for (int i = 0; i < min; i++) {
         String key = options[i];
         String value = data[i];
         if (key == null || value == null) {
            continue;
         }
         try {
            if ("offset".equals(key)) {
               String[] v = value.split(";");
               this.offset = new OffsetVector(Double.parseDouble(v[0]), Double.parseDouble(v[1]), Double.parseDouble(v[2]));
            } else if ("shoulder_side".equals(key) || "side".equals(key)) {
               this.rightSide = Integer.parseInt(value) == 1;
            } else if ("texture".equals(key) || "mojang_uuid".equals(key)) {
               this.textureUUID = UUID.fromString(value);
            } else if ("rgb".equals(key)) {
               int n = this.colors.length;
               this.colors = Arrays.copyOf(this.colors, n + 1);
               this.colors[n] = Color.decode("#" + value);
            }
         } catch (Throwable ignored) {
         }
      }
   }

   public static class OffsetVector {
      public double x;
      public double y;
      public double z;

      public OffsetVector(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }
   }
}
