package net.labymod.voice.protocol.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.VoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class KeepAlivePacket extends VoicePacket<VoicePacketHandler> {
   public KeepAlivePacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buffer) {
   }

   @Override
   public void read(ByteArrayInputStream buffer, int remoteProtocolVersion) {
   }

   @Override
   public void handle(VoicePacketHandler handler) {
      handler.handleKeepAlive(this);
   }
}
