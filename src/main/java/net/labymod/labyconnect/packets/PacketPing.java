package net.labymod.labyconnect.packets;

import net.labymod.labyconnect.handling.PacketHandler;

public class PacketPing extends Packet {
   @Override
   public void read(PacketBuf buf) {
   }

   @Override
   public void write(PacketBuf buf) {
   }

   @Override
   public void handle(PacketHandler packetHandler) {
      packetHandler.handle(this);
   }
}
