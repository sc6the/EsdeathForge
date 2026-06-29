package net.labymod.addons.voicechat.listener;

import net.labymod.addons.voicechat.VoiceChat;
import net.labymod.api.events.MessageSendEvent;
import net.labymod.main.LabyMod;
import net.labymod.utils.ModColor;

/**
 * Intercepts /voicemute chat command. The original opens GuiAdminMutePlayer /
 * GuiReportPlayer; we currently print a message saying the mute UI is not
 * yet ported to vanilla GUI -- TODO when GuiScreen-based UI lands.
 */
public class MessageSendListener implements MessageSendEvent {
   private final VoiceChat voiceChat;

   public MessageSendListener(VoiceChat voiceChat) {
      this.voiceChat = voiceChat;
   }

   @Override
   public boolean onSend(String message) {
      if (this.voiceChat.getVoiceClient() == null) return false;
      if (!message.toLowerCase().startsWith("/voicemute")) return false;
      if (!message.contains(" ")) {
         LabyMod.getInstance().displayMessageInChat(ModColor.cl('c') + "/voicemute <name>");
      } else {
         LabyMod.getInstance().displayMessageInChat(
            ModColor.cl('c') + "Voice mute UI not yet ported. Use the in-game volume slider to set a player's volume to 0.");
      }
      return true;
   }
}
