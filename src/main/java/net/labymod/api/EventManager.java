package net.labymod.api;

import com.google.gson.JsonElement;
import net.labymod.api.events.ServerMessageEvent;
import net.labymod.labyconnect.packets.PacketAddonDevelopment;
import net.labymod.labyconnect.packets.PacketAddonMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal event bus for the ported LabyConnect code. Only the channels LabyConnect
 * actually uses are wired:
 *   - {@link ServerMessageEvent} (LMC plugin-channel "server_*" messages)
 *   - shutdown hooks (disconnect on quit)
 *   - addon-message callbacks (no-op until Phase 4 addon support)
 */
public class EventManager {

    private final List<ServerMessageEvent> serverMessage = new ArrayList<ServerMessageEvent>();
    private final List<Runnable> shutdownHooks = new ArrayList<Runnable>();

    public void register(ServerMessageEvent listener) {
        serverMessage.add(listener);
    }

    public void registerShutdownHook(Runnable runnable) {
        shutdownHooks.add(runnable);
    }

    public void callServerMessage(String messageKey, JsonElement serverMessage) {
        for (ServerMessageEvent listener : this.serverMessage) {
            try {
                listener.onServerMessage(messageKey, serverMessage);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void callShutdownHook() {
        for (Runnable hook : shutdownHooks) {
            try {
                hook.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /** Phase 4 (addon support). No-op for now. */
    public void callAddonMessage(PacketAddonMessage packet) {
    }

    /** Phase 4 (addon support). No-op for now. */
    public void callAddonDevelopmentPacket(PacketAddonDevelopment packet) {
    }
}
