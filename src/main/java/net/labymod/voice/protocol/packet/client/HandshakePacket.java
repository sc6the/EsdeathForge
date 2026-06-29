package net.labymod.voice.protocol.packet.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import net.labymod.voice.protocol.ProtocolVersion;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ClientVoicePacketHandler;
import net.labymod.voice.protocol.type.AuthenticationMethod;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class HandshakePacket extends VoicePacket<ClientVoicePacketHandler> {
   private static final byte[] FIREWALL_IDENTIFIER = new byte[]{-128, 26, -79, -79, 31, -36, 78};
   private int protocolVersion;
   private AuthenticationMethod method;
   private byte[] symKey = new byte[16];
   private UUID player;
   private String string;

   public HandshakePacket() {
      super(EncryptType.ASYM, ConnectionState.HANDSHAKE);
   }

   @Override
   public void write(ByteArrayOutputStream buffer) throws IOException {
      writeInt(ProtocolVersion.VERSION, buffer);
      buffer.write(this.symKey);
      writeUUID(this.player, buffer);
      writeEnum(this.method, buffer);
      writeString(this.string, buffer);
   }

   @Override
   public void read(ByteArrayInputStream buffer, int remoteProtocolVersion) throws IOException {
      this.protocolVersion = readInt(buffer);
      buffer.read(this.symKey);
      this.player = readUUID(buffer);
      this.method = (AuthenticationMethod)readEnum(buffer, AuthenticationMethod.values());
      this.string = readString(buffer);
   }

   public void handle(ClientVoicePacketHandler handler) {
      handler.handleHandshake(this);
   }

   @Override
   public byte[] getFirewallIdentifier() {
      return FIREWALL_IDENTIFIER;
   }

   public void setProtocolVersion(int protocolVersion) {
      this.protocolVersion = protocolVersion;
   }

   public void setMethod(AuthenticationMethod method) {
      this.method = method;
   }

   public void setSymKey(byte[] symKey) {
      this.symKey = symKey;
   }

   public void setPlayer(UUID player) {
      this.player = player;
   }

   public void setString(String string) {
      this.string = string;
   }

   public int getProtocolVersion() {
      return this.protocolVersion;
   }

   public AuthenticationMethod getMethod() {
      return this.method;
   }

   public byte[] getSymKey() {
      return this.symKey;
   }

   public UUID getPlayer() {
      return this.player;
   }

   public String getString() {
      return this.string;
   }
}
