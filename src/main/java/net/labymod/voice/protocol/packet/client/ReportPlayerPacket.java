package net.labymod.voice.protocol.packet.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ClientVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class ReportPlayerPacket extends VoicePacket<ClientVoicePacketHandler> {
   private UUID player;
   private String reason = "";

   public ReportPlayerPacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buffer) throws IOException {
      writeUUID(this.player, buffer);
      writeString(this.reason, buffer);
   }

   @Override
   public void read(ByteArrayInputStream buffer, int remoteProtocolVersion) throws IOException {
      this.player = readUUID(buffer);
      this.reason = readString(buffer);
   }

   public void handle(ClientVoicePacketHandler handler) {
      handler.handleReportPlayer(this);
   }

   public UUID getPlayer() {
      return this.player;
   }

   public String getReason() {
      return this.reason;
   }

   public void setPlayer(UUID player) {
      this.player = player;
   }

   public void setReason(String reason) {
      this.reason = reason;
   }
}
