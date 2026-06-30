package net.labymod.labyconnect.packets;

import java.util.UUID;
import net.labymod.labyconnect.handling.PacketHandler;

public class PacketUserBadge extends Packet {
   private UUID[] uuids;
   private byte[] ranks;

   public PacketUserBadge(UUID[] uuids) {
      this.uuids = uuids;
   }

   public PacketUserBadge(UUID[] uuids, byte[] ranks) {
      this.uuids = uuids;
      this.ranks = ranks;
   }

   @Override
   public void read(PacketBuf buf) {
      int size = buf.readVarIntFromBuffer();
      this.uuids = new UUID[size];

      for (int i = 0; i < size; i++) {
         this.uuids[i] = new UUID(buf.readLong(), buf.readLong());
      }

      byte[] bytes = new byte[size];
      buf.readBytes(bytes);
      this.ranks = bytes;
   }

   @Override
   public void write(PacketBuf buf) {
      buf.writeVarIntToBuffer(this.uuids.length);

      for (int i = 0; i < this.uuids.length; i++) {
         UUID uuid = this.uuids[i];
         buf.writeLong(uuid.getMostSignificantBits());
         buf.writeLong(uuid.getLeastSignificantBits());
      }
   }

   @Override
   public void handle(PacketHandler packetHandler) {
      packetHandler.handle(this);
   }

   public UUID[] getUuids() {
      return this.uuids;
   }

   public byte[] getRanks() {
      return this.ranks;
   }

   public PacketUserBadge() {
   }
}
