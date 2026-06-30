package net.labymod.labyconnect.packets;

import net.labymod.labyconnect.handling.PacketHandler;

public abstract class Packet {
   public abstract void read(PacketBuf var1);

   public abstract void write(PacketBuf var1);

   public abstract void handle(PacketHandler var1);
}
