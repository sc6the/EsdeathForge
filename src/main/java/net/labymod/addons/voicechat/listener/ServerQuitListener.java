package net.labymod.addons.voicechat.listener;

import net.labymod.addons.voicechat.VoiceChat;
import net.labymod.utils.Consumer;
import net.labymod.utils.ServerData;

public class ServerQuitListener implements Consumer<ServerData> {
   private VoiceChat voiceChat;

   public ServerQuitListener(VoiceChat voiceChat) {
      this.voiceChat = voiceChat;
   }

   public void accept(ServerData accepted) {
      this.voiceChat.reset(false);
   }
}
