package net.labymod.labyconnect.packets;

import net.labymod.labyconnect.handling.PacketHandler;

public class PacketHelloPong extends Packet {
   private long a;

   public PacketHelloPong() {
   }

   public PacketHelloPong(long a) {
      this.a = a;
   }

   @Override
   public void read(PacketBuf buf) {
      this.a = buf.readLong();
   }

   @Override
   public void write(PacketBuf buf) {
      buf.writeLong(this.a);
   }

   public int getId() {
      return 1;
   }

   @Override
   public void handle(PacketHandler packetHandler) {
      packetHandler.handle(this);
   }
}
