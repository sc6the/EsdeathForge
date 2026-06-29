package me.txb1.extras.settings;

import java.awt.Color;
import java.util.HashMap;
import me.txb1.extras.settings.anzeige.HudEditorGui;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

public class SettingsUtil {
   public static HashMap<String, Integer> vals = new HashMap<>();
   public static Integer load = 0;

   // HUD positioning now lives in the unified editor opened from the Esc-menu "Display" panel, so the
   // old per-module "Display" drag box is gone (these are kept as no-ops for the modules that call them).
   public static void drawVisual(int var0, int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
         "§7Use the move handle (bottom-left of the module) to reposition.", (float) (var2 + 5), (float) (var3 + 18), -1);
   }

   public static void mouseClickVisual(int var0, int var1, int var2, int var3, int var4, int var5, int var6, Module var7) {
   }
}
