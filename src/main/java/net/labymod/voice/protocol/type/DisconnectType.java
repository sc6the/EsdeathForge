package net.labymod.voice.protocol.type;

public enum DisconnectType {
   TIMEOUT,
   KICK,
   DISCONNECT,
   AUTHENTICATION_FAILED,
   ALREADY_CONNECTED;

   public static DisconnectType of(HandshakeResponse response) {
      switch (response) {
         case AUTH_FAIL:
            return AUTHENTICATION_FAILED;
         case ALREADY_CONNECTED:
            return ALREADY_CONNECTED;
         default:
            return DISCONNECT;
      }
   }
}
