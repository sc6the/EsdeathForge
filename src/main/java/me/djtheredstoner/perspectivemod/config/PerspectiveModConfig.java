package me.djtheredstoner.perspectivemod.config;

import me.djtheredstoner.perspectivemod.gui.PerspectiveModGui;
import net.minecraft.client.gui.GuiScreen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PerspectiveModConfig {

    private final File file = new File("./config/meowmeow.properties");

    public boolean modEnabled = true;
    public boolean holdMode = true;
    public boolean invertPitch = false;

    public PerspectiveModConfig() {
        load();
    }

    /**
     * Kept for compatibility with the previous Vigilance-based config; simply
     * (re)loads the config from disk.
     */
    public void preload() {
        load();
    }

    public void load() {
        if (!file.exists()) {
            save();
            return;
        }

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        modEnabled = Boolean.parseBoolean(props.getProperty("modEnabled", String.valueOf(modEnabled)));
        holdMode = Boolean.parseBoolean(props.getProperty("holdMode", String.valueOf(holdMode)));
        invertPitch = Boolean.parseBoolean(props.getProperty("invertPitch", String.valueOf(invertPitch)));
    }

    public void save() {
        Properties props = new Properties();
        props.setProperty("modEnabled", String.valueOf(modEnabled));
        props.setProperty("holdMode", String.valueOf(holdMode));
        props.setProperty("invertPitch", String.valueOf(invertPitch));

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileOutputStream out = new FileOutputStream(file)) {
            props.store(out, "MeowMeow config");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GuiScreen gui() {
        return new PerspectiveModGui(this);
    }
}
