package net.labymod.user;

import com.mojang.authlib.GameProfile;
import net.labymod.user.group.LabyGroup;

import java.util.UUID;

/**
 * Minimal compat {@code User} for the ported LabyConnect code. The original tracked
 * cosmetics, familiars, badges and per-user textures; LabyConnect (Phase 1) only needs
 * identity + familiar/group flags. Cosmetic wiring is bridged to EsdeathForge separately.
 */
public class User {

    private final UUID uuid;
    private GameProfile gameProfile;
    private boolean familiar;
    private LabyGroup group;

    public User(UUID uuid) {
        this.uuid = uuid;
        this.gameProfile = new GameProfile(uuid, null);
    }

    public UUID getUuid() {
        return uuid;
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    public long getMostSignificantBits() {
        return uuid == null ? 0L : uuid.getMostSignificantBits();
    }

    public long getLeastSignificantBits() {
        return uuid == null ? 0L : uuid.getLeastSignificantBits();
    }

    public boolean isFamiliar() {
        return familiar;
    }

    public void setFamiliar(boolean familiar) {
        this.familiar = familiar;
    }

    public LabyGroup getGroup() {
        return group;
    }

    public void setGroup(LabyGroup group) {
        this.group = group;
    }

    /** Phase 4: unload per-user cosmetic textures. No-op for now. */
    public void unloadCosmeticTextures() {
    }
}
