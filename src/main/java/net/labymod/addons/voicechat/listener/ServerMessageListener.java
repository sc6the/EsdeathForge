package net.labymod.addons.voicechat.listener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.labymod.addons.voicechat.VoiceChat;
import net.labymod.api.events.ServerMessageEvent;

import java.util.List;
import java.util.UUID;

/**
 * Handles server-pushed voicechat configuration messages on the LMC plugin channel.
 * Original behaviour preserved except the GuiApplyServerSettings prompt -- we
 * apply server-required settings immediately instead of asking the user.
 */
public class ServerMessageListener implements ServerMessageEvent {
   private final VoiceChat voiceChat;

   public ServerMessageListener(VoiceChat voiceChat) {
      this.voiceChat = voiceChat;
   }

   @Override
   public void onServerMessage(String messageKey, JsonElement serverMessage) {
      if (!"voicechat".equals(messageKey)) return;

      JsonObject obj = serverMessage.getAsJsonObject();
      if (obj.has("allowed")) this.voiceChat.setAllowed(obj.get("allowed").getAsBoolean());
      if (obj.has("keep_settings_on_server_switch")) {
         this.voiceChat.setResetOnServerSwitch(!obj.get("keep_settings_on_server_switch").getAsBoolean());
      }
      if (obj.has("request_settings")) {
         JsonObject requestSettings = (JsonObject) obj.get("request_settings");
         JsonObject settings = (JsonObject) requestSettings.get("settings");
         this.voiceChat.applySettings(settings);
      }
      if (obj.has("suppress_voice_activity")) {
         this.voiceChat.voiceActivitySuppressed = obj.get("suppress_voice_activity").getAsBoolean();
      }
      if (obj.has("mute_player")) {
         JsonObject mutePlayer = (JsonObject) obj.get("mute_player");
         boolean mute = mutePlayer.get("mute").getAsBoolean();
         UUID uuid = UUID.fromString(mutePlayer.get("target").getAsString());
         List<UUID> serverMutes = this.voiceChat.getServerMutes();
         if (mute) {
            if (!serverMutes.contains(uuid)) serverMutes.add(uuid);
         } else serverMutes.remove(uuid);
      }
   }

   public VoiceChat getVoiceChat() { return voiceChat; }
}
