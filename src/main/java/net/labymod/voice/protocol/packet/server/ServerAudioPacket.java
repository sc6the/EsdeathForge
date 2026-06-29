package net.labymod.voice.protocol.packet.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ServerVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class ServerAudioPacket extends VoicePacket<ServerVoicePacketHandler> {
   private UUID player;
   private byte[] data;

   public ServerAudioPacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buffer) throws IOException {
      writeUUID(this.player, buffer);
      writeInt(this.data.length, buffer);
      buffer.write(this.data);
   }

   @Override
   public void read(ByteArrayInputStream buffer, int remoteProtocolVersion) throws IOException {
      this.player = readUUID(buffer);
      int length = readInt(buffer);
      this.data = new byte[length];
      buffer.read(this.data);
   }

   public void handle(ServerVoicePacketHandler handler) {
      handler.handleServerAudio(this);
   }

   public void setPlayer(UUID player) {
      this.player = player;
   }

   public void setData(byte[] data) {
      this.data = data;
   }

   public UUID getPlayer() {
      return this.player;
   }

   public byte[] getData() {
      return this.data;
   }
}
