package me.txb1.player.modulesystem.modules.render.crosshair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;

// Ported from CrosshairMod-1.0.0 (com.customxhair.CrosshairConfig). Pure data + JSON
// persistence (gson + commons-io, both already on the MC classpath). No Forge.
public final class CrosshairConfig {
   public static final int SIZE_15 = 15;
   public static final int SIZE_16 = 16;
   public static final int SIZE_32 = 32;
   public static final int SIZE_33 = 33;
   public static final int SCALE_AUTO = 0;
   public static final int SCALE_SMALL = 1;
   public static final int SCALE_NORMAL = 2;
   public static final int SCALE_LARGE = 3;
   public int gridSize = 15;
   public int colorARGB = -1;
   public boolean vanillaBlending = true;
   public int scale = 0;
   public boolean[][] pixels15 = new boolean[15][15];
   public boolean[][] pixels16 = new boolean[16][16];
   public boolean[][] pixels32 = new boolean[32][32];
   public boolean[][] pixels33 = new boolean[33][33];
   private File file;

   public CrosshairConfig() {
      seedDefaultCross(this.pixels15, 15);
      seedDefaultCross(this.pixels16, 16);
      seedDefaultCross(this.pixels32, 32);
      seedDefaultCross(this.pixels33, 33);
   }

   public void attach(File configDir) {
      this.file = new File(configDir, "crosshairmod.json");
      if (this.file.exists()) {
         this.load();
      } else {
         this.save();
      }
   }

   public boolean[][] activeGrid() {
      switch (this.gridSize) {
         case 16:
            return this.pixels16;
         case 32:
            return this.pixels32;
         case 33:
            return this.pixels33;
         case 15:
         default:
            return this.pixels15;
      }
   }

   public void clearActive() {
      boolean[][] g = this.activeGrid();
      for (int y = 0; y < g.length; y++) {
         for (int x = 0; x < g[y].length; x++) {
            g[y][x] = false;
         }
      }
   }

   public void load() {
      try {
         String json = FileUtils.readFileToString(this.file, Charset.forName("UTF-8"));
         JsonObject root = new JsonParser().parse(json).getAsJsonObject();
         if (root.has("gridSize")) {
            int g = root.get("gridSize").getAsInt();
            if (g == 15 || g == 16 || g == 32 || g == 33) {
               this.gridSize = g;
            }
         }
         if (root.has("colorARGB")) {
            this.colorARGB = root.get("colorARGB").getAsInt();
         }
         if (root.has("vanillaBlending")) {
            this.vanillaBlending = root.get("vanillaBlending").getAsBoolean();
         }
         if (root.has("scale")) {
            int s = root.get("scale").getAsInt();
            if (s >= 0 && s <= 3) {
               this.scale = s;
            }
         }
         if (root.has("pixels15")) {
            decode(root.get("pixels15").getAsString(), this.pixels15);
         }
         if (root.has("pixels16")) {
            decode(root.get("pixels16").getAsString(), this.pixels16);
         }
         if (root.has("pixels32")) {
            decode(root.get("pixels32").getAsString(), this.pixels32);
         }
         if (root.has("pixels33")) {
            decode(root.get("pixels33").getAsString(), this.pixels33);
         }
         if (this.gridSize != 15 && this.gridSize != 16 && this.gridSize != 32 && this.gridSize != 33) {
            this.gridSize = 15;
         }
      } catch (Exception var4) {
         System.err.println("[CrosshairMod] failed to load config: " + var4);
      }
   }

   public void save() {
      try {
         if (this.file == null) {
            return;
         }
         JsonObject root = new JsonObject();
         root.addProperty("gridSize", this.gridSize);
         root.addProperty("colorARGB", this.colorARGB);
         root.addProperty("vanillaBlending", this.vanillaBlending);
         root.addProperty("scale", this.scale);
         root.addProperty("pixels15", encode(this.pixels15));
         root.addProperty("pixels16", encode(this.pixels16));
         root.addProperty("pixels32", encode(this.pixels32));
         root.addProperty("pixels33", encode(this.pixels33));
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
         FileUtils.writeStringToFile(this.file, gson.toJson(root), Charset.forName("UTF-8"));
      } catch (Exception var3) {
         System.err.println("[CrosshairMod] failed to save config: " + var3);
      }
   }

   private static String encode(boolean[][] g) {
      StringBuilder sb = new StringBuilder(g.length * g.length);
      for (int y = 0; y < g.length; y++) {
         for (int x = 0; x < g[y].length; x++) {
            sb.append((char)(g[y][x] ? '1' : '0'));
         }
      }
      return sb.toString();
   }

   private static void decode(String s, boolean[][] g) {
      int n = g.length;
      if (s != null && s.length() >= n * n) {
         for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
               g[y][x] = s.charAt(y * n + x) == '1';
            }
         }
      }
   }

   private static void seedDefaultCross(boolean[][] g, int n) {
      int c = n / 2;
      int half = n >= 32 ? 6 : 3;
      for (int dy = -half; dy <= half; dy++) {
         g[c + dy][c] = true;
      }
      for (int dx = -half; dx <= half; dx++) {
         g[c][c + dx] = true;
      }
   }
}
