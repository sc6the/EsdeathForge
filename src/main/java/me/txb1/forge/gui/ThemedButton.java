package me.txb1.forge.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

/**
 * A GuiButton that renders with the current Esdeath theme (Fancy texture / Semi-transparent slab /
 * Vanilla), matching the pause-menu side panels. Reuse anywhere a themed button is wanted.
 */
public class ThemedButton extends GuiButton {

    private static final ResourceLocation BUTTON = new ResourceLocation("EsdeathClient/button.jpg");

    public ThemedButton(int id, int x, int y, int w, int h, String text) {
        super(id, x, y, w, h, text);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) return;
        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition
            && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

        EsdeathIngameMenu.drawThemedButton(mc, BUTTON, this.xPosition, this.yPosition, this.width, this.height, this.hovered);

        FontRenderer fr = mc.fontRendererObj;
        int color = !this.enabled ? 0xFFA0A0A0 : this.hovered ? 0xFFFFFFA0 : 0xFFE0E0E0;
        this.drawCenteredString(fr, this.displayString,
            this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, color);
    }
}
