package me.proxycracked.universalaccountmanager.gui;

import me.proxycracked.universalaccountmanager.UniversalAccountManager;
import me.proxycracked.universalaccountmanager.auth.Account;
import me.proxycracked.universalaccountmanager.auth.RefreshTokenAuth;
import me.proxycracked.universalaccountmanager.auth.SessionManager;
import me.proxycracked.universalaccountmanager.utils.Notification;
import me.proxycracked.universalaccountmanager.utils.TextFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.Session;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

public class GuiRefreshTokenLogin extends GuiScreen {
  private final GuiScreen previousScreen;
  private GuiTextField tokenField;
  private GuiButton loginButton;
  private GuiButton saveButton;
  private GuiButton cancelButton;
  private boolean busy = false;
  private String status = "&7Paste a Microsoft refresh token. It auto-converts to an access token on every list refresh.&r";

  public GuiRefreshTokenLogin(GuiScreen previousScreen) {
    this.previousScreen = previousScreen;
  }

  @Override
  public void initGui() {
    Keyboard.enableRepeatEvents(true);
    buttonList.clear();
    int cx = width / 2;
    int cy = height / 2;

    tokenField = new GuiTextField(0, fontRendererObj, cx - 150, cy - 12, 300, 20);
    tokenField.setMaxStringLength(32767);
    tokenField.setFocused(true);

    buttonList.add(loginButton  = new GuiButton(0, cx - 150, cy + 16, 95, 20, "Login Now"));
    buttonList.add(saveButton   = new GuiButton(1, cx - 50,  cy + 16, 100, 20, "Save Account"));
    buttonList.add(cancelButton = new GuiButton(2, cx + 55,  cy + 16, 95, 20, "Cancel"));
  }

  @Override
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    drawCenteredString(fontRendererObj, "Refresh Token Login", width / 2, height / 2 - 60, 0xFFFFFF);
    drawString(fontRendererObj, "Microsoft refresh token:", width / 2 - 150, height / 2 - 26, 0xAAAAAA);
    if (status != null) {
      String s = TextFormatting.translate(status);
      int w = fontRendererObj.getStringWidth(s);
      Gui.drawRect(width / 2 - w / 2 - 4, height / 2 + 44, width / 2 + w / 2 + 4, height / 2 + 56, 0x40000000);
      drawCenteredString(fontRendererObj, s, width / 2, height / 2 + 46, -1);
    }
    tokenField.drawTextBox();
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) {
    tokenField.textboxKeyTyped(typedChar, keyCode);
    if (keyCode == Keyboard.KEY_ESCAPE) {
      mc.displayGuiScreen(previousScreen);
    } else if (keyCode == Keyboard.KEY_RETURN) {
      actionPerformed(loginButton);
    }
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    try { super.mouseClicked(mouseX, mouseY, mouseButton); } catch (Exception ignored) {}
    tokenField.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button == null || !button.enabled || busy) return;
    switch (button.id) {
      case 0: doConvert(true);  break;
      case 1: doSave();         break;
      case 2: mc.displayGuiScreen(previousScreen); break;
    }
  }

  // Stores the refresh token as a refresh account and returns to the manager,
  // where it will be converted to a usable token account on the next refresh.
  private void doSave() {
    final String token = tokenField.getText().trim();
    if (StringUtils.isBlank(token)) {
      status = "&cRefresh token is empty.&r";
      return;
    }
    for (Account a : UniversalAccountManager.accounts) {
      if (token.equals(a.getRefreshToken())) {
        status = "&eThat refresh token is already saved.&r";
        return;
      }
    }
    UniversalAccountManager.accounts.add(new Account(Account.TYPE_REFRESH, token, "", "", ""));
    UniversalAccountManager.save();
    mc.displayGuiScreen(new GuiAccountManager(previousScreen,
      new Notification(TextFormatting.translate("&aSaved refresh-token account. &7Converting...&r"), 5000L)));
  }

  // Converts the refresh token immediately, logs in, and saves it as a refresh
  // account (so it keeps auto-refreshing on future list refreshes).
  private void doConvert(final boolean alsoSave) {
    final String token = tokenField.getText().trim();
    if (StringUtils.isBlank(token)) {
      status = "&cRefresh token is empty.&r";
      return;
    }
    busy = true;
    status = "&7Converting refresh token...&r";
    new Thread(() -> {
      try {
        RefreshTokenAuth.Result r = RefreshTokenAuth.convert(token);
        Session session = new Session(r.username, r.uuid, r.mcAccessToken, Session.Type.MOJANG.toString());
        SessionManager.set(session);
        if (alsoSave) {
          Account existing = null;
          for (Account a : UniversalAccountManager.accounts) {
            if (token.equals(a.getRefreshToken())
                || (StringUtils.isNotBlank(a.getUsername()) && a.getUsername().equalsIgnoreCase(r.username))) {
              existing = a;
              break;
            }
          }
          if (existing == null) {
            existing = new Account(Account.TYPE_REFRESH, r.refreshToken, r.mcAccessToken, r.username, r.uuid);
            UniversalAccountManager.accounts.add(existing);
          } else {
            existing.setType(Account.TYPE_REFRESH);
            existing.setRefreshToken(r.refreshToken);
            existing.setAccessToken(r.mcAccessToken);
            existing.setUsername(r.username);
            existing.setUuid(r.uuid);
          }
          existing.setAvailable(Boolean.TRUE);
          UniversalAccountManager.save();
        }
        Minecraft.getMinecraft().addScheduledTask(() -> mc.displayGuiScreen(new GuiAccountManager(previousScreen,
          new Notification(TextFormatting.translate("&aLogged in as " + r.username + (alsoSave ? " &7(saved)" : "") + "&r"), 5000L))));
      } catch (Exception e) {
        busy = false;
        status = TextFormatting.translate("&cConversion failed: " + e.getMessage() + "&r");
      }
    }, "UniversalAccountManager-RefreshLogin").start();
  }
}
