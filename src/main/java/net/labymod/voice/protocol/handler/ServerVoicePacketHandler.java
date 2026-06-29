package net.labymod.voice.protocol.handler;

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

public interface ServerVoicePacketHandler extends VoicePacketHandler {
   void handleHandshakeResponse(HandshakeResponsePacket var1);

   void handleInvalidKey(InvalidKeyPacket var1);

   void handleKick(KickPacket var1);

   void handleMuted(MutedPacket var1);

   void handlePlayerAlive(PlayerAlivePacket var1);

   void handlePlayerDead(PlayerDeadPacket var1);

   void handleReportPlayerCount(ReportPlayerCountPacket var1);

   void handleServerAudio(ServerAudioPacket var1);

   void handleCallEnded(ServerCallEndedPacket var1);

   void handleCallUserAdded(ServerCallUserAddedPacket var1);

   void handleCallUserRemoved(ServerCallUserRemovedPacket var1);

   void handleDirectCallRequest(ServerRequestDirectCallPacket var1);
}
