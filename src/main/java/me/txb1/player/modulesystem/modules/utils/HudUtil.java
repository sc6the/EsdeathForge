package me.txb1.player.modulesystem.modules.utils;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

// Shared HUD text renderer for the Utils HUD modules: draws lines at (x,y) with a scale (percent) and
// a solid colour. Line spacing scales with the text size.
public final class HudUtil {
   private HudUtil() {
   }

   public static void draw(List<String> lines, int x, int y, int scalePct, int rgb) {
      if (lines.isEmpty()) {
         return;
      }
      float s = Math.max(10, scalePct) / 100.0F;
      int color = 0xFF000000 | (rgb & 0xFFFFFF);
      Minecraft mc = Minecraft.getMinecraft();
      GlStateManager.pushMatrix();
      GlStateManager.scale(s, s, s);
      for (int i = 0; i < lines.size(); i++) {
         mc.fontRendererObj.drawStringWithShadow(lines.get(i), x / s, y / s + i * 10, color);
      }
      GlStateManager.popMatrix();
   }
}
