package net.labymod.main;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

import java.awt.Desktop;
import java.net.URI;
import java.util.UUID;

/**
 * Stub of LabyMod's main class. Only exposes what VoiceChat actually calls.
 *
 * Most methods delegate to vanilla Minecraft. Premium check is hard-coded to true
 * (we trust whatever Mojang session the user is logged in with).
 */
public final class LabyMod {

    private static final LabyMod INSTANCE = new LabyMod();

    private final PinManager pinManager = new PinManager();
    private final LabyModAPIStub api = new LabyModAPIStub();
    private final DrawUtilsStub drawUtils = new DrawUtilsStub();

    private LabyMod() {}

    public static LabyMod getInstance() {
        return INSTANCE;
    }

    public UUID getPlayerUUID() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.getSession() == null || mc.getSession().getProfile() == null) return null;
        return mc.getSession().getProfile().getId();
    }

    public String getPlayerName() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.getSession() == null) return null;
        return mc.getSession().getUsername();
    }

    public boolean isInGame() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc != null && mc.thePlayer != null && mc.theWorld != null;
    }

    public boolean isPremium() {
        // We don't have access to LabyMod's premium flag. Trust the Mojang session;
        // if the session is unauthenticated, the joinServer hash flow will fail anyway
        // and the voice client will surface the error.
        return true;
    }

    public boolean isHasLeftHand() {
        return false;
    }

    public void displayMessageInChat(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(message));
        } else {
            System.out.println("[VoiceChat] " + message);
        }
    }

    public boolean openWebpage(String urlString, boolean request) {
        try {
            Desktop.getDesktop().browse(URI.create(urlString));
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public PinManager getPinManager() {
        return pinManager;
    }

    public LabyModAPIStub getLabyModAPI() {
        return api;
    }

    public DrawUtilsStub getDrawUtils() {
        return drawUtils;
    }

    /** Always reports no LabyConnect PIN — forces Mojang authentication path. */
    public static final class PinManager {
        public boolean hasValidPin(UUID uuid) { return false; }
        public String getValidPinOf(UUID uuid) { return null; }
    }

    /**
     * Stub of LabyModAPI. Currently the only method VoiceChat calls is
     * sendJsonMessageToServer for the LMC plugin channel. We log and drop until
     * the Forge plugin-channel implementation is wired up — see IMPLEMENTATION_PLAN.md.
     */
    public static final class LabyModAPIStub {
        public void sendJsonMessageToServer(String key, com.google.gson.JsonObject object) {
            try {
                dev.mergedvoicechat.MergedVoiceChat.sendLmcJson(key, object.toString());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        public void displayMessageInChat(String message) {
            getInstance().displayMessageInChat(message);
        }
    }

    /**
     * Stub of DrawUtils. Just enough to not NPE.
     * Real drawing during the in-game indicator is TODO -- bind to vanilla Gui.drawString.
     */
    public static final class DrawUtilsStub {
        public int getWidth() {
            net.minecraft.client.gui.ScaledResolution sr =
                new net.minecraft.client.gui.ScaledResolution(Minecraft.getMinecraft());
            return sr.getScaledWidth();
        }
        public int getHeight() {
            net.minecraft.client.gui.ScaledResolution sr =
                new net.minecraft.client.gui.ScaledResolution(Minecraft.getMinecraft());
            return sr.getScaledHeight();
        }
        public void drawString(String text, double x, double y) {
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, (float) x, (float) y, 0xFFFFFFFF);
        }
        public void drawCenteredString(String text, double x, double y) {
            int w = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
            drawString(text, x - w / 2.0, y);
        }
        public void drawRightString(String text, double x, double y) {
            int w = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
            drawString(text, x - w, y);
        }
        public void drawTexture(double x, double y, double u, double v, double w, double h) {
            // TODO: port texture-quad rendering
        }
    }
}
