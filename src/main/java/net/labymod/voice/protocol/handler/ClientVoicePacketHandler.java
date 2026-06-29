package net.labymod.voice.protocol.handler;

import net.labymod.voice.protocol.packet.client.ClientAudioPacket;
import net.labymod.voice.protocol.packet.client.HandshakePacket;
import net.labymod.voice.protocol.packet.client.MutePlayerPacket;
import net.labymod.voice.protocol.packet.client.ReportPlayerPacket;
import net.labymod.voice.protocol.packet.client.SwitchServerPacket;
import net.labymod.voice.protocol.packet.client.UpdateVisiblePlayersPacket;
import net.labymod.voice.protocol.packet.client.call.ClientCallRequestResponsePacket;
import net.labymod.voice.protocol.packet.client.call.ClientEndCallPacket;
import net.labymod.voice.protocol.packet.client.call.ClientRequestDirectCallPacket;

public interface ClientVoicePacketHandler extends VoicePacketHandler {
   void handleClientAudio(ClientAudioPacket var1);

   void handleHandshake(HandshakePacket var1);

   void handleMutePlayer(MutePlayerPacket var1);

   void handleReportPlayer(ReportPlayerPacket var1);

   void handleSwitchServer(SwitchServerPacket var1);

   void handleUpdateVisiblePlayers(UpdateVisiblePlayersPacket var1);

   void handleDirectCallRequest(ClientRequestDirectCallPacket var1);

   void handleCallRequestResponse(ClientCallRequestResponsePacket var1);

   void handleCallEnd(ClientEndCallPacket var1);
}
