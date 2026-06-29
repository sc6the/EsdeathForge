package net.labymod.voice.client.call;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.labymod.voice.client.VoiceClient;
import net.labymod.voice.protocol.packet.client.call.ClientEndCallPacket;
import net.labymod.voice.protocol.packet.client.call.ClientRequestDirectCallPacket;

public class PrivateCall {
   private final VoiceClient client;
   private final Set<UUID> users = new HashSet<>();
   private final Map<UUID, CompletableFuture<Boolean>> requestedUsers = new ConcurrentHashMap<>();

   public PrivateCall(VoiceClient client) {
      this.client = client;
   }

   public CompletableFuture<Boolean> requestUser(UUID user) {
      if (this.requestedUsers.containsKey(user)) {
         return this.requestedUsers.get(user);
      } else if (this.users.contains(user)) {
         return CompletableFuture.completedFuture(false);
      } else {
         ClientRequestDirectCallPacket packet = new ClientRequestDirectCallPacket();
         packet.setTargetId(user);
         this.client.sendPacket(packet);
         CompletableFuture<Boolean> future = new CompletableFuture<>();
         this.requestedUsers.put(user, future);
         return future;
      }
   }

   public void onCallJoined(UUID user) {
      CompletableFuture<Boolean> future = this.requestedUsers.remove(user);
      if (future != null) {
         future.complete(true);
      }

      this.users.add(user);
   }

   public boolean onCallLeft(UUID user) {
      CompletableFuture<Boolean> future = this.requestedUsers.remove(user);
      if (future != null) {
         future.complete(false);
         return false;
      } else {
         return this.users.remove(user);
      }
   }

   public void onCallEnded() {
      this.users.clear();
      this.requestedUsers.clear();
   }

   public void endCall() {
      this.client.sendPacket(new ClientEndCallPacket());
      this.users.clear();
   }

   public boolean wasRequested(UUID user) {
      return !this.users.isEmpty() && this.requestedUsers.containsKey(user);
   }

   public boolean isInCall(UUID user) {
      return this.users.contains(user);
   }

   public Set<UUID> getUsers() {
      return this.users;
   }

   public CompletableFuture<Boolean> getRequestFuture(UUID user) {
      return this.requestedUsers.get(user);
   }
}
