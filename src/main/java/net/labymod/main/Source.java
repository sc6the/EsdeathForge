package net.labymod.main;

import net.minecraft.client.Minecraft;

import java.io.File;

public final class Source {
    public static final String ABOUT_MC_VERSION = "1.8.9";
    public static final int ABOUT_MC_PROTOCOL_VERSION = 47;
    public static final String VERSION = "merged-3.9.62";

    /** Data folder for ported LabyConnect state (chat logs, dashboard pins). */
    public static final File FILE_LABYMOD_FOLDER =
        new File(Minecraft.getMinecraft().mcDataDir, "EsdeathClient/labyconnect");
    public static final File FILE_CHATLOG = new File(FILE_LABYMOD_FOLDER, "chatlog");
    public static final File FILE_PINS = new File(FILE_LABYMOD_FOLDER, "pins.json");

    static {
        try {
            FILE_CHATLOG.mkdirs();
        } catch (Throwable ignored) {
        }
    }

    private Source() {}
}
