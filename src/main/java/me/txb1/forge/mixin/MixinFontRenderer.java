package me.txb1.forge.mixin;

import me.txb1.extras.settings.theme.LowercaseMode;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// "All Lowercase" mode: lowercases the text of every string rendered through FontRenderer. Hooking
// renderString (the single funnel used by drawString/drawStringWithShadow) covers menus, the HUD,
// chat and in-world text. § colour codes are already lowercase letters, so this leaves them intact.
@Mixin(FontRenderer.class)
public class MixinFontRenderer {

   @ModifyVariable(method = "renderString", at = @At("HEAD"), argsOnly = true)
   private String esdeath$lowercase(String text) {
      if (LowercaseMode.enabled && text != null) {
         return text.toLowerCase();
      }
      return text;
   }

   // While the HUD editor measures, record each drawn string's screen rect so it can outline elements.
   @org.spongepowered.asm.mixin.injection.Inject(method = "renderString", at = @At("HEAD"))
   private void esdeath$measure(String text, float x, float y, int color, boolean dropShadow,
         org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Integer> cir) {
      if (me.txb1.extras.settings.anzeige.HudBoundsRecorder.recording && text != null && !text.isEmpty()) {
         net.minecraft.client.gui.FontRenderer self = (net.minecraft.client.gui.FontRenderer) (Object) this;
         me.txb1.extras.settings.anzeige.HudBoundsRecorder.report((int) x, (int) y, self.getStringWidth(text), self.FONT_HEIGHT);
      }
   }

   // Width must be measured on the same (lowercased) text that gets drawn, otherwise centred text
   // (drawCenteredString uses getStringWidth) is positioned for the wider uppercase width and ends up
   // off-centre.
   @ModifyVariable(method = "getStringWidth", at = @At("HEAD"), argsOnly = true)
   private String esdeath$lowercaseWidth(String text) {
      if (LowercaseMode.enabled && text != null) {
         return text.toLowerCase();
      }
      return text;
   }
}

