package net.labymod.addons.voicechat.audio.surround;

import net.labymod.addons.voicechat.VoiceChat;
import net.labymod.addons.voicechat.audio.AudioModifier;
import net.labymod.main.LabyMod;
import net.labymod.main.Source;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import paulscode.sound.SoundSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SurroundManager {
   private final OpenALPlayer openALPlayer;
   private final SoundHandler soundHandler;
   private SoundManager soundManager;
   public SoundSystem sndSystem;
   private VoiceChat voiceChat;
   private long lastSoundSystemLoadedChecked = -1L;
   private Map<UUID, UserStream> userStreams = new ConcurrentHashMap<>();
   private List<UUID> detectedUsers = new ArrayList<>();
   private long lastSelftalking = -1L;

   public SurroundManager(VoiceChat voiceChat, SoundHandler soundHandler) {
      this.voiceChat = voiceChat;
      this.soundHandler = soundHandler;
      try {
         this.soundManager = (SoundManager) ReflectionHelper.getPrivateValue(SoundHandler.class, soundHandler, 5);
         if (this.soundManager != null) {
            this.sndSystem = (SoundSystem) ReflectionHelper.getPrivateValue(
                SoundManager.class, this.soundManager,
                Source.ABOUT_MC_VERSION.startsWith("1.8") ? 4 : 5);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      MinecraftForge.EVENT_BUS.register(this);
      this.openALPlayer = new OpenALPlayer(this);
   }

   @SubscribeEvent
   public void onTick(ClientTickEvent event) {
      if (event.phase == Phase.END) {
         try {
            Collection<UserStream> collection = this.userStreams.values();
            for (int i = 0; i < collection.size(); i++) {
               UserStream userStream = (UserStream) collection.toArray()[i];
               if (userStream.isTimedOut()) this.endUserStream(userStream.getUuid());
            }
         } catch (Throwable t) {
            if (this.voiceChat.crashFix) t.printStackTrace();
         }
      }
   }

   public void feedVoiceData(EntityPlayer entityPlayer, UserStream userStream, byte[] data, long time) {
      if (this.voiceChat.isEnabled() && this.sndSystem != null && this.voiceChat.isAllowed() && LabyMod.getInstance().isInGame()) {
         try {
            userStream.writeVisualBuffer(data);
            float volume = 0.1F * (float) this.voiceChat.getSurroundVolume();
            data = AudioModifier.adjustVolume(data, volume);
            userStream.getQueue().add(data);
            userStream.keepAlive();
         } catch (Throwable t) {
            t.printStackTrace();
         }
      }
   }

   public UserStream getUserStream(UUID uuid) {
      UserStream userStream = this.userStreams.get(uuid);
      if (userStream == null) {
         this.userStreams.put(uuid, userStream = new UserStream(uuid));
      }
      return userStream;
   }

   private void endUserStream(UUID uuid) {
      try {
         UserStream userStream = this.userStreams.remove(uuid);
         userStream.cleanup();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void clear() {
      for (UserStream userStream : this.userStreams.values()) this.endUserStream(userStream.getUuid());
      this.detectedUsers.clear();
      this.lastSelftalking = -1L;
   }

   public void userEnteredView(UUID uuid) {
      if (!this.detectedUsers.contains(uuid)) this.detectedUsers.add(uuid);
   }

   public void userLeftView(UUID uuid) { this.detectedUsers.remove(uuid); }

   public void updateSelfTalking() {
      if (this.lastSelftalking == -1L) this.userEnteredView(LabyMod.getInstance().getPlayerUUID());
      this.lastSelftalking = System.currentTimeMillis();
   }

   public boolean isTalking(UUID uuid) {
      return LabyMod.getInstance().getPlayerUUID().equals(uuid)
         ? this.lastSelftalking + 500L > System.currentTimeMillis()
         : this.userStreams.containsKey(uuid);
   }

   public boolean isListening(UUID uuid) {
      return this.detectedUsers.contains(uuid) || LabyMod.getInstance().getPlayerUUID().equals(uuid);
   }

   public OpenALPlayer getOpenALPlayer() { return openALPlayer; }
   public SoundHandler getSoundHandler() { return soundHandler; }
   public SoundManager getSoundManager() { return soundManager; }
   public SoundSystem getSndSystem() { return sndSystem; }
   public VoiceChat getVoiceChat() { return voiceChat; }
   public long getLastSoundSystemLoadedChecked() { return lastSoundSystemLoadedChecked; }
   public Map<UUID, UserStream> getUserStreams() { return userStreams; }
   public List<UUID> getDetectedUsers() { return detectedUsers; }
   public long getLastSelftalking() { return lastSelftalking; }
}
