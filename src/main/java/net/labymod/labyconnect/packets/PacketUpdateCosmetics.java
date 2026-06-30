package net.labymod.labyconnect.packets;

import java.beans.ConstructorProperties;
import net.labymod.labyconnect.handling.PacketHandler;

public class PacketUpdateCosmetics extends Packet {
   private String json = null;

   public PacketUpdateCosmetics() {
   }

   @Override
   public void read(PacketBuf buf) {
      boolean hasJsonString = buf.readBoolean();
      if (hasJsonString) {
         this.json = buf.readString();
      }
   }

   @Override
   public void write(PacketBuf buf) {
   }

   @Override
   public void handle(PacketHandler packetHandler) {
      packetHandler.handle(this);
   }

   @ConstructorProperties({"json"})
   public PacketUpdateCosmetics(String json) {
      this.json = json;
   }

   public String getJson() {
      return this.json;
   }
}
