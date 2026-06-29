package me.djtheredstoner.perspectivemod.gui;

import me.djtheredstoner.perspectivemod.config.PerspectiveModConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class PerspectiveModGui extends GuiScreen {

    private static final int ID_MOD_ENABLED = 0;
    private static final int ID_HOLD_MODE = 1;
    private static final int ID_INVERT_PITCH = 2;
    private static final int ID_DONE = 100;

    private final PerspectiveModConfig config;

    public PerspectiveModGui(PerspectiveModConfig config) {
        this.config = config;
    }

    @Override
    public void initGui() {
        buttonList.clear();

        int x = width / 2 - 100;
        int y = height / 4;

        buttonList.add(new GuiButton(ID_MOD_ENABLED, x, y, label("Perspective Mod", config.modEnabled)));
        buttonList.add(new GuiButton(ID_HOLD_MODE, x, y + 24, label("Hold Mode", config.holdMode)));
        buttonList.add(new GuiButton(ID_INVERT_PITCH, x, y + 48, label("Invert Pitch", config.invertPitch)));
        buttonList.add(new GuiButton(ID_DONE, x, y + 84, "Done"));
    }

    private String label(String name, boolean state) {
        return name + ": " + (state ? "§aON" : "§cOFF");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case ID_MOD_ENABLED:
                config.modEnabled = !config.modEnabled;
                break;
            case ID_HOLD_MODE:
                config.holdMode = !config.holdMode;
                break;
            case ID_INVERT_PITCH:
                config.invertPitch = !config.invertPitch;
                break;
            case ID_DONE:
                mc.displayGuiScreen(null);
                return;
            default:
                return;
        }

        config.save();
        initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "MeowMeow", width / 2, height / 4 - 20, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
