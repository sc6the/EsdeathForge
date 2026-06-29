package net.labymod.voice.protocol.packet.client.call;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ClientVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class ClientRequestDirectCallPacket extends VoicePacket<ClientVoicePacketHandler> {
   private UUID targetId;

   public ClientRequestDirectCallPacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buf) throws IOException {
      writeUUID(this.targetId, buf);
   }

   @Override
   public void read(ByteArrayInputStream buf, int remoteProtocolVersion) throws IOException {
      this.targetId = readUUID(buf);
   }

   public void handle(ClientVoicePacketHandler handler) {
      handler.handleDirectCallRequest(this);
   }

   public UUID getTargetId() {
      return this.targetId;
   }

   public void setTargetId(UUID targetId) {
      this.targetId = targetId;
   }
}
