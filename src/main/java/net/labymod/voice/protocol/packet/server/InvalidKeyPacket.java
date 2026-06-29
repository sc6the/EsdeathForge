package net.labymod.voice.protocol.packet.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ServerVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class InvalidKeyPacket extends VoicePacket<ServerVoicePacketHandler> {
   public InvalidKeyPacket() {
      super(EncryptType.NONE, ConnectionState.HANDSHAKE);
   }

   @Override
   public void write(ByteArrayOutputStream buffer) throws IOException {
   }

   @Override
   public void read(ByteArrayInputStream buffer, int remoteProtocolVersion) throws IOException {
   }

   public void handle(ServerVoicePacketHandler handler) {
      handler.handleInvalidKey(this);
   }
}
