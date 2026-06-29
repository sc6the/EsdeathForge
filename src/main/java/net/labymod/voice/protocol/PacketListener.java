package net.labymod.voice.protocol;

import java.net.DatagramPacket;

public interface PacketListener<T extends VoicePacket> {
   void handle(T var1, DatagramPacket var2);
}
