package net.labymod.voice.protocol.handler;

import net.labymod.voice.protocol.packet.CallFeatureAvailablePacket;
import net.labymod.voice.protocol.packet.KeepAlivePacket;

public interface VoicePacketHandler {
   void handleKeepAlive(KeepAlivePacket var1);

   void handleCallFeatureAvailable(CallFeatureAvailablePacket var1);
}
