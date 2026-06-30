package net.labymod.labyconnect;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.sun.management.OperatingSystemMXBean;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ConnectTimeoutException;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.concurrent.GenericFutureListener;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.Proxy;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.UnresolvedAddressException;
import java.security.PublicKey;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import net.labymod.labyconnect.handling.PacketEncryptingDecoder;
import net.labymod.labyconnect.handling.PacketEncryptingEncoder;
import net.labymod.labyconnect.handling.PacketHandler;
import net.labymod.labyconnect.log.MessageChatComponent;
import net.labymod.labyconnect.log.SingleChat;
import net.labymod.labyconnect.packets.CryptManager;
import net.labymod.labyconnect.packets.EnumConnectionState;
import net.labymod.labyconnect.packets.Packet;
import net.labymod.labyconnect.packets.PacketActionBroadcast;
import net.labymod.labyconnect.packets.PacketAddonMessage;
import net.labymod.labyconnect.packets.PacketChatVisibilityChange;
import net.labymod.labyconnect.packets.PacketDisconnect;
import net.labymod.labyconnect.packets.PacketEncryptionRequest;
import net.labymod.labyconnect.packets.PacketEncryptionResponse;
import net.labymod.labyconnect.packets.PacketHelloPing;
import net.labymod.labyconnect.packets.PacketHelloPong;
import net.labymod.labyconnect.packets.PacketKick;
import net.labymod.labyconnect.packets.PacketLoginComplete;
import net.labymod.labyconnect.packets.PacketLoginData;
import net.labymod.labyconnect.packets.PacketLoginFriend;
import net.labymod.labyconnect.packets.PacketLoginOptions;
import net.labymod.labyconnect.packets.PacketLoginRequest;
import net.labymod.labyconnect.packets.PacketLoginTime;
import net.labymod.labyconnect.packets.PacketLoginVersion;
import net.labymod.labyconnect.packets.PacketMessage;
import net.labymod.labyconnect.packets.PacketMojangStatus;
import net.labymod.labyconnect.packets.PacketNotAllowed;
import net.labymod.labyconnect.packets.PacketPing;
import net.labymod.labyconnect.packets.PacketPlayChangeOptions;
import net.labymod.labyconnect.packets.PacketPlayDenyFriendRequest;
import net.labymod.labyconnect.packets.PacketPlayFriendPlayingOn;
import net.labymod.labyconnect.packets.PacketPlayFriendRemove;
import net.labymod.labyconnect.packets.PacketPlayFriendStatus;
import net.labymod.labyconnect.packets.PacketPlayPlayerOnline;
import net.labymod.labyconnect.packets.PacketPlayRequestAddFriend;
import net.labymod.labyconnect.packets.PacketPlayRequestAddFriendResponse;
import net.labymod.labyconnect.packets.PacketPlayRequestRemove;
import net.labymod.labyconnect.packets.PacketPlayServerStatus;
import net.labymod.labyconnect.packets.PacketPlayServerStatusUpdate;
import net.labymod.labyconnect.packets.PacketPlayTyping;
import net.labymod.labyconnect.packets.PacketPong;
import net.labymod.labyconnect.packets.PacketServerMessage;
import net.labymod.labyconnect.packets.PacketUpdateCosmetics;
import net.labymod.labyconnect.packets.PacketUserBadge;
import net.labymod.labyconnect.user.ChatRequest;
import net.labymod.labyconnect.user.ChatUser;
import net.labymod.main.Source;
import net.labymod.main.lang.LanguageManager;
import net.labymod.support.util.Debug;
import net.labymod.user.User;
import net.labymod.user.UserManager;
import net.labymod.user.group.LabyGroup;
import net.labymod.utils.Consumer;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

@Sharable
public class ClientConnection extends PacketHandler {
   private NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Chat#%d").build());
   private ExecutorService executorService = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("Helper#%d").build());
   private NioSocketChannel nioSocketChannel;
   private Bootstrap bootstrap;
   private EnumConnectionState currentConnectionState = EnumConnectionState.OFFLINE;
   private LabyConnect labyConnect;
   private String lastKickMessage = "Unknown";
   private String mojang;
   public String customIp = null;
   public int customPort = -1;
   private long lastPing = System.currentTimeMillis();
   private Consumer<String> dashboardPinConsumer;
   private boolean premium = false;
   private boolean connectionEstablished = false;
   /** Phase 2: last add-friend response ("true" or a reason), shown by the Esdeath friends screen. */
   public String lastAddFriendResponse = null;

   public ClientConnection(LabyConnect labyConnect) {
      this.labyConnect = labyConnect;
      this.premium = net.labymod.main.LabyMod.getInstance().isPremium();

      Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
         long duration = System.currentTimeMillis() - this.lastPing;
         if (this.currentConnectionState != EnumConnectionState.OFFLINE && duration > 60000L) {
            this.disconnect(false);
         }
      }, 0L, 60L, TimeUnit.SECONDS);
   }

   public void connect() {
      String defaultIp = "chat.labymod.net";
      int defaultPort = 30336;
      String customPort = System.getProperty("customChatPort");
      if (customPort != null) {
         defaultPort = Integer.parseInt(customPort);
      }

      if (this.customIp != null) {
         defaultIp = this.customIp;
      }

      if (this.customPort != -1) {
         defaultPort = this.customPort;
      }

      this.connect(defaultIp, defaultPort);
   }

   public void connect(final String ip, final int port) {
      this.connectionEstablished = false;
      if (this.nioSocketChannel != null && this.nioSocketChannel.isOpen()) {
         this.nioSocketChannel.close();
         this.nioSocketChannel = null;
      }

      this.labyConnect.setForcedLogout(false);
      this.labyConnect.getChatlogManager().loadChatlogs(net.labymod.main.LabyMod.getInstance().getPlayerUUID());
      this.lastPing = System.currentTimeMillis();
      this.premium = net.labymod.main.LabyMod.getInstance().isPremium();
      this.updateConnectionState(EnumConnectionState.HELLO);
      this.mojang = Minecraft.getMinecraft().getSession().getToken();
      this.bootstrap = new Bootstrap();
      this.bootstrap.group(this.nioEventLoopGroup);
      this.bootstrap.option(ChannelOption.TCP_NODELAY, true);
      this.bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
      this.bootstrap.channel(NioSocketChannel.class);
      this.bootstrap.handler(new ClientChannelInitializer(this.labyConnect, this));
      this.executorService.execute(new Runnable() {
         @Override
         public void run() {
            try {
               Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "Connecting to " + ip + ":" + port);
               ClientConnection.this.bootstrap.connect(ip, port).syncUninterruptibly();
               ClientConnection.this.sendPacket(new PacketHelloPing(System.currentTimeMillis()));
            } catch (UnresolvedAddressException var2) {
               ClientConnection.this.updateConnectionState(EnumConnectionState.OFFLINE);
               ClientConnection.this.lastKickMessage = var2.getMessage() == null ? "Unknown error" : var2.getMessage();
               Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "UnresolvedAddressException: " + var2.getMessage());
               var2.printStackTrace();
            } catch (Throwable var3) {
               if (var3 instanceof ConnectTimeoutException || var3 instanceof TimeoutException) {
                  if (ip.equals("chat.labymod.net")) {
                     ClientConnection.this.connect("chat2.labymod.net", port);
                  } else {
                     ClientConnection.this.updateConnectionState(EnumConnectionState.OFFLINE);
                     ClientConnection.this.lastKickMessage = "Could not reach our servers.";
                     var3.printStackTrace();
                     IssueCollector.handle(ip, port);
                  }

                  return;
               }

               ClientConnection.this.updateConnectionState(EnumConnectionState.OFFLINE);
               ClientConnection.this.lastKickMessage = var3.getMessage() == null ? "Unknown error" : var3.getMessage();
               Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "Throwable: " + var3.getMessage());
               var3.printStackTrace();
               if (ClientConnection.this.lastKickMessage.contains("no further information") || var3.getMessage() == null) {
                  ClientConnection.this.lastKickMessage = LanguageManager.translate("chat_not_reachable");
               }
            }
         }
      });
   }

   public void disconnect(final boolean kicked) {
      if (this.currentConnectionState != EnumConnectionState.OFFLINE) {
         this.updateConnectionState(EnumConnectionState.OFFLINE);
         net.labymod.main.LabyMod.getInstance().getUserManager().resetFamiliars();
         this.executorService.execute(new Runnable() {
            @Override
            public void run() {
               ClientConnection.this.labyConnect.getChatlogManager().saveChatlogs(net.labymod.main.LabyMod.getInstance().getPlayerUUID());
               if (ClientConnection.this.nioSocketChannel != null && !kicked) {
                  ClientConnection.this.nioSocketChannel.writeAndFlush(new PacketDisconnect("Logout")).addListener(new ChannelFutureListener() {
                     public void operationComplete(ChannelFuture arg0) throws Exception {
                        if (ClientConnection.this.nioSocketChannel != null) {
                           ClientConnection.this.nioSocketChannel.close();
                        }
                     }
                  });
               }
            }
         });
      }
   }

   public void updateConnectionState(EnumConnectionState connectionState) {
      this.currentConnectionState = connectionState;
   }

   public void enableEncryption(SecretKey key) {
      this.nioSocketChannel.pipeline().addBefore("splitter", "decrypt", new PacketEncryptingDecoder(CryptManager.createNetCipherInstance(2, key)));
      this.nioSocketChannel.pipeline().addBefore("prepender", "encrypt", new PacketEncryptingEncoder(CryptManager.createNetCipherInstance(1, key)));
      Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "Enabled LabyConnect encryption!");
   }

   @Override
   public void handle(PacketLoginData packet) {
   }

   @Override
   public void handle(PacketHelloPing packet) {
   }

   @Override
   public void handle(PacketHelloPong packet) {
      this.connectionEstablished = true;
      this.sendPacket(new PacketLoginVersion(27, Source.ABOUT_MC_VERSION + "_" + "3.9.62"));
      if (net.labymod.main.LabyMod.getInstance().isPremium()) {
         this.updateConnectionState(EnumConnectionState.LOGIN);
         this.sendPacket(
            new PacketLoginData(
               net.labymod.main.LabyMod.getInstance().getPlayerUUID(),
               net.labymod.main.LabyMod.getInstance().getPlayerName(),
               net.labymod.main.LabyMod.getSettings().motd
            )
         );
         this.sendPacket(
            new PacketLoginOptions(
               net.labymod.main.LabyMod.getSettings().showConnectedIp,
               this.labyConnect.getClientProfile().getUserStatus(),
               this.labyConnect.getClientProfile().getTimeZone()
            )
         );
         this.labyConnect.getFriends().clear();
         this.labyConnect.getRequests().clear();
      }

      this.lastPing = System.currentTimeMillis();
   }

   @Override
   public void handle(PacketPlayPlayerOnline packet) {
      ChatUser chatUser = this.labyConnect.getChatUserByUUID(packet.getPlayer().getGameProfile().getId());
      chatUser.setStatus(packet.getPlayer().getStatus());
      chatUser.setStatusMessage(packet.getPlayer().getStatusMessage());
      if (net.labymod.main.LabyMod.getSettings().alertsOnlineStatus) {
         net.labymod.main.LabyMod.getInstance()
            .notifyFriendStatus(packet.getPlayer().getGameProfile(), packet.getPlayer().isOnline());
      }

      this.labyConnect.sortFriendList(net.labymod.main.LabyMod.getSettings().friendSortType);
   }

   @Override
   public void handle(PacketLoginComplete packet) {
      this.updateConnectionState(EnumConnectionState.PLAY);
      if (packet.getDashboardPin() != null && !packet.getDashboardPin().isEmpty()) {
         try {
            JsonObject jsonObject = (JsonObject)new JsonParser().parse(packet.getDashboardPin());
            if (jsonObject.has("pin")) {
               String pin = jsonObject.get("pin").getAsString();
               long expiresAt = jsonObject.get("expires_at").getAsLong();
               net.labymod.main.LabyMod.getInstance().getPinManager().update(net.labymod.main.LabyMod.getInstance().getPlayerUUID(), pin, expiresAt);
            }
         } catch (Exception var6) {
            var6.printStackTrace();
         }
      }

      net.labymod.main.LabyMod.getInstance().getUserManager().resetFamiliars();
      this.sendStatistics();
      this.labyConnect.getTracker().onLabyConnectConnect();
   }

   @Override
   public void handle(PacketChatVisibilityChange packet) {
   }

   @Override
   public void handle(PacketKick packet) {
      this.disconnect(true);
      this.lastKickMessage = packet.getReason() == null ? LanguageManager.translate("chat_unknown_kick_reason") : packet.getReason();
      net.labymod.main.LabyMod.getInstance().notifyMessageRaw(LanguageManager.translate("chat_disconnected_title"), this.lastKickMessage);
   }

   @Override
   public void handle(PacketDisconnect packet) {
      this.disconnect(true);
      this.lastKickMessage = packet.getReason() == null ? LanguageManager.translate("chat_unknown_disconnect_reason") : packet.getReason();
      net.labymod.main.LabyMod.getInstance().notifyMessageRaw(LanguageManager.translate("chat_disconnected_title"), this.lastKickMessage);
   }

   @Override
   public void handle(PacketPlayRequestAddFriend packet) {
   }

   @Override
   public void handle(PacketLoginFriend packet) {
      this.labyConnect.getFriends().addAll(packet.getFriends());
      this.labyConnect.sortFriendList(net.labymod.main.LabyMod.getSettings().friendSortType);
   }

   @Override
   public void handle(PacketLoginRequest packet) {
      if (net.labymod.main.LabyMod.getSettings().ignoreRequests) {
         for (ChatRequest chatRequest : packet.getRequests()) {
            this.sendPacket(new PacketPlayDenyFriendRequest(chatRequest));
         }
      } else {
         this.labyConnect.getRequests().addAll(packet.getRequests());

         for (ChatRequest chatRequest : this.labyConnect.getRequests()) {
            net.labymod.main.LabyMod.getInstance()
               .notifyMessageProfile(chatRequest.getGameProfile(), ModColor.cl("f") + LanguageManager.translate("chat_user_friend_request"));
         }
      }

      // Phase 2: refresh the Esdeath friends screen's request buttons if it's open.
   }

   @Override
   public void handle(PacketNotAllowed packet) {
   }

   @Override
   public void handle(PacketPing packet) {
      this.sendPacket(new PacketPong());
      this.lastPing = System.currentTimeMillis();
   }

   @Override
   public void handle(PacketPong packet) {
   }

   @Override
   public void handle(PacketServerMessage packet) {
      net.labymod.main.LabyMod.getInstance().notifyMessageRaw(LanguageManager.translate("chat_server_message_title"), packet.getMessage());
   }

   @Override
   public void handle(final PacketMessage packet) {
      ChatUser chatUser = this.labyConnect.getChatUserByUUID(packet.getSender().getGameProfile().getId());
      if (chatUser != null) {
         chatUser.setLastTyping(0L);
         final SingleChat singleChat = this.labyConnect.getChatlogManager().getChat(chatUser);
         Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
               singleChat.addMessage(new MessageChatComponent(packet.getSender().getGameProfile().getName(), System.currentTimeMillis(), packet.getMessage()));
            }
         });
         net.labymod.main.LabyMod.getInstance().notifyMessageProfile(packet.getSender().getGameProfile(), packet.getMessage());
      }
   }

   @Override
   public void handle(PacketPlayTyping packet) {
      ChatUser chatUser = this.labyConnect.getChatUserByUUID(packet.getPlayer().getGameProfile().getId());
      if (chatUser != null) {
         chatUser.setLastTyping(System.currentTimeMillis());
      }
   }

   @Override
   public void handle(PacketPlayRequestAddFriendResponse packet) {
      // Phase 2: surface add-friend result in the Esdeath friends screen.
      lastAddFriendResponse = packet.isRequestSent() ? "true" : packet.getReason();
   }

   @Override
   public void handle(PacketPlayRequestRemove packet) {
      Iterator<ChatRequest> iterator = this.labyConnect.getRequests().iterator();

      while (iterator.hasNext()) {
         ChatRequest next = iterator.next();
         if (next.getGameProfile().getName().equalsIgnoreCase(packet.getPlayerName())) {
            iterator.remove();
         }
      }
   }

   @Override
   public void handle(PacketPlayDenyFriendRequest packet) {
   }

   @Override
   public void handle(PacketPlayFriendRemove packet) {
      Iterator<ChatUser> iterator = this.labyConnect.getFriends().iterator();

      while (iterator.hasNext()) {
         ChatUser next = iterator.next();
         if (next.equals(packet.getToRemove())) {
            iterator.remove();
         }
      }

      this.labyConnect.sortFriendList(net.labymod.main.LabyMod.getSettings().friendSortType);
      GameIconHelper.updateIcon(true, false);
   }

   @Override
   public void handle(PacketLoginOptions packet) {
   }

   @Override
   public void handle(PacketPlayServerStatus packet) {
   }

   @Override
   public void handle(PacketPlayServerStatusUpdate packet) {
   }

   @Override
   public void handle(PacketPlayFriendStatus packet) {
      ChatUser chatUser = this.labyConnect.getChatUser(packet.getPlayer());
      chatUser.setCurrentServerInfo(packet.getPlayerInfo());
   }

   @Override
   public void handle(PacketPlayFriendPlayingOn packet) {
      if (net.labymod.main.LabyMod.getSettings().alertsPlayingOn) {
         if (packet.getGameModeName() != null && !packet.getGameModeName().isEmpty()) {
            String message = null;
            if (packet.getGameModeName().contains(".")) {
               message = LanguageManager.translate("chat_user_now_playing_on", packet.getGameModeName());
            } else {
               message = LanguageManager.translate("chat_user_now_playing", packet.getGameModeName());
            }

            net.labymod.main.LabyMod.getInstance().notifyMessageProfile(packet.getPlayer().getGameProfile(), message);
         }
      }
   }

   @Override
   public void handle(PacketPlayChangeOptions packet) {
   }

   @Override
   public void handle(PacketLoginTime packet) {
      this.labyConnect.getClientProfile().setFirstJoined(packet.getDateJoined());
   }

   @Override
   public void handle(PacketLoginVersion packet) {
   }

   @Override
   public void handle(PacketEncryptionRequest encryptionRequest) {
      UUID uuid = Minecraft.getMinecraft().getSession().getProfile().getId();
      PublicKey publicKey = CryptManager.decodePublicKey(encryptionRequest.getPublicKey());
      final SecretKey secretKey = CryptManager.createNewSharedKey();
      if (uuid == null) {
         this.lastKickMessage = LanguageManager.translate("chat_invalid_session");
         Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, this.lastKickMessage);
         this.disconnect(false);
      } else {
         String pin = net.labymod.main.LabyMod.getInstance().getPinManager().getValidPinOf(uuid);
         if (pin != null) {
            this.sendPacket(
               new PacketEncryptionResponse(secretKey, publicKey, encryptionRequest.getVerifyToken(), pin), channel -> this.enableEncryption(secretKey)
            );
         } else {
            try {
               String serverId = encryptionRequest.getServerId();
               String hash = new BigInteger(CryptManager.getServerIdHash(serverId, publicKey, secretKey)).toString(16);
               MinecraftSessionService minecraftSessionService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString())
                  .createMinecraftSessionService();
               minecraftSessionService.joinServer(Minecraft.getMinecraft().getSession().getProfile(), this.mojang, hash);
               this.sendPacket(new PacketEncryptionResponse(secretKey, publicKey, encryptionRequest.getVerifyToken()), new Consumer<NioSocketChannel>() {
                  public void accept(NioSocketChannel channel) {
                     ClientConnection.this.enableEncryption(secretKey);
                  }
               });
               return;
            } catch (AuthenticationUnavailableException var9) {
               this.lastKickMessage = LanguageManager.translate("chat_authentication_unavaileable");
            } catch (InvalidCredentialsException var10) {
               this.lastKickMessage = LanguageManager.translate("chat_invalid_session");
            } catch (AuthenticationException var11) {
               this.lastKickMessage = LanguageManager.translate("chat_login_failed");
            }

            Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, this.lastKickMessage);
            this.updateConnectionState(EnumConnectionState.HELLO);
            this.premium = false;
         }
      }
   }

   @Override
   public void handle(PacketEncryptionResponse packet) {
   }

   @Override
   public void handle(PacketMojangStatus packet) {
   }

   @Override
   public void handle(PacketUpdateCosmetics packet) {
      UUID uuid = net.labymod.main.LabyMod.getInstance().getPlayerUUID();
      if (uuid != null) {
         String json = packet.getJson();
         UserManager userManager = net.labymod.main.LabyMod.getInstance().getUserManager();
         if (json == null) {
            userManager.removeCheckedUser(uuid);
            userManager.getUser(uuid).unloadCosmeticTextures();
         } else {
            userManager.updateUsersJson(uuid, json, new Consumer<Boolean>() {
               public void accept(Boolean accepted) {
                  try {
                     Thread.sleep(100L);
                  } catch (InterruptedException var3) {
                     var3.printStackTrace();
                  }
               }
            });
         }
      }
   }

   @Override
   public void handle(PacketUserBadge packetUserStatus) {
      UserManager userManager = net.labymod.main.LabyMod.getInstance().getUserManager();
      UUID[] uuids = packetUserStatus.getUuids();
      byte[] ranks = packetUserStatus.getRanks();
      boolean validRanks = uuids.length == ranks.length;

      for (int i = 0; i < packetUserStatus.getUuids().length; i++) {
         UUID uuid = uuids[i];
         User user = userManager.getUser(uuid);
         user.setFamiliar(true);
         if (validRanks) {
            int rank = ranks[i];
            if (rank > 0) {
               try {
                  LabyGroup group = userManager.getGroupManager().getGroupById((short)ranks[i]);
                  user.setGroup(group);
               } catch (Exception var11) {
                  Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "Error on updating user rank of " + uuid.toString() + ": " + ranks[i]);
               }
            }
         }
      }
   }

   @Override
   public void handle(PacketActionBroadcast packet) {
      switch (packet.getType()) {
         case EMOTE:
            short emoteId = ByteBuffer.wrap(packet.getData()).order(ByteOrder.LITTLE_ENDIAN).getShort();
            net.labymod.main.LabyMod.getInstance().getEmoteRegistry().handleEmote(packet.getUniqueId(), emoteId);
            break;
         case COSMETIC_CHANGE:
            String json = new String(packet.getData());
            net.labymod.main.LabyMod.getInstance().getUserManager().updateUsersJson(packet.getUniqueId(), json, null);
            break;
         case STICKER:
            short stickerId = ByteBuffer.wrap(packet.getData()).order(ByteOrder.LITTLE_ENDIAN).getShort();
            UserManager userManager = net.labymod.main.LabyMod.getInstance().getUserManager();
            net.labymod.main.LabyMod.getInstance().getStickerRegistry().handleSticker(userManager.getUser(packet.getUniqueId()), stickerId);
      }
   }

   @Override
   public void handle(PacketAddonMessage packet) {
      net.labymod.main.LabyMod.getInstance().getEventManager().callAddonMessage(packet);
      String key = packet.getKey();
      if (key.equals("UPDATE")) {
         net.labymod.main.LabyMod.getInstance().getUpdater().setForceUpdate(true);
      }

      if (key.equals("refresh_labymod")) {
         net.labymod.main.LabyMod.getInstance().getUserManager().refresh();
      }

      if (key.equals("UPDATE-BACKUP")) {
         net.labymod.main.LabyMod.getInstance().getUpdater().setBackupMethod(true);
         net.labymod.main.LabyMod.getInstance().getUpdater().setForceUpdate(true);
      }

      if (key.equals("invalidate_pin")) {
         net.labymod.main.LabyMod.getInstance().getPinManager().invalidatePinOf(net.labymod.main.LabyMod.getInstance().getPlayerUUID());
      }

      if (key.equals("dashboard_pin")) {
         JsonObject jsonObject = (JsonObject)new JsonParser().parse(packet.getJson());
         if (jsonObject.has("pin")) {
            String pin = jsonObject.get("pin").getAsString();
            if (this.dashboardPinConsumer != null) {
               this.dashboardPinConsumer.accept(pin);
            }
         }
      }

      if (key.equals("server_message")) {
         JsonObject jsonObject = (JsonObject)new JsonParser().parse(packet.getJson());
         net.labymod.main.LabyMod.getInstance().displayMessageInChat(ModColor.createColors(jsonObject.get("message").getAsString()));
      }

      if (key.equals("language_flags")) {
         JsonObject map = (JsonObject)new JsonParser().parse(packet.getJson());

         try {
            Map<UUID, ResourceLocation> flags = net.labymod.main.LabyMod.getInstance().getPriorityOverlayRenderer().getLanguageFlags();

            for (Entry<String, JsonElement> entry : map.entrySet()) {
               UUID uuid = UUID.fromString(entry.getKey());
               String code = entry.getValue().getAsString();
               if (!code.contains(".") && !code.contains("/") && !code.contains("\\") && !code.equals("??")) {
                  flags.put(uuid, new ResourceLocation(String.format("labymod/textures/flags/%s.png", code)));
               }
            }
         } catch (Exception var10) {
            var10.printStackTrace();
         }
      }

      // "labynet" settings GUI is a Phase 2/4 feature (LabyNet account screen) — not ported.

      if (key.equals("unauthenticated")) {
         this.updateConnectionState(EnumConnectionState.HELLO);
         this.premium = false;
      }
   }

   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      this.disconnect(false);
      if (!(cause instanceof IOException)) {
         cause.printStackTrace();
         ctx.close();
      }
   }

   public void sendPacket(final Packet packet, final Consumer<NioSocketChannel> consumer) {
      if (this.nioSocketChannel != null) {
         if (this.nioSocketChannel.isOpen() && this.nioSocketChannel.isWritable() && this.currentConnectionState != EnumConnectionState.OFFLINE) {
            if (this.nioSocketChannel.eventLoop().inEventLoop()) {
               this.nioSocketChannel.writeAndFlush(packet).addListeners(new GenericFutureListener[]{ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE});
               if (consumer != null) {
                  consumer.accept(this.nioSocketChannel);
               }
            } else {
               this.nioSocketChannel
                  .eventLoop()
                  .execute(
                     new Runnable() {
                        @Override
                        public void run() {
                           ClientConnection.this.nioSocketChannel
                              .writeAndFlush(packet)
                              .addListeners(new GenericFutureListener[]{ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE});
                           if (consumer != null) {
                              consumer.accept(ClientConnection.this.nioSocketChannel);
                           }
                        }
                     }
                  );
            }
         }
      }
   }

   public void sendPacket(Packet packet) {
      this.sendPacket(packet, null);
   }

   private void sendStatistics() {
      // Anonymous hardware/addon telemetry from the original LabyMod is intentionally
      // not sent from the EsdeathForge port. (Disabled by default via ModSettings anyway.)
   }

   @Deprecated
   public boolean isPinAvailable() {
      return net.labymod.main.LabyMod.getInstance().getPinManager().hasValidPin(net.labymod.main.LabyMod.getInstance().getPlayerUUID());
   }

   @Deprecated
   public void requestPin(Consumer<String> consumer) {
      String pin = net.labymod.main.LabyMod.getInstance().getPinManager().getValidPinOf(net.labymod.main.LabyMod.getInstance().getPlayerUUID());
      if (pin != null) {
         consumer.accept(pin);
      }
   }

   public void requestDashboardPin(Consumer<String> consumer) {
      this.dashboardPinConsumer = consumer;
      this.sendPacket(new PacketAddonMessage("dashboard_pin", new JsonObject().toString()));
   }

   public void setNioSocketChannel(NioSocketChannel nioSocketChannel) {
      this.nioSocketChannel = nioSocketChannel;
   }

   public EnumConnectionState getCurrentConnectionState() {
      return this.currentConnectionState;
   }

   public String getLastKickMessage() {
      return this.lastKickMessage;
   }

   public String getCustomIp() {
      return this.customIp;
   }

   public boolean isPremium() {
      return this.premium;
   }

   public boolean isConnectionEstablished() {
      return this.connectionEstablished;
   }
}
