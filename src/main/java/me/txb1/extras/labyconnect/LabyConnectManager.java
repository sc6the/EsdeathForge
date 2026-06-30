package me.txb1.extras.labyconnect;

import net.labymod.labyconnect.LabyConnect;
import net.labymod.main.LabyMod;

/**
 * EsdeathForge bootstrap for the ported LabyConnect socket (chat.labymod.net).
 *
 * Constructing {@link LabyConnect} starts its own auto-reconnect scheduler (authenticates with
 * the player's Mojang session, loads the friend list, receives presence + cosmetic pushes), so
 * {@link #init()} is all that's needed to come online. {@link #onClientTick()} drives the
 * near-player user tracker and periodic "playing on" presence updates.
 *
 * Phase 2 (Esdeath friends/DM screen) and Phase 3 (emote broadcast) read/extend the same
 * {@code LabyMod.getInstance().getLabyConnect()} instance this holds.
 */
public final class LabyConnectManager {

    private static LabyConnect labyConnect;
    private static long nextPresenceUpdate = 0L;
    private static long nextTrackScan = 0L;

    private LabyConnectManager() {}

    /** Called once from EsdeathForgeMod.init(). Safe to call after the Minecraft session exists. */
    public static void init() {
        if (labyConnect != null) return;
        try {
            labyConnect = new LabyConnect();
            LabyMod.getInstance().setLabyConnect(labyConnect);
            System.out.println("[LabyConnect] Initialised — connecting to chat.labymod.net as "
                + LabyMod.getInstance().getPlayerName());
        } catch (Throwable t) {
            System.out.println("[LabyConnect] Failed to initialise:");
            t.printStackTrace();
        }
    }

    public static LabyConnect get() {
        return labyConnect;
    }

    public static boolean isOnline() {
        return labyConnect != null && labyConnect.isOnline();
    }

    /** Drive from a client tick. Cheap; internally gated on being connected. */
    public static void onClientTick() {
        if (labyConnect == null) return;
        try {
            // Tell LabyConnect which players are visible so it pushes their cosmetics. In LabyMod this
            // was driven by ASM render hooks; here we re-scan periodically (the tracker dedups).
            long now0 = System.currentTimeMillis();
            if (labyConnect.isOnline() && now0 >= nextTrackScan) {
                nextTrackScan = now0 + 2000L;
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
                if (mc.theWorld != null) {
                    for (net.minecraft.entity.player.EntityPlayer p :
                            new java.util.ArrayList<net.minecraft.entity.player.EntityPlayer>(mc.theWorld.playerEntities)) {
                        try {
                            labyConnect.getTracker().onVisiblePlayer(p.getUniqueID());
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }

            labyConnect.getTracker().onGameTick();
            long now = System.currentTimeMillis();
            if (now >= nextPresenceUpdate) {
                nextPresenceUpdate = now + 5000L;
                if (labyConnect.isOnline()) {
                    // Refresh "playing on <server>" presence from the current Minecraft server.
                    labyConnect.updatePlayingOnServerState(
                        labyConnect.getLastPacketPlayServerStatus() == null
                            ? ""
                            : labyConnect.getLastPacketPlayServerStatus().getGamemode());
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
