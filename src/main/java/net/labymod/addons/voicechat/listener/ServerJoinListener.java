package net.labymod.addons.voicechat.listener;

import net.labymod.addons.voicechat.VoiceChat;
import net.labymod.utils.Consumer;
import net.labymod.utils.ServerData;

public class ServerJoinListener implements Consumer<ServerData> {
   private final VoiceChat voiceChat;

   public ServerJoinListener(VoiceChat voiceChat) {
      this.voiceChat = voiceChat;
   }

   @Override
   public void accept(ServerData accepted) {
      this.voiceChat.reset(false);
      // Initiate the voice connection on server join. Without this the client only ever ran the
      // onTick *reconnect* path (gated on a reconnect status), so it never made the first connection
      // on its own — it appeared to only work once a separate LabyMod instance had connected.
      this.voiceChat.connect();
      this.voiceChat.connectToSelectedMicrophone();
      // Original called voiceChat.displayRules() after a 1s delay; our shell sets
      // rulesAccepted=true unconditionally, so the rules screen prompt is skipped.
   }
}
