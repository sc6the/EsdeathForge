package net.labymod.voice.protocol.packet.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ServerVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class KickPacket extends VoicePacket<ServerVoicePacketHandler> {
   private String reason;

   public KickPacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buffer) throws IOException {
      writeString(this.reason, buffer);
   }

   @Override
   public void read(ByteArrayInputStream buffer, int remoteProtocolVersion) throws IOException {
      this.reason = readString(buffer);
   }

   public void handle(ServerVoicePacketHandler handler) {
      handler.handleKick(this);
   }

   public void setReason(String reason) {
      this.reason = reason;
   }

   public String getReason() {
      return this.reason;
   }
}
