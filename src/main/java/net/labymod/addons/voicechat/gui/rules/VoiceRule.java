package net.labymod.addons.voicechat.gui.rules;

public class VoiceRule {
   private final String title;
   private final String text;

   public VoiceRule(String title, String text) {
      this.title = title;
      this.text = text;
   }

   public String getTitle() {
      return this.title;
   }

   public String getText() {
      return this.text;
   }
}
