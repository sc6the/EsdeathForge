package dev.mergedvoicechat;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

/**
 * Registers Forge KeyBindings (so users can rebind from the vanilla controls menu)
 * and exposes them to the rest of the mod.
 *
 * VoiceChat's tick loop still reads the int key code in {@code voiceChat.keyPushToTalk}
 * etc. directly via {@code Keyboard.isKeyDown}; we just sync those ints from the
 * KeyBinding values on each tick (see {@link MergedVoiceChat#onTick}).
 */
public final class Keybinds {

    public static final String CATEGORY = "Merged VoiceChat";

    public static final KeyBinding PUSH_TO_TALK     = new KeyBinding("Push To Talk",       Keyboard.KEY_V,     CATEGORY);
    public static final KeyBinding TOGGLE_VOICE     = new KeyBinding("Toggle VoiceChat",   Keyboard.KEY_NONE,  CATEGORY);
    public static final KeyBinding TOGGLE_PERMAVOICE= new KeyBinding("Toggle PermaVoice",  Keyboard.KEY_NONE,  CATEGORY);
    public static final KeyBinding OPEN_SETTINGS    = new KeyBinding("Open VoiceChat Settings", Keyboard.KEY_NONE, CATEGORY);
    public static final KeyBinding OPEN_VOLUME_GUI  = new KeyBinding("Open Volume GUI for Looked-At Player", Keyboard.KEY_NONE, CATEGORY);

    private Keybinds() {}

    public static void register() {
        ClientRegistry.registerKeyBinding(PUSH_TO_TALK);
        ClientRegistry.registerKeyBinding(TOGGLE_VOICE);
        ClientRegistry.registerKeyBinding(TOGGLE_PERMAVOICE);
        ClientRegistry.registerKeyBinding(OPEN_SETTINGS);
        ClientRegistry.registerKeyBinding(OPEN_VOLUME_GUI);
    }
}
