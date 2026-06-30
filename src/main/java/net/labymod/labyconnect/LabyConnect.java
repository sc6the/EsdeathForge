package net.labymod.labyconnect;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.labymod.api.events.ServerMessageEvent;
import net.labymod.labyconnect.log.ChatlogManager;
import net.labymod.labyconnect.packets.EnumConnectionState;
import net.labymod.labyconnect.packets.PacketPlayServerStatusUpdate;
import net.labymod.labyconnect.user.ChatRequest;
import net.labymod.labyconnect.user.ChatUser;
import net.labymod.labyconnect.user.ClientProfile;
import net.labymod.labyconnect.user.EnumAlertDisplayType;
import net.labymod.support.util.Debug;
import net.labymod.utils.ServerData;

public class LabyConnect implements ServerMessageEvent {
   private static final long RECONNECT_INTERVAL = 60L;
   private ClientConnection clientConnection;
   private ChatlogManager chatlogManager = new ChatlogManager();
   private List<ChatUser> friends = new ArrayList<>();
   private List<ChatRequest> requests = new ArrayList<>();
   private List<ChatUser> sortFriends = new ArrayList<>();
   private ClientProfile clientProfile;
   private PacketPlayServerStatusUpdate lastPacketPlayServerStatus;
   private EnumAlertDisplayType alertDisplayType;
   private final LabyConnectUserTracker tracker = new LabyConnectUserTracker(this);
   private boolean forcedLogout = false;
   private boolean viaServerList = false;

   public LabyConnect() {
      try {
         this.clientConnection = new ClientConnection(this);
         this.clientProfile = new ClientProfile(this, this.clientConnection);
      } catch (Throwable var3) {
         var3.printStackTrace();
      }

      this.updateAlertDisplayType();
      net.labymod.main.LabyMod.getInstance().getEventManager().registerShutdownHook(new Runnable() {
         @Override
         public void run() {
            if (LabyConnect.this.clientConnection != null) {
               LabyConnect.this.clientConnection.disconnect(false);
            }
         }
      });
      net.labymod.main.LabyMod.getInstance().getEventManager().register(this);
      String disableAutoReconnectString = System.getProperty("disableAutoReconnect");
      boolean disableAutoReconnect = disableAutoReconnectString != null && disableAutoReconnectString.equalsIgnoreCase("true");
      if (disableAutoReconnect) {
         this.clientConnection.connect();
      } else {
         Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
            try {
               if (this.clientConnection.getCurrentConnectionState() == EnumConnectionState.OFFLINE && !this.forcedLogout) {
                  this.clientConnection.connect();
               }
            } catch (Throwable var2x) {
               var2x.printStackTrace();
            }
         }, 0L, 60L, TimeUnit.SECONDS);
      }
   }

   @Override
   public void onServerMessage(String messageKey, JsonElement serverMessage) {
      if (messageKey.equals("server_gamemode")) {
         try {
            JsonObject jsonObject = serverMessage.getAsJsonObject();
            if (jsonObject.has("show_gamemode")) {
               boolean showGamemode = jsonObject.get("show_gamemode").getAsBoolean();
               if (showGamemode) {
                  if (jsonObject.has("gamemode_name")) {
                     this.updatePlayingOnServerState(jsonObject.get("gamemode_name").getAsString());
                  }
               } else {
                  this.updatePlayingOnServerState(null);
               }
            }
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }
   }

   public void updateAlertDisplayType() {
      try {
         this.alertDisplayType = EnumAlertDisplayType.valueOf(net.labymod.main.LabyMod.getSettings().alertDisplayType);
      } catch (Exception var2) {
         this.alertDisplayType = EnumAlertDisplayType.ACHIEVEMENT;
         var2.printStackTrace();
      }
   }

   public boolean isOnline() {
      return this.clientConnection != null && this.clientConnection.getCurrentConnectionState() == EnumConnectionState.PLAY;
   }

   public ChatUser getChatUser(ChatUser chatUser) {
      return this.getChatUserByUUID(chatUser.getGameProfile().getId());
   }

   public ChatUser getChatUserByUUID(UUID uuid) {
      for (ChatUser chatUser : this.friends) {
         if (chatUser.getGameProfile().getId().equals(uuid)) {
            return chatUser;
         }
      }

      return null;
   }

   public void updatePlayingOnServerState(String gamemode) {
      ServerData serverData = net.labymod.main.LabyMod.getInstance().getCurrentServerData();
      boolean viaServerlist = this.viaServerList && serverData != null;
      PacketPlayServerStatusUpdate packet;
      if (serverData != null && net.labymod.main.LabyMod.getInstance().isInGame() && !net.minecraft.client.Minecraft.getMinecraft().isSingleplayer()) {
         packet = new PacketPlayServerStatusUpdate(serverData.getIp(), serverData.getPort(), gamemode == null ? "" : gamemode, viaServerlist);
      } else {
         packet = new PacketPlayServerStatusUpdate();
      }

      if (this.lastPacketPlayServerStatus == null || !this.lastPacketPlayServerStatus.equals(packet)) {
         this.lastPacketPlayServerStatus = packet;
         this.getClientConnection().sendPacket(packet);
         if (packet.getServerIp() != null && !packet.getServerIp().isEmpty()) {
            Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "Now playing on " + packet.getServerIp() + ":" + packet.getPort() + " " + packet.getGamemode());
         }
      }
   }

   public void sortFriendList(final int sortMode) {
      List<ChatUser> sortedList = new ArrayList<>();

      for (ChatUser chatUser : this.friends) {
         switch (sortMode) {
            case 1:
               if (!chatUser.isOnline()) {
                  continue;
               }
               break;
            case 2:
               if (this.getChatlogManager().getChat(chatUser).getMessages().isEmpty()) {
                  continue;
               }
         }

         sortedList.add(chatUser);
      }

      Collections.sort(sortedList, new Comparator<ChatUser>() {
         public int compare(ChatUser a, ChatUser b) {
            if (a.isParty()) {
               return Integer.MIN_VALUE;
            } else {
               switch (sortMode) {
                  case 0: {
                     long la = a.isOnline() ? a.getLastOnline() / 1000L + 1L : a.getLastOnline() / 2000L;
                     long lb = b.isOnline() ? b.getLastOnline() / 1000L + 1L : b.getLastOnline() / 2000L;
                     return (int)(lb - la);
                  }
                  case 1:
                     return 0;
                  case 2: {
                     long la = a.isOnline() ? a.getLastInteraction() / 1000L : a.getLastInteraction() / 2000L;
                     long lb = b.isOnline() ? b.getLastInteraction() / 1000L : b.getLastInteraction() / 2000L;
                     return (int)(lb - la);
                  }
                  default:
                     return 0;
               }
            }
         }
      });
      this.sortFriends = sortedList;
   }

   public boolean hasFriendOnline(UUID uuid) {
      for (ChatUser friend : this.friends) {
         if (friend.isOnline() && friend.getGameProfile().getId().equals(uuid)) {
            return true;
         }
      }

      return false;
   }

   public LabyConnectUserTracker getTracker() {
      return this.tracker;
   }

   public ClientConnection getClientConnection() {
      return this.clientConnection;
   }

   public ChatlogManager getChatlogManager() {
      return this.chatlogManager;
   }

   public List<ChatUser> getFriends() {
      return this.friends;
   }

   public List<ChatRequest> getRequests() {
      return this.requests;
   }

   public List<ChatUser> getSortFriends() {
      return this.sortFriends;
   }

   public ClientProfile getClientProfile() {
      return this.clientProfile;
   }

   public PacketPlayServerStatusUpdate getLastPacketPlayServerStatus() {
      return this.lastPacketPlayServerStatus;
   }

   public EnumAlertDisplayType getAlertDisplayType() {
      return this.alertDisplayType;
   }

   public boolean isForcedLogout() {
      return this.forcedLogout;
   }

   public boolean isViaServerList() {
      return this.viaServerList;
   }

   public void setForcedLogout(boolean forcedLogout) {
      this.forcedLogout = forcedLogout;
   }

   public void setViaServerList(boolean viaServerList) {
      this.viaServerList = viaServerList;
   }
}
