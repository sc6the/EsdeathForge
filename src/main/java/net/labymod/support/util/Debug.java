package net.labymod.support.util;

public final class Debug {

    public enum EnumDebugMode {
        ADDON, NETWORK, PROTOCOL, AUDIO, GENERAL
    }

    private static final boolean ACTIVE = Boolean.getBoolean("mergedvoicechat.debug");

    private Debug() {}

    public static boolean isActive() { return ACTIVE; }

    public static void log(EnumDebugMode mode, String message) {
        if (ACTIVE) {
            System.out.println("[MergedVoiceChat][" + mode.name() + "] " + message);
        }
    }
}
