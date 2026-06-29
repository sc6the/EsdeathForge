package net.labymod.voice.protocol.packet.server.call;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ServerVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class ServerCallUserRemovedPacket extends VoicePacket<ServerVoicePacketHandler> {
   private UUID removedUserId;

   public ServerCallUserRemovedPacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buf) throws IOException {
      writeUUID(this.removedUserId, buf);
   }

   @Override
   public void read(ByteArrayInputStream buf, int remoteProtocolVersion) throws IOException {
      this.removedUserId = readUUID(buf);
   }

   public void handle(ServerVoicePacketHandler handler) {
      handler.handleCallUserRemoved(this);
   }

   public UUID getRemovedUserId() {
      return this.removedUserId;
   }

   public void setRemovedUserId(UUID removedUserId) {
      this.removedUserId = removedUserId;
   }
}
