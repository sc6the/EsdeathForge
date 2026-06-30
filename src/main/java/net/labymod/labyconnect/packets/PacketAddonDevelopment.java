package net.labymod.labyconnect.packets;

import java.util.UUID;
import net.labymod.labyconnect.handling.PacketHandler;
import net.labymod.utils.GZIPCompression;

public class PacketAddonDevelopment extends Packet {
   private UUID sender;
   private UUID[] receivers;
   private String key;
   private byte[] data;

   public PacketAddonDevelopment(UUID sender, String key, byte[] data) {
      this.sender = sender;
      this.key = key;
      this.data = GZIPCompression.compress(data);
      this.receivers = new UUID[0];
   }

   public PacketAddonDevelopment(UUID sender, UUID[] receivers, String key, byte[] data) {
      this.sender = sender;
      this.receivers = receivers;
      this.key = key;
      this.data = GZIPCompression.compress(data);
   }

   @Override
   public void read(PacketBuf buf) {
      this.sender = new UUID(buf.readLong(), buf.readLong());
      int receiverCnt = buf.readShort();
      this.receivers = new UUID[receiverCnt];

      for (int i = 0; i < this.receivers.length; i++) {
         this.receivers[i] = new UUID(buf.readLong(), buf.readLong());
      }

      this.key = buf.readString();
      byte[] data = new byte[buf.readInt()];
      buf.readBytes(data);
      this.data = data;
   }

   @Override
   public void write(PacketBuf buf) {
      buf.writeLong(this.sender.getMostSignificantBits());
      buf.writeLong(this.sender.getLeastSignificantBits());
      buf.writeShort(this.receivers.length);

      for (UUID receiver : this.receivers) {
         buf.writeLong(receiver.getMostSignificantBits());
         buf.writeLong(receiver.getLeastSignificantBits());
      }

      buf.writeString(this.key);
      buf.writeInt(this.data.length);
      buf.writeBytes(this.data);
   }

   @Override
   public void handle(PacketHandler packetHandler) {
      net.labymod.main.LabyMod.getInstance().getEventManager().callAddonDevelopmentPacket(this);
   }

   public byte[] getData() {
      return GZIPCompression.decompress(this.data);
   }

   public UUID getSender() {
      return this.sender;
   }

   public UUID[] getReceivers() {
      return this.receivers;
   }

   public String getKey() {
      return this.key;
   }

   public PacketAddonDevelopment() {
   }
}
