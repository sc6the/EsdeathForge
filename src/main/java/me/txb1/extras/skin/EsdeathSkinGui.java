package me.txb1.extras.skin;

import me.proxycracked.universalaccountmanager.skin.SessionSkin;
import me.proxycracked.universalaccountmanager.skin.SkinChanger;
import me.proxycracked.universalaccountmanager.skin.SkinPreview3D;
import me.proxycracked.universalaccountmanager.utils.HttpUtils;
import me.proxycracked.universalaccountmanager.utils.TextFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import javax.imageio.ImageIO;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

// In-game Skinchanger (opened from the ESC menu). Applies the chosen skin LOCALLY first via the
// Force Skin system (instant, no Mojang round-trip), then queues the real Mojang upload to fire when
// you quit the server (PendingSkin + SkinDisconnectHandler) — uploading mid-session wouldn't show to
// other players until a reconnect anyway, and Mojang rate-limits the endpoint. When you're not on a
// server (main menu), the Mojang upload is flushed immediately.
public class EsdeathSkinGui extends GuiScreen {
   private final GuiScreen previousScreen;
   private GuiTextField inputField;
   private GuiButton variantButton;
   private GuiButton uploadButton;
   private String variant = "classic";
   private String status = "&7Enter a username, paste a skin URL, or upload a file.&r";

   private byte[] uploadedBytes = null;
   private String uploadedName = null;

   // live preview: a rotating 3D player model of whatever skin the current input resolves to, so you
   // see the result before committing. previewKey identifies what previewRL currently shows; the
   // resolve is debounced (text) / immediate (upload) and runs off-thread, registering the texture
   // back on the render thread.
   private ResourceLocation previewRL;
   private boolean previewSlim;
   private String previewKey = "";   // key of the skin previewRL shows
   private String pendingKey = "";   // key we last saw as the desired preview
   private boolean resolving;
   private long debounceAt;
   private float spin;

   public EsdeathSkinGui(GuiScreen previousScreen) {
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
      buttonList.add(uploadButton = new GuiButton(4, cx + 5, cy + 10, 145, 20, uploadLabel()));
      buttonList.add(new GuiButton(1, cx - 150, cy + 34, 145, 20, "Apply Skin"));
      buttonList.add(new GuiButton(3, cx + 5, cy + 34, 145, 20, "Clear Skin"));
      buttonList.add(new GuiButton(2, cx - 75, cy + 60, 150, 20, "Back"));
   }

   private String variantLabel() {
      return "Model: " + ("slim".equals(variant) ? "Slim (3px)" : "Classic (4px)");
   }

   private String uploadLabel() {
      return uploadedBytes == null ? "Upload File..."
         : ("File: " + (uploadedName != null && uploadedName.length() <= 12 ? uploadedName : "(loaded)"));
   }

   private boolean onServer() {
      return mc.theWorld != null && mc.getCurrentServerData() != null;
   }

   @Override
   public void onGuiClosed() {
      Keyboard.enableRepeatEvents(false);
      // drop the preview's dynamic texture so it doesn't leak a GL handle
      if (previewRL != null) {
         try {
            mc.getTextureManager().deleteTexture(previewRL);
         } catch (Exception ignored) {
         }
         previewRL = null;
      }
   }

   @Override
   public void updateScreen() {
      super.updateScreen();
      this.spin += 1.4F; // slow idle turntable
      if (this.spin >= 360.0F) {
         this.spin -= 360.0F;
      }

      // figure out what the preview SHOULD show from the current input
      String want;
      if (uploadedBytes != null) {
         want = "u:" + uploadedName + ":" + uploadedBytes.length;
      } else {
         String t = inputField.getText().trim();
         want = t.isEmpty() ? "" : "t:" + t.toLowerCase();
      }

      if (!want.equals(pendingKey)) {
         pendingKey = want;
         debounceAt = System.currentTimeMillis() + 300; // debounce typing
      }
      if (!want.isEmpty() && !want.equals(previewKey) && !resolving
            && System.currentTimeMillis() >= debounceAt) {
         kickPreview(want);
      }
   }

   // Resolve the current input to a processed skin texture off-thread, then register it on the render
   // thread. Mirrors the three Apply paths (file / URL / username) so the preview matches what Apply
   // will do. For usernames it also syncs the model toggle to the resolved variant.
   private void kickPreview(final String key) {
      resolving = true;
      final byte[] bytes = uploadedBytes;
      final String input = inputField.getText().trim();
      final boolean curSlim = "slim".equals(variant);
      new Thread(() -> {
         try {
            byte[] raw;
            boolean slim = curSlim;
            if (bytes != null) {
               raw = bytes;
            } else if (input.regionMatches(true, 0, "http://", 0, 7)
                  || input.regionMatches(true, 0, "https://", 0, 8)) {
               raw = HttpUtils.getBytes(input);
            } else {
               SkinChanger.SkinInfo info = SkinChanger.resolveSkin(input);
               if (info == null) {
                  finishPreview(key, null, false);
                  return;
               }
               raw = HttpUtils.getBytes(info.url);
               slim = "slim".equals(info.variant);
            }
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(raw));
            if (img == null) {
               finishPreview(key, null, false);
               return;
            }
            BufferedImage processed = new ImageBufferDownload().parseUserSkin(img);
            finishPreview(key, processed != null ? processed : img, slim);
         } catch (Exception e) {
            finishPreview(key, null, false);
         }
      }, "Esdeath-SkinPreview").start();
   }

   private void finishPreview(final String key, final BufferedImage img, final boolean slim) {
      Minecraft.getMinecraft().addScheduledTask(() -> {
         resolving = false;
         if (img == null) {
            return; // leave the previous preview up; status already reflects errors on Apply
         }
         // only adopt if this is still the skin the user wants (input may have moved on)
         if (!key.equals(pendingKey)) {
            return;
         }
         if (previewRL != null) {
            try {
               mc.getTextureManager().deleteTexture(previewRL);
            } catch (Exception ignored) {
            }
         }
         previewRL = mc.getTextureManager().getDynamicTextureLocation(
            "esdeath_skin_preview", new DynamicTexture(img));
         previewSlim = slim;
         previewKey = key;
         // keep the model toggle honest when a username resolved to a specific variant
         if (slim != "slim".equals(variant)) {
            variant = slim ? "slim" : "classic";
            if (variantButton != null) {
               variantButton.displayString = variantLabel();
            }
         }
      });
   }

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      drawDefaultBackground();
      drawCenteredString(fontRendererObj, "Skinchanger", width / 2, height / 2 - 80, 0xFFFFFF);
      drawCenteredString(fontRendererObj, TextFormatting.translate(
         "&7Previews instantly. The real Mojang change is applied when you leave the server.&r"),
         width / 2, height / 2 - 68, -1);
      drawCenteredString(fontRendererObj, TextFormatting.translate(onServer()
            ? "&8On a server — Mojang upload is queued for disconnect.&r"
            : "&8Not on a server — Mojang upload runs immediately.&r"),
         width / 2, height / 2 - 56, -1);
      drawString(fontRendererObj, "Username or Skin URL:", width / 2 - 150, height / 2 - 34, 0xAAAAAA);
      if (status != null) {
         drawCenteredString(fontRendererObj, TextFormatting.translate(status), width / 2, height / 2 + 88, -1);
      }
      inputField.drawTextBox();
      drawPreview();
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   // 3D turntable of the resolved skin, to the left of the input column.
   private void drawPreview() {
      int boxCX = width / 2 - 205;
      int boxTop = height / 2 - 56;
      int boxW = 76, boxH = 128;
      int boxX = boxCX - boxW / 2;
      drawRect(boxX - 1, boxTop - 1, boxX + boxW + 1, boxTop + boxH + 1, 0x80000000);
      drawCenteredString(fontRendererObj, TextFormatting.translate("&7Preview"), boxCX, boxTop - 11, -1);

      if (previewRL != null) {
         SkinPreview3D.draw(boxCX, boxTop + boxH - 12, 45, this.spin, 0.0F, previewRL, previewSlim, true);
      } else {
         drawCenteredString(fontRendererObj, TextFormatting.translate(
            resolving ? "&7loading..." : "&8no skin"), boxCX, boxTop + boxH / 2 - 4, -1);
      }
   }

   @Override
   protected void keyTyped(char typedChar, int keyCode) {
      inputField.textboxKeyTyped(typedChar, keyCode);
      if (keyCode == Keyboard.KEY_ESCAPE) {
         mc.displayGuiScreen(previousScreen);
      }
   }

   @Override
   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
      try {
         super.mouseClicked(mouseX, mouseY, mouseButton);
      } catch (Exception ignored) {
      }
      inputField.mouseClicked(mouseX, mouseY, mouseButton);
   }

   @Override
   protected void actionPerformed(GuiButton button) {
      if (button == null || !button.enabled) {
         return;
      }
      switch (button.id) {
         case 0:
            variant = "slim".equals(variant) ? "classic" : "slim";
            variantButton.displayString = variantLabel();
            break;
         case 1:
            apply();
            break;
         case 2:
            mc.displayGuiScreen(previousScreen);
            break;
         case 3:
            clearSkin();
            break;
         case 4:
            pickFile();
            break;
      }
   }

   // Clear the session skin override + drop any pending Mojang change. Does NOT touch the user's saved
   // Force Skin, and does NOT revert a skin already uploaded to Mojang (apply a different one for that).
   private void clearSkin() {
      SessionSkin.clear();
      PendingSkin.clear();
      status = "&aSession skin override cleared.&r";
   }

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
               while ((n = in.read(chunk)) > 0) {
                  buf.write(chunk, 0, n);
               }
               data = buf.toByteArray();
            }
            uploadedBytes = data;
            uploadedName = name;
            uploadButton.displayString = uploadLabel();
            status = "&aLoaded " + name + " (" + data.length + " bytes)&r";
         } catch (Exception e) {
            status = "&cUpload failed: " + e.getMessage() + "&r";
         }
      }, "Esdeath-SkinPicker").start();
   }

   private void apply() {
      final boolean slim = "slim".equals(variant);

      // Uploaded file takes precedence over the text field.
      if (uploadedBytes != null) {
         final byte[] bytes = uploadedBytes;
         status = "&7Applying skin from file...&r";
         new Thread(() -> {
            try {
               SessionSkin.setFromBytes(bytes, slim); // session override (doesn't touch Force Skin)
               PendingSkin.queueBytes(bytes, variant);
               uploadedBytes = null;
               uploadedName = null;
               uploadButton.displayString = uploadLabel();
               status = finishStatus();
            } catch (Exception e) {
               status = "&cSkin apply failed: " + e.getMessage() + "&r";
            }
         }, "Esdeath-Skin").start();
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
         status = "&7Downloading skin...&r";
         new Thread(() -> {
            try {
               SessionSkin.setFromUrl(input, slim); // session override (doesn't touch Force Skin)
               PendingSkin.queueUrl(input, variant);
               status = finishStatus();
            } catch (Exception e) {
               status = "&cSkin apply failed: " + e.getMessage() + "&r";
            }
         }, "Esdeath-Skin").start();
         return;
      }

      // username -> resolve to Mojang skin URL + variant, then apply
      status = "&7Resolving " + input + "'s skin...&r";
      new Thread(() -> {
         SkinChanger.SkinInfo info = SkinChanger.resolveSkin(input);
         if (info == null) {
            status = "&cCouldn't find a skin for " + input + "&r";
            return;
         }
         variant = info.variant;
         variantButton.displayString = variantLabel();
         try {
            SessionSkin.setFromUrl(info.url, "slim".equals(info.variant)); // session override
            PendingSkin.queueUrl(info.url, info.variant);
            status = finishStatus();
         } catch (Exception e) {
            status = "&cSkin apply failed: " + e.getMessage() + "&r";
         }
      }, "Esdeath-Skin").start();
   }

   // Local preview is live. If we're not on a server, push the real Mojang change now; otherwise it
   // waits for the disconnect.
   private String finishStatus() {
      if (!onServer()) {
         PendingSkin.flushAsync();
         return "&aSkin applied — uploading to Mojang now.&r";
      }
      return "&aSkin applied locally — Mojang upload queued for when you leave the server.&r";
   }
}
