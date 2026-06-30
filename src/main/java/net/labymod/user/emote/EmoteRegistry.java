package net.labymod.user.emote;

import java.util.UUID;

/**
 * Phase 1 stub. Phase 3 replaces this with the real ported emote engine
 * (loader + keyframe poses + wheel). LabyConnect's PacketActionBroadcast(EMOTE) and
 * PacketActionPlayResponse call handleEmote when another player emotes; for now we drop it.
 */
public class EmoteRegistry {

    public EmoteRenderer handleEmote(UUID player, short emoteId) {
        return null;
    }

    public EmoteRenderer handleEmote(UUID player, byte[] data) {
        return null;
    }

    public short bytesToShort(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return 0;
        }
        return (short) ((bytes[0] & 0xFF) | (bytes[1] & 0xFF) << 8);
    }
}
