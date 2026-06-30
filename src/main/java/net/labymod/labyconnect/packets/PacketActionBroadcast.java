package net.labymod.labyconnect.packets;

import java.util.UUID;
import net.labymod.labyconnect.handling.PacketHandler;

public class PacketActionBroadcast extends Packet {
   private UUID uniqueId;
   private PacketActionBroadcast.ActionType type;
   private byte[] data;

   @Override
   public void read(PacketBuf buf) {
      this.uniqueId = new UUID(buf.readLong(), buf.readLong());
      this.type = PacketActionBroadcast.ActionType.values()[buf.readShort() - 1];
      this.data = new byte[buf.readVarIntFromBuffer()];
      buf.readBytes(this.data);
   }

   @Override
   public void write(PacketBuf buf) {
      buf.writeLong(this.uniqueId.getMostSignificantBits());
      buf.writeLong(this.uniqueId.getLeastSignificantBits());
      buf.writeShort((short)this.type.getId());
      buf.writeVarIntToBuffer(this.data.length);
      buf.writeBytes(this.data);
   }

   @Override
   public void handle(PacketHandler packetHandler) {
      packetHandler.handle(this);
   }

   public UUID getUniqueId() {
      return this.uniqueId;
   }

   public PacketActionBroadcast.ActionType getType() {
      return this.type;
   }

   public byte[] getData() {
      return this.data;
   }

   public static enum ActionType {
      EMOTE(1),
      COSMETIC_CHANGE(2),
      STICKER(3);

      private final int id;

      private ActionType(int id) {
         this.id = id;
      }

      public int getId() {
         return this.id;
      }
   }
}
