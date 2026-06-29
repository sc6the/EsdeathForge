package net.labymod.addons.voicechat.client;

import com.google.gson.Gson;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.labymod.addons.voicechat.VoiceChat;
import net.labymod.addons.voicechat.audio.AudioModifier;
import net.labymod.addons.voicechat.audio.opus.EnumOpusCodecDirection;
import net.labymod.addons.voicechat.audio.opus.EnumOpusError;
import net.labymod.addons.voicechat.audio.opus.OpusCodecManager;
import net.labymod.addons.voicechat.audio.surround.UserStream;
import net.labymod.core.LabyModCore;
import net.labymod.main.LabyMod;
import net.labymod.support.util.Debug;
import net.labymod.support.util.Debug.EnumDebugMode;
import net.labymod.voice.client.config.VoiceClientConfiguration;
import net.labymod.voice.client.listener.VoiceClientListener;
import net.labymod.voice.protocol.type.DisconnectType;
import net.minecraft.entity.player.EntityPlayer;

public class DefaultVoiceClientListener implements VoiceClientListener {
   private static final Gson GSON = new Gson();
   private final ExecutorService executorService = Executors.newFixedThreadPool(3);
   private AudioConfig audioConfig;
   private final VoiceChat voiceChat;
   private final Map<UUID, Long> highSwapMutedUsers = new HashMap<>();
   private final Map<UUID, Mute> globalMuted = new HashMap<>();
   private final Map<UUID, Report> reports = new HashMap<>();
   private boolean isAdmin;

   public DefaultVoiceClientListener(VoiceChat voiceChat) {
      this.voiceChat = voiceChat;
   }

   @Override
   public void onDisconnected(DisconnectType type, String reason) {
      this.voiceChat.setKicked(reason != null);
      if (reason == null) {
         reason = "VoiceChat: " + type.name();
      }

      this.voiceChat.focusedUser = null;
      this.voiceChat.log("Kick reason: " + reason);
      this.voiceChat.setLoginCompleted(false);
      this.voiceChat.setLastConnectionStatus(reason);
      this.voiceChat.setLastConnectionHandling(System.currentTimeMillis());
      this.voiceChat.setNextConnectionHandling(System.currentTimeMillis() + 10000L);
   }

   @Override
   public void onAuthenticated(VoiceClientConfiguration configuration, boolean admin) {
      this.isAdmin = admin;
      String json = configuration.getJsonConfig();
      this.audioConfig = (AudioConfig)GSON.fromJson(json, AudioConfig.class);
      this.globalMuted.clear();
      this.voiceChat.focusedUser = null;
      this.voiceChat.getServerIncomingPacketListener().setLastPlayersCount(0);
      this.voiceChat.getServerIncomingPacketListener().setPrevPlayersArrayHash(0);
      this.voiceChat.getServerIncomingPacketListener().setPrevUUIDArrayHash(0);
      if (this.audioConfig.getAudioFormat() == null) {
         this.voiceChat.log("Invalid audio format received: " + json);
         this.voiceChat.setLastConnectionStatus("VoiceChat has invalid audio format");
         this.voiceChat.setKicked(true);
         this.voiceChat.disconnect(false);
      } else if (this.audioConfig.getOpusCodec() != null && this.audioConfig.getOpusCodec().getSampleRate() != 0) {
         this.voiceChat.changeServer();
         OpusCodecManager opusCodecManager = this.voiceChat.getOpusCodecManager();
         opusCodecManager.init(this.audioConfig.getOpusCodec(), this.voiceChat.isExternalOpusService(), status -> {
            if (status == EnumOpusError.OK) {
               this.voiceChat.setLoginCompleted(true);
               this.voiceChat.connectToSelectedMicrophone();
               this.voiceChat.setLastConnectionStatus("VoiceChat login completed");
               this.voiceChat.log("Voice chat login completed");
            } else {
               this.voiceChat.log("Could not initialize opus codec manager: " + status.name());
               this.voiceChat.setLastConnectionStatus(status.getMessage());
               this.voiceChat.setKicked(true);
               this.voiceChat.disconnect(false);
            }
         });
      } else {
         this.voiceChat.log("Invalid opus codec format received: " + json);
         this.voiceChat.setLastConnectionStatus("VoiceChat has invalid opus codec format");
         this.voiceChat.setKicked(true);
         this.voiceChat.disconnect(false);
      }
   }

   @Override
   public void onMute(UUID player, String reason, long secondsLeft, String mutedBy) {
      long muteUntil = System.currentTimeMillis() + secondsLeft * 1000L;
      this.globalMuted.put(player, new Mute(reason, muteUntil, mutedBy));
   }

   @Override
   public void onUnmute(UUID player) {
      this.globalMuted.remove(player);
   }

   @Override
   public void onUpdateReport(UUID player, String reason, int count) {
      this.reports.put(player, new Report(count, reason));
   }

   @Override
   public void onPlayerDiscovered(List<UUID> players) {
      for (UUID uuid : players) {
         this.voiceChat.getSurroundManager().userEnteredView(uuid);
      }
   }

   @Override
   public void onPlayerDisappeared(List<UUID> players) {
      for (UUID uuid : players) {
         this.voiceChat.getSurroundManager().userLeftView(uuid);
      }
   }

   @Override
   public void onAudioReceived(UUID uuid, byte[] data) {
      if (this.voiceChat.isEnabled() && LabyMod.getInstance().isInGame()) {
         if (this.voiceChat.getVolume(uuid) != 0) {
            if (this.voiceChat.getFocusedUser() == null || this.voiceChat.getFocusedUser().equals(uuid)) {
               if (!this.voiceChat.getServerMutes().contains(uuid)) {
                  EntityPlayer entityPlayer = LabyModCore.getMinecraft().getWorld().b(uuid);
                  if (entityPlayer != null) {
                     this.voiceChat.getSurroundManager().userEnteredView(uuid);

                     try {
                        ByteBuffer buffer = ByteBuffer.wrap(data);
                        long time = buffer.getLong();
                        byte[] encodedData = new byte[buffer.remaining()];
                        buffer.get(encodedData);
                        this.decode(entityPlayer, encodedData, time);
                     } catch (Throwable var8) {
                        Debug.log(EnumDebugMode.ADDON, "VoiceChat decode exception: " + var8.getMessage());
                     }
                  }
               }
            }
         }
      }
   }

   private void decode(EntityPlayer entityPlayer, byte[] encodedChunk, long time) throws Exception {
      this.voiceChat.getOpusCodecManager().convert(entityPlayer.getUniqueID(), EnumOpusCodecDirection.DECODE, encodedChunk, decodedChunk -> {
         UserStream userStream = this.voiceChat.getSurroundManager().getUserStream(entityPlayer.getUniqueID());
         if (!userStream.isInitialized()) {
            userStream.init(this);
         }

         if (decodedChunk.length != 0) {
            int compressorTarget = this.voiceChat.getCompressorTarget() * 100;
            int maxSwapRate = this.voiceChat.getMaxSwapRate();
            decodedChunk = AudioModifier.compress(decodedChunk, compressorTarget, maxSwapRate, userStream);
            int volume = this.voiceChat.getVolume(entityPlayer.getUniqueID());
            if (volume != 100) {
               decodedChunk = AudioModifier.adjustVolume(decodedChunk, (float)volume / 100.0F);
            }

            if (this.isProtected(entityPlayer.getUniqueID()) && this.voiceChat.isScreamerProtection()) {
               decodedChunk = AudioModifier.adjustVolume(decodedChunk, 0.1F);
            }

            this.voiceChat.getSurroundManager().feedVoiceData(entityPlayer, userStream, decodedChunk, time);
         }
      });
   }

   public boolean isProtected(UUID uuid) {
      if (!this.voiceChat.isScreamerProtection()) {
         return false;
      } else {
         Long screamProtection = this.highSwapMutedUsers.get(uuid);
         return screamProtection == null ? false : screamProtection + this.audioConfig.getScreamProtectionDuration() > System.currentTimeMillis();
      }
   }

   public AudioConfig getAudioConfig() {
      return this.audioConfig;
   }

   public boolean isAdmin() {
      return this.isAdmin;
   }

   public ExecutorService getExecutorService() {
      return this.executorService;
   }

   public VoiceChat getVoiceChat() {
      return this.voiceChat;
   }

   public Map<UUID, Long> getHighSwapMutedUsers() {
      return this.highSwapMutedUsers;
   }

   public Map<UUID, Mute> getGlobalMuted() {
      return this.globalMuted;
   }

   public Map<UUID, Report> getReports() {
      return this.reports;
   }
}
