package net.labymod.voice.client.config;

public class VoiceClientConfiguration {
   private String jsonConfig;
   private String publicKey;

   public VoiceClientConfiguration(String jsonConfig, String publicKey) {
      this.jsonConfig = jsonConfig;
      this.publicKey = publicKey;
   }

   public String getJsonConfig() {
      return this.jsonConfig;
   }

   public String getPublicKey() {
      return this.publicKey;
   }

   @Override
   public String toString() {
      return "VoiceClientConfiguration(jsonConfig=" + this.getJsonConfig() + ", publicKey=" + this.getPublicKey() + ")";
   }
}
