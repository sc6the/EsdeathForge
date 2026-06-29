package net.labymod.utils;

public final class OSUtil {
    private static final String OS = System.getProperty("os.name", "").toLowerCase();
    private OSUtil() {}
    public static boolean isWindows() { return OS.contains("win"); }
    public static boolean isMac() { return OS.contains("mac") || OS.contains("darwin"); }
    public static boolean isUnix() { return OS.contains("nix") || OS.contains("nux") || OS.contains("aix"); }
    public static String getName() { return System.getProperty("os.name", "Unknown"); }
}
