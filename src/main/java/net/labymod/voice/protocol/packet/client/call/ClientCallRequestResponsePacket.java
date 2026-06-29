package net.labymod.voice.protocol.packet.client.call;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ClientVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class ClientCallRequestResponsePacket extends VoicePacket<ClientVoicePacketHandler> {
   private UUID callerId;
   private ClientCallRequestResponsePacket.ResponseType type;

   public ClientCallRequestResponsePacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buf) throws IOException {
      writeUUID(this.callerId, buf);
      writeEnum(this.type, buf);
   }

   @Override
   public void read(ByteArrayInputStream buf, int remoteProtocolVersion) throws IOException {
      this.callerId = readUUID(buf);
      this.type = (ClientCallRequestResponsePacket.ResponseType)readEnum(buf, ClientCallRequestResponsePacket.ResponseType.values());
   }

   public void handle(ClientVoicePacketHandler handler) {
      handler.handleCallRequestResponse(this);
   }

   public UUID getCallerId() {
      return this.callerId;
   }

   public ClientCallRequestResponsePacket.ResponseType getType() {
      return this.type;
   }

   public void setCallerId(UUID callerId) {
      this.callerId = callerId;
   }

   public void setType(ClientCallRequestResponsePacket.ResponseType type) {
      this.type = type;
   }

   public static enum ResponseType {
      ACCEPTED,
      REJECTED;
   }
}
