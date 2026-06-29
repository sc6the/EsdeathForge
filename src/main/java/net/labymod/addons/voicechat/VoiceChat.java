package net.labymod.addons.voicechat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.labymod.addons.voicechat.audio.mic.Microphone;
import net.labymod.addons.voicechat.audio.opus.OpusCodecManager;
import net.labymod.addons.voicechat.audio.surround.SurroundManager;
import net.labymod.addons.voicechat.client.DefaultVoiceClientListener;
import net.labymod.addons.voicechat.client.Mute;
import net.labymod.addons.voicechat.listener.ServerIncomingPacketListener;
import net.labymod.main.LabyMod;
import net.labymod.utils.ModColor;
import net.labymod.utils.ModUtils;
import net.labymod.utils.OSUtil;
import net.labymod.voice.client.VoiceClient;
import net.labymod.voice.client.auth.AuthenticationResponse;
import net.labymod.voice.protocol.type.AuthenticationMethod;
import net.labymod.addons.voicechat.audio.opus.EnumOpusError;
import net.labymod.addons.voicechat.client.Mute;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

/**
 * Standalone VoiceChat — port of net.labymod.addons.voicechat.VoiceChat with the
 * LabyMod settings UI / Module / event-bus glue removed. PermaVoice and LouderVoiceChat
 * features are baked in directly:
 *
 *   - PermaVoice    : permaVoiceActive flag forces pushToTalkPressed=true on every tick.
 *                     permaVoiceKey toggles permaVoiceActive on/off (with optional chat msg).
 *   - LouderVoiceChat: surroundVolume range raised to 0..500. playerVolumes per-UUID range
 *                      stays 0..500. Use the right-click user menu (TODO) or the volume key
 *                      to open a per-player slider.
 *
 * Things still TODO before this is a working voice client:
 *   - Settings GUI rewrite (vanilla GuiScreen) — see fillSettings TODO below.
 *   - JSON config persistence (loadConfig/saveConfig) — currently in-memory defaults only.
 *   - In-game render hint overlay — see drawNotification stubs.
 *   - LMC plugin-channel send (sendSettingsToServer) — see LabyMod.LabyModAPIStub TODO.
 *   - Listener registration — currently only the ones registered manually in init().
 *   - Voice rules acceptance flow — set rulesAccepted=true to bypass.
 *   - Microphone testing UI button.
 */
public class VoiceChat {

    private static final String TEXTURE_PATH = "mergedvoicechat:textures/voicechat/";
    public static final ResourceLocation TEXTURE_SPEAKER_OFF = new ResourceLocation("mergedvoicechat", "textures/voicechat/speaker_off.png");
    public static final ResourceLocation TEXTURE_SPEAKER_ON = new ResourceLocation("mergedvoicechat", "textures/voicechat/speaker_on.png");
    public static final ResourceLocation TEXTURE_SPEAKER_MUTED = new ResourceLocation("mergedvoicechat", "textures/voicechat/speaker_muted.png");
    public static final ResourceLocation TEXTURE_SPEAKER_PROTECTED = new ResourceLocation("mergedvoicechat", "textures/voicechat/speaker_protected.png");

    public static final boolean RUNNING_ON_MAC = System.getProperty("os.name", "").toLowerCase().contains("mac");
    public static final int MICROPHONE_VOLUME_RANGE = 40;
    public static final int SURROUND_VOLUME_RANGE = 500;          // LouderVoiceChat: was 40
    public static final int SURROUND_RANGE = 18;
    public static final int MIN_RECONNECT_DELAY_SECONDS = 45;
    public static final int MAX_RECONNECT_DELAY_SECONDS = 120;
    public static final int LOUDER_VOLUME_MAX = 500;              // LouderVoiceChat per-player cap

    private final VoiceClient voiceClient = new VoiceClient();
    private final DefaultVoiceClientListener voiceClientListener = new DefaultVoiceClientListener(this);
    private boolean loginCompleted = false;
    private String lastConnectionStatus = "VoiceChat is not ingame";
    private boolean kicked;
    private long lastConnectionHandling = -1L;
    private long nextConnectionHandling = -1L;
    private final Gson gson = new Gson();
    private Microphone microphone;
    private SurroundManager surroundManager;
    public boolean testingMicrophone;
    public boolean pushToTalkPressed;                              // public so PermaVoice can set it directly
    private ServerIncomingPacketListener serverIncomingPacketListener;
    private boolean allowed = true;
    private boolean resetOnServerSwitch = true;

    // Settings (loaded with hardcoded defaults; replace with JSON persistence)
    public boolean enabled = true;
    public int keyToggleVoiceChat = -1;
    public int keyPushToTalk = -1;
    public int microphoneVolume = 10;
    public String selectedMicrophone = null;
    public int surroundRange = 10;
    public int surroundVolume = 10;
    public int keyOpenMutedPlayers = -1;
    public Map<UUID, Integer> playerVolumes = new HashMap<>();
    public boolean screamerProtection = true;
    public int maxSwapRate = 40;
    public int compressorTarget = 15;
    public boolean externalOpusService = !RUNNING_ON_MAC;
    public boolean crashFix = false;
    public boolean rulesAccepted = true;                            // bypass — TODO: real rules flow

    // Voice activity (auto-detect speaking)
    public boolean voiceActivity;
    public int lastRMSLevel;
    public int voiceActivityActivationLevel = 1000;
    public boolean voiceActivitySuppressed = false;
    public long lastVoiceActivityActivation = -1L;

    // PermaVoice integration
    public boolean permaVoiceEnabled = true;
    public boolean permaVoiceActive = false;
    public boolean permaVoiceChatMessages = true;
    public int permaVoiceKey = -1;
    private boolean permaVoiceTogglePressed = false;

    // Speaker HUD (draggable)
    public boolean hudEnabled = true;
    public boolean hudBackground = true;
    public int hudX = 5;
    public int hudY = 5;
    public int hudScale = 100; // percent, 50..300
    public int hudOpacity = 100; // HUD background opacity %, 0..100

    // NoNicknames override: when true, force nonicknames mod's BotUtil.isBot -> true so all nametags show.
    public boolean showAllNameplates = false;

    // Player-model outline effect (replaces the old floating speaker icon).
    // speakingGlow: green glow outline + low-opacity fill while a player is talking.
    // idleOutline: faint gray outline on voice participants while they are not talking.
    public boolean speakingGlow = true;
    public boolean idleOutline = false;
    public int glowOpacity = 100; // speaking glow/outline opacity %, 10..100
    public int idleOpacity = 100; // idle gray outline opacity %, 10..100

    private boolean togglePressed;
    public boolean cleanup = true;
    private UUID connectedAsUUID = null;
    public UUID focusedUser = null;
    public List<UUID> serverMutes = new ArrayList<>();
    public JsonObject prevSettings = null;
    public boolean serverSettingsMatch = false;
    private final OpusCodecManager opusCodecManager = new OpusCodecManager();

    public void init() {
        this.voiceClient.setListener(this.voiceClientListener);
        this.voiceClient.setAuthenticator(AuthenticationMethod.MOJANG, hash -> {
            try {
                Session session = Minecraft.getMinecraft().getSession();
                MinecraftSessionService minecraftSessionService = new YggdrasilAuthenticationService(
                    Proxy.NO_PROXY, UUID.randomUUID().toString()).createMinecraftSessionService();
                minecraftSessionService.joinServer(session.getProfile(), session.getToken(), hash);
                return AuthenticationResponse.createMojang(session.getUsername());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        this.voiceClient.setAuthenticator(AuthenticationMethod.LABYCONNECT, hash -> {
            UUID uuid = LabyMod.getInstance().getPlayerUUID();
            String pin = LabyMod.getInstance().getPinManager().getValidPinOf(uuid);
            return uuid == null ? null : AuthenticationResponse.createLabyConnect(pin, uuid);
        });
        this.surroundManager = new SurroundManager(this, Minecraft.getMinecraft().getSoundHandler());
        this.serverIncomingPacketListener = new ServerIncomingPacketListener(this);
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        // Reconnect handling
        if (LabyMod.getInstance().isInGame() && this.enabled && !this.isConnected()
                && this.opusCodecManager.getStatus().isReconnect()
                && this.nextConnectionHandling < System.currentTimeMillis()) {
            this.lastConnectionHandling = System.currentTimeMillis();
            this.nextConnectionHandling = System.currentTimeMillis() + 10000L;
            this.connect();
        }

        // Push-to-talk
        if (this.keyPushToTalk != -1) {
            boolean active = (this.keyPushToTalk < 0
                ? Mouse.isButtonDown(this.keyPushToTalk + 100)
                : Keyboard.isKeyDown(this.keyPushToTalk))
                && Minecraft.getMinecraft().currentScreen == null;
            if (!this.pushToTalkPressed && active) this.cleanup = true;
            this.pushToTalkPressed = active;
            if (this.microphone != null && active && this.testingMicrophone) this.testingMicrophone = false;
        }

        // PermaVoice override — force PTT pressed when enabled+active
        if (this.permaVoiceEnabled && this.permaVoiceActive && this.keyPushToTalk != -1) {
            this.pushToTalkPressed = true;
        }
        // PermaVoice toggle hotkey
        if (this.permaVoiceKey != -1 && Minecraft.getMinecraft().currentScreen == null) {
            if (Keyboard.isKeyDown(this.permaVoiceKey)) {
                if (!this.permaVoiceTogglePressed) {
                    this.permaVoiceTogglePressed = true;
                    this.permaVoiceActive = !this.permaVoiceActive;
                    if (this.permaVoiceChatMessages) {
                        LabyMod.getInstance().displayMessageInChat(
                            "§ePermaVoice §8» §" + (this.permaVoiceActive ? "aON" : "cOFF"));
                    }
                }
            } else {
                this.permaVoiceTogglePressed = false;
            }
        }

        // Toggle voicechat
        if (this.keyToggleVoiceChat != -1) {
            if (Minecraft.getMinecraft().currentScreen == null
                && (this.keyToggleVoiceChat < 0
                    ? Mouse.isButtonDown(this.keyToggleVoiceChat + 100)
                    : Keyboard.isKeyDown(this.keyToggleVoiceChat))) {
                if (!this.togglePressed) {
                    this.togglePressed = true;
                    this.enabled = !this.enabled;
                }
            } else this.togglePressed = false;
        } else this.togglePressed = false;
    }

    /**
     * Status hint overlay -- ported from the original onRender. Shows when push-to-talk
     * is held and voice chat isn't fully connected/operational.
     */
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR) return;
        if (!this.pushToTalkPressed) return;

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int w = sr.getScaledWidth();
        int h = sr.getScaledHeight();
        int color = 0xFFFF5555;

        String msg = null;
        if (!this.enabled) {
            msg = this.rulesAccepted
                ? "You disabled the voicechat!"
                : "You need to enable the voicechat to use it!";
        } else if (!this.isConnected()) {
            long countdown = Math.max(0L, this.nextConnectionHandling - System.currentTimeMillis());
            if (!this.allowed) msg = "VoiceChat is not allowed on this server!";
            else if (this.opusCodecManager.getStatus().isReconnect()) {
                if (this.loginCompleted) {
                    msg = countdown <= 100L
                        ? "Connecting to voice chat.."
                        : "Reconnecting in " + ModUtils.parseTimer((int)(countdown / 1000));
                } else {
                    String reason = this.kicked ? this.lastConnectionStatus : "Trying to connect..";
                    msg = reason + " (Retry in " + ModUtils.parseTimer((int)(countdown / 1000)) + ")";
                }
            } else {
                msg = this.opusCodecManager.getStatus().getMessage();
            }
        } else {
            boolean micActive = this.microphone != null && this.microphone.isRunning();
            if (!this.loginCompleted) msg = "Waiting for voiceserver..";
            else if (!micActive) msg = "VoiceChat: Microphone not found";
            else if (this.opusCodecManager.getStatus() != EnumOpusError.OK) msg = this.opusCodecManager.getStatus().getMessage();
            else if (!this.allowed) msg = "The server disabled the VoiceChat for you!";
            else {
                java.util.UUID self = LabyMod.getInstance().getPlayerUUID();
                Mute mute = this.voiceClientListener.getGlobalMuted().get(self);
                if (mute != null && !mute.isExpired()) {
                    msg = "Globally muted (" + mute.getReason() + ", " + mute.getTimeLeft() + " left)";
                } else if (this.serverMutes.contains(self)) {
                    msg = "The server muted you in the voice chat";
                }
            }
        }

        if (msg != null) {
            int textWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(msg);
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(msg, w / 2f - textWidth / 2f, h - 60, color);
        }
    }

    public void connect() {
        try {
            if (!this.enabled) return;
            this.lastConnectionHandling = System.currentTimeMillis();
            if (!LabyMod.getInstance().isPremium()) {
                this.lastConnectionStatus = "Not authenticated with Mojang";
                return;
            }
            this.log("Connecting to voice server..");
            this.surroundManager.clear();
            boolean validPin = LabyMod.getInstance().getPinManager().hasValidPin(LabyMod.getInstance().getPlayerUUID());
            this.lastConnectionHandling = System.currentTimeMillis();
            this.connectedAsUUID = LabyMod.getInstance().getPlayerUUID();
            this.lastConnectionStatus = "Initializing..";
            if (this.microphone != null) this.microphone.stopNow();

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    this.voiceClient.connect(
                        new InetSocketAddress(
                            this.isUseBackupServer() ? "voice2.labymod.net" : "voice.labymod.net",
                            this.isDebug() ? 8067 : 8066),
                        validPin ? AuthenticationMethod.LABYCONNECT : AuthenticationMethod.MOJANG);
                } catch (Exception e) { e.printStackTrace(); }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private boolean isUseBackupServer() {
        return OSUtil.isUnix() || (System.getProperty("voiceBackup") != null && "true".equals(System.getProperty("voiceBackup")));
    }

    public void changeServer() {
        net.minecraft.client.multiplayer.ServerData currentServer = Minecraft.getMinecraft().getCurrentServerData();
        if (currentServer != null && this.isConnected()) {
            UUID self = LabyMod.getInstance().getPlayerUUID();
            if (self != null && self.equals(this.connectedAsUUID)) {
                try {
                    String address = currentServer.serverIP;
                    int port = 25565;
                    if (address.contains(":")) {
                        String[] data = address.split(":");
                        address = data[0];
                        port = Integer.parseInt(data[1]);
                    }
                    this.voiceClient.sendSwitchServer(address, port);
                    this.opusCodecManager.destroyCodecs();
                } catch (Exception e) { e.printStackTrace(); }
            } else this.disconnect(true);
        }
    }

    public void connectToSelectedMicrophone() {
        if (this.isConnected()) {
            if (this.microphone != null) this.microphone.stopNow();
            new Thread(() -> {
                if (VoiceChat.this.isConnected()) {
                    Microphone mic = new Microphone(VoiceChat.this);
                    mic.openMicrophoneByName(VoiceChat.this.selectedMicrophone);
                    mic.start();
                    VoiceChat.this.microphone = mic;
                }
            }).start();
        }
    }

    public void disconnect(boolean instantReconnect) {
        if (this.microphone != null) this.microphone.stopNow();
        this.nextConnectionHandling = System.currentTimeMillis() + 10000L;
        if (!this.kicked) {
            this.lastConnectionStatus = LabyMod.getInstance().isInGame() ? "Not ingame" : "Unknown disconnect";
        }
        if (this.isConnected()) {
            this.log("Disconnect from voice server..");
            this.voiceClient.stop();
            this.loginCompleted = false;
            this.voiceClientListener.getReports().clear();
        }
    }

    public boolean isConnected() { return this.voiceClient.isConnected(); }

    /** LouderVoiceChat: range 0-500, default 100. */
    public int getVolume(UUID uuid) {
        Integer volume = this.playerVolumes.get(uuid);
        return volume == null ? 100 : Math.max(0, Math.min(LOUDER_VOLUME_MAX, volume));
    }

    public void savePlayersVolumes() {
        // TODO: persist this.playerVolumes to JSON config
    }

    public void sendSettingsToServer() {
        JsonObject object = new JsonObject();
        object.addProperty("enabled", this.enabled);
        object.addProperty("surround_range", this.surroundRange);
        object.addProperty("surround_volume", this.surroundVolume);
        object.addProperty("screamer_protection", this.screamerProtection);
        object.addProperty("screamer_protection_level", this.maxSwapRate);
        object.addProperty("screamer_max_volume", this.compressorTarget);
        object.addProperty("microphone_volume", this.microphoneVolume);
        object.addProperty("accepted_settings", this.prevSettings != null);
        LabyMod.getInstance().getLabyModAPI().sendJsonMessageToServer("voicechat", object);
    }

    public void applySettings(JsonObject settings) {
        // TODO: parse server-pushed settings into our fields, then sendSettingsToServer
        for (Entry<String, com.google.gson.JsonElement> set : settings.entrySet()) {
            // see decompiled-reference for the original loadConfig() handling.
        }
    }

    public boolean isDebug() {
        String v = System.getProperty("voiceDebug");
        return "true".equals(v);
    }

    public void reset(boolean sendSettings) {
        this.allowed = true;
        this.resetOnServerSwitch = true;
        this.serverMutes.clear();
        if (this.prevSettings != null) {
            JsonObject settings = this.prevSettings;
            this.prevSettings = null;
            this.applySettings(settings);
            this.serverSettingsMatch = false;
        }
        this.voiceActivity = false;
        this.voiceActivitySuppressed = false;
        if (sendSettings && LabyMod.getInstance().isInGame()) this.sendSettingsToServer();
    }

    public void log(String message) { System.out.println("[VoiceChat] " + message); }

    // ---- accessors ----
    public DefaultVoiceClientListener getVoiceClientListener() { return voiceClientListener; }
    public VoiceClient getVoiceClient() { return voiceClient; }
    public boolean isLoginCompleted() { return loginCompleted; }
    public String getLastConnectionStatus() { return lastConnectionStatus; }
    public boolean isKicked() { return kicked; }
    public long getLastConnectionHandling() { return lastConnectionHandling; }
    public long getNextConnectionHandling() { return nextConnectionHandling; }
    public Gson getGson() { return gson; }
    public Microphone getMicrophone() { return microphone; }
    public SurroundManager getSurroundManager() { return surroundManager; }
    public boolean isTestingMicrophone() { return testingMicrophone; }
    public boolean isPushToTalkPressed() { return pushToTalkPressed; }
    public ServerIncomingPacketListener getServerIncomingPacketListener() { return serverIncomingPacketListener; }
    public boolean isAllowed() { return allowed; }
    public boolean isResetOnServerSwitch() { return resetOnServerSwitch; }
    public boolean isEnabled() { return enabled; }
    public int getKeyToggleVoiceChat() { return keyToggleVoiceChat; }
    public int getKeyPushToTalk() { return keyPushToTalk; }
    public int getMicrophoneVolume() { return microphoneVolume; }
    public String getSelectedMicrophone() { return selectedMicrophone; }
    public int getSurroundRange() { return surroundRange; }
    public int getSurroundVolume() { return surroundVolume; }
    public int getKeyOpenMutedPlayers() { return keyOpenMutedPlayers; }
    public Map<UUID, Integer> getPlayerVolumes() { return playerVolumes; }
    public boolean isScreamerProtection() { return screamerProtection; }
    public int getMaxSwapRate() { return maxSwapRate; }
    public int getCompressorTarget() { return compressorTarget; }
    public boolean isExternalOpusService() { return externalOpusService; }
    public boolean isCrashFix() { return crashFix; }
    public boolean isVoiceActivity() { return voiceActivity; }
    public int getLastRMSLevel() { return lastRMSLevel; }
    public int getVoiceActivityActivationLevel() { return voiceActivityActivationLevel; }
    public boolean isVoiceActivitySuppressed() { return voiceActivitySuppressed; }
    public long getLastVoiceActivityActivation() { return lastVoiceActivityActivation; }
    public boolean isTogglePressed() { return togglePressed; }
    public boolean isCleanup() { return cleanup; }
    public UUID getConnectedAsUUID() { return connectedAsUUID; }
    public UUID getFocusedUser() { return focusedUser; }
    public List<UUID> getServerMutes() { return serverMutes; }
    public JsonObject getPrevSettings() { return prevSettings; }
    public boolean isServerSettingsMatch() { return serverSettingsMatch; }
    public OpusCodecManager getOpusCodecManager() { return opusCodecManager; }
    public boolean isRulesAccepted() { return rulesAccepted; }

    // ---- mutators ----
    public void setLoginCompleted(boolean v) { this.loginCompleted = v; }
    public void setLastConnectionStatus(String v) { this.lastConnectionStatus = v; }
    public void setKicked(boolean v) { this.kicked = v; }
    public void setLastConnectionHandling(long v) { this.lastConnectionHandling = v; }
    public void setNextConnectionHandling(long v) { this.nextConnectionHandling = v; }
    public void setAllowed(boolean v) { this.allowed = v; }
    public void setResetOnServerSwitch(boolean v) { this.resetOnServerSwitch = v; }
    public void setEnabled(boolean v) { this.enabled = v; }
    public void setRulesAccepted(boolean v) { this.rulesAccepted = v; }
}
