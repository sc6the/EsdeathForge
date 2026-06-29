package net.labymod.voice.client.auth;

import java.util.UUID;

public class AuthenticationResponse {
   private String string;
   private UUID player;

   private AuthenticationResponse() {
   }

   public static AuthenticationResponse createLabyConnect(String pin, UUID uuid) {
      return new AuthenticationResponse(pin, uuid);
   }

   public static AuthenticationResponse createMojang(String username) {
      return new AuthenticationResponse(username, null);
   }

   public AuthenticationResponse(String string, UUID player) {
      this.string = string;
      this.player = player;
   }

   public String getString() {
      return this.string;
   }

   public UUID getPlayer() {
      return this.player;
   }

   @Override
   public String toString() {
      return "AuthenticationResponse(string=" + this.getString() + ", player=" + this.getPlayer() + ")";
   }
}
