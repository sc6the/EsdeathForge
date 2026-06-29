package net.labymod.voice.client;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.labymod.voice.client.auth.AuthenticationResponse;
import net.labymod.voice.client.auth.Authenticator;
import net.labymod.voice.client.call.CallListener;
import net.labymod.voice.client.call.PrivateCall;
import net.labymod.voice.client.config.VoiceClientConfiguration;
import net.labymod.voice.client.listener.VoiceClientListener;
import net.labymod.voice.client.listener.VoiceClientListenerAdapter;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ServerVoicePacketHandler;
import net.labymod.voice.protocol.packet.CallFeatureAvailablePacket;
import net.labymod.voice.protocol.packet.KeepAlivePacket;
import net.labymod.voice.protocol.packet.client.ClientAudioPacket;
import net.labymod.voice.protocol.packet.client.HandshakePacket;
import net.labymod.voice.protocol.packet.client.MutePlayerPacket;
import net.labymod.voice.protocol.packet.client.ReportPlayerPacket;
import net.labymod.voice.protocol.packet.client.SwitchServerPacket;
import net.labymod.voice.protocol.packet.client.UpdateVisiblePlayersPacket;
import net.labymod.voice.protocol.packet.client.call.ClientCallRequestResponsePacket;
import net.labymod.voice.protocol.packet.server.HandshakeResponsePacket;
import net.labymod.voice.protocol.packet.server.InvalidKeyPacket;
import net.labymod.voice.protocol.packet.server.KickPacket;
import net.labymod.voice.protocol.packet.server.MutedPacket;
import net.labymod.voice.protocol.packet.server.PlayerAlivePacket;
import net.labymod.voice.protocol.packet.server.PlayerDeadPacket;
import net.labymod.voice.protocol.packet.server.ReportPlayerCountPacket;
import net.labymod.voice.protocol.packet.server.ServerAudioPacket;
import net.labymod.voice.protocol.packet.server.call.ServerCallEndedPacket;
import net.labymod.voice.protocol.packet.server.call.ServerCallUserAddedPacket;
import net.labymod.voice.protocol.packet.server.call.ServerCallUserRemovedPacket;
import net.labymod.voice.protocol.packet.server.call.ServerRequestDirectCallPacket;
import net.labymod.voice.protocol.type.AuthenticationMethod;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.DisconnectType;
import net.labymod.voice.protocol.type.HandshakeResponse;
import net.labymod.voice.protocol.util.HttpRequest;

public class VoiceClient implements ServerVoicePacketHandler {
   private static final String URL_PUBLIC_KEY = "https://dl.labymod.net/labyconnect/voice/key.pub";
   private static final String URL_AUDIO = "https://dl.labymod.net/labyconnect/voice/audio.json";
   private static final String BRAND_NAME = "laby";
   private final AtomicBoolean isConnectingState = new AtomicBoolean(false);
   private final Map<AuthenticationMethod, Authenticator> authenticators = new HashMap<>();
   private UdpClient udpClient;
   private VoiceClientConfiguration configuration;
   private VoiceClientListener listener = new VoiceClientListenerAdapter();
   private CallListener callListener;
   private PrivateCall call;
   private final Collection<UUID> requestingUsersForCalls = ConcurrentHashMap.newKeySet();
   private final Collection<UUID> callFeatureAvailable = new HashSet<>();
   private boolean isAdmin;
   private ScheduledFuture<?> handshakeTask;
   private int handshakesSent = 0;
   private int nextVisiblePlayersRequestId = 0;

   public void connect(InetSocketAddress serverAddress, AuthenticationMethod method) throws Exception {
      if (!this.isConnectingState.getAndSet(true)) {
         try {
            if (this.udpClient != null && this.udpClient.isRunning()) {
               this.udpClient.stop(DisconnectType.DISCONNECT);
            }

            if (this.handshakeTask != null && !this.handshakeTask.isCancelled()) {
               this.handshakeTask.cancel(true);
            }

            String pubKey = HttpRequest.getRequest("https://dl.labymod.net/labyconnect/voice/key.pub");
            String config = HttpRequest.getRequest("https://dl.labymod.net/labyconnect/voice/audio.json");
            if (pubKey == null) {
               return;
            }

            if (config != null) {
               this.configuration = new VoiceClientConfiguration(config, pubKey);
               this.udpClient = new UdpClient(this, serverAddress, this.configuration.getPublicKey());
               byte[] sharedKey = this.udpClient.getSymEncryption().getShareKey().getEncoded();
               byte[] publicKey = this.udpClient.getAsymEncryption().getPubKey().getEncoded();
               MessageDigest sha = MessageDigest.getInstance("SHA-1");
               sha.update("laby".getBytes(StandardCharsets.ISO_8859_1));
               sha.update(sharedKey);
               sha.update(publicKey);
               String hash = URLEncoder.encode(new BigInteger(sha.digest()).toString(16), "UTF-8");
               Authenticator authenticator = this.authenticators.get(method);
               if (authenticator == null) {
                  throw new RuntimeException("No authenticator for " + method + " defined!");
               }

               Executors.newSingleThreadExecutor().execute(() -> {
                  try {
                     AuthenticationResponse response = authenticator.request(hash);
                     if (response == null) {
                        this.udpClient.stop(DisconnectType.AUTHENTICATION_FAILED);
                        return;
                     }

                     HandshakePacket handshake = new HandshakePacket();
                     handshake.setMethod(method);
                     handshake.setSymKey(this.udpClient.getSymEncryption().getShareKey().getEncoded());
                     handshake.setPlayer(response.getPlayer());
                     handshake.setString(response.getString());
                     this.handshakesSent = 0;
                     this.handshakeTask = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                        if (this.handshakesSent < 5 && this.udpClient.getState() == ConnectionState.HANDSHAKE) {
                           try {
                              this.udpClient.sendPacket(handshake);
                              this.handshakesSent++;
                           } catch (Exception var3x) {
                              var3x.printStackTrace();
                           }
                        } else {
                           this.handshakeTask.cancel(true);
                        }
                     }, 0L, 1L, TimeUnit.SECONDS);
                  } catch (Exception var6x) {
                     var6x.printStackTrace();
                     this.udpClient.stop(DisconnectType.AUTHENTICATION_FAILED);
                  }
               });
               this.udpClient.setDisconnectListener(reason -> this.listener.onDisconnected(reason, "Disconnected"));
               return;
            }
         } finally {
            this.isConnectingState.set(false);
         }
      }
   }

   @Override
   public void handleHandshakeResponse(HandshakeResponsePacket packet) {
      this.udpClient.setRemoteProtocolVersion(packet.getProtocolVersion());
      if (packet.getResponse() == HandshakeResponse.SUCCESS) {
         if (this.callListener != null) {
            this.requestCallFeatureAvailable(Collections.emptyList());
         }

         this.udpClient.setState(ConnectionState.CONNECTED);
         this.listener.onAuthenticated(this.configuration, this.isAdmin = packet.isAdmin());
      } else {
         this.udpClient.stop(DisconnectType.of(packet.getResponse()));
      }
   }

   @Override
   public void handleKick(KickPacket packet) {
      this.udpClient.stop(DisconnectType.KICK);
      this.listener.onDisconnected(DisconnectType.KICK, packet.getReason());
   }

   @Override
   public void handleMuted(MutedPacket packet) {
      if (packet.isMuted()) {
         this.listener.onMute(packet.getPlayer(), packet.getReason(), packet.getSecondsLeft(), packet.getMutedBy());
      } else {
         this.listener.onUnmute(packet.getPlayer());
      }
   }

   @Override
   public void handlePlayerAlive(PlayerAlivePacket packet) {
      this.listener.onPlayerDiscovered(packet.getPlayers());
   }

   @Override
   public void handlePlayerDead(PlayerDeadPacket packet) {
      this.listener.onPlayerDisappeared(packet.getPlayers());
   }

   @Override
   public void handleReportPlayerCount(ReportPlayerCountPacket packet) {
      this.listener.onUpdateReport(packet.getPlayer(), packet.getReason(), packet.getCount());
   }

   @Override
   public void handleServerAudio(ServerAudioPacket packet) {
      this.listener.onAudioReceived(packet.getPlayer(), packet.getData());
   }

   @Override
   public void handleCallEnded(ServerCallEndedPacket packet) {
      if (this.isPrivateCallingSupported()) {
         PrivateCall call = this.call;
         if (call != null) {
            Set<UUID> users = new HashSet<>(call.getUsers());
            call.onCallEnded();
            this.call = null;
            this.callListener.onCallEnd(users);
         }
      }
   }

   @Override
   public void handleCallUserAdded(ServerCallUserAddedPacket packet) {
      if (this.isPrivateCallingSupported()) {
         PrivateCall call = this.call;
         if (call != null) {
            call.onCallJoined(packet.getAddedUserId());
            this.callListener.onCallJoin(packet.getAddedUserId());
         }
      }
   }

   @Override
   public void handleCallUserRemoved(ServerCallUserRemovedPacket packet) {
      if (this.isPrivateCallingSupported()) {
         if (this.requestingUsersForCalls.remove(packet.getRemovedUserId())) {
            this.callListener.onCallRequestRetracted(packet.getRemovedUserId());
         }

         PrivateCall call = this.call;
         if (call != null && call.onCallLeft(packet.getRemovedUserId())) {
            this.callListener.onCallLeave(packet.getRemovedUserId());
         }
      }
   }

   @Override
   public void handleDirectCallRequest(ServerRequestDirectCallPacket packet) {
      UUID callerId = packet.getCallerId();
      if (!this.isPrivateCallingSupported()) {
         this.sendCallRequestResponse(callerId, ClientCallRequestResponsePacket.ResponseType.REJECTED);
      } else {
         this.requestingUsersForCalls.add(callerId);
         this.callListener.onCallRequest(packet.getCallerId(), accepted -> {
            if (accepted) {
               this.requestingUsersForCalls.clear();
               this.call = new PrivateCall(this);
               this.sendCallRequestResponse(callerId, ClientCallRequestResponsePacket.ResponseType.ACCEPTED);
            } else {
               this.requestingUsersForCalls.remove(callerId);
               this.sendCallRequestResponse(callerId, ClientCallRequestResponsePacket.ResponseType.REJECTED);
            }
         });
      }
   }

   @Override
   public void handleInvalidKey(InvalidKeyPacket packet) {
      this.udpClient.stop(DisconnectType.AUTHENTICATION_FAILED);
      this.listener.onDisconnected(DisconnectType.AUTHENTICATION_FAILED, "Invalid encryption");
   }

   @Override
   public void handleKeepAlive(KeepAlivePacket packet) {
   }

   @Override
   public void handleCallFeatureAvailable(CallFeatureAvailablePacket packet) {
      if (this.isPrivateCallingSupported()) {
         for (UUID uniqueId : packet.getUniqueIds()) {
            if (this.callFeatureAvailable.add(uniqueId)) {
               this.callListener.onCallFeatureAvailable(uniqueId);
            }
         }
      }
   }

   public void sendVisiblePlayers(UUID... visible) {
      UpdateVisiblePlayersPacket packet = new UpdateVisiblePlayersPacket();
      packet.setRequestId(this.nextVisiblePlayersRequestId);

      for (UUID uuid : visible) {
         packet.getPlayers().add(uuid);
         if (packet.getPlayers().size() >= 50) {
            this.udpClient.sendPacket(packet);
            packet = new UpdateVisiblePlayersPacket();
            packet.setRequestId(this.nextVisiblePlayersRequestId);
         }
      }

      if (packet.getPlayers().size() > 0) {
         this.udpClient.sendPacket(packet);
      }

      this.nextVisiblePlayersRequestId = (this.nextVisiblePlayersRequestId + 1) % 127;
   }

   public void sendUnmutePlayer(UUID player) {
      MutePlayerPacket mutePlayer = new MutePlayerPacket();
      mutePlayer.setPlayer(player);
      mutePlayer.setMute(false);
      this.udpClient.sendPacket(mutePlayer);
   }

   public void sendMutePlayer(UUID player, String reason, int hours) {
      MutePlayerPacket mutePlayer = new MutePlayerPacket();
      mutePlayer.setPlayer(player);
      mutePlayer.setMute(true);
      mutePlayer.setReason(reason);
      mutePlayer.setHours(hours);
      this.udpClient.sendPacket(mutePlayer);
   }

   public void sendReportPlayer(UUID player, String reason) {
      ReportPlayerPacket reportPlayer = new ReportPlayerPacket();
      reportPlayer.setPlayer(player);
      reportPlayer.setReason(reason);
      this.udpClient.sendPacket(reportPlayer);
   }

   public void sendSwitchServer(String server, int port) {
      SwitchServerPacket switchServer = new SwitchServerPacket();
      switchServer.setServer(server);
      switchServer.setPort(port);
      this.udpClient.sendPacket(switchServer);
   }

   public void sendAudioChunk(byte[] data) {
      ClientAudioPacket voiceContainer = new ClientAudioPacket();
      voiceContainer.setData(data);
      this.udpClient.sendPacket(voiceContainer);
   }

   public CompletableFuture<Boolean> sendCallRequest(UUID targetId) {
      if (!this.isPrivateCallingSupported()) {
         return CompletableFuture.completedFuture(false);
      } else {
         PrivateCall runningCall = this.call;
         if (runningCall != null && runningCall.wasRequested(targetId)) {
            CompletableFuture<Boolean> future = runningCall.getRequestFuture(targetId);
            return future != null ? future : CompletableFuture.completedFuture(false);
         } else {
            if (runningCall != null) {
               Set<UUID> users = new HashSet<>(runningCall.getUsers());
               this.call = null;
               this.callListener.onCallEnd(users);
            }

            runningCall = new PrivateCall(this);
            this.call = runningCall;
            return runningCall.requestUser(targetId);
         }
      }
   }

   public void endCall() {
      if (this.isPrivateCallingSupported()) {
         PrivateCall runningCall = this.call;
         if (runningCall != null) {
            Set<UUID> users = new HashSet<>(runningCall.getUsers());
            runningCall.endCall();
            this.call = null;
            this.callListener.onCallEnd(users);
         }
      }
   }

   public void requestCallFeatureAvailable(Collection<UUID> uniqueIds) {
      CallFeatureAvailablePacket packet = new CallFeatureAvailablePacket();
      packet.setUniqueIds(uniqueIds);
      this.sendPacket(packet);
   }

   private void sendCallRequestResponse(UUID callerId, ClientCallRequestResponsePacket.ResponseType type) {
      ClientCallRequestResponsePacket packet = new ClientCallRequestResponsePacket();
      packet.setCallerId(callerId);
      packet.setType(type);
      this.udpClient.sendPacket(packet);
   }

   public void sendPacket(VoicePacket<?> packet) {
      this.udpClient.sendPacket(packet);
   }

   public void setAuthenticator(AuthenticationMethod method, Authenticator authenticator) {
      this.authenticators.put(method, authenticator);
   }

   public boolean isConnected() {
      return this.udpClient != null && this.udpClient.isRunning();
   }

   public void stop() {
      this.udpClient.stop(DisconnectType.DISCONNECT);
   }

   public boolean isInCall() {
      return this.call != null;
   }

   public boolean isCallFeatureAvailable(UUID uniqueId) {
      return this.callFeatureAvailable.contains(uniqueId);
   }

   public boolean isPrivateCallingSupported() {
      return this.callListener != null;
   }

   public VoiceClientConfiguration getConfiguration() {
      return this.configuration;
   }

   public void setListener(VoiceClientListener listener) {
      this.listener = listener;
   }

   public void setCallListener(CallListener callListener) {
      this.callListener = callListener;
   }

   public PrivateCall getCall() {
      return this.call;
   }

   public boolean isAdmin() {
      return this.isAdmin;
   }
}
