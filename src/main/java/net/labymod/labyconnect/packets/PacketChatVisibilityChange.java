package net.labymod.labyconnect.packets;

import net.labymod.labyconnect.handling.PacketHandler;

public class PacketChatVisibilityChange extends Packet {
   private boolean visible;

   @Override
   public void read(PacketBuf buf) {
      this.visible = buf.readBoolean();
   }

   @Override
   public void write(PacketBuf buf) {
      buf.writeBoolean(this.visible);
   }

   @Override
   public void handle(PacketHandler packetHandler) {
      packetHandler.handle(this);
   }

   public boolean isVisible() {
      return this.visible;
   }
}
