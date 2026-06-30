package net.labymod.main.lang;

/**
 * Stub. The original LanguageManager loaded LabyMod's translation bundles.
 * We just return the key — VoiceChat only uses this for time-unit pluralization
 * ("time_seconds" etc.) and falling back to the key reads fine in English.
 */
public final class LanguageManager {
    private LanguageManager() {}

    // Readable English for the LabyConnect chat keys (the real translation bundles aren't shipped, so
    // without these the raw keys like "chat_user_now_online" leaked into chat). %s is filled by args.
    private static final java.util.Map<String, String> EN = new java.util.HashMap<String, String>();
    static {
        EN.put("chat_user_now_online", "is now online");
        EN.put("chat_user_now_offline", "is now offline");
        EN.put("chat_user_friend_request", "sent you a friend request");
        EN.put("chat_user_now_playing", "is now playing");
        EN.put("chat_user_now_playing_on", "is now playing on %s");
        EN.put("chat_disconnected_title", "Disconnected");
        EN.put("chat_server_message_title", "Server");
        EN.put("chat_connected", "Connected to LabyConnect");
        EN.put("chat_connecting", "Connecting…");
    }

    public static String translate(String key) {
        if (key == null) return "";
        if (key.startsWith("time_")) {
            return key.substring("time_".length());
        }
        String v = EN.get(key);
        return v != null ? v : key;
    }

    public static String translate(String key, Object... args) {
        String base = translate(key);
        if (args != null && args.length > 0 && base.contains("%s")) {
            try {
                return String.format(base, args);
            } catch (Throwable ignored) {
            }
        }
        return base;
    }
}
