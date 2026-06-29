package net.labymod.addons.voicechat.listener;

import com.google.common.base.Objects;
import java.util.List;
import java.util.UUID;
import net.labymod.addons.voicechat.VoiceChat;
import net.labymod.core.LabyModCore;
import net.labymod.main.LabyMod;
import net.labymod.utils.Consumer;
import net.minecraft.entity.player.EntityPlayer;

public class ServerIncomingPacketListener implements Consumer<Object> {
   private VoiceChat voiceChat;
   private int lastPlayersCount = 0;
   private int prevUUIDArrayHash = 0;
   private int prevPlayersArrayHash = 0;

   public ServerIncomingPacketListener(VoiceChat voiceChat) {
      this.voiceChat = voiceChat;
   }

   public void accept(Object object) {
      try {
         if (!this.voiceChat.isEnabled() || !LabyMod.getInstance().isInGame() || !this.voiceChat.isConnected() || !this.voiceChat.isLoginCompleted()) {
            this.lastPlayersCount = 0;
            this.prevPlayersArrayHash = 0;
            this.prevUUIDArrayHash = 0;
            return;
         }

         List<EntityPlayer> players = LabyModCore.getMinecraft().getWorld().j;
         if (players == null) {
            return;
         }

         int playersHash = Objects.hashCode(new Object[]{players});
         if (playersHash == this.prevPlayersArrayHash) {
            return;
         }

         this.prevPlayersArrayHash = playersHash;
         this.lastPlayersCount = players.size();
         UUID[] uuids = new UUID[players.size()];

         for (int i = 0; i < uuids.length; i++) {
            EntityPlayer player = players.get(i);
            if (player == null) {
               return;
            }

            uuids[i] = player.getUniqueID();
         }

         int uuidHash = Objects.hashCode(uuids);
         if (uuidHash == this.prevUUIDArrayHash) {
            return;
         }

         this.prevUUIDArrayHash = uuidHash;
         this.voiceChat.getVoiceClient().sendVisiblePlayers(uuids);
      } catch (Exception var7) {
         var7.printStackTrace();
      }
   }

   public VoiceChat getVoiceChat() {
      return this.voiceChat;
   }

   public int getLastPlayersCount() {
      return this.lastPlayersCount;
   }

   public int getPrevUUIDArrayHash() {
      return this.prevUUIDArrayHash;
   }

   public int getPrevPlayersArrayHash() {
      return this.prevPlayersArrayHash;
   }

   public void setVoiceChat(VoiceChat voiceChat) {
      this.voiceChat = voiceChat;
   }

   public void setLastPlayersCount(int lastPlayersCount) {
      this.lastPlayersCount = lastPlayersCount;
   }

   public void setPrevUUIDArrayHash(int prevUUIDArrayHash) {
      this.prevUUIDArrayHash = prevUUIDArrayHash;
   }

   public void setPrevPlayersArrayHash(int prevPlayersArrayHash) {
      this.prevPlayersArrayHash = prevPlayersArrayHash;
   }
}
