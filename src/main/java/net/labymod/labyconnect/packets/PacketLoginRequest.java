package net.labymod.labyconnect.packets;

import java.util.ArrayList;
import java.util.List;
import net.labymod.labyconnect.handling.PacketHandler;
import net.labymod.labyconnect.user.ChatRequest;

public class PacketLoginRequest extends Packet {
   private List<ChatRequest> requesters;

   public PacketLoginRequest(List<ChatRequest> requesters) {
      this.requesters = requesters;
   }

   public PacketLoginRequest() {
   }

   public List<ChatRequest> getRequests() {
      return this.requesters;
   }

   @Override
   public void read(PacketBuf buf) {
      this.requesters = new ArrayList<>();
      int a = buf.readInt();

      for (int i = 0; i < a; i++) {
         this.requesters.add((ChatRequest)buf.readChatUser());
      }
   }

   @Override
   public void write(PacketBuf buf) {
      buf.writeInt(this.getRequests().size());

      for (int i = 0; i < this.getRequests().size(); i++) {
         buf.writeChatUser(this.getRequests().get(i));
      }
   }

   @Override
   public void handle(PacketHandler packetHandler) {
      packetHandler.handle(this);
   }
}
