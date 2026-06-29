package me.txb1.forge.coremod;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

// Registers RenderGlobalInitFix. High SortingIndex so it loads after the deobf boundary (1000) and
// after raven's transformer, letting us patch RenderGlobal.<init> last. Coexists with the
// MixinTweaker declared in the same manifest.
// Transformer packages are excluded from being transformed themselves (esp. the bundled voice-chat
// OpenAL transformer, which the standalone mergedvoicechat coremod declared the same exclusion for).
@IFMLLoadingPlugin.SortingIndex(100000)
@IFMLLoadingPlugin.TransformerExclusions({
   // All of Esdeath's own code. Nothing legitimately transforms these (our mixins/transformers target
   // net.minecraft + the bundled mods), and the co-loaded raven transformer NPEs on some of them
   // (e.g. PinnedPacks), which crashed opening the resource-pack screen. Excluding the whole package
   // keeps raven (and every transformer) off our classes.
   "me.txb1",
   // UAM (account manager / skin / cookie auth) — raven NPEs transforming e.g. GuiAccountManager,
   // which crashed opening the Account Manager. Exclude the whole package like our own code.
   "me.proxycracked",
   "me.powns.glintcolorizer.asm",
   "dev.mergedvoicechat.coremod",
   "me.djtheredstoner.perspectivemod.asm",
   // SmoothFont's own classes (its FontRendererHook is injected into FontRenderer) must not be run
   // through the transformer chain themselves.
   "bre.smoothfont"
})
public class EsdeathCorePlugin implements IFMLLoadingPlugin {
   @Override
   public String[] getASMTransformerClass() {
      return new String[] {
         "me.txb1.forge.coremod.RenderGlobalInitFix",
         "me.txb1.forge.coremod.DamageTintTransformer",
         // GlintColorizer: armor transformer is already constant-pattern (name-agnostic); the item
         // one is re-gated for the SRG stage. Both read me.powns.glintcolorizer.asm.Colors.
         "me.powns.glintcolorizer.asm.armor.LayerArmorTransformer",
         "me.txb1.forge.coremod.GlintItemTransformer",
         // mergedvoicechat (bundled): patches paulscode SourceLWJGLOpenAL so surround voice audio
         // can hook Minecraft's OpenAL output. Targets a non-MC class, so the deobf stage is moot.
         "dev.mergedvoicechat.coremod.OpenALTransformer",
         // PerspectiveMod v4 (bundled): free-look third-person camera. Its transformers match via
         // FMLDeobfuscatingRemapper (MCP+SRG names), so they're SortingIndex-agnostic and work at
         // our post-deobf stage. Standalone PerspectiveModTweaker is NOT used (one FMLCorePlugin/jar).
         "me.djtheredstoner.perspectivemod.asm.ClassTransformer",
         // SmoothFont (bundled): smooth/AA fonts. Transforms FontRenderer/ScaledResolution/
         // TextureManager/GuiIngameForge by their deobf names, so it works at our post-deobf stage.
         "bre.smoothfont.asm.Transformer"
      };
   }

   @Override
   public String getModContainerClass() {
      // SmoothFont ships as a coremod whose mod (bre.smoothfont.mod_SmoothFont) is loaded via this
      // DummyModContainer rather than an @Mod annotation. Providing it here co-loads SmoothFont (its
      // /sfont command, config GUI + keybind) without adding a second FMLCorePlugin to this jar.
      return "bre.smoothfont.asm.ModContainer";
   }

   @Override
   public String getSetupClass() {
      return null;
   }

   @Override
   public void injectData(Map<String, Object> data) {
      // Run SmoothFont's own coremod setup: detect OptiFine and, crucially, create + load
      // CommonConfig.globalConfig. Its Transformer dereferences globalConfig while transforming
      // FontRenderer, so this MUST happen before that class is loaded.
      try {
         new bre.smoothfont.asm.CorePlugin().injectData(data);
      } catch (Throwable t) {
         System.err.println("[Esdeath] SmoothFont coremod setup failed: " + t);
      }
   }

   @Override
   public String getAccessTransformerClass() {
      return null;
   }
}
