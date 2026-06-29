package net.labymod.addons.voicechat.audio.opus;

public enum EnumOpusError {
   NOT_INITIALIZED(true, "VoiceChat is not initialzed yet.", ""),
   OK(true, "Success", ""),
   NATIVE_CRASH(false, "VoiceChat is currently not supported by your computer hardware.", ""),
   UNKNOWN_EXCEPTION(false, "Unknown VoiceChat exception occured.", "Try to disable the external opus service in the settings!"),
   SERVICE_OFFLINE(false, "External opus service not available.", "Try to disable the external opus service in the settings!"),
   MAX_RETRY_REACHED(false, "External opus service crashed too many times.", "Try to disable the external opus service in the settings!"),
   SERVICE_RESOURCE_NOT_FOUND(false, "External service resource not found.", "Try to disable the external opus service in the settings!"),
   EXTRACT_SERVICE_FAILED(false, "Can't extract external service.", "Try to disable the external opus service in the settings!"),
   LOAD_NATIVES_IN_MC_FAILED(false, "The current java version is not supported by the VoiceChat.", "");

   private boolean reconnect;
   private String message;
   private String subMessage;

   public boolean isReconnect() {
      return this.reconnect;
   }

   public String getMessage() {
      return this.message;
   }

   public String getSubMessage() {
      return this.subMessage;
   }

   private EnumOpusError(boolean reconnect, String message, String subMessage) {
      this.reconnect = reconnect;
      this.message = message;
      this.subMessage = subMessage;
   }
}
