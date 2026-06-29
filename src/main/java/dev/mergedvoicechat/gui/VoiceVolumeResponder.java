package dev.mergedvoicechat.gui;

import java.util.UUID;
import net.labymod.addons.voicechat.VoiceChat;
import net.minecraft.client.gui.GuiPageButtonList;

// Top-level to avoid raven.jar's transformer NPE on lazily-loaded inner classes (see
// VoiceSettingsResponder). Drives the per-player volume slider in GuiVolume.
public final class VoiceVolumeResponder implements GuiPageButtonList.GuiResponder {
   private final VoiceChat voiceChat;
   private final UUID uuid;

   public VoiceVolumeResponder(VoiceChat voiceChat, UUID uuid) {
      this.voiceChat = voiceChat;
      this.uuid = uuid;
   }

   @Override
   public void func_175321_a(int id, boolean v) {
   }

   @Override
   public void onTick(int id, float value) {
      voiceChat.playerVolumes.put(uuid, (int) value);
   }

   @Override
   public void func_175319_a(int id, String s) {
   }
}
