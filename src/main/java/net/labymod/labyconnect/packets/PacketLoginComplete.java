package net.labymod.labyconnect.packets;

import net.labymod.labyconnect.handling.PacketHandler;

public class PacketLoginComplete extends Packet {
   private String showDashboardButton;

   public PacketLoginComplete(String string) {
      this.showDashboardButton = string;
   }

   public PacketLoginComplete() {
   }

   @Override
   public void read(PacketBuf buf) {
      this.showDashboardButton = buf.readString();
   }

   @Override
   public void write(PacketBuf buf) {
      buf.writeString(this.showDashboardButton);
   }

   public int getId() {
      return 2;
   }

   @Override
   public void handle(PacketHandler packetHandler) {
      packetHandler.handle(this);
   }

   public String getDashboardPin() {
      return this.showDashboardButton;
   }
}
