package net.labymod.voice.protocol.packet.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ServerVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class PlayerDeadPacket extends VoicePacket<ServerVoicePacketHandler> {
   private final List<UUID> players = new ArrayList<>();

   public PlayerDeadPacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buffer) throws IOException {
      if (this.players.size() > 127) {
         buffer.write(0);
      } else {
         buffer.write(this.players.size());

         for (UUID player : this.players) {
            writeUUID(player, buffer);
         }
      }
   }

   @Override
   public void read(ByteArrayInputStream buffer, int remoteProtocolVersion) throws IOException {
      byte count = (byte)buffer.read();

      for (int i = 0; i < count; i++) {
         this.players.add(readUUID(buffer));
      }
   }

   public void handle(ServerVoicePacketHandler handler) {
      handler.handlePlayerDead(this);
   }

   public List<UUID> getPlayers() {
      return this.players;
   }
}
