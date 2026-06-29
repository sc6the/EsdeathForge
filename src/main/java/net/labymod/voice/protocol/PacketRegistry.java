package net.labymod.voice.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import net.labymod.voice.protocol.packet.CallFeatureAvailablePacket;
import net.labymod.voice.protocol.packet.KeepAlivePacket;
import net.labymod.voice.protocol.packet.client.ClientAudioPacket;
import net.labymod.voice.protocol.packet.client.HandshakePacket;
import net.labymod.voice.protocol.packet.client.MutePlayerPacket;
import net.labymod.voice.protocol.packet.client.ReportPlayerPacket;
import net.labymod.voice.protocol.packet.client.SwitchServerPacket;
import net.labymod.voice.protocol.packet.client.UpdateVisiblePlayersPacket;
import net.labymod.voice.protocol.packet.client.call.ClientCallRequestResponsePacket;
import net.labymod.voice.protocol.packet.client.call.ClientEndCallPacket;
import net.labymod.voice.protocol.packet.client.call.ClientRequestDirectCallPacket;
import net.labymod.voice.protocol.packet.server.HandshakeResponsePacket;
import net.labymod.voice.protocol.packet.server.InvalidKeyPacket;
import net.labymod.voice.protocol.packet.server.KickPacket;
import net.labymod.voice.protocol.packet.server.MutedPacket;
import net.labymod.voice.protocol.packet.server.PlayerAlivePacket;
import net.labymod.voice.protocol.packet.server.PlayerDeadPacket;
import net.labymod.voice.protocol.packet.server.ReportPlayerCountPacket;
import net.labymod.voice.protocol.packet.server.ServerAudioPacket;
import net.labymod.voice.protocol.packet.server.call.ServerCallEndedPacket;
import net.labymod.voice.protocol.packet.server.call.ServerCallUserAddedPacket;
import net.labymod.voice.protocol.packet.server.call.ServerCallUserRemovedPacket;
import net.labymod.voice.protocol.packet.server.call.ServerRequestDirectCallPacket;

public class PacketRegistry {
   private static final Map<Byte, Callable<? extends VoicePacket>> REGISTERED_PACKETS = new HashMap<>();
   private static final Map<Class<? extends VoicePacket>, Byte> PACKET_IDS = new HashMap<>();

   private static void register(int id, Callable<? extends VoicePacket> callable) {
      try {
         REGISTERED_PACKETS.put((byte)id, callable);
         PACKET_IDS.put((Class<? extends VoicePacket>)callable.call().getClass(), (byte)id);
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }

   public static boolean isValidPacket(byte id) {
      return REGISTERED_PACKETS.containsKey(id);
   }

   public static VoicePacket createPacket(byte id) {
      try {
         return REGISTERED_PACKETS.get(id).call();
      } catch (Exception var2) {
         var2.printStackTrace();
         return null;
      }
   }

   public static <T extends VoicePacket> byte getPacketId(T packet) {
      Byte packetId = PACKET_IDS.get(packet.getClass());
      if (packetId == null) {
         System.out.println("Invalid packet " + packet.getClass().getSimpleName());
         return -1;
      } else {
         return packetId;
      }
   }

   static {
      register(0, HandshakePacket::new);
      register(1, HandshakeResponsePacket::new);
      register(2, InvalidKeyPacket::new);
      register(3, KeepAlivePacket::new);
      register(4, KickPacket::new);
      register(5, MutedPacket::new);
      register(6, MutePlayerPacket::new);
      register(7, UpdateVisiblePlayersPacket::new);
      register(8, ClientAudioPacket::new);
      register(9, ServerAudioPacket::new);
      register(10, PlayerAlivePacket::new);
      register(11, PlayerDeadPacket::new);
      register(12, SwitchServerPacket::new);
      register(13, ReportPlayerPacket::new);
      register(14, ReportPlayerCountPacket::new);
      register(15, ClientRequestDirectCallPacket::new);
      register(16, ClientCallRequestResponsePacket::new);
      register(17, ClientEndCallPacket::new);
      register(18, ServerRequestDirectCallPacket::new);
      register(19, ServerCallUserAddedPacket::new);
      register(20, ServerCallUserRemovedPacket::new);
      register(21, ServerCallEndedPacket::new);
      register(22, CallFeatureAvailablePacket::new);
   }
}
