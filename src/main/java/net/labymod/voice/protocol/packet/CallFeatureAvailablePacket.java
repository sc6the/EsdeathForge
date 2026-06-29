package net.labymod.voice.protocol.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.VoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public class CallFeatureAvailablePacket extends VoicePacket<VoicePacketHandler> {
   private Collection<UUID> uniqueIds;

   public CallFeatureAvailablePacket() {
      super(EncryptType.SYM, ConnectionState.CONNECTED);
   }

   @Override
   public void write(ByteArrayOutputStream buf) throws IOException {
      writeInt(this.uniqueIds.size(), buf);

      for (UUID uniqueId : this.uniqueIds) {
         writeUUID(uniqueId, buf);
      }
   }

   @Override
   public void read(ByteArrayInputStream buf, int remoteProtocolVersion) throws IOException {
      int size = readInt(buf);
      this.uniqueIds = new ArrayList<>(size);

      for (int i = 0; i < size; i++) {
         this.uniqueIds.add(readUUID(buf));
      }
   }

   @Override
   public void handle(VoicePacketHandler handler) {
      handler.handleCallFeatureAvailable(this);
   }

   public Collection<UUID> getUniqueIds() {
      return this.uniqueIds;
   }

   public void setUniqueIds(Collection<UUID> uniqueIds) {
      this.uniqueIds = uniqueIds;
   }
}
