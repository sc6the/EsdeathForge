package net.labymod.labyconnect.packets;

import net.labymod.labyconnect.handling.PacketHandler;

public class PacketMojangStatus extends Packet {
   @Override
   public void read(PacketBuf buf) {
      buf.readInt();
      buf.readString();
   }

   @Override
   public void write(PacketBuf buf) {
   }

   @Override
   public void handle(PacketHandler packetHandler) {
      packetHandler.handle(this);
   }
}
