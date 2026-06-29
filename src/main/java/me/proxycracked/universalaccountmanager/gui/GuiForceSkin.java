package me.proxycracked.universalaccountmanager.gui;

import me.proxycracked.universalaccountmanager.skin.ForceSkinManager;
import me.proxycracked.universalaccountmanager.skin.SkinChanger;
import me.proxycracked.universalaccountmanager.utils.TextFormatting;
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

// Dedicated Force Skin editor. Three input modes (username, URL, local file)
// all funnel into ForceSkinManager.applyFromBytes / applyFromUrl, which
// schedule a ForceSkinLoader.reload() so the override is live in the current
// session — no restart and no external mod required.
public class GuiForceSkin extends GuiScreen {
  private final GuiScreen previousScreen;
  private GuiTextField inputField;
  private GuiButton variantButton;
  private GuiButton applyButton;
  private GuiButton uploadButton;
  private GuiButton toggleButton;
  private GuiButton backButton;
  private String variant = "classic";
  private String status = "&7Enter a username, paste a skin URL, or upload a file.&r";

  // Picked file's bytes, retained across renders until Apply consumes them.
  private byte[] uploadedBytes = null;
  private String uploadedName = null;

  public GuiForceSkin(GuiScreen previousScreen) {
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
    buttonList.add(applyButton   = new GuiButton(1, cx - 150, cy + 34, 145, 20, "Apply Force Skin"));
    buttonList.add(toggleButton  = new GuiButton(3, cx + 5,   cy + 34, 145, 20, toggleLabel()));
    buttonList.add(backButton    = new GuiButton(2, cx - 75,  cy + 60, 150, 20, "Back"));

    refreshToggleButton();
  }

  private String variantLabel() {
    return "Model: " + ("slim".equals(variant) ? "Slim (3px)" : "Classic (4px)");
  }

  private String uploadLabel() {
    return uploadedBytes == null ? "Upload File..."
      : ("File: " + (uploadedName != null && uploadedName.length() <= 12
          ? uploadedName : "(loaded)"));
  }

  private String toggleLabel() {
    if (ForceSkinManager.exists())     return "Disable Forced Skin";
    if (ForceSkinManager.isDisabled()) return "Enable Forced Skin";
    return "No Forced Skin Set";
  }

  private void refreshToggleButton() {
    if (toggleButton == null) return;
    toggleButton.displayString = toggleLabel();
    toggleButton.enabled = ForceSkinManager.exists() || ForceSkinManager.isDisabled();
  }

  @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    drawCenteredString(fontRendererObj, "Force Skin", width / 2, height / 2 - 80, 0xFFFFFF);
    drawCenteredString(fontRendererObj, TextFormatting.translate(
      "&7Overrides your in-game skin locally — visible only to you.&r"),
      width / 2, height / 2 - 68, -1);
    drawCenteredString(fontRendererObj, TextFormatting.translate(
      "&8Disable hides the file without deleting it — toggle to bring it back.&r"),
      width / 2, height / 2 - 56, -1);
    drawString(fontRendererObj, "Username or Skin URL:", width / 2 - 150, height / 2 - 34, 0xAAAAAA);
    if (status != null) {
      drawCenteredString(fontRendererObj, TextFormatting.translate(status),
        width / 2, height / 2 + 88, -1);
    }
    inputField.drawTextBox();
    super.drawScreen(mouseX, mouseY, partialTicks);
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
      case 3:
        if (ForceSkinManager.exists()) {
          status = ForceSkinManager.disable()
            ? "&aForce Skin disabled.&r"
            : "&cCouldn't disable Force Skin.&r";
        } else if (ForceSkinManager.isDisabled()) {
          status = ForceSkinManager.enable()
            ? "&aForce Skin re-enabled.&r"
            : "&cCouldn't re-enable Force Skin.&r";
        }
        refreshToggleButton();
        break;
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
    // Uploaded file takes precedence over the text field.
    if (uploadedBytes != null) {
      final byte[] bytes = uploadedBytes;
      final boolean slim = "slim".equals(variant);
      status = "&7Writing Force Skin from file...&r";
      new Thread(() -> {
        try {
          ForceSkinManager.applyFromBytes(bytes, slim);
          uploadedBytes = null;
          uploadedName = null;
          uploadButton.displayString = uploadLabel();
          refreshToggleButton();
          status = "&aForce Skin saved from upload.&r";
        } catch (Exception e) {
          status = "&cForce Skin write failed: " + e.getMessage() + "&r";
        }
      }, "UniversalAccountManager-ForceSkin").start();
      return;
    }

    final String input = inputField.getText().trim();
    if (StringUtils.isBlank(input)) {
      status = "&cEnter a username, URL, or upload a file.&r";
      return;
    }
    final boolean isUrl = input.regionMatches(true, 0, "http://", 0, 7)
                       || input.regionMatches(true, 0, "https://", 0, 8);

    if (isUrl) {
      final boolean slim = "slim".equals(variant);
      status = "&7Downloading skin...&r";
      new Thread(() -> {
        try {
          ForceSkinManager.applyFromUrl(input, slim);
          refreshToggleButton();
          status = "&aForce Skin saved.&r";
        } catch (Exception e) {
          status = "&cForce Skin write failed: " + e.getMessage() + "&r";
        }
      }, "UniversalAccountManager-ForceSkin").start();
      return;
    }

    status = "&7Resolving " + input + "'s skin...&r";
    new Thread(() -> {
      SkinChanger.SkinInfo info = SkinChanger.resolveSkin(input);
      if (info == null) { status = "&cCouldn't find a skin for " + input + "&r"; return; }
      variant = info.variant;
      variantButton.displayString = variantLabel();
      status = "&7Downloading skin...&r";
      try {
        ForceSkinManager.applyFromUrl(info.url, "slim".equals(info.variant));
        refreshToggleButton();
        status = "&aForce Skin saved from " + input + ".&r";
      } catch (Exception e) {
        status = "&cForce Skin write failed: " + e.getMessage() + "&r";
      }
    }, "UniversalAccountManager-ForceSkin").start();
  }
}
