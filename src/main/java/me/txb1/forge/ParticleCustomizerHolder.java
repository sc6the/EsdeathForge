package me.txb1.forge;

import me.powns.particlecustomiser.ParticleCustomiserMod;
import me.powns.particlecustomiser.gui.screens.EditorMenu;
import me.powns.particlecustomiser.settings.ParticleSettings;
import me.powns.particlecustomiser.settings.Settings;
import me.powns.particlecustomiser.utils.ColorGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

// Bridges the bundled ParticleCustomiser into Esdeath. Its mod class is a DummyModContainer that
// the original coremod instantiated; since we dropped the coremod, we create + register it here
// (config load + event bus) and expose the editor GUI. MixinEntityFX calls the colour/scale/opacity
// getters below, which reuse the customiser's ParticleSettings (filled by its editor GUI).
public final class ParticleCustomizerHolder {
   private static ParticleCustomiserMod mod;
   private static boolean inited;

   private ParticleCustomizerHolder() {}

   public static synchronized void init() {
      if (inited) {
         return;
      }
      inited = true;
      try {
         mod = new ParticleCustomiserMod();
         MinecraftForge.EVENT_BUS.register(mod);
         FMLCommonHandler.instance().bus().register(mod);
         ParticleCustomiserMod.getSettings().loadConfig();
      } catch (Throwable t) {
         System.err.println("[Esdeath] ParticleCustomiser init failed: " + t);
      }
   }

   public static void openEditor() {
      init();
      if (mod != null) {
         Minecraft.getMinecraft().displayGuiScreen(new EditorMenu(mod));
      }
   }

   private static ParticleSettings settingFor(EntityFX fx) {
      Settings s = ParticleCustomiserMod.getSettings();
      return s == null ? null : s.getSetting(fx);
   }

   // ---- colour/scale/opacity getters used by MixinEntityFX. The mixin passes the original field
   //      value (EntityFX's colour fields are protected); we return that when not customised. (The
   //      original ColorGetter returned scale for unconfigured colours, a bug we avoid here.) ----
   public static float getRed(EntityFX fx, float original) {
      ParticleSettings s = settingFor(fx);
      if (s != null && s.isCustom() && s.isCustomColor()) {
         int c = s.isChroma() ? ColorGetter.getChromaColor() : s.getColor();
         return (c >> 16 & 0xFF) / 255.0F;
      }
      return original;
   }

   public static float getGreen(EntityFX fx, float original) {
      ParticleSettings s = settingFor(fx);
      if (s != null && s.isCustom() && s.isCustomColor()) {
         int c = s.isChroma() ? ColorGetter.getChromaColor() : s.getColor();
         return (c >> 8 & 0xFF) / 255.0F;
      }
      return original;
   }

   public static float getBlue(EntityFX fx, float original) {
      ParticleSettings s = settingFor(fx);
      if (s != null && s.isCustom() && s.isCustomColor()) {
         int c = s.isChroma() ? ColorGetter.getChromaColor() : s.getColor();
         return (c & 0xFF) / 255.0F;
      }
      return original;
   }

   public static float getAlpha(EntityFX fx, float original) {
      ParticleSettings s = settingFor(fx);
      if (s != null && s.isCustom() && s.isCustomOpacity()) {
         return s.getOpacity();
      }
      return original;
   }

   public static float getScale(EntityFX fx, float original) {
      ParticleSettings s = settingFor(fx);
      if (s != null && s.isCustom() && s.isCustomScale()) {
         return s.getScale();
      }
      return original;
   }
}
