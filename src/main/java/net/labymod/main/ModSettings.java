package net.labymod.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Compatibility settings holder for the ported LabyConnect / LabyChat code.
 *
 * The original LabyMod {@code ModSettings} had hundreds of fields driven by the
 * AutoConfig-style settings GUI. LabyConnect only reads the handful below, so we
 * keep a tiny POJO with sane defaults.
 *
 * These are persisted to {@code EsdeathClient/labyconnect/settings.json} so the
 * user's chosen online status (and other LabyChat prefs) survive a restart and are
 * sent to the server on the next login. {@link #save()} is called whenever a value
 * is changed from the GUI; {@link #load()} runs once at construction.
 */
public class ModSettings {
    /** EnumAlertDisplayType name: CHAT or ACHIEVEMENT. */
    public String alertDisplayType = "ACHIEVEMENT";
    public boolean alertPlaySounds = true;
    public boolean alertsOnlineStatus = true;
    public boolean alertsPlayingOn = true;
    /** 0 = last online, 1 = online only, 2 = last interaction. */
    public int friendSortType = 0;
    public boolean ignoreRequests = false;
    public String motd = "";
    /** UserStatus id (see net.labymod.labyconnect.user.UserStatus). */
    public int onlineStatus = 0;
    public boolean sendAnonymousStatistics = false;
    public boolean showConnectedIp = true;
    public boolean unreadMessageIcon = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    // Guards against re-entrant load() while Gson constructs the deserialized instance
    // (Gson invokes this no-arg constructor via reflection).
    private static boolean LOADING = false;

    public ModSettings() {
        if (!LOADING) {
            load();
        }
    }

    private static File file() {
        return new File(Source.FILE_LABYMOD_FOLDER, "settings.json");
    }

    public void load() {
        try {
            File f = file();
            if (!f.exists()) {
                return;
            }
            ModSettings loaded;
            LOADING = true;
            try (FileReader r = new FileReader(f)) {
                loaded = GSON.fromJson(r, ModSettings.class);
            } finally {
                LOADING = false;
            }
            if (loaded != null) {
                this.alertDisplayType = loaded.alertDisplayType;
                this.alertPlaySounds = loaded.alertPlaySounds;
                this.alertsOnlineStatus = loaded.alertsOnlineStatus;
                this.alertsPlayingOn = loaded.alertsPlayingOn;
                this.friendSortType = loaded.friendSortType;
                this.ignoreRequests = loaded.ignoreRequests;
                this.motd = loaded.motd == null ? "" : loaded.motd;
                this.onlineStatus = loaded.onlineStatus;
                this.sendAnonymousStatistics = loaded.sendAnonymousStatistics;
                this.showConnectedIp = loaded.showConnectedIp;
                this.unreadMessageIcon = loaded.unreadMessageIcon;
            }
        } catch (Throwable ignored) {
            LOADING = false;
        }
    }

    public void save() {
        try {
            Source.FILE_LABYMOD_FOLDER.mkdirs();
            try (FileWriter w = new FileWriter(file())) {
                GSON.toJson(this, w);
            }
        } catch (Throwable ignored) {
        }
    }
}
