package me.proxycracked.universalaccountmanager.gui;

import me.proxycracked.universalaccountmanager.auth.SessionManager;
import me.proxycracked.universalaccountmanager.skin.SessionSkinCache;
import me.proxycracked.universalaccountmanager.skin.SkinChanger;
import me.proxycracked.universalaccountmanager.skin.SkinHeadCache;
import me.proxycracked.universalaccountmanager.skin.SkinPreview3D;
import me.proxycracked.universalaccountmanager.utils.TextFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

public class GuiChangeSkin extends GuiScreen {
  private final GuiScreen previousScreen;
  private GuiTextField inputField;
  private GuiButton variantButton;
  private GuiButton applyButton;
  private GuiButton uploadButton;
  private GuiButton cancelButton;
  private String variant = "classic";
  private String status = "&7Enter a username, paste a skin URL, or upload a file.&r";

  // Picked file's bytes, retained across renders until Apply consumes them.
  private byte[] uploadedBytes = null;
  private String uploadedName = null;

  // Live preview state. lookupKey caches what we last queried so we don't
  // re-fire on every keystroke; debounceUntil throttles fetches while typing.
  private String lookupKey = "";
  private long debounceUntil = 0L;

  public GuiChangeSkin(GuiScreen previousScreen) {
    this.previousScreen = previousScreen;
  }

  @Override
  public void initGui() {
    Keyboard.enableRepeatEvents(true);
    buttonList.clear();
    int cx = width / 2;
    int cy = height / 2;

    inputField = new GuiTextField(0, fontRendererObj, cx - 150, cy - 20, 300, 20);
    inputField.setMaxStringLength(32767);
    inputField.setFocused(true);

    buttonList.add(variantButton = new GuiButton(0, cx - 150, cy + 10, 145, 20, variantLabel()));
    buttonList.add(uploadButton  = new GuiButton(4, cx + 5,   cy + 10, 145, 20, uploadLabel()));
    buttonList.add(applyButton   = new GuiButton(1, cx - 75,  cy + 34, 150, 20, "Apply Skin"));
    buttonList.add(cancelButton  = new GuiButton(2, cx - 75,  cy + 58, 150, 20, "Back"));
  }

  private String variantLabel() {
    return "Model: " + ("slim".equals(variant) ? "Slim (3px)" : "Classic (4px)");
  }

  private String uploadLabel() {
    return uploadedBytes == null ? "Upload File..."
      : ("File: " + (uploadedName != null && uploadedName.length() <= 12
          ? uploadedName : "(loaded)"));
  }

  @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    String headerName = me.proxycracked.universalaccountmanager.UniversalAccountManager.streamerMode
      ? me.proxycracked.universalaccountmanager.UniversalAccountManager.HIDDEN_LABEL
      : SessionManager.get().getUsername();
    drawCenteredString(fontRendererObj, "Skinchanger (active session: "
      + headerName + ")", width / 2, height / 2 - 60, 0xFFFFFF);
    drawString(fontRendererObj, "Username or Skin URL:", width / 2 - 150, height / 2 - 34, 0xAAAAAA);
    if (status != null) {
      drawCenteredString(fontRendererObj, TextFormatting.translate(status),
        width / 2, height / 2 + 88, -1);
    }
    inputField.drawTextBox();

    drawLivePreview();

    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  // 3D preview of the skin currently equipped on the active session, plus the
  // skin of whatever username is being typed (if any). Both lookups are
  // debounced/cached via SessionSkinCache.
  private void drawLivePreview() {
    int previewY = height / 2 - 30;
    int leftX  = width / 2 - 240;
    int rightX = width / 2 + 240;

    SessionSkinCache.CachedSkin own = SessionSkinCache.get(
      SessionManager.get().getUsername(), SessionManager.get().getPlayerID());
    drawPreviewBox(leftX, previewY, "Current", own);

    String text = inputField.getText().trim();
    boolean isUrl = text.regionMatches(true, 0, "http://", 0, 7)
                 || text.regionMatches(true, 0, "https://", 0, 8);

    if (!isUrl && !text.isEmpty()) {
      // Debounce typing — only fire a lookup once the user has been idle 250ms.
      // Caching is keyed by lowercased name inside SessionSkinCache.
      maybeKickLookup(text);
      SessionSkinCache.CachedSkin typed = SessionSkinCache.get(text, "");
      drawPreviewBox(rightX, previewY, text, typed);
    } else if (isUrl) {
      drawPreviewBox(rightX, previewY, "(URL)", null);
    }
  }

  private void maybeKickLookup(String text) {
    long now = System.currentTimeMillis();
    if (!text.equalsIgnoreCase(lookupKey)) {
      lookupKey = text;
      debounceUntil = now + 250;
      return;
    }
    if (now >= debounceUntil) {
      // Cache.get is idempotent — kick fetch if not already loading/cached.
      SessionSkinCache.get(text, "");
    }
  }

  private void drawPreviewBox(int cx, int cy, String label, SessionSkinCache.CachedSkin skin) {
    int boxW = 60, boxH = 96;
    int boxX = cx - boxW / 2;
    int boxY = cy - boxH / 2;
    drawRect(boxX - 1, boxY - 1, boxX + boxW + 1, boxY + boxH + 1, 0x80000000);

    drawCenteredString(fontRendererObj, TextFormatting.translate("&8" + label),
      cx, boxY - 10, -1);

    if (skin == null) {
      drawCenteredString(fontRendererObj, TextFormatting.translate("&7loading..."),
        cx, cy - 4, -1);
      return;
    }
    if (skin.isUnavailable()) {
      drawCenteredString(fontRendererObj, TextFormatting.translate("&8not found"),
        cx, cy - 4, -1);
      return;
    }
    SkinPreview3D.draw(cx, cy + boxH / 2 - 8, 38, 0F, 0F, skin.rl, "slim".equals(skin.type));
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) {
    inputField.textboxKeyTyped(typedChar, keyCode);
    if (keyCode == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(previousScreen);
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    try { super.mouseClicked(mouseX, mouseY, mouseButton); } catch (Exception ignored) {}
    inputField.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button == null || !button.enabled) return;
    switch (button.id) {
      case 0:
        variant = "slim".equals(variant) ? "classic" : "slim";
        variantButton.displayString = variantLabel();
        break;
      case 1: apply(); break;
      case 2: mc.displayGuiScreen(previousScreen); break;
      case 4: pickFile(); break;
    }
  }

  // AWT FileDialog runs on its own thread so the LWJGL render loop keeps
  // going. The native dialog blocks until dismissed.
  private void pickFile() {
    status = "&7Opening file picker...&r";
    new Thread(() -> {
      try {
        FileDialog fd = new FileDialog((Frame) null, "Select a skin PNG", FileDialog.LOAD);
        fd.setFile("*.png");
        fd.setVisible(true);
        String dir = fd.getDirectory();
        String name = fd.getFile();
        if (dir == null || name == null) {
          status = "&7Upload cancelled.&r";
          return;
        }
        byte[] data;
        try (InputStream in = new FileInputStream(new java.io.File(dir, name))) {
          ByteArrayOutputStream buf = new ByteArrayOutputStream();
          byte[] chunk = new byte[8192];
          int n;
          while ((n = in.read(chunk)) > 0) buf.write(chunk, 0, n);
          data = buf.toByteArray();
        }
        uploadedBytes = data;
        uploadedName = name;
        uploadButton.displayString = uploadLabel();
        status = "&aLoaded " + name + " (" + data.length + " bytes)&r";
      } catch (Exception e) {
        status = "&cUpload failed: " + e.getMessage() + "&r";
      }
    }, "UniversalAccountManager-Picker").start();
  }

  private void apply() {
    final String token = Minecraft.getMinecraft().getSession().getToken();

    // Uploaded file takes precedence — multipart POST to Mojang directly.
    if (uploadedBytes != null) {
      final byte[] bytes = uploadedBytes;
      final String variantUsed = variant;
      status = "&7Uploading skin (" + variantUsed + ")...&r";
      new Thread(() -> {
        try {
          int code = SkinChanger.applySkinFile(bytes, variantUsed, token);
          if (code == 200) {
            uploadedBytes = null;
            uploadedName = null;
            uploadButton.displayString = uploadLabel();
            // No URL to seed caches with — the next session-server fetch will pick it up.
          }
          status = formatResult(code);
        } catch (Exception e) {
          status = "&cFailed: " + e.getMessage() + "&r";
        }
      }, "UniversalAccountManager-Skin").start();
      return;
    }

    final String input = inputField.getText().trim();
    if (StringUtils.isBlank(input)) { status = "&cEnter a username, URL, or upload a file.&r"; return; }
    final boolean isUrl = input.regionMatches(true, 0, "http://", 0, 7)
                       || input.regionMatches(true, 0, "https://", 0, 8);

    if (isUrl) {
      final String variantUsed = variant;
      status = "&7Applying skin (" + variantUsed + ")...&r";
      new Thread(() -> {
        try {
          int code = SkinChanger.applySkinUrl(input, variantUsed, token);
          if (code == 200) refreshActiveHead(input);
          status = formatResult(code);
        } catch (Exception e) {
          status = "&cFailed: " + e.getMessage() + "&r";
        }
      }, "UniversalAccountManager-Skin").start();
      return;
    }

    status = "&7Resolving " + input + "'s skin...&r";
    new Thread(() -> {
      try {
        SkinChanger.SkinInfo info = SkinChanger.resolveSkin(input);
        if (info == null) { status = "&cCouldn't find a skin for " + input + "&r"; return; }
        variant = info.variant;
        variantButton.displayString = variantLabel();
        status = "&7Applying skin (" + variant + ")...&r";
        int code = SkinChanger.applySkinUrl(info.url, variant, token);
        if (code == 200) refreshActiveHead(info.url);
        status = formatResult(code);
      } catch (Exception e) {
        status = "&cFailed: " + e.getMessage() + "&r";
      }
    }, "UniversalAccountManager-Skin").start();
  }

  private void refreshActiveHead(String appliedSkinUrl) {
    String uuid = SessionManager.get().getPlayerID();
    String name = SessionManager.get().getUsername();
    String key = (uuid == null || uuid.isEmpty()) ? name : uuid;
    if (key == null || key.isEmpty()) return;
    SkinHeadCache.applyFromSkinUrl(key, appliedSkinUrl);
    // Seed the live-preview cache from the URL we just uploaded. Going
    // through SkinChanger.resolveSkin would hit Mojang's sessionserver,
    // which caches profile textures for ~60s and would return the OLD
    // skin URL right after a change.
    SessionSkinCache.putFromUrl(name, uuid, appliedSkinUrl, variant);
  }

  private String formatResult(int code) {
    switch (code) {
      case 200: return "&aSkin applied!&r";
      case 401: return "&cInvalid or expired token. Re-login first.&r";
      case 429: return "&cToo many requests, try again later.&r";
      default:  return "&cMojang returned status " + code + "&r";
    }
  }
}
