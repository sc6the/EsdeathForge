package me.txb1.player.modulesystem.modules.render;

import me.powns.glintcolorizer.GlintColorizerMod;
import me.powns.glintcolorizer.asm.Colors;
import me.powns.glintcolorizer.gui.MainSettingsGui;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.gui.GuiScreen;

// GlintCustomizer: recolours the enchantment glint (items + armour), with strength/speed/chroma —
// ported from GlintColorizer. The (name-agnostic) glint transformers always read Colors, so when
// the module is OFF we hold the vanilla values there to keep the stock purple glint. The settings
// pencil opens GlintColorizer's own MainSettingsGui.
public class GlintCustomizer extends Module {
   public static boolean active;
   private static GlintColorizerMod glintMod;

   public GlintCustomizer() {
      super("GlintCustomizer", "GlintCustomizer", Category.RENDER, true);
      // default to the stock glint so it looks vanilla until the module is enabled
      applyVanilla();
   }

   private static GlintColorizerMod mod() {
      if (glintMod == null) {
         glintMod = new GlintColorizerMod();
      }
      return glintMod;
   }

   @Override
   public void onEnable() {
      GlintColorizerMod.loadConfig(); // sets Colors from GlintColorizer/colors_189.settings
      active = true;
   }

   @Override
   public void onDisable() {
      active = false;
      applyVanilla();
   }

   @Override
   public GuiScreen getCustomSettingsGui() {
      return new MainSettingsGui(mod());
   }

   // vanilla glint: item colour -8372020, armour tint (0.5,0.25,0.8), default animation periods.
   private static void applyVanilla() {
      Colors.onepoint8glintcolorI = -8372020;
      Colors.armorGlintR = 0.5F;
      Colors.armorGlintG = 0.25F;
      Colors.armorGlintB = 0.8F;
      Colors.glintPeriodA = 3000L;
      Colors.glintPeriodB = 4873L;
      Colors.glintPeriodAF = 3000.0F;
      Colors.glintPeriodBF = 4873.0F;
      Colors.chroma = false;
   }
}
