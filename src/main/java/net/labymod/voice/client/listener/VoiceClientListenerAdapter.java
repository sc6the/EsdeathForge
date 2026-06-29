package net.labymod.voice.client.listener;

import java.util.List;
import java.util.UUID;
import net.labymod.voice.client.config.VoiceClientConfiguration;
import net.labymod.voice.protocol.type.DisconnectType;

public class VoiceClientListenerAdapter implements VoiceClientListener {
   @Override
   public void onDisconnected(DisconnectType type, String name) {
   }

   @Override
   public void onAuthenticated(VoiceClientConfiguration configuration, boolean admin) {
   }

   @Override
   public void onMute(UUID player, String reason, long secondsLeft, String mutedBy) {
   }

   @Override
   public void onUnmute(UUID player) {
   }

   @Override
   public void onPlayerDiscovered(List<UUID> players) {
   }

   @Override
   public void onPlayerDisappeared(List<UUID> players) {
   }

   @Override
   public void onUpdateReport(UUID player, String reason, int count) {
   }

   @Override
   public void onAudioReceived(UUID player, byte[] data) {
   }
}
