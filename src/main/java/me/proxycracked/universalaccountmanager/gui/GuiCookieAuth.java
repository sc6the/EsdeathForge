package me.proxycracked.universalaccountmanager.gui;

import me.proxycracked.universalaccountmanager.UniversalAccountManager;
import me.proxycracked.universalaccountmanager.auth.Account;
import me.proxycracked.universalaccountmanager.auth.CookieAuth;
import me.proxycracked.universalaccountmanager.auth.SessionManager;
import me.proxycracked.universalaccountmanager.utils.Notification;
import me.proxycracked.universalaccountmanager.utils.TextFormatting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GuiCookieAuth extends GuiScreen {
  private final GuiScreen previousScreen;

  private GuiButton openButton = null;
  private GuiButton cancelButton = null;
  private boolean openButtonEnabled = true;
  private volatile String status = "&fSelect an exported cookies.txt file (Netscape format).&r";
  private volatile String cause = null;
  private ExecutorService executor;
  private CompletableFuture<?> task;
  private volatile boolean success = false;
  private volatile String successName = null;

  public GuiCookieAuth(GuiScreen previousScreen) {
    this.previousScreen = previousScreen;
  }

  @Override
  public void initGui() {
    buttonList.clear();
    int cy = height / 2 + fontRendererObj.FONT_HEIGHT / 2 + fontRendererObj.FONT_HEIGHT;
    buttonList.add(openButton   = new GuiButton(0, width / 2 - 75 - 2, cy, 75, 20, "Open"));
    buttonList.add(cancelButton = new GuiButton(1, width / 2 + 2,      cy, 75, 20, "Cancel"));
    if (executor == null) executor = Executors.newSingleThreadExecutor();
  }

  @Override
  public void onGuiClosed() {
    if (task != null && !task.isDone()) task.cancel(true);
    if (executor != null) executor.shutdownNow();
  }

  @Override
  public void updateScreen() {
    if (success && successName != null) {
      mc.displayGuiScreen(new GuiAccountManager(previousScreen,
        new Notification(TextFormatting.translate(
          String.format("&aSuccessful login! (%s)&r", successName)), 5000L)));
      success = false;
    }
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    if (openButton != null) openButton.enabled = openButtonEnabled;
    drawDefaultBackground();
    super.drawScreen(mouseX, mouseY, partialTicks);

    drawCenteredString(fontRendererObj, "Cookie Authentication",
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
      case 0:
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
          FileDialog dialog = new FileDialog((Frame) null, "Select Cookie File", FileDialog.LOAD);
          dialog.setDirectory(System.getProperty("user.home") + File.separator + "Downloads");
          dialog.setFile("*.txt");
          dialog.setModal(true);
          status = "&aFile picker opened in background.&r";
          dialog.setVisible(true);
          String name = dialog.getFile();
          if (name == null) {
            status = "&eFile selection canceled.&r";
            return;
          }
          File file = new File(dialog.getDirectory(), name);
          if (!file.exists()) {
            status = "&cSelected file does not exist!&r";
            return;
          }
          openButtonEnabled = false;
          status = "&fStarting cookie authentication...&r";
          cause = null;
          task = CookieAuth.loginFromFile(file, s -> status = s, executor)
            .thenAccept(result -> {
              String username = result.session.getUsername();
              String uuid = result.session.getPlayerID();
              Account existing = null;
              for (Account a : UniversalAccountManager.accounts) {
                boolean uuidMatch = uuid != null && !uuid.isEmpty()
                  && a.getUuid() != null && uuid.equalsIgnoreCase(a.getUuid());
                boolean nameMatch = (uuid == null || uuid.isEmpty())
                  && a.getUsername() != null && username.equalsIgnoreCase(a.getUsername());
                if (uuidMatch || nameMatch) { existing = a; break; }
              }

              // Persist the cookie file so this account can re-auth on every
              // list refresh instead of dying when its access token expires.
              String storedPath = null;
              try {
                String key = (uuid != null && !uuid.isEmpty()) ? uuid : username;
                storedPath = UniversalAccountManager.storeCookieFile(file, key);
              } catch (Exception ex) {
                System.err.println("[UniversalAccountManager] Failed to store cookie file: " + ex.getMessage());
              }

              if (existing == null) {
                Account acc = new Account(Account.TYPE_COOKIE, "", result.accessToken, username, uuid);
                if (storedPath != null) acc.setCookieFile(storedPath);
                UniversalAccountManager.accounts.add(acc);
                UniversalAccountManager.save();
              } else {
                // Always refresh: we just obtained a valid token + cookie copy.
                existing.setType(Account.TYPE_COOKIE);
                existing.setAccessToken(result.accessToken);
                existing.setUsername(username);
                if (uuid != null && !uuid.isEmpty()) existing.setUuid(uuid);
                if (storedPath != null) existing.setCookieFile(storedPath);
                UniversalAccountManager.save();
              }

              SessionManager.set(result.session);
              successName = username;
              success = true;
            })
            .exceptionally(err -> {
              openButtonEnabled = true;
              status = String.format("&c%s&r", err.getMessage());
              cause = err.getCause() != null ? String.format("&c%s&r", err.getCause().getMessage()) : null;
              return null;
            });
        });
        break;
      case 1:
        mc.displayGuiScreen(new GuiAccountManager(previousScreen));
        break;
    }
  }
}
