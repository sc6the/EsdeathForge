package net.labymod.addons.voicechat.gui.server;

public enum ServerCustomSetting {
   ENABLED("enabled", "Enabled", false),
   MICROPHONE_VOLUME("microphoneVolume", "Microphone volume", false),
   SURROUND_RANGE("surroundRange", "Surround range", false),
   SURROUND_VOLUME("surroundVolume", "Surround volume", false),
   VOICE_ACTIVITY("voiceActivity", "Voice Activity", true);

   private String id;
   private String displayName;
   private boolean booleanSpecialFeature;

   public String getId() {
      return this.id;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public boolean isBooleanSpecialFeature() {
      return this.booleanSpecialFeature;
   }

   private ServerCustomSetting(String id, String displayName, boolean booleanSpecialFeature) {
      this.id = id;
      this.displayName = displayName;
      this.booleanSpecialFeature = booleanSpecialFeature;
   }
}
