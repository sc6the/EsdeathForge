package me.proxycracked.universalaccountmanager.gui;

import me.proxycracked.universalaccountmanager.UniversalAccountManager;
import me.proxycracked.universalaccountmanager.auth.Account;
import me.proxycracked.universalaccountmanager.auth.MicrosoftAuth;
import me.proxycracked.universalaccountmanager.auth.SessionManager;
import me.proxycracked.universalaccountmanager.utils.Notification;
import me.proxycracked.universalaccountmanager.utils.SystemUtils;
import me.proxycracked.universalaccountmanager.utils.TextFormatting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.apache.commons.lang3.RandomStringUtils;
import org.lwjgl.input.Keyboard;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class GuiMicrosoftAuth extends GuiScreen {
  private final GuiScreen previousScreen;
  private final String state;

  private GuiButton openButton = null;
  private boolean openButtonEnabled = true;
  private GuiButton cancelButton = null;
  private String status = null;
  private String cause = null;
  private ExecutorService executor = null;
  private CompletableFuture<Void> task = null;
  private boolean success = false;

  public GuiMicrosoftAuth(GuiScreen previousScreen) {
    this.previousScreen = previousScreen;
    this.state = RandomStringUtils.randomAlphanumeric(8);
  }

  @Override
  public void initGui() {
    buttonList.clear();
    int cy = height / 2 + fontRendererObj.FONT_HEIGHT / 2 + fontRendererObj.FONT_HEIGHT;
    buttonList.add(openButton = new GuiButton(0, width / 2 - 75 - 2, cy, 75, 20, "Open"));
    buttonList.add(cancelButton = new GuiButton(1, width / 2 + 2, cy, 75, 20, "Cancel"));

    if (task == null) {
      URI url = MicrosoftAuth.getMSAuthLink(state);
      SystemUtils.setClipboard(url != null ? url.toString() : "");
      status = "&fLogin link has been copied to the clipboard!&r";

      if (executor == null) executor = Executors.newSingleThreadExecutor();
      AtomicReference<String> refreshToken = new AtomicReference<>("");
      AtomicReference<String> accessToken = new AtomicReference<>("");
      task = MicrosoftAuth.acquireMSAuthCode(state, executor)
        .thenComposeAsync(code -> {
          openButtonEnabled = false;
          status = "&fAcquiring Microsoft access tokens&r";
          return MicrosoftAuth.acquireMSAccessTokens(code, executor);
        })
        .thenComposeAsync(t -> {
          status = "&fAcquiring Xbox access token&r";
          refreshToken.set(t.get("refresh_token"));
          return MicrosoftAuth.acquireXboxAccessToken(t.get("access_token"), executor);
        })
        .thenComposeAsync(x -> {
          status = "&fAcquiring Xbox XSTS token&r";
          return MicrosoftAuth.acquireXboxXstsToken(x, executor);
        })
        .thenComposeAsync(x -> {
          status = "&fAcquiring Minecraft access token&r";
          return MicrosoftAuth.acquireMCAccessToken(x.get("Token"), x.get("uhs"), executor);
        })
        .thenComposeAsync(mc -> {
          status = "&fFetching your Minecraft profile&r";
          accessToken.set(mc);
          return MicrosoftAuth.login(mc, executor);
        })
        .thenAccept(session -> {
          status = null;
          Account acc = new Account(Account.TYPE_MS, refreshToken.get(), accessToken.get(),
            session.getUsername(), session.getPlayerID());
          UniversalAccountManager.accounts.add(acc);
          UniversalAccountManager.save();
          SessionManager.set(session);
          success = true;
        })
        .exceptionally(error -> {
          openButtonEnabled = false;
          status = String.format("&c%s&r", error.getMessage());
          cause = error.getCause() != null ? String.format("&c%s&r", error.getCause().getMessage()) : null;
          return null;
        });
    }
  }

  @Override
  public void onGuiClosed() {
    if (task != null && !task.isDone()) {
      task.cancel(true);
      executor.shutdownNow();
    }
  }

  @Override
  public void updateScreen() {
    if (success) {
      mc.displayGuiScreen(new GuiAccountManager(
        previousScreen,
        new Notification(TextFormatting.translate(String.format(
          "&aSuccessful login! (%s)&r", SessionManager.get().getUsername())), 5000L)
      ));
      success = false;
    }
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    if (openButton != null) openButton.enabled = openButtonEnabled;
    drawDefaultBackground();
    super.drawScreen(mouseX, mouseY, partialTicks);

    drawCenteredString(fontRendererObj, "Microsoft Authentication",
      width / 2, height / 2 - fontRendererObj.FONT_HEIGHT / 2 - fontRendererObj.FONT_HEIGHT * 2, 0xAAAAAA);

    if (status != null) {
      drawCenteredString(fontRendererObj, TextFormatting.translate(status),
        width / 2, height / 2 - fontRendererObj.FONT_HEIGHT / 2, -1);
    }

    if (cause != null) {
      String causeText = TextFormatting.translate(cause);
      Gui.drawRect(0, height - 2 - fontRendererObj.FONT_HEIGHT - 3,
        3 + mc.fontRendererObj.getStringWidth(causeText) + 3, height, 0x64000000);
      drawString(fontRendererObj, causeText, 3, height - 2 - fontRendererObj.FONT_HEIGHT, -1);
    }
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) {
    if (keyCode == Keyboard.KEY_ESCAPE) actionPerformed(cancelButton);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button == null || !button.enabled) return;
    switch (button.id) {
      case 0: SystemUtils.openWebLink(MicrosoftAuth.getMSAuthLink(state)); break;
      case 1: mc.displayGuiScreen(new GuiAccountManager(previousScreen)); break;
    }
  }
}
