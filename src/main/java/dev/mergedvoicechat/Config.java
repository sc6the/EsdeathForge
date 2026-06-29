package dev.mergedvoicechat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.labymod.addons.voicechat.VoiceChat;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Plain-JSON config for VoiceChat fields. Serialized to/from a single file
 * (the suggested config file Forge gives us at preInit).
 */
public final class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final JsonParser PARSER = new JsonParser();

    private static File configFile;

    private Config() {}

    public static void setFile(File file) {
        configFile = file;
    }

    public static synchronized void load(VoiceChat vc) {
        if (configFile == null || !configFile.exists()) return;
        try (FileReader r = new FileReader(configFile)) {
            JsonObject root = PARSER.parse(r).getAsJsonObject();
            if (root.has("enabled"))                vc.enabled = root.get("enabled").getAsBoolean();
            if (root.has("keyToggleVoiceChat"))     vc.keyToggleVoiceChat = root.get("keyToggleVoiceChat").getAsInt();
            if (root.has("keyPushToTalk"))          vc.keyPushToTalk = root.get("keyPushToTalk").getAsInt();
            if (root.has("microphoneVolume"))       vc.microphoneVolume = root.get("microphoneVolume").getAsInt();
            if (root.has("selectedMicrophone"))     vc.selectedMicrophone = root.get("selectedMicrophone").getAsString();
            if (root.has("surroundRange"))          vc.surroundRange = root.get("surroundRange").getAsInt();
            if (root.has("surroundVolume"))         vc.surroundVolume = root.get("surroundVolume").getAsInt();
            if (root.has("screamerProtection"))     vc.screamerProtection = root.get("screamerProtection").getAsBoolean();
            if (root.has("maxSwapRate"))            vc.maxSwapRate = root.get("maxSwapRate").getAsInt();
            if (root.has("compressorTarget"))       vc.compressorTarget = root.get("compressorTarget").getAsInt();
            if (root.has("externalOpusService"))    vc.externalOpusService = root.get("externalOpusService").getAsBoolean();
            if (root.has("crashFix"))               vc.crashFix = root.get("crashFix").getAsBoolean();
            if (root.has("rulesAccepted"))          vc.rulesAccepted = root.get("rulesAccepted").getAsBoolean();
            if (root.has("permaVoiceEnabled"))      vc.permaVoiceEnabled = root.get("permaVoiceEnabled").getAsBoolean();
            if (root.has("permaVoiceKey"))          vc.permaVoiceKey = root.get("permaVoiceKey").getAsInt();
            if (root.has("permaVoiceChatMessages")) vc.permaVoiceChatMessages = root.get("permaVoiceChatMessages").getAsBoolean();
            if (root.has("hudEnabled"))             vc.hudEnabled = root.get("hudEnabled").getAsBoolean();
            if (root.has("hudBackground"))          vc.hudBackground = root.get("hudBackground").getAsBoolean();
            if (root.has("hudX"))                   vc.hudX = root.get("hudX").getAsInt();
            if (root.has("hudY"))                   vc.hudY = root.get("hudY").getAsInt();
            if (root.has("hudScale"))               vc.hudScale = root.get("hudScale").getAsInt();
            if (root.has("hudOpacity"))             vc.hudOpacity = root.get("hudOpacity").getAsInt();
            if (root.has("showAllNameplates"))      vc.showAllNameplates = root.get("showAllNameplates").getAsBoolean();
            if (root.has("speakingGlow"))           vc.speakingGlow = root.get("speakingGlow").getAsBoolean();
            if (root.has("idleOutline"))            vc.idleOutline = root.get("idleOutline").getAsBoolean();
            if (root.has("glowOpacity"))            vc.glowOpacity = root.get("glowOpacity").getAsInt();
            if (root.has("idleOpacity"))            vc.idleOpacity = root.get("idleOpacity").getAsInt();
            if (root.has("playerVolumes")) {
                JsonObject pv = root.getAsJsonObject("playerVolumes");
                Map<UUID, Integer> map = new HashMap<>();
                for (Map.Entry<String, JsonElement> e : pv.entrySet()) {
                    map.put(UUID.fromString(e.getKey()), e.getValue().getAsInt());
                }
                vc.playerVolumes = map;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static synchronized void save(VoiceChat vc) {
        if (configFile == null) return;
        try {
            File parent = configFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            JsonObject root = new JsonObject();
            root.addProperty("enabled", vc.enabled);
            root.addProperty("keyToggleVoiceChat", vc.keyToggleVoiceChat);
            root.addProperty("keyPushToTalk", vc.keyPushToTalk);
            root.addProperty("microphoneVolume", vc.microphoneVolume);
            if (vc.selectedMicrophone != null) root.addProperty("selectedMicrophone", vc.selectedMicrophone);
            root.addProperty("surroundRange", vc.surroundRange);
            root.addProperty("surroundVolume", vc.surroundVolume);
            root.addProperty("screamerProtection", vc.screamerProtection);
            root.addProperty("maxSwapRate", vc.maxSwapRate);
            root.addProperty("compressorTarget", vc.compressorTarget);
            root.addProperty("externalOpusService", vc.externalOpusService);
            root.addProperty("crashFix", vc.crashFix);
            root.addProperty("rulesAccepted", vc.rulesAccepted);
            root.addProperty("permaVoiceEnabled", vc.permaVoiceEnabled);
            root.addProperty("permaVoiceKey", vc.permaVoiceKey);
            root.addProperty("permaVoiceChatMessages", vc.permaVoiceChatMessages);
            root.addProperty("hudEnabled", vc.hudEnabled);
            root.addProperty("hudBackground", vc.hudBackground);
            root.addProperty("hudX", vc.hudX);
            root.addProperty("hudY", vc.hudY);
            root.addProperty("hudScale", vc.hudScale);
            root.addProperty("hudOpacity", vc.hudOpacity);
            root.addProperty("showAllNameplates", vc.showAllNameplates);
            root.addProperty("speakingGlow", vc.speakingGlow);
            root.addProperty("idleOutline", vc.idleOutline);
            root.addProperty("glowOpacity", vc.glowOpacity);
            root.addProperty("idleOpacity", vc.idleOpacity);
            JsonObject pv = new JsonObject();
            for (Map.Entry<UUID, Integer> e : vc.playerVolumes.entrySet()) {
                pv.addProperty(e.getKey().toString(), e.getValue());
            }
            root.add("playerVolumes", pv);
            try (FileWriter w = new FileWriter(configFile)) {
                GSON.toJson(root, w);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
