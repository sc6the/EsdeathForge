package net.labymod.labyconnect.handling;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.labymod.core.LabyModCore;
import net.labymod.labyconnect.LabyConnect;
import net.labymod.labyconnect.packets.Packet;
import net.labymod.labyconnect.packets.PacketBuf;
import net.labymod.labyconnect.packets.Protocol;
import net.labymod.support.util.Debug;

public class PacketEncoder extends MessageToByteEncoder<Packet> {
   private LabyConnect labyConnect;

   public PacketEncoder(LabyConnect labyConnect) {
      this.labyConnect = labyConnect;
   }

   protected void encode(ChannelHandlerContext channelHandlerContext, Packet packet, ByteBuf byteBuf) throws Exception {
      PacketBuf packetBuffer = LabyModCore.getMinecraft().createPacketBuf(byteBuf);
      int id = Protocol.getProtocol().getPacketId(packet);
      if (id != 62 && id != 63 || this.labyConnect.getClientConnection().getCustomIp() != null) {
         Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "[OUT] " + id + " " + packet.getClass().getSimpleName());
      }

      packetBuffer.writeVarIntToBuffer(id);
      packet.write(packetBuffer);
   }
}
