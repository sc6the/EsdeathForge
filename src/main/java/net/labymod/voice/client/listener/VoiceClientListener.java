package net.labymod.voice.client.listener;

import java.util.List;
import java.util.UUID;
import net.labymod.voice.client.config.VoiceClientConfiguration;
import net.labymod.voice.protocol.type.DisconnectType;

public interface VoiceClientListener {
   void onDisconnected(DisconnectType var1, String var2);

   void onAuthenticated(VoiceClientConfiguration var1, boolean var2);

   void onMute(UUID var1, String var2, long var3, String var5);

   void onUnmute(UUID var1);

   void onPlayerDiscovered(List<UUID> var1);

   void onPlayerDisappeared(List<UUID> var1);

   void onUpdateReport(UUID var1, String var2, int var3);

   void onAudioReceived(UUID var1, byte[] var2);
}
