package net.labymod.voice.protocol.packet.client.call;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ClientVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class ClientEndCallPacket extends VoicePacket<ClientVoicePacketHandler> {
   public ClientEndCallPacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buf) throws IOException {
   }

   @Override
   public void read(ByteArrayInputStream buf, int remoteProtocolVersion) throws IOException {
   }

   public void handle(ClientVoicePacketHandler handler) {
      handler.handleCallEnd(this);
   }
}
