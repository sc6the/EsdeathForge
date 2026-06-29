package net.labymod.utils;

/**
 * LabyMod's own ServerData wrapper (do NOT confuse with net.minecraft.client.multiplayer.ServerData).
 * Used in event-listener signatures and the plugin-message channel.
 */
public class ServerData {
    public final String address;
    public final int port;
    public final String name;

    public ServerData(String address, int port, String name) {
        this.address = address;
        this.port = port;
        this.name = name;
    }

    public ServerData(String address, int port) {
        this(address, port, null);
    }

    public String getAddress() { return address; }
    public int getPort() { return port; }
    public String getName() { return name; }
}
