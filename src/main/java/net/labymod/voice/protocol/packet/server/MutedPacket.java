package net.labymod.voice.protocol.packet.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ServerVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class MutedPacket extends VoicePacket<ServerVoicePacketHandler> {
   private UUID player;
   private boolean muted;
   private long secondsLeft;
   private String reason;
   private String mutedBy;

   public MutedPacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buffer) throws IOException {
      writeUUID(this.player, buffer);
      buffer.write(this.muted ? 1 : 0);
      writeVarLong(this.secondsLeft, buffer);
      writeString(this.reason, buffer);
      writeString(this.mutedBy, buffer);
   }

   @Override
   public void read(ByteArrayInputStream buffer, int remoteProtocolVersion) throws IOException {
      this.player = readUUID(buffer);
      this.muted = buffer.read() == 1;
      this.secondsLeft = readVarLong(buffer);
      this.reason = readString(buffer);
      this.mutedBy = readString(buffer);
   }

   public void handle(ServerVoicePacketHandler handler) {
      handler.handleMuted(this);
   }

   public void setPlayer(UUID player) {
      this.player = player;
   }

   public void setMuted(boolean muted) {
      this.muted = muted;
   }

   public void setSecondsLeft(long secondsLeft) {
      this.secondsLeft = secondsLeft;
   }

   public void setReason(String reason) {
      this.reason = reason;
   }

   public void setMutedBy(String mutedBy) {
      this.mutedBy = mutedBy;
   }

   public UUID getPlayer() {
      return this.player;
   }

   public boolean isMuted() {
      return this.muted;
   }

   public long getSecondsLeft() {
      return this.secondsLeft;
   }

   public String getReason() {
      return this.reason;
   }

   public String getMutedBy() {
      return this.mutedBy;
   }
}
