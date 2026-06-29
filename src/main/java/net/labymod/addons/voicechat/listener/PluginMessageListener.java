package net.labymod.addons.voicechat.listener;

import java.beans.ConstructorProperties;
import net.labymod.addons.voicechat.VoiceChat;
import net.labymod.api.events.PluginMessageEvent;
import net.minecraft.network.PacketBuffer;

public class PluginMessageListener implements PluginMessageEvent {
   private VoiceChat voiceChat;

   public void receiveMessage(String channelName, PacketBuffer packetBuffer) {
      if (channelName.equals("MC|Brand")) {
         if (this.voiceChat.isResetOnServerSwitch()) {
            this.voiceChat.reset(true);
         }

         this.voiceChat.changeServer();
      }
   }

   @ConstructorProperties({"voiceChat"})
   public PluginMessageListener(VoiceChat voiceChat) {
      this.voiceChat = voiceChat;
   }
}
