package net.labymod.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;
import java.util.UUID;

/**
 * Bridges between obfuscated LabyMod source and our Forge MCP environment.
 *
 * The voice code calls:
 *   LabyModCore.getMinecraft().getWorld().b(uuid)   // -> EntityPlayer by UUID
 *   LabyModCore.getMinecraft().getWorld().j         // -> List<EntityPlayer> playerEntities
 *
 * We expose those names as a thin facade over WorldClient.
 */
public final class LabyModCore {

    private static final MinecraftAdapter MC = new MinecraftAdapter();

    private LabyModCore() {}

    public static MinecraftAdapter getMinecraft() {
        return MC;
    }

    public static final class MinecraftAdapter {
        public WorldAdapter getWorld() {
            WorldClient world = Minecraft.getMinecraft().theWorld;
            return world == null ? WorldAdapter.EMPTY : new WorldAdapter(world);
        }
        public EntityPlayerSP getPlayer() {
            return Minecraft.getMinecraft().thePlayer;
        }
    }

    public static final class WorldAdapter {
        static final WorldAdapter EMPTY = new WorldAdapter(null);

        public final List<EntityPlayer> j;
        private final WorldClient world;

        WorldAdapter(WorldClient world) {
            this.world = world;
            this.j = world == null ? java.util.Collections.<EntityPlayer>emptyList() : world.playerEntities;
        }

        /** Notch-named getPlayerEntityByUUID(UUID). */
        public EntityPlayer b(UUID uuid) {
            if (world == null) return null;
            return world.getPlayerEntityByUUID(uuid);
        }
    }
}
