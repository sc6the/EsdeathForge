package net.labymod.labyconnect.packets;

import java.util.ArrayList;
import java.util.List;
import net.labymod.labyconnect.handling.PacketHandler;
import net.labymod.labyconnect.user.ChatUser;

public class PacketLoginFriend extends Packet {
   private List<ChatUser> friends;

   public PacketLoginFriend(List<ChatUser> friends) {
      this.friends = friends;
   }

   public PacketLoginFriend() {
   }

   @Override
   public void read(PacketBuf buf) {
      List<ChatUser> players = new ArrayList<>();
      int a = buf.readInt();

      for (int i = 0; i < a; i++) {
         players.add(buf.readChatUser());
      }

      this.friends = new ArrayList<>();
      this.friends.addAll(players);
   }

   @Override
   public void write(PacketBuf buf) {
      buf.writeInt(this.getFriends().size());

      for (int i = 0; i < this.getFriends().size(); i++) {
         ChatUser p = this.getFriends().get(i);
         buf.writeChatUser(p);
      }
   }

   @Override
   public void handle(PacketHandler packetHandler) {
      packetHandler.handle(this);
   }

   public List<ChatUser> getFriends() {
      return this.friends;
   }
}
