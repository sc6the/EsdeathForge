package net.labymod.voice.protocol.packet.server.call;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ServerVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class ServerCallEndedPacket extends VoicePacket<ServerVoicePacketHandler> {
   public ServerCallEndedPacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buf) throws IOException {
   }

   @Override
   public void read(ByteArrayInputStream buf, int remoteProtocolVersion) throws IOException {
   }

   public void handle(ServerVoicePacketHandler handler) {
      handler.handleCallEnded(this);
   }
}
