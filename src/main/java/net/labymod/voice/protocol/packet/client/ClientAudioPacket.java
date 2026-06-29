package net.labymod.voice.protocol.packet.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ClientVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class ClientAudioPacket extends VoicePacket<ClientVoicePacketHandler> {
   private byte[] data;

   public ClientAudioPacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buffer) throws IOException {
      buffer.write(intToBytes(this.data.length));
      buffer.write(this.data);
   }

   @Override
   public void read(ByteArrayInputStream buffer, int remoteProtocolVersion) throws IOException {
      byte[] lenData = new byte[4];
      buffer.read(lenData);
      this.data = new byte[byteArrayToInt(lenData)];
      buffer.read(this.data);
   }

   public void handle(ClientVoicePacketHandler handler) {
      handler.handleClientAudio(this);
   }

   public void setData(byte[] data) {
      this.data = data;
   }

   public byte[] getData() {
      return this.data;
   }
}
