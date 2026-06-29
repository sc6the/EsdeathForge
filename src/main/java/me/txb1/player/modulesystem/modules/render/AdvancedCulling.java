package me.txb1.player.modulesystem.modules.render;

import java.lang.reflect.Field;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;

// AdvancedCulling: front-end for the separately-loaded EntityCulling mod (dev.tr7zw.entityculling).
// This build's Config has no "Hide Foliage"/"Chams Mode" fields (those never existed in tr7zw
// EntityCulling), so we toggle the two real boolean options it does expose — nametags-through-walls
// (Config.renderNametagsThroughWalls) and skip-marker-armorstands (Config.skipMarkerArmorStands) —
// via reflection, since EntityCulling is its own jar (not a compile dependency). No-ops gracefully if
// EntityCulling isn't installed.
public class AdvancedCulling extends Module {
   private static final String BASE = "dev.tr7zw.entityculling.EntityCullingModBase";

   public AdvancedCulling() {
      super("AdvancedCulling", "AdvancedCulling", Category.RENDER, true);
   }

   private static Object config() throws Exception {
      Class<?> base = Class.forName(BASE);
      Object instance = base.getField("instance").get(null);
      return base.getField("config").get(instance);
   }

   private static boolean get(String field) {
      try {
         Object cfg = config();
         return cfg.getClass().getField(field).getBoolean(cfg);
      } catch (Throwable t) {
         return false;
      }
   }

   private static void toggle(String field) {
      try {
         Object cfg = config();
         Field f = cfg.getClass().getField(field);
         f.setBoolean(cfg, !f.getBoolean(cfg));
         // persist via EntityCullingModBase#writeConfig()
         Class<?> base = Class.forName(BASE);
         Object instance = base.getField("instance").get(null);
         base.getMethod("writeConfig").invoke(instance);
      } catch (Throwable ignored) {
      }
   }

   private static boolean available() {
      try {
         Class.forName(BASE);
         return true;
      } catch (Throwable t) {
         return false;
      }
   }

   // content sits below the Back button (drawn by SettingsGui at y+5..y+18) to avoid overlapping it.
   private static final int TITLE_DY = 24;
   private static final int ROW0_DY = 38;
   private static final int ROW_STEP = 14;

   @Override
   public void onSettingsDrawScreen(int mouseX, int mouseY, int x, int y, int right, int bottom, int height, int width) {
      int sx = x + 10;
      if (!available()) {
         Minecraft.getMinecraft().fontRendererObj
            .drawStringWithShadow("§cEntityCulling not installed", (float) sx, (float) (y + TITLE_DY), -1);
         return;
      }
      Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("§7Advanced Culling", (float) sx, (float) (y + TITLE_DY), -1);
      row(sx, y + ROW0_DY, "Nametags Thru Walls", get("renderNametagsThroughWalls"));
      row(sx, y + ROW0_DY + ROW_STEP, "Skip Marker Stands", get("skipMarkerArmorStands"));
   }

   private void row(int x, int y, String label, boolean on) {
      Minecraft.getMinecraft().fontRendererObj
         .drawStringWithShadow((on ? "§a[ON] " : "§c[OFF] ") + "§7" + label, (float) x, (float) y, -1);
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button, int x, int y, int height, int width) {
      if (button != 0 || !available()) {
         return;
      }
      int sx = x + 10;
      int row0 = y + ROW0_DY;
      int w = Minecraft.getMinecraft().fontRendererObj.getStringWidth("[OFF] Nametags Thru Walls") + 10;
      if (mouseX < sx || mouseX > sx + w) {
         return;
      }
      if (mouseY >= row0 && mouseY <= row0 + 9) {
         toggle("renderNametagsThroughWalls");
      } else if (mouseY >= row0 + ROW_STEP && mouseY <= row0 + ROW_STEP + 9) {
         toggle("skipMarkerArmorStands");
      }
   }
}
