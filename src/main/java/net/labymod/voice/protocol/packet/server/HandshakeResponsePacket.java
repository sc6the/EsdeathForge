package net.labymod.voice.protocol.packet.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import net.labymod.voice.protocol.ProtocolVersion;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ServerVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;
import net.labymod.voice.protocol.type.HandshakeResponse;

public class HandshakeResponsePacket extends VoicePacket<ServerVoicePacketHandler> {
   private HandshakeResponse response;
   private boolean isAdmin;
   private int protocolVersion = ProtocolVersion.VERSION;

   public HandshakeResponsePacket() {
      super(EncryptType.SYM, ConnectionState.HANDSHAKE);
   }

   @Override
   public void write(ByteArrayOutputStream buffer) throws IOException {
      writeInt(this.protocolVersion, buffer);
      writeEnum(this.response, buffer);
      buffer.write(this.isAdmin ? 1 : 0);
   }

   @Override
   public void read(ByteArrayInputStream buffer, int remoteProtocolVersion) throws IOException {
      this.protocolVersion = readInt(buffer);
      this.response = (HandshakeResponse)readEnum(buffer, HandshakeResponse.values());
      this.isAdmin = buffer.read() == 1;
   }

   public void handle(ServerVoicePacketHandler handler) {
      handler.handleHandshakeResponse(this);
   }

   public HandshakeResponse getResponse() {
      return this.response;
   }

   public boolean isAdmin() {
      return this.isAdmin;
   }

   public int getProtocolVersion() {
      return this.protocolVersion;
   }

   public void setResponse(HandshakeResponse response) {
      this.response = response;
   }

   public void setAdmin(boolean isAdmin) {
      this.isAdmin = isAdmin;
   }

   public void setProtocolVersion(int protocolVersion) {
      this.protocolVersion = protocolVersion;
   }

   @Override
   public String toString() {
      return "HandshakeResponsePacket(response=" + this.getResponse() + ", isAdmin=" + this.isAdmin() + ", protocolVersion=" + this.getProtocolVersion() + ")";
   }
}
