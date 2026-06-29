package net.labymod.voice.protocol.packet.server.call;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ServerVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class ServerCallUserAddedPacket extends VoicePacket<ServerVoicePacketHandler> {
   private UUID addedUserId;

   public ServerCallUserAddedPacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buf) throws IOException {
      writeUUID(this.addedUserId, buf);
   }

   @Override
   public void read(ByteArrayInputStream buf, int remoteProtocolVersion) throws IOException {
      this.addedUserId = readUUID(buf);
   }

   public void handle(ServerVoicePacketHandler handler) {
      handler.handleCallUserAdded(this);
   }

   public UUID getAddedUserId() {
      return this.addedUserId;
   }

   public void setAddedUserId(UUID addedUserId) {
      this.addedUserId = addedUserId;
   }
}
