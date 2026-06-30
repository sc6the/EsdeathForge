package net.labymod.main;

import com.mojang.authlib.GameProfile;
import net.labymod.api.EventManager;
import net.labymod.labyconnect.LabyConnect;
import net.labymod.labyconnect.PinManager;
import net.labymod.labyplay.LabyPlay;
import net.labymod.main.update.Updater;
import net.labymod.user.UserManager;
import net.labymod.user.emote.EmoteRegistry;
import net.labymod.user.sticker.StickerRegistry;
import net.labymod.utils.ModColor;
import net.labymod.utils.ServerData;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;

import java.awt.Desktop;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Compatibility facade for LabyMod's main class. Originally the {@code @Mod} singleton that
 * wired every subsystem; here it's a lightweight singleton exposing only what the ported
 * LabyConnect / LabyChat / VoiceChat code calls. Most methods delegate to vanilla Minecraft
 * or to the minimal compat managers in this source tree.
 */
public final class LabyMod {

    private static final LabyMod INSTANCE = new LabyMod();
    private static final ModSettings SETTINGS = new ModSettings();

    private final PinManager pinManager = PinManager.load();
    private final EventManager eventManager = new EventManager();
    private final UserManager userManager = new UserManager();
    private final Updater updater = new Updater();
    private final EmoteRegistry emoteRegistry = new EmoteRegistry();
    private final StickerRegistry stickerRegistry = new StickerRegistry();
    private final LabyPlay labyPlay = new LabyPlay();
    private final OverlayRenderer overlayRenderer = new OverlayRenderer();
    private final LabyModAPIStub api = new LabyModAPIStub();
    private final DrawUtilsStub drawUtils = new DrawUtilsStub();

    private LabyConnect labyConnect;

    private LabyMod() {}

    public static LabyMod getInstance() {
        return INSTANCE;
    }

    public static ModSettings getSettings() {
        return SETTINGS;
    }

    // ---- identity / state (delegates to vanilla) ----

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
        // Trust the Mojang session; the joinServer auth flow will fail loudly otherwise.
        return true;
    }

    public boolean isHasLeftHand() {
        return false;
    }

    /** Current Minecraft server, wrapped for LabyConnect presence. Null when not on a server. */
    public ServerData getCurrentServerData() {
        net.minecraft.client.multiplayer.ServerData sd = Minecraft.getMinecraft().getCurrentServerData();
        if (sd == null || sd.serverIP == null || sd.serverIP.isEmpty()) return null;
        String ip = sd.serverIP;
        int port = 25565;
        int colon = ip.indexOf(':');
        if (colon != -1) {
            try {
                port = Integer.parseInt(ip.substring(colon + 1).trim());
            } catch (NumberFormatException ignored) {
            }
            ip = ip.substring(0, colon);
        }
        return new ServerData(ip, port, sd.serverName);
    }

    /** Switch the player to another Minecraft server (used by some LabyConnect actions). */
    public void connectToServer(String address) {
        // Phase 1: minimal — only handled when EsdeathForge wires real server switching.
        // A null address means "return to the main menu"; we leave the current screen as-is.
    }

    // ---- subsystems ----

    public PinManager getPinManager() {
        return pinManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public Updater getUpdater() {
        return updater;
    }

    public EmoteRegistry getEmoteRegistry() {
        return emoteRegistry;
    }

    public StickerRegistry getStickerRegistry() {
        return stickerRegistry;
    }

    public LabyPlay getLabyPlay() {
        return labyPlay;
    }

    public OverlayRenderer getPriorityOverlayRenderer() {
        return overlayRenderer;
    }

    public LabyConnect getLabyConnect() {
        return labyConnect;
    }

    public void setLabyConnect(LabyConnect labyConnect) {
        this.labyConnect = labyConnect;
    }

    public LabyModAPIStub getLabyModAPI() {
        return api;
    }

    public DrawUtilsStub getDrawUtils() {
        return drawUtils;
    }

    // ---- chat / notifications ----

    public void displayMessageInChat(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(message));
        } else {
            System.out.println("[LabyConnect] " + message);
        }
    }

    public void notifyMessageProfile(GameProfile gameProfile, String message) {
        displayMessageInChat(ModColor.cl("7") + (gameProfile == null ? "?" : gameProfile.getName())
            + ModColor.cl("f") + ": " + message);
    }

    // Esdeath-styled friend status alert: "[Friends] <name> is now online" with a coloured prefix.
    public void notifyFriendStatus(GameProfile gameProfile, boolean online) {
        String name = gameProfile == null ? "?" : gameProfile.getName();
        String prefix = ModColor.cl("8") + "[" + ModColor.cl("d") + "Friends" + ModColor.cl("8") + "] ";
        displayMessageInChat(prefix + ModColor.cl("f") + name + " "
            + (online ? ModColor.cl("a") + "is now online" : ModColor.cl("7") + "is now offline"));
    }

    public void notifyMessageRaw(String title, String message) {
        displayMessageInChat(ModColor.cl("7") + title + ModColor.cl("f") + ": " + message);
    }

    public boolean openWebpage(String urlString, boolean request) {
        try {
            Desktop.getDesktop().browse(URI.create(urlString));
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /** Stub of the overlay renderer; only the language-flag map LabyConnect writes to is exposed. */
    public static final class OverlayRenderer {
        private final Map<UUID, ResourceLocation> languageFlags = new ConcurrentHashMap<UUID, ResourceLocation>();

        public Map<UUID, ResourceLocation> getLanguageFlags() {
            return languageFlags;
        }
    }

    /**
     * Stub of LabyModAPI. VoiceChat calls sendJsonMessageToServer for the LMC plugin channel.
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

    /** Stub of DrawUtils. Just enough to not NPE. */
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
        }
    }
}
