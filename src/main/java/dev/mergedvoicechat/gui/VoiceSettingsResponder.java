package dev.mergedvoicechat.gui;

import net.labymod.addons.voicechat.VoiceChat;
import net.minecraft.client.gui.GuiPageButtonList;

// Top-level (NOT an inner class) on purpose: raven.jar's obfuscated transformer NPEs when it lazily
// transforms inner/anonymous classes the first time the settings GUI opens (NoClassDefFoundError on
// GuiSettings$SettingsResponder). Hoisting it out avoids that.
public final class VoiceSettingsResponder implements GuiPageButtonList.GuiResponder {
   private final VoiceChat vc;

   public VoiceSettingsResponder(VoiceChat vc) {
      this.vc = vc;
   }

   @Override
   public void func_175321_a(int id, boolean v) {
   }

   @Override
   public void onTick(int id, float value) {
      int v = (int) value;
      switch (id) {
         case 10: vc.surroundVolume = v; break;
         case 11: vc.surroundRange = v; break;
         case 12: vc.microphoneVolume = v; break;
         case 13: vc.maxSwapRate = v; break;
         case 14: vc.compressorTarget = v; break;
         case 17: vc.hudScale = v; break;
         case 20: vc.glowOpacity = v; break;
         case 21: vc.idleOpacity = v; break;
         case 22: vc.hudOpacity = v; break;
      }
   }

   @Override
   public void func_175319_a(int id, String s) {
   }
}
