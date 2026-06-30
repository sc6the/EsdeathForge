package net.labymod.user.sticker;

import net.labymod.user.User;

/**
 * Phase 1 stub. Stickers (the floating bubble emotes above a player's head) are a later
 * port; LabyConnect's PacketActionBroadcast(STICKER) calls handleSticker for now we drop it.
 */
public class StickerRegistry {

    public void handleSticker(User user, short id) {
    }

    public short bytesToShort(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return 0;
        }
        return (short) ((bytes[0] & 0xFF) | (bytes[1] & 0xFF) << 8);
    }
}
