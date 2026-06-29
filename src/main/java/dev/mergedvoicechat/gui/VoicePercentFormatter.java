package dev.mergedvoicechat.gui;

import net.minecraft.client.gui.GuiSlider;

// Top-level to avoid raven.jar's transformer NPE on lazily-loaded inner classes.
public final class VoicePercentFormatter implements GuiSlider.FormatHelper {
   @Override
   public String getText(int id, String name, float value) {
      return name + ": " + (int) value + "%";
   }
}
