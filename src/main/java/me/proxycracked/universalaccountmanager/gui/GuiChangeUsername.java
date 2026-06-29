package me.proxycracked.universalaccountmanager.gui;

import me.proxycracked.universalaccountmanager.UniversalAccountManager;
import me.proxycracked.universalaccountmanager.auth.Account;
import me.proxycracked.universalaccountmanager.auth.NameAvailabilityCache;
import me.proxycracked.universalaccountmanager.auth.NameChanger;
import me.proxycracked.universalaccountmanager.auth.NameHistoryCache;
import me.proxycracked.universalaccountmanager.auth.SessionManager;
import me.proxycracked.universalaccountmanager.skin.SessionSkinCache;
import me.proxycracked.universalaccountmanager.skin.SkinHeadCache;
import me.proxycracked.universalaccountmanager.utils.TextFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.Session;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class GuiChangeUsername extends GuiScreen {
  private final GuiScreen previousScreen;
  private GuiTextField nameField;
  private GuiButton applyButton;
  private GuiButton cancelButton;
  private String status = "&7Type to check availability live.&r";

  // Debounce typing — only fire a Mojang lookup once the user has been idle
  // 250ms. Prevents hammering the API on every keystroke.
  private String lookupKey = "";
  private long debounceUntil = 0L;

  public GuiChangeUsername(GuiScreen previousScreen) {
    this.previousScreen = previousScreen;
  }

  @Override
  public void initGui() {
    Keyboard.enableRepeatEvents(true);
    buttonList.clear();
    int cx = width / 2;
    int cy = height / 2;

    nameField = new GuiTextField(0, fontRendererObj, cx - 150, cy - 20, 300, 20);
    nameField.setMaxStringLength(16);
    nameField.setFocused(true);

    buttonList.add(applyButton  = new GuiButton(1, cx - 150, cy + 10, 300, 20, "Change Name"));
    buttonList.add(cancelButton = new GuiButton(2, cx - 75,  cy + 34, 150, 20, "Back"));
  }

  @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    String headerName = me.proxycracked.universalaccountmanager.UniversalAccountManager.streamerMode
      ? me.proxycracked.universalaccountmanager.UniversalAccountManager.HIDDEN_LABEL
      : SessionManager.get().getUsername();
    drawCenteredString(fontRendererObj, "Change Username (active session: "
      + headerName + ")", width / 2, height / 2 - 60, 0xFFFFFF);
    drawString(fontRendererObj, "New Username:", width / 2 - 150, height / 2 - 34, 0xAAAAAA);

    nameField.drawTextBox();

    String typed = nameField.getText().trim();
    drawAvailability(typed);
    if (status != null) {
      drawCenteredString(fontRendererObj, TextFormatting.translate(status),
        width / 2, height / 2 + 64, -1);
    }

    drawNameHistory();

    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  // ---- live availability indicator ----------------------------------------
  // Drawn directly under the input field. Shows live state (loading / a green
  // check / a red X) based on the public Mojang profile lookup.
  private void drawAvailability(String typed) {
    if (typed.isEmpty()) return;
    maybeKickAvailability(typed);
    NameAvailabilityCache.State state = NameAvailabilityCache.get(typed);
    String label;
    switch (state) {
      case LOADING:   label = "&7checking..."; break;
      case AVAILABLE: label = "&a✔ available"; break;
      case TAKEN:     label = "&c✘ taken";     break;
      case INVALID:   label = "&8invalid";     break;
      default:        label = "&8couldn't check"; break;
    }
    drawString(fontRendererObj, TextFormatting.translate(label),
      width / 2 - 150, height / 2 + 2, -1);
  }

  private void maybeKickAvailability(String typed) {
    long now = System.currentTimeMillis();
    if (!typed.equalsIgnoreCase(lookupKey)) {
      lookupKey = typed;
      debounceUntil = now + 250;
      return;
    }
    if (now >= debounceUntil) {
      NameAvailabilityCache.get(typed); // idempotent
    }
  }

  // ---- name history panel (centered, below the status line) --------------
  // Skin previews intentionally omitted: this menu is for renaming, not for
  // skin browsing — the skin GUI handles that separately.
  private void drawNameHistory() {
    int cx = width / 2;
    int hy = height / 2 + 84;
    String ownName = SessionManager.get().getUsername();
    String ownUuid = SessionManager.get().getPlayerID();

    drawCenteredString(fontRendererObj,
      TextFormatting.translate("&7&lNAME HISTORY"), cx, hy, -1);
    hy += 11;

    List<NameHistoryCache.Entry> hist = NameHistoryCache.get(ownName, ownUuid);
    if (hist == null) {
      drawCenteredString(fontRendererObj, TextFormatting.translate("&8loading..."), cx, hy, -1);
    } else if (hist.isEmpty()) {
      drawCenteredString(fontRendererObj, TextFormatting.translate("&8unavailable"), cx, hy, -1);
    } else {
      // Show newest first, capped to keep layout tidy.
      int maxRows = 6;
      int shown = Math.min(maxRows, hist.size());
      for (int i = hist.size() - 1; i >= hist.size() - shown; i--) {
        NameHistoryCache.Entry e = hist.get(i);
        String date = (e.changedAt == null) ? "first" : NameHistoryCache.formatDate(e.changedAt);
        drawCenteredString(fontRendererObj,
          TextFormatting.translate("&8" + date + " &f" + e.name), cx, hy, -1);
        hy += 10;
      }
      if (shown < hist.size()) {
        drawCenteredString(fontRendererObj,
          TextFormatting.translate("&8+" + (hist.size() - shown) + " older"), cx, hy, -1);
        hy += 10;
      }
    }
    // Mojang retired the official names endpoint in Sept 2022; we get the
    // rename log from crafty.gg's community-crawled mirror, so anything
    // pre-2022 (or for accounts crafty hasn't seen) may be missing.
    drawCenteredString(fontRendererObj,
      TextFormatting.translate("&8&ohistory may be incomplete"),
      cx, hy + 4, -1);
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) {
    nameField.textboxKeyTyped(typedChar, keyCode);
    if (keyCode == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(previousScreen);
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    try { super.mouseClicked(mouseX, mouseY, mouseButton); } catch (Exception ignored) {}
    nameField.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button == null || !button.enabled) return;
    switch (button.id) {
      case 1: doChange(); break;
      case 2: mc.displayGuiScreen(previousScreen); break;
    }
  }

  private void doChange() {
    final String name = nameField.getText().trim();
    if (StringUtils.isBlank(name)) { status = "&cEnter a username.&r"; return; }
    final String token = Minecraft.getMinecraft().getSession().getToken();
    status = "&7Changing username to " + name + "...&r";
    new Thread(() -> {
      try {
        int code = NameChanger.changeName(name, token);
        switch (code) {
          case 200:
            applyToSession(name, token);
            status = "&aUsername changed to " + name + "!&r";
            break;
          case 400: status = "&cInvalid username.&r"; break;
          case 401: status = "&cInvalid or expired token. Re-login first.&r"; break;
          case 403: status = "&cName is unavailable or on cooldown.&r"; break;
          case 429: status = "&cToo many requests, try again later.&r"; break;
          default:  status = "&cMojang returned status " + code + "&r";
        }
      } catch (Exception e) {
        status = "&cFailed: " + e.getMessage() + "&r";
      }
    }, "UniversalAccountManager-Name").start();
  }

  private void applyToSession(String newName, String token) {
    Session current = SessionManager.get();
    Session updated = new Session(newName, current.getPlayerID(), current.getToken(),
        Session.Type.MOJANG.toString());
    SessionManager.set(updated);

    for (Account acc : UniversalAccountManager.accounts) {
      if (token.equals(acc.getAccessToken())) {
        acc.setUsername(newName);
        String key = (acc.getUuid() == null || acc.getUuid().isEmpty())
            ? acc.getUsername() : acc.getUuid();
        SkinHeadCache.invalidate(key);
        SessionSkinCache.invalidate(newName, acc.getUuid());
      }
    }
    UniversalAccountManager.save();
  }
}
