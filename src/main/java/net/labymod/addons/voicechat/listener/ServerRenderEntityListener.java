package net.labymod.addons.voicechat.listener;

import net.labymod.addons.voicechat.VoiceChat;
import net.labymod.api.events.RenderEntityEvent;
import net.minecraft.entity.Entity;

/**
 * Speaker-icon overlay above player nameplates.
 *
 * The original is heavily coupled to LabyMod's User/UserManager system, the
 * tag manager, and obfuscated render-manager fields. Voice chat works without
 * the speaker icons -- it's a purely cosmetic indicator. Stubbed as no-op.
 *
 * TODO: reimplement as a Forge RenderLivingEvent.Pre subscriber that draws the
 * speaker icon using vanilla Gui.drawTexturedModalRect after a billboard transform.
 * Reference: decompiled-reference/.../ServerRenderEntityListener.java
 */
public class ServerRenderEntityListener implements RenderEntityEvent {
   @SuppressWarnings("unused")
   private final VoiceChat voiceChat;

   public ServerRenderEntityListener(VoiceChat voiceChat) {
      this.voiceChat = voiceChat;
   }

   @Override
   public void onRender(Entity entity, double x, double y, double z, float partialTicks) {
      // no-op
   }
}
