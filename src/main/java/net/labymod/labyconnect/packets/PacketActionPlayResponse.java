package net.labymod.labyconnect.packets;

import net.labymod.labyconnect.handling.PacketHandler;

public class PacketActionPlayResponse extends Packet {
   private short requestId;
   private boolean allowed;
   private String reason;

   public PacketActionPlayResponse() {
   }

   public PacketActionPlayResponse(boolean allowed) {
      this.allowed = allowed;
   }

   @Override
   public void read(PacketBuf buf) {
      this.requestId = buf.readShort();
      this.allowed = buf.readBoolean();
      if (!this.allowed) {
         this.reason = buf.readString();
      }
   }

   @Override
   public void write(PacketBuf buf) {
      buf.writeShort(this.requestId);
      buf.writeBoolean(this.allowed);
      if (!this.allowed) {
         buf.writeString(this.reason);
      }
   }

   @Override
   public void handle(PacketHandler packetHandler) {
      net.labymod.main.LabyMod.getInstance().getUserManager().resolveAction(this.requestId, this);
   }

   public boolean isAllowed() {
      return this.allowed;
   }

   public short getRequestId() {
      return this.requestId;
   }

   public String getReason() {
      return this.reason;
   }
}
