package dev.mergedvoicechat.gui;

import dev.mergedvoicechat.Config;
import net.labymod.addons.voicechat.VoiceChat;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;

import java.io.IOException;

public class GuiSettings extends GuiScreen {

    private final VoiceChat vc;

    private GuiButton btnEnabled;
    private GuiButton btnScreamer;
    private GuiButton btnExternalOpus;
    private GuiButton btnHud;
    private GuiButton btnHudBg;
    private GuiButton btnShowAllNames;
    private GuiButton btnSpeakingGlow;
    private GuiButton btnIdleOutline;

    public GuiSettings(VoiceChat vc) {
        this.vc = vc;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int leftCol = this.width / 2 - 155;
        int rightCol = this.width / 2 + 5;
        int y = 30;
        int row = 24;

        btnEnabled       = addButton(leftCol,  y, "Voice chat: " + onOff(vc.enabled), 1);
        btnScreamer      = addButton(rightCol, y, "Screamer protection: " + onOff(vc.screamerProtection), 2); y += row;
        btnExternalOpus  = addButton(leftCol,  y, "External opus service: " + onOff(vc.externalOpusService), 3);
        addButton(rightCol, y, "Reset volumes (each player)", 6); y += row;
        btnHud           = addButton(leftCol,  y, "Speaker HUD: " + onOff(vc.hudEnabled), 8);
        addButton(rightCol, y, "Edit HUD position...", 9); y += row;
        btnHudBg         = addButton(leftCol,  y, "HUD background: " + onOff(vc.hudBackground), 16);
        btnShowAllNames  = addButton(rightCol, y, "Show all nameplates: " + onOff(vc.showAllNameplates), 15); y += row;
        btnSpeakingGlow  = addButton(leftCol,  y, "Speaking glow: " + onOff(vc.speakingGlow), 18);
        btnIdleOutline   = addButton(rightCol, y, "Idle gray outline: " + onOff(vc.idleOutline), 19); y += row * 2;

        // Sliders
        this.buttonList.add(slider(10, leftCol,  y, "Surround volume",   0, 500, vc.surroundVolume));
        this.buttonList.add(slider(11, rightCol, y, "Surround range",    5, 18, vc.surroundRange));      y += row;
        this.buttonList.add(slider(12, leftCol,  y, "Microphone volume", 0, 40, vc.microphoneVolume));
        this.buttonList.add(slider(13, rightCol, y, "Activation rate",   20, 100, vc.maxSwapRate));      y += row;
        this.buttonList.add(slider(14, leftCol,  y, "Volume limit",      1, 30, vc.compressorTarget));
        this.buttonList.add(slider(17, rightCol, y, "HUD scale (%)",     50, 300, vc.hudScale));         y += row;

        // Opacity sliders (percent)
        this.buttonList.add(sliderPct(20, leftCol,  y, "Speaking glow opacity", 10, 100, vc.glowOpacity));
        this.buttonList.add(sliderPct(21, rightCol, y, "Idle outline opacity",  10, 100, vc.idleOpacity)); y += row;
        this.buttonList.add(sliderPct(22, leftCol,  y, "HUD opacity",           0, 100, vc.hudOpacity));    y += row * 2;

        addButton(this.width / 2 - 100, y, "Done", 0); y += row;
        addButton(this.width / 2 - 100, y, "Reconnect voice server", 7);
    }

    private GuiButton addButton(int x, int y, String label, int id) {
        GuiButton b = new GuiButton(id, x, y, 150, 20, label);
        this.buttonList.add(b);
        return b;
    }

    private GuiSlider slider(int id, int x, int y, String name, int min, int max, int initial) {
        // NOTE: responder/formatter are top-level classes (VoiceSettingsResponder/VoiceIntFormatter),
        // not inner classes — raven.jar's transformer NPEs on lazily-loaded inner classes.
        return new GuiSlider(
            new VoiceSettingsResponder(vc),
            id, x, y, name, min, max, Math.max(min, Math.min(max, initial)),
            new VoiceIntFormatter());
    }

    // percent-suffixed slider for the opacity controls
    private GuiSlider sliderPct(int id, int x, int y, String name, int min, int max, int initial) {
        return new GuiSlider(
            new VoiceSettingsResponder(vc),
            id, x, y, name, min, max, Math.max(min, Math.min(max, initial)),
            new VoicePercentFormatter());
    }

    private static String onOff(boolean b) { return b ? "§aON" : "§cOFF"; }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1:  vc.enabled = !vc.enabled; btnEnabled.displayString = "Voice chat: " + onOff(vc.enabled); break;
            case 2:  vc.screamerProtection = !vc.screamerProtection; btnScreamer.displayString = "Screamer protection: " + onOff(vc.screamerProtection); break;
            case 3:  vc.externalOpusService = !vc.externalOpusService; btnExternalOpus.displayString = "External opus service: " + onOff(vc.externalOpusService); break;
            case 6:  vc.playerVolumes.clear(); vc.savePlayersVolumes(); break;
            case 7:  vc.disconnect(true); vc.connect(); break;
            case 8:  vc.hudEnabled = !vc.hudEnabled; btnHud.displayString = "Speaker HUD: " + onOff(vc.hudEnabled); break;
            case 9:
                Config.save(vc);
                openHudEditor();
                return;
            case 15: vc.showAllNameplates = !vc.showAllNameplates; btnShowAllNames.displayString = "Show all nameplates: " + onOff(vc.showAllNameplates); break;
            case 16: vc.hudBackground = !vc.hudBackground; btnHudBg.displayString = "HUD background: " + onOff(vc.hudBackground); break;
            case 18: vc.speakingGlow = !vc.speakingGlow; btnSpeakingGlow.displayString = "Speaking glow: " + onOff(vc.speakingGlow); break;
            case 19: vc.idleOutline = !vc.idleOutline; btnIdleOutline.displayString = "Idle gray outline: " + onOff(vc.idleOutline); break;
            case 0:
                Config.save(vc);
                vc.sendSettingsToServer();
                this.mc.displayGuiScreen(null);
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj,
            "Merged VoiceChat — Settings",
            this.width / 2, 8, 0xFFFFFFFF);
        this.drawCenteredString(this.fontRendererObj,
            "§7Rebind keys from §fOptions → Controls → Merged VoiceChat",
            this.width / 2, 18, 0xFFFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        Config.save(vc);
    }

    /**
     * Reflectively opens GuiHudEditor. We avoid a direct type reference because some
     * coremod's class transformer chokes during eager verification of GuiSettings if
     * GuiHudEditor is referenced statically -- causes NoClassDefFoundError on settings open.
     */
    private void openHudEditor() {
        try {
            Class<?> cls = Class.forName("dev.mergedvoicechat.gui.GuiHudEditor");
            Object screen = cls.getConstructor(VoiceChat.class, SpeakerHud.class)
                .newInstance(vc, dev.mergedvoicechat.MergedVoiceChat.INSTANCE.speakerHud);
            this.mc.displayGuiScreen((net.minecraft.client.gui.GuiScreen) screen);
        } catch (Throwable t) {
            t.printStackTrace();
            if (this.mc.thePlayer != null) {
                this.mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                    "§cFailed to open HUD editor: " + t.getMessage()));
            }
        }
    }
}
