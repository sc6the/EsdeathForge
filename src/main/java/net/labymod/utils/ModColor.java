package net.labymod.utils;

public final class ModColor {
    public static final char COLOR_CHAR = '§';

    private ModColor() {}

    public static String cl(char c) { return String.valueOf(COLOR_CHAR) + c; }

    public static String cl(String c) {
        if (c == null || c.isEmpty()) return "";
        return COLOR_CHAR + c.substring(0, 1);
    }

    public static String createColors(String text) {
        if (text == null) return null;
        return text.replace('&', COLOR_CHAR);
    }

    public static String stripColor(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == COLOR_CHAR && i + 1 < input.length()) i++;
            else sb.append(c);
        }
        return sb.toString();
    }
}
