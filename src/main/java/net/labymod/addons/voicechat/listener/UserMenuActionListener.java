package net.labymod.addons.voicechat.listener;

import net.labymod.addons.voicechat.VoiceChat;
import net.labymod.api.events.UserMenuActionEvent;

/**
 * Original LabyMod right-click user-action menu listener -- adds Mute/Report
 * options for the targeted player. We don't ship LabyMod's user menu, so this
 * is a no-op placeholder. Replace with a vanilla keybind that opens the
 * volume/mute GUI for Minecraft.objectMouseOver -- see IMPLEMENTATION_PLAN.md.
 */
public class UserMenuActionListener implements UserMenuActionEvent {
   @SuppressWarnings("unused")
   private final VoiceChat voiceChat;

   public UserMenuActionListener(VoiceChat voiceChat) {
      this.voiceChat = voiceChat;
   }
}
