package net.labymod.main.lang;

/**
 * Stub. The original LanguageManager loaded LabyMod's translation bundles.
 * We just return the key — VoiceChat only uses this for time-unit pluralization
 * ("time_seconds" etc.) and falling back to the key reads fine in English.
 */
public final class LanguageManager {
    private LanguageManager() {}

    public static String translate(String key) {
        if (key == null) return "";
        if (key.startsWith("time_")) {
            return key.substring("time_".length());
        }
        return key;
    }

    public static String translate(String key, Object... args) {
        return translate(key);
    }
}
