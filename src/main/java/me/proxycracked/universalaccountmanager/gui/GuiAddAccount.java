package me.proxycracked.universalaccountmanager.gui;

import me.proxycracked.universalaccountmanager.utils.TextFormatting;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

public class GuiAddAccount extends GuiScreen {
  private final GuiScreen previousScreen;
  private GuiButton cancelButton;

  public GuiAddAccount(GuiScreen previousScreen) {
    this.previousScreen = previousScreen;
  }

  @Override
  public void initGui() {
    buttonList.clear();
    int btnW = 140;
    int gap = 6;
    int cx = width / 2;
    int cy = height / 2;

    buttonList.add(new GuiButton(0, cx - btnW / 2, cy - 42,           btnW, 20, "Microsoft"));
    buttonList.add(new GuiButton(1, cx - btnW / 2, cy - 42 + 24,      btnW, 20, "Cookie"));
    buttonList.add(new GuiButton(2, cx - btnW / 2, cy - 42 + 24 * 2,  btnW, 20, "Token"));
    buttonList.add(new GuiButton(4, cx - btnW / 2, cy - 42 + 24 * 3,  btnW, 20, "Refresh Token"));
    buttonList.add(cancelButton = new GuiButton(3, cx - btnW / 2, cy - 42 + 24 * 3 + gap + 20, btnW, 20, "Cancel"));
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    drawCenteredString(fontRendererObj,
      TextFormatting.translate("&fAdd Account&r"),
      width / 2, height / 2 - 42 - fontRendererObj.FONT_HEIGHT - 6, -1);
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) {
    if (keyCode == Keyboard.KEY_ESCAPE) actionPerformed(cancelButton);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button == null || !button.enabled) return;
    switch (button.id) {
      case 0: mc.displayGuiScreen(new GuiMicrosoftAuth(previousScreen)); break;
      case 1: mc.displayGuiScreen(new GuiCookieAuth(previousScreen)); break;
      case 2: mc.displayGuiScreen(new GuiTokenLogin(previousScreen)); break;
      case 4: mc.displayGuiScreen(new GuiRefreshTokenLogin(previousScreen)); break;
      case 3: mc.displayGuiScreen(new GuiAccountManager(previousScreen)); break;
    }
  }
}
