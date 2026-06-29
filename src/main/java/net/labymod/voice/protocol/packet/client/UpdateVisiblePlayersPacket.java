package net.labymod.voice.protocol.packet.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ClientVoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class UpdateVisiblePlayersPacket extends VoicePacket<ClientVoicePacketHandler> {
   private final List<UUID> players = new ArrayList<>();
   private int requestId;

   public UpdateVisiblePlayersPacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buffer) throws IOException {
      buffer.write(this.requestId);
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
      this.requestId = buffer.read();
      byte count = (byte)buffer.read();

      for (int i = 0; i < count; i++) {
         this.players.add(readUUID(buffer));
      }
   }

   public void handle(ClientVoicePacketHandler handler) {
      handler.handleUpdateVisiblePlayers(this);
   }

   public List<UUID> getPlayers() {
      return this.players;
   }

   public int getRequestId() {
      return this.requestId;
   }

   public void setRequestId(int requestId) {
      this.requestId = requestId;
   }
}
