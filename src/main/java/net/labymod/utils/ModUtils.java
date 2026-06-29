package net.labymod.utils;

public final class ModUtils {
    private ModUtils() {}

    public static String parseTimer(int seconds) {
        if (seconds < 0) seconds = 0;
        int min = seconds / 60;
        int sec = seconds % 60;
        if (min > 0) return min + "m " + sec + "s";
        return sec + "s";
    }

    public static String parseTimer(long milliseconds) {
        return parseTimer((int) (milliseconds / 1000L));
    }
}
