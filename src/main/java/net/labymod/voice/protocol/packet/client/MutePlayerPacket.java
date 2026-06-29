package net.labymod.voice.protocol.packet.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ClientVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class MutePlayerPacket extends VoicePacket<ClientVoicePacketHandler> {
   private UUID player;
   private boolean mute;
   private String reason = "";
   private int hours = 0;

   public MutePlayerPacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buffer) throws IOException {
      writeUUID(this.player, buffer);
      buffer.write(this.mute ? 1 : 0);
      writeString(this.reason, buffer);
      writeInt(this.hours, buffer);
   }

   @Override
   public void read(ByteArrayInputStream buffer, int remoteProtocolVersion) throws IOException {
      this.player = readUUID(buffer);
      this.mute = buffer.read() == 1;
      this.reason = readString(buffer);
      this.hours = readInt(buffer);
   }

   public void handle(ClientVoicePacketHandler handler) {
      handler.handleMutePlayer(this);
   }

   public UUID getPlayer() {
      return this.player;
   }

   public boolean isMute() {
      return this.mute;
   }

   public String getReason() {
      return this.reason;
   }

   public int getHours() {
      return this.hours;
   }

   public void setPlayer(UUID player) {
      this.player = player;
   }

   public void setMute(boolean mute) {
      this.mute = mute;
   }

   public void setReason(String reason) {
      this.reason = reason;
   }

   public void setHours(int hours) {
      this.hours = hours;
   }

   @Override
   public String toString() {
      return "MutePlayerPacket(player=" + this.getPlayer() + ", mute=" + this.isMute() + ", reason=" + this.getReason() + ", hours=" + this.getHours() + ")";
   }
}
