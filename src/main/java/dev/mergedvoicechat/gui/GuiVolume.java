package dev.mergedvoicechat.gui;

import dev.mergedvoicechat.Config;
import net.labymod.addons.voicechat.VoiceChat;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.util.ChatComponentText;

import java.io.IOException;
import java.util.UUID;

/**
 * Per-player volume slider, ported from LouderVoiceChat's VolumeGui.
 * Range 0-500. Save persists into voiceChat.playerVolumes + JSON config.
 *
 * Uses static nested classes for the GuiResponder/FormatHelper instead of
 * anonymous inner classes -- LaunchClassLoader/Mixin's SRG remap pipeline
 * trips on anonymous inner classes that implement MCP-named interfaces and
 * capture an outer this reference.
 */
public class GuiVolume extends GuiScreen {

    private final VoiceChat voiceChat;
    private final UUID uuid;
    private final String username;

    public GuiVolume(VoiceChat voiceChat, UUID uuid, String username) {
        this.voiceChat = voiceChat;
        this.uuid = uuid;
        this.username = username;
    }

    @Override
    public void initGui() {
        int w = this.width;
        int h = this.height;

        int initial = voiceChat.getVolume(uuid);
        GuiSlider slider = new GuiSlider(
            new VoiceVolumeResponder(voiceChat, uuid),
            4, w / 2 - 100, h / 2 - 30, "Volume",
            0.0F, 500.0F, (float) initial,
            new VoicePercentFormatter()
        );
        this.buttonList.add(slider);
        this.buttonList.add(new GuiButton(6969, w / 2 - 100, h / 2 + 15, 200, 20, "Reset to 100%"));
        this.buttonList.add(new GuiButton(5,    w / 2 - 100, h / 2 + 40, 200, 20, "Save & Close"));
        this.buttonList.add(new GuiButton(7,    w / 2 - 100, h / 2 + 65, 200, 20, "Mute (volume = 0)"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj,
            "VoiceChat volume for " + username,
            this.width / 2, this.height / 2 - 60, 0xFFFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 6969) {
            voiceChat.playerVolumes.put(uuid, 100);
            saveAndClose();
        } else if (button.id == 5) {
            saveAndClose();
        } else if (button.id == 7) {
            voiceChat.playerVolumes.put(uuid, 0);
            saveAndClose();
        }
    }

    private void saveAndClose() {
        voiceChat.savePlayersVolumes();
        Config.save(voiceChat);
        if (this.mc.thePlayer != null) {
            this.mc.thePlayer.addChatMessage(new ChatComponentText(
                "§7VoiceChat volume for §f" + username + "§7 set to §f" + voiceChat.getVolume(uuid) + "%"));
        }
        this.mc.displayGuiScreen(null);
    }
}
