package com.example.containerstrafe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;

@Mod(modid = ContainerStrafeForge.MODID, name = ContainerStrafeForge.NAME, version = ContainerStrafeForge.VERSION, clientSideOnly = true)
public class ContainerStrafeForge {

    public static final String MODID = "containerstrafe";
    public static final String NAME = "Container Strafe";
    public static final String VERSION = "1.0";

    public static ContainerStrafeForge INSTANCE;

    // Config state
    public boolean enabled = true;
    public boolean disableInCreativeInventorySearch = true;
    public boolean left = true;
    public boolean right = true;
    public boolean backward = true;
    public boolean forward = true;

    private Configuration config;

    // Per-key "ignore until released" flag so we don't instantly close a GUI
    // that the user opened while already holding the movement key.
    private boolean ignoreLeft, ignoreRight, ignoreBackward, ignoreForward;

    private Field selectedTabField;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        INSTANCE = this;
        config = new Configuration(event.getSuggestedConfigurationFile());
        loadConfig();

        try {
            // SRG-obfuscated field name for GuiContainerCreative.selectedTabIndex
            // stable_22 mappings: field_147058_w
            try {
                selectedTabField = GuiContainerCreative.class.getDeclaredField("selectedTabIndex");
            } catch (NoSuchFieldException e) {
                selectedTabField = GuiContainerCreative.class.getDeclaredField("field_147058_w");
            }
            selectedTabField.setAccessible(true);
        } catch (Throwable t) {
            selectedTabField = null;
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new ContainerStrafeCommand());
    }

    public void loadConfig() {
        config.load();
        enabled = config.getBoolean("enabled", "general", true, "Master toggle");
        disableInCreativeInventorySearch = config.getBoolean("disableInCreativeInventorySearch", "general", true, "Disable while Creative inventory search tab is open");
        left = config.getBoolean("left", "keys", true, "Close on strafe left (A)");
        right = config.getBoolean("right", "keys", true, "Close on strafe right (D)");
        backward = config.getBoolean("backward", "keys", true, "Close on walk back (S)");
        forward = config.getBoolean("forward", "keys", true, "Close on walk forward (W)");
        if (config.hasChanged()) config.save();
    }

    public void saveConfig() {
        config.get("general", "enabled", true).set(enabled);
        config.get("general", "disableInCreativeInventorySearch", true).set(disableInCreativeInventorySearch);
        config.get("keys", "left", true).set(left);
        config.get("keys", "right", true).set(right);
        config.get("keys", "backward", true).set(backward);
        config.get("keys", "forward", true).set(forward);
        config.save();
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen screen = event.gui;
        if (!(screen instanceof GuiContainer)) return;

        GameSettings gs = Minecraft.getMinecraft().gameSettings;
        ignoreLeft = isKeyDown(gs.keyBindLeft);
        ignoreRight = isKeyDown(gs.keyBindRight);
        ignoreBackward = isKeyDown(gs.keyBindBack);
        ignoreForward = isKeyDown(gs.keyBindForward);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!enabled) return;

        Minecraft mc = Minecraft.getMinecraft();
        GuiScreen screen = mc.currentScreen;
        if (!(screen instanceof GuiContainer)) return;

        // Creative search tab exemption
        if (disableInCreativeInventorySearch && screen instanceof GuiContainerCreative && selectedTabField != null) {
            try {
                int tab = selectedTabField.getInt(null);
                if (tab == CreativeTabs.tabAllSearch.getTabIndex()) return;
            } catch (IllegalAccessException ignored) {}
        }

        GameSettings gs = mc.gameSettings;

        if (checkKey(gs.keyBindLeft, left, ignoreLeft)) { close(mc); return; }
        if (checkKey(gs.keyBindRight, right, ignoreRight)) { close(mc); return; }
        if (checkKey(gs.keyBindBack, backward, ignoreBackward)) { close(mc); return; }
        if (checkKey(gs.keyBindForward, forward, ignoreForward)) { close(mc); return; }

        // Clear ignore flags once keys released
        if (ignoreLeft && !isKeyDown(gs.keyBindLeft)) ignoreLeft = false;
        if (ignoreRight && !isKeyDown(gs.keyBindRight)) ignoreRight = false;
        if (ignoreBackward && !isKeyDown(gs.keyBindBack)) ignoreBackward = false;
        if (ignoreForward && !isKeyDown(gs.keyBindForward)) ignoreForward = false;
    }

    private boolean checkKey(KeyBinding kb, boolean enabledSetting, boolean ignore) {
        if (!enabledSetting) return false;
        if (ignore) return false;
        return isKeyDown(kb);
    }

    private static boolean isKeyDown(KeyBinding kb) {
        int code = kb.getKeyCode();
        if (code == 0) return false;
        try {
            return Keyboard.isKeyDown(code);
        } catch (Throwable t) {
            return false;
        }
    }

    private static void close(Minecraft mc) {
        try {
            // EntityPlayerSP.closeScreen() sends C0DPacketCloseWindow with the current openContainer's
            // windowId and resets openContainer to the inventory. Calling displayGuiScreen(null)
            // alone does NOT send that packet — vanilla relies on ESC/E going through closeScreen()
            // first. Skipping the packet leaves the server thinking the chest is still open, which
            // on Hypixel manifests as the next compass right-click being silently rejected until
            // the user opens + closes the vanilla inventory (which routes through closeScreen()
            // and re-syncs the server's window state).
            if (mc.thePlayer != null) {
                mc.thePlayer.closeScreen();
            }
            mc.displayGuiScreen(null);
        } catch (Throwable ignored) {}
    }
}
