package net.labymod.voice.protocol.packet.server.call;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ServerVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class ServerRequestDirectCallPacket extends VoicePacket<ServerVoicePacketHandler> {
   private UUID callerId;

   public ServerRequestDirectCallPacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buf) throws IOException {
      writeUUID(this.callerId, buf);
   }

   @Override
   public void read(ByteArrayInputStream buf, int remoteProtocolVersion) throws IOException {
      this.callerId = readUUID(buf);
   }

   public void handle(ServerVoicePacketHandler handler) {
      handler.handleDirectCallRequest(this);
   }

   public UUID getCallerId() {
      return this.callerId;
   }

   public void setCallerId(UUID callerId) {
      this.callerId = callerId;
   }
}
