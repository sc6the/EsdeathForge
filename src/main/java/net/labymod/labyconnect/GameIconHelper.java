package net.labymod.labyconnect;

/**
 * Phase 1 stub. The original updated the OS taskbar/dock icon with an unread-message badge,
 * using an obfuscated OS-detection util and bundled LabyMod icon PNGs (assets/minecraft/labymod/...).
 * Those assets aren't shipped with EsdeathForge, so the badge is disabled for now. Restore by
 * bundling the icon PNGs and re-porting provideIconBuffer/addUnreadMessageNumber against
 * net.labymod.utils.OSUtil + org.lwjgl.opengl.Display.setIcon.
 */
public class GameIconHelper {

    public static void updateIcon(boolean force, boolean increase) {
        // no-op: taskbar unread-message badge not yet wired in EsdeathForge
    }
}
