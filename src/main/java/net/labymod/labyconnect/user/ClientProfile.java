package net.labymod.labyconnect.user;

import com.mojang.authlib.GameProfile;
import java.util.Calendar;
import java.util.TimeZone;
import net.labymod.labyconnect.ClientConnection;
import net.labymod.labyconnect.LabyConnect;
import net.labymod.labyconnect.packets.PacketPlayChangeOptions;

public class ClientProfile {
   private ClientConnection clientConnection;
   private LabyConnect chatClient;
   private long firstJoined = 0L;

   public ClientProfile(LabyConnect chatClient, ClientConnection clientConnection) {
      this.chatClient = chatClient;
      this.clientConnection = clientConnection;
   }

   public UserStatus getUserStatus() {
      return UserStatus.getById(net.labymod.main.LabyMod.getSettings().onlineStatus);
   }

   public void setUserStatus(UserStatus userStatus) {
      net.labymod.main.LabyMod.getSettings().onlineStatus = userStatus.getId();
      // Persist so the chosen status is restored + resent on the next game launch.
      net.labymod.main.LabyMod.getSettings().save();
   }

   public TimeZone getTimeZone() {
      return Calendar.getInstance().getTimeZone();
   }

   public void sendSettingsToServer() {
      this.clientConnection
         .sendPacket(new PacketPlayChangeOptions(net.labymod.main.LabyMod.getSettings().showConnectedIp, this.getUserStatus(), this.getTimeZone()));
   }

   public ChatUser buildClientUser() {
      GameProfile gameProfile = new GameProfile(net.labymod.main.LabyMod.getInstance().getPlayerUUID(), net.labymod.main.LabyMod.getInstance().getPlayerName());
      return new ChatUser(
         gameProfile,
         this.getUserStatus(),
         net.labymod.main.LabyMod.getSettings().motd,
         new ServerInfo(
            net.labymod.main.LabyMod.getInstance().getCurrentServerData() == null ? "" : net.labymod.main.LabyMod.getInstance().getCurrentServerData().getIp(),
            net.labymod.main.LabyMod.getInstance().getCurrentServerData() == null
               ? 25565
               : net.labymod.main.LabyMod.getInstance().getCurrentServerData().getPort()
         ),
         0,
         System.currentTimeMillis(),
         0L,
         this.getTimeZone().getID(),
         System.currentTimeMillis(),
         this.firstJoined,
         this.chatClient.getFriends().size(),
         false
      );
   }

   public long getFirstJoined() {
      return this.firstJoined;
   }

   public void setFirstJoined(long firstJoined) {
      this.firstJoined = firstJoined;
   }
}
