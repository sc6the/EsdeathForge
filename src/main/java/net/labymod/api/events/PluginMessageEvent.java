package net.labymod.api.events;

import net.minecraft.network.PacketBuffer;

/**
 * Stub of LabyMod's PluginMessageEvent interface.
 * The real LabyMod EventManager dispatched plugin-channel messages here.
 * In our build the listener has to be plumbed through Forge's
 * FMLNetworkEvent / NetworkRegistry channel handler — see MergedVoiceChat.init() TODO.
 */
public interface PluginMessageEvent {
    void receiveMessage(String channelName, PacketBuffer packetBuffer);
}
