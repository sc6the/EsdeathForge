package dev.mergedvoicechat.gui;

import dev.mergedvoicechat.Config;
import net.labymod.addons.voicechat.VoiceChat;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/**
 * HUD position editor. Shows the live SpeakerHud, click + drag to move.
 * Position is clamped to the screen and saved to config on close.
 */
public class GuiHudEditor extends GuiScreen {

    private final VoiceChat vc;
    private final SpeakerHud hud;
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;

    public GuiHudEditor(VoiceChat vc, SpeakerHud hud) {
        this.vc = vc;
        this.hud = hud;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height - 28, 200, 20, "Done"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj,
            "§fClick + drag the box. Done to save.",
            this.width / 2, 8, 0xFFFFFF);
        boolean prevHud = vc.hudEnabled;
        vc.hudEnabled = true;
        hud.draw(true);
        vc.hudEnabled = prevHud;
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) return;
        int[] box = hud.computeBox();
        int x = box[0], y = box[1], w = box[2], h = box[3];
        if (mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h) {
            dragging = true;
            dragOffsetX = mouseX - x;
            dragOffsetY = mouseY - y;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging) {
            vc.hudX = mouseX - dragOffsetX;
            vc.hudY = mouseY - dragOffsetY;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (state == 0 && dragging) {
            dragging = false;
            Config.save(vc);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            Config.save(vc);
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
