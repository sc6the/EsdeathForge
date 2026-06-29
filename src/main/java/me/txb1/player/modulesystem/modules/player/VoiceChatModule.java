package me.txb1.player.modulesystem.modules.player;

import dev.mergedvoicechat.Config;
import dev.mergedvoicechat.MergedVoiceChat;
import dev.mergedvoicechat.gui.GuiSettings;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.labymod.addons.voicechat.VoiceChat;
import net.minecraft.client.gui.GuiScreen;

// Front-end for the bundled mergedvoicechat (@Mod dev.mergedvoicechat.MergedVoiceChat). The voice mod
// co-loads and owns all the lifecycle (network channels, audio, config, mixins); this module just
// reflects/toggles its `enabled` flag and opens its settings screen via the edit pencil.
public class VoiceChatModule extends Module {

   public VoiceChatModule() {
      super("VoiceChat", "VoiceChat", Category.PLAYER, true);
   }

   private static VoiceChat vc() {
      return MergedVoiceChat.INSTANCE != null ? MergedVoiceChat.INSTANCE.voiceChat : null;
   }

   // Read/write the voice mod's live `enabled` flag directly so the module button always matches the
   // real state (same pattern as PerspectiveModule, avoids a desynced local `toggled`).
   @Override
   public boolean isEnabled() {
      VoiceChat v = vc();
      return v != null && v.enabled;
   }

   @Override
   public void toggle() {
      VoiceChat v = vc();
      if (v == null) return;
      v.enabled = !v.enabled;
      try {
         Config.save(v);
         v.sendSettingsToServer();
      } catch (Throwable ignored) {
      }
   }

   @Override
   public GuiScreen getCustomSettingsGui() {
      VoiceChat v = vc();
      if (v == null) return null;
      try {
         return new GuiSettings(v);
      } catch (Throwable t) {
         return null;
      }
   }
}
