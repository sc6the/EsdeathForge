package net.labymod.voice.protocol.packet.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ServerVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class ReportPlayerCountPacket extends VoicePacket<ServerVoicePacketHandler> {
   private UUID player;
   private String reason = "";
   private int count;

   public ReportPlayerCountPacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buffer) throws IOException {
      writeUUID(this.player, buffer);
      writeString(this.reason, buffer);
      writeInt(this.count, buffer);
   }

   @Override
   public void read(ByteArrayInputStream buffer, int remoteProtocolVersion) throws IOException {
      this.player = readUUID(buffer);
      this.reason = readString(buffer);
      this.count = readInt(buffer);
   }

   public void handle(ServerVoicePacketHandler handler) {
      handler.handleReportPlayerCount(this);
   }

   public UUID getPlayer() {
      return this.player;
   }

   public String getReason() {
      return this.reason;
   }

   public int getCount() {
      return this.count;
   }

   public void setPlayer(UUID player) {
      this.player = player;
   }

   public void setReason(String reason) {
      this.reason = reason;
   }

   public void setCount(int count) {
      this.count = count;
   }
}
