package me.proxycracked.universalaccountmanager.gui;

import me.proxycracked.universalaccountmanager.UniversalAccountManager;
import me.proxycracked.universalaccountmanager.auth.Account;
import me.proxycracked.universalaccountmanager.auth.AccountValidator;
import me.proxycracked.universalaccountmanager.auth.CookieAuth;
import me.proxycracked.universalaccountmanager.auth.MicrosoftAuth;
import me.proxycracked.universalaccountmanager.auth.RefreshTokenAuth;
import me.proxycracked.universalaccountmanager.auth.SessionManager;
import me.proxycracked.universalaccountmanager.auth.TokenAuth;
import me.proxycracked.universalaccountmanager.skin.SkinHeadCache;
import me.proxycracked.universalaccountmanager.utils.Notification;
import me.proxycracked.universalaccountmanager.utils.TextFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class GuiAccountManager extends GuiScreen {
  private final GuiScreen previousScreen;

  private GuiButton addAccountButton;
  private GuiButton clearExpiredButton;
  private GuiButton changeSkinButton;
  private GuiButton changeUsernameButton;
  private GuiButton saveLauncherButton;
  private GuiButton forceSkinButton;
  private GuiButton deleteButton;
  private GuiButton cancelButton;
  private GuiButton streamerModeButton;
  private GuiAccountList accountList;
  private Notification notification;
  private int selectedAccount = -1;
  private ExecutorService executor;
  private CompletableFuture<Void> task;

  // Drag-to-reorder state. dragSource = slot grabbed; dragTarget = where it
  // will land if released right now (already constrained against the pinned
  // section). Both -1 when not dragging.
  private int dragSource = -1;
  private int dragTarget = -1;
  private int dragMouseY = 0;
  private boolean dragMoved = false;

  // Index of the account awaiting delete confirmation. -1 when no prompt is open.
  private int pendingDeleteIndex = -1;
  private static final int CONFIRM_DELETE_ID = 100;

  // Right-click context menu state. ctxOpen=false when no menu is showing.
  private boolean ctxOpen = false;
  private int ctxX, ctxY;
  private int ctxTarget = -1;
  private static final int CTX_W = 78;
  private static final int CTX_ROW_H = 14;

  public GuiAccountManager(GuiScreen previousScreen) {
    this.previousScreen = previousScreen;
  }

  public GuiAccountManager(GuiScreen previousScreen, Notification notification) {
    this.previousScreen = previousScreen;
    this.notification = notification;
  }

  @Override
  public void initGui() {
    UniversalAccountManager.load();
    Keyboard.enableRepeatEvents(true);

    buttonList.clear();
    int btnW = 100;
    int gap = 4;
    int rowY1 = height - 76;
    int rowY2 = height - 52;
    int rowY3 = height - 28;
    int rowStart = width / 2 - (btnW * 3 + gap * 2) / 2;

    // Row 1 — username editor (single button, swaps to Save as Token for launcher accounts)
    buttonList.add(changeUsernameButton = new GuiButton(6,  width / 2 - btnW / 2,           rowY1, btnW, 20, "Change Username"));
    buttonList.add(saveLauncherButton   = new GuiButton(9,  width / 2 - btnW / 2,           rowY1, btnW, 20, "Save as Token"));
    // Row 2 — primary actions
    buttonList.add(changeSkinButton     = new GuiButton(5,  rowStart,                       rowY2, btnW, 20, "Skinchanger"));
    buttonList.add(addAccountButton     = new GuiButton(1,  rowStart + (btnW + gap),        rowY2, btnW, 20, "Add Account"));
    buttonList.add(forceSkinButton      = new GuiButton(10, rowStart + (btnW + gap) * 2,    rowY2, btnW, 20, "Force Skin"));
    // Row 3 — destructive / exit
    buttonList.add(clearExpiredButton   = new GuiButton(8,  rowStart,                       rowY3, btnW, 20, "Clear Expired"));
    buttonList.add(deleteButton         = new GuiButton(2,  rowStart + (btnW + gap),        rowY3, btnW, 20, "Delete Account"));
    buttonList.add(cancelButton         = new GuiButton(3,  rowStart + (btnW + gap) * 2,    rowY3, btnW, 20, "Cancel"));

    // Streamer mode toggle — top right corner.
    int smW = 100;
    buttonList.add(streamerModeButton   = new GuiButton(13, width - smW - 4, 4, smW, 20, streamerButtonLabel()));

    accountList = new GuiAccountList(mc);
    accountList.registerScrollButtons(11, 12);

    AccountValidator.validateAll();
    // Convert any stored refresh-token accounts into usable token accounts by
    // re-deriving a fresh access token. Runs every time the list refreshes.
    RefreshTokenAuth.refreshAll(null);
    // Re-auth cookie accounts that have a stored cookie file, so their
    // short-lived access tokens don't expire and grey the account out.
    CookieAuth.refreshAll(null);
    updateScreen();
  }

  private Account activeAccount() {
    String token = SessionManager.get().getToken();
    if (token == null) return null;
    for (Account acc : UniversalAccountManager.accounts) {
      if (token.equals(acc.getAccessToken())) return acc;
    }
    return null;
  }

  @Override
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
    if (task != null && !task.isDone()) {
      task.cancel(true);
      executor.shutdownNow();
    }
  }

  @Override
  public void updateScreen() {
    boolean has = selectedAccount >= 0 && selectedAccount < UniversalAccountManager.accounts.size();
    if (deleteButton != null) {
      deleteButton.enabled = has;
    }
    Account active = activeAccount();
    if (changeUsernameButton != null) {
      // Hide only for launcher accounts (Yggdrasil token gets rejected by
      // Mojang's rename endpoint).
      changeUsernameButton.visible = active != null && !active.isLauncher();
    }
    if (saveLauncherButton != null) {
      saveLauncherButton.visible = active != null && active.isLauncher();
    }
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float renderPartialTicks) {
    if (accountList != null) accountList.drawScreen(mouseX, mouseY, renderPartialTicks);
    super.drawScreen(mouseX, mouseY, renderPartialTicks);

    drawCenteredString(fontRendererObj,
      TextFormatting.translate(String.format("&fUniversal Account Manager &8(&7%s&8)&r", UniversalAccountManager.accounts.size())),
      width / 2, 16, -1);

    String activeName = UniversalAccountManager.streamerMode
      ? UniversalAccountManager.HIDDEN_LABEL
      : SessionManager.get().getUsername();
    String activeText = TextFormatting.translate(String.format(
      "&7Active: &b%s&r", activeName));
    drawString(fontRendererObj, activeText, 5, 5, -1);

    if (notification != null && !notification.isExpired()) {
      String t = notification.getMessage();
      int w = fontRendererObj.getStringWidth(t);
      Gui.drawRect(width / 2 - w / 2 - 4, 28, width / 2 + w / 2 + 4, 28 + fontRendererObj.FONT_HEIGHT + 6, 0x80000000);
      drawCenteredString(fontRendererObj, t, width / 2, 31, -1);
    }

    // Drag feedback: render the dragged account as a translucent ghost row
    // following the cursor.
    if (dragSource >= 0 && dragMoved && accountList != null
        && dragSource < UniversalAccountManager.accounts.size()) {
      int top = accountList.getTopY();
      int bot = accountList.getBottomY() - 28;
      int rowY = Math.max(top, Math.min(bot, mouseY - 14));
      int rowX = width / 2 - accountList.getListWidth() / 2 + 4;
      Gui.drawRect(rowX - 2, rowY - 1, rowX + accountList.getListWidth() - 4, rowY + 27, 0xC0202830);
      Gui.drawRect(rowX - 2, rowY - 1, rowX + accountList.getListWidth() - 4, rowY,     0xFFFFD700);
      Gui.drawRect(rowX - 2, rowY + 26, rowX + accountList.getListWidth() - 4, rowY + 27, 0xFFFFD700);
      accountList.drawAccountRow(rowX, rowY, dragSource);
    }

    if (ctxOpen) {
      drawContextMenu(mouseX, mouseY);
    }
  }

  private void drawContextMenu(int mouseX, int mouseY) {
    String[] items = contextItems();
    int h = items.length * CTX_ROW_H + 2;
    Gui.drawRect(ctxX - 1, ctxY - 1, ctxX + CTX_W + 1, ctxY + h + 1, 0xFF000000);
    Gui.drawRect(ctxX, ctxY, ctxX + CTX_W, ctxY + h, 0xFF202830);
    for (int i = 0; i < items.length; i++) {
      int rowY = ctxY + 1 + i * CTX_ROW_H;
      boolean hover = mouseX >= ctxX && mouseX <= ctxX + CTX_W
                   && mouseY >= rowY && mouseY < rowY + CTX_ROW_H;
      if (hover) Gui.drawRect(ctxX, rowY, ctxX + CTX_W, rowY + CTX_ROW_H, 0xFF3A4858);
      drawString(fontRendererObj, TextFormatting.translate(items[i]),
        ctxX + 6, rowY + 3, hover ? 0xFFFFFFFF : 0xFFCCCCCC);
    }
  }

  private String[] contextItems() {
    if (ctxTarget < 0 || ctxTarget >= UniversalAccountManager.accounts.size()) {
      return new String[0];
    }
    Account a = UniversalAccountManager.accounts.get(ctxTarget);
    return new String[]{
      a.isPinned() ? "&eUnpin&r" : "&ePin&r",
      "&aLogin&r"
    };
  }

  private int contextItemAt(int mouseX, int mouseY) {
    String[] items = contextItems();
    if (mouseX < ctxX || mouseX > ctxX + CTX_W) return -1;
    for (int i = 0; i < items.length; i++) {
      int rowY = ctxY + 1 + i * CTX_ROW_H;
      if (mouseY >= rowY && mouseY < rowY + CTX_ROW_H) return i;
    }
    return -1;
  }

  private boolean contextHit(int mouseX, int mouseY) {
    if (!ctxOpen) return false;
    int h = contextItems().length * CTX_ROW_H + 2;
    return mouseX >= ctxX && mouseX <= ctxX + CTX_W
        && mouseY >= ctxY && mouseY <= ctxY + h;
  }

  private void openContext(int mouseX, int mouseY, int slot) {
    ctxOpen = true;
    ctxTarget = slot;
    int h = contextItems().length * CTX_ROW_H + 2;
    ctxX = Math.min(mouseX, width - CTX_W - 2);
    ctxY = Math.min(mouseY, height - h - 2);
  }

  private void closeContext() {
    ctxOpen = false;
    ctxTarget = -1;
  }

  @Override
  public void handleMouseInput() throws IOException {
    if (ctxOpen) {
      int mx = org.lwjgl.input.Mouse.getEventX() * this.width / mc.displayWidth;
      int my = this.height - org.lwjgl.input.Mouse.getEventY() * this.height / mc.displayHeight - 1;
      if (!contextHit(mx, my) && accountList != null) accountList.handleMouseInput();
    } else if (accountList != null) {
      accountList.handleMouseInput();
    }
    super.handleMouseInput();
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    if (ctxOpen) {
      if (mouseButton == 0) {
        int item = contextItemAt(mouseX, mouseY);
        int target = ctxTarget;
        closeContext();
        if (item == 0) {
          togglePin(target);
        } else if (item == 1) {
          selectedAccount = target;
          updateScreen();
          doLogin();
        }
        return;
      }
      closeContext();
      return;
    }
    super.mouseClicked(mouseX, mouseY, mouseButton);

    if (mouseButton == 1 && accountList != null
        && mouseY >= accountList.getTopY() && mouseY <= accountList.getBottomY()) {
      int idx = accountList.slotForDrag(mouseY);
      if (idx >= 0 && idx < UniversalAccountManager.accounts.size()) {
        selectedAccount = idx;
        openContext(mouseX, mouseY, idx);
        updateScreen();
      }
    }
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) {
    switch (keyCode) {
      case Keyboard.KEY_UP:
        if (selectedAccount > 0) {
          if (isCtrlKeyDown()) {
            if (canMoveUp(selectedAccount)) {
              Collections.swap(UniversalAccountManager.accounts, selectedAccount, selectedAccount - 1);
              --selectedAccount;
              UniversalAccountManager.save();
            }
          } else {
            --selectedAccount;
          }
        }
        break;
      case Keyboard.KEY_DOWN:
        if (selectedAccount < UniversalAccountManager.accounts.size() - 1) {
          if (isCtrlKeyDown()) {
            if (canMoveDown(selectedAccount)) {
              Collections.swap(UniversalAccountManager.accounts, selectedAccount, selectedAccount + 1);
              ++selectedAccount;
              UniversalAccountManager.save();
            }
          } else {
            ++selectedAccount;
          }
        }
        break;
      case Keyboard.KEY_RETURN: doLogin(); break;
      case Keyboard.KEY_DELETE: actionPerformed(deleteButton); break;
      case Keyboard.KEY_ESCAPE: actionPerformed(cancelButton); break;
    }
    if (isKeyComboCtrlC(keyCode) && selectedAccount >= 0
        && selectedAccount < UniversalAccountManager.accounts.size()) {
      setClipboardString(UniversalAccountManager.accounts.get(selectedAccount).getUsername());
    }
  }

  private boolean canMoveUp(int idx) {
    if (idx <= 0) return false;
    Account a = UniversalAccountManager.accounts.get(idx);
    int floor = UniversalAccountManager.firstUnpinnedIndex();
    // Pinned accounts: free to move up within the pinned section.
    // Unpinned accounts: blocked from crossing the floor into pinned territory.
    return a.isPinned() ? idx > 0 : idx > floor;
  }

  private boolean canMoveDown(int idx) {
    if (idx >= UniversalAccountManager.accounts.size() - 1) return false;
    Account a = UniversalAccountManager.accounts.get(idx);
    int floor = UniversalAccountManager.firstUnpinnedIndex();
    // Pinned accounts can move down only while still inside the pinned block.
    return a.isPinned() ? idx < floor - 1 : true;
  }

  @Override
  protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    if (clickedMouseButton != 0 || dragSource < 0) return;
    if (Math.abs(mouseY - dragMouseY) > 3) dragMoved = true;
    int target = accountList.slotForDrag(mouseY);
    if (target < 0) return;
    Account src = UniversalAccountManager.accounts.get(dragSource);
    int floor = UniversalAccountManager.firstUnpinnedIndex();
    if (src.isPinned()) {
      // Clamp drag into the pinned section: [0, floor-1].
      if (target >= floor) target = Math.max(0, floor - 1);
    } else {
      if (target < floor) target = floor;
    }
    dragTarget = target;
  }

  @Override
  protected void mouseReleased(int mouseX, int mouseY, int state) {
    super.mouseReleased(mouseX, mouseY, state);
    if (state == 0 && dragSource >= 0 && dragMoved && dragTarget >= 0 && dragSource != dragTarget) {
      Account moving = UniversalAccountManager.accounts.remove(dragSource);
      int insert = dragTarget;
      if (insert > UniversalAccountManager.accounts.size()) insert = UniversalAccountManager.accounts.size();
      UniversalAccountManager.accounts.add(insert, moving);
      selectedAccount = UniversalAccountManager.accounts.indexOf(moving);
      UniversalAccountManager.save();
      updateScreen();
    }
    dragSource = -1;
    dragTarget = -1;
    dragMoved = false;
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button == null || !button.enabled) return;
    switch (button.id) {
      case 0: doLogin(); break;
      case 1: mc.displayGuiScreen(new GuiAddAccount(previousScreen)); break;
      case 2: doDelete(); break;
      case 3: mc.displayGuiScreen(previousScreen); break;
      case 5: mc.displayGuiScreen(new GuiChangeSkin(this)); break;
      case 6: mc.displayGuiScreen(new GuiChangeUsername(this)); break;
      case 8: doClearExpired(); break;
      case 9: doSaveLauncher(); break;
      case 10: mc.displayGuiScreen(new GuiForceSkin(this)); break;
      case 13: toggleStreamerMode(); break;
      default: if (accountList != null) accountList.actionPerformed(button);
    }
  }

  private String streamerButtonLabel() {
    return TextFormatting.translate(UniversalAccountManager.streamerMode
      ? "&aStreamer: ON&r"
      : "&7Streamer: OFF&r");
  }

  private void toggleStreamerMode() {
    UniversalAccountManager.streamerMode = !UniversalAccountManager.streamerMode;
    UniversalAccountManager.savePrefs();
    if (streamerModeButton != null) streamerModeButton.displayString = streamerButtonLabel();
  }

  private void togglePin(int idx) {
    if (idx < 0 || idx >= UniversalAccountManager.accounts.size()) return;
    Account acc = UniversalAccountManager.accounts.get(idx);
    acc.setPinned(!acc.isPinned());
    UniversalAccountManager.resort();
    selectedAccount = UniversalAccountManager.accounts.indexOf(acc);
    UniversalAccountManager.save();
    updateScreen();
  }

  private void doClearExpired() {
    int before = UniversalAccountManager.accounts.size();
    UniversalAccountManager.accounts.removeIf(a -> Boolean.FALSE.equals(a.getAvailable()));
    int removed = before - UniversalAccountManager.accounts.size();
    if (removed > 0) {
      UniversalAccountManager.save();
      selectedAccount = -1;
      notification = new Notification(TextFormatting.translate(String.format(
        "&aRemoved %d expired account%s.&r", removed, removed == 1 ? "" : "s")), 4000L);
    } else {
      notification = new Notification(TextFormatting.translate(
        "&7No expired accounts to remove.&r"), 3000L);
    }
    updateScreen();
  }

  private void doSaveLauncher() {
    Account active = activeAccount();
    if (active == null || !active.isLauncher()) return;
    active.setType(Account.TYPE_TOKEN);
    UniversalAccountManager.save();
    notification = new Notification(TextFormatting.translate(String.format(
      "&aSaved &b%s&a as a stored account.&r", active.getUsername())), 4000L);
    updateScreen();
  }

  private void doDelete() {
    if (selectedAccount < 0 || selectedAccount >= UniversalAccountManager.accounts.size()) return;
    Account acc = UniversalAccountManager.accounts.get(selectedAccount);
    String username = StringUtils.isBlank(acc.getUsername()) ? "this account" : acc.getUsername();
    pendingDeleteIndex = selectedAccount;
    mc.displayGuiScreen(new GuiYesNo(this,
      "Delete \"" + username + "\"?",
      "This cannot be undone.",
      "Delete", "Cancel",
      CONFIRM_DELETE_ID));
  }

  @Override
  public void confirmClicked(boolean result, int id) {
    if (id == CONFIRM_DELETE_ID) {
      int idx = pendingDeleteIndex;
      pendingDeleteIndex = -1;
      if (result && idx >= 0 && idx < UniversalAccountManager.accounts.size()) {
        UniversalAccountManager.accounts.remove(idx);
        UniversalAccountManager.save();
        selectedAccount = -1;
      }
      mc.displayGuiScreen(this);
      updateScreen();
      return;
    }
    super.confirmClicked(result, id);
  }

  private void doLogin() {
    if (task != null && !task.isDone()) return;
    if (selectedAccount < 0 || selectedAccount >= UniversalAccountManager.accounts.size()) return;
    if (executor == null) executor = Executors.newSingleThreadExecutor();
    final Account account = UniversalAccountManager.accounts.get(selectedAccount);
    final String username = StringUtils.isBlank(account.getUsername()) ? "???" : account.getUsername();

    if (account.isRefresh()) {
      notification = new Notification(TextFormatting.translate(String.format(
        "&7Converting refresh token... (%s)&r", username)), -1L);
      task = CompletableFuture.runAsync(() -> {
        try {
          RefreshTokenAuth.Result r = RefreshTokenAuth.convert(account.getRefreshToken());
          account.setAccessToken(r.mcAccessToken);
          if (StringUtils.isNotBlank(r.refreshToken)) account.setRefreshToken(r.refreshToken);
          account.setUsername(r.username);
          account.setUuid(r.uuid);
          account.setAvailable(Boolean.TRUE);
          UniversalAccountManager.save();
          SessionManager.set(new Session(r.username, r.uuid, r.mcAccessToken, Session.Type.MOJANG.toString()));
          notification = new Notification(TextFormatting.translate(String.format(
            "&aLogged in! (%s)&r", r.username)), 5000L);
        } catch (Exception e) {
          account.setAvailable(Boolean.FALSE);
          notification = new Notification(TextFormatting.translate(String.format(
            "&cRefresh failed: %s (%s)&r", e.getMessage(), username)), 5000L);
        }
      }, executor);
      return;
    }

    // Cookie accounts with a stored cookie file re-auth from the long-lived
    // cookies, guaranteeing a fresh token even if the saved one expired.
    if (account.isCookie() && !account.getCookieFile().isEmpty() && new File(account.getCookieFile()).exists()) {
      notification = new Notification(TextFormatting.translate(String.format(
        "&7Refreshing cookie session... (%s)&r", username)), -1L);
      task = CookieAuth.loginFromFile(new File(account.getCookieFile()), s -> {}, executor)
        .thenAccept(result -> {
          account.setAccessToken(result.accessToken);
          account.setUsername(result.session.getUsername());
          account.setUuid(result.session.getPlayerID());
          account.setAvailable(Boolean.TRUE);
          UniversalAccountManager.save();
          SessionManager.set(result.session);
          notification = new Notification(TextFormatting.translate(String.format(
            "&aLogged in! (%s)&r", result.session.getUsername())), 5000L);
        })
        .exceptionally(err -> {
          account.setAvailable(Boolean.FALSE);
          notification = new Notification(TextFormatting.translate(String.format(
            "&cCookie re-auth failed (%s) — re-import the cookie file.&r", username)), 5000L);
          return null;
        });
      return;
    }

    if (account.isToken() || account.isCookie()) {
      notification = new Notification(TextFormatting.translate(String.format(
        "&7Validating token... (%s)&r", username)), -1L);
      task = TokenAuth.login(account.getAccessToken(), executor)
        .thenAccept(session -> {
          account.setUsername(session.getUsername());
          account.setUuid(session.getPlayerID());
          UniversalAccountManager.save();
          SessionManager.set(session);
          notification = new Notification(TextFormatting.translate(String.format(
            "&aLogged in! (%s)&r", account.getUsername())), 5000L);
        })
        .exceptionally(err -> {
          notification = new Notification(TextFormatting.translate(String.format(
            "&cToken rejected (%s) — re-add this account.&r", username)), 5000L);
          return null;
        });
      return;
    }

    AtomicReference<String> refreshToken = new AtomicReference<>("");
    AtomicReference<String> accessToken = new AtomicReference<>("");
    notification = new Notification(TextFormatting.translate(String.format(
      "&7Fetching profile... (%s)&r", username)), -1L);
    task = MicrosoftAuth.login(account.getAccessToken(), executor)
      .handle((session, error) -> {
        if (session != null) {
          account.setUsername(session.getUsername());
          account.setUuid(session.getPlayerID());
          UniversalAccountManager.save();
          SessionManager.set(session);
          notification = new Notification(TextFormatting.translate(String.format(
            "&aLogged in! (%s)&r", account.getUsername())), 5000L);
          return true;
        }
        return false;
      })
      .thenComposeAsync(completed -> {
        if (completed) throw new NoSuchElementException();
        notification = new Notification(TextFormatting.translate(String.format(
          "&7Refreshing tokens... (%s)&r", username)), -1L);
        return MicrosoftAuth.refreshMSAccessTokens(account.getRefreshToken(), executor);
      })
      .thenComposeAsync(t -> {
        notification = new Notification(TextFormatting.translate(String.format(
          "&7Acquiring Xbox token... (%s)&r", username)), -1L);
        refreshToken.set(t.get("refresh_token"));
        return MicrosoftAuth.acquireXboxAccessToken(t.get("access_token"), executor);
      })
      .thenComposeAsync(x -> {
        notification = new Notification(TextFormatting.translate(String.format(
          "&7Acquiring XSTS token... (%s)&r", username)), -1L);
        return MicrosoftAuth.acquireXboxXstsToken(x, executor);
      })
      .thenComposeAsync(x -> {
        notification = new Notification(TextFormatting.translate(String.format(
          "&7Acquiring MC token... (%s)&r", username)), -1L);
        return MicrosoftAuth.acquireMCAccessToken(x.get("Token"), x.get("uhs"), executor);
      })
      .thenComposeAsync(t -> {
        notification = new Notification(TextFormatting.translate(String.format(
          "&7Fetching profile... (%s)&r", username)), -1L);
        accessToken.set(t);
        return MicrosoftAuth.login(t, executor);
      })
      .thenAccept(session -> {
        account.setRefreshToken(refreshToken.get());
        account.setAccessToken(accessToken.get());
        account.setUsername(session.getUsername());
        account.setUuid(session.getPlayerID());
        UniversalAccountManager.save();
        SessionManager.set(session);
        notification = new Notification(TextFormatting.translate(String.format(
          "&aLogged in! (%s)&r", account.getUsername())), 5000L);
      })
      .exceptionally(err -> {
        if (!(err.getCause() instanceof NoSuchElementException)) {
          notification = new Notification(TextFormatting.translate(String.format(
            "&c%s (%s)&r", err.getMessage(), username)), 5000L);
        }
        return null;
      });
  }

  // ==================================================================
  class GuiAccountList extends GuiSlot {
    public GuiAccountList(Minecraft mc) {
      super(mc, GuiAccountManager.this.width, GuiAccountManager.this.height,
        46, GuiAccountManager.this.height - 88, 28);
    }

    @Override protected int getSize() { return UniversalAccountManager.accounts.size(); }
    @Override protected boolean isSelected(int slot) { return slot == selectedAccount; }
    @Override protected int getScrollBarX() { return (this.width + getListWidth()) / 2 + 2; }
    @Override public int getListWidth() { return 360; }
    @Override protected int getContentHeight() { return UniversalAccountManager.accounts.size() * 28; }
    @Override protected void drawBackground() { drawDefaultBackground(); }

    int getTopY()    { return this.top; }
    int getBottomY() { return this.bottom; }

    int slotForDrag(int mouseY) {
      int idx = getSlotIndexFromScreenCoords(GuiAccountManager.this.width / 2, mouseY);
      if (idx >= 0) return idx;
      int size = UniversalAccountManager.accounts.size();
      if (size == 0) return -1;
      if (mouseY < this.top) return 0;
      return size - 1;
    }

    @Override
    protected void elementClicked(int slot, boolean dbl, int mx, int my) {
      selectedAccount = slot;
      if (slot >= 0 && slot < UniversalAccountManager.accounts.size()) {
        dragSource = slot;
        dragTarget = slot;
        dragMouseY = my;
        dragMoved = false;
      }
      updateScreen();
      if (dbl) doLogin();
    }

    @Override
    protected void drawSlot(int id, int x, int y, int k, int mx, int my) {
      if (dragSource == id && dragMoved) return;
      drawAccountRow(x, y, id);
    }

    void drawAccountRow(int x, int y, int id) {
      FontRenderer fr = fontRendererObj;
      Account acc = UniversalAccountManager.accounts.get(id);
      Session active = SessionManager.get();

      boolean unavailable = Boolean.FALSE.equals(acc.getAvailable());

      // Skin head
      int headSize = 24;
      int headX = x + 2;
      int headY = y + 1;
      ResourceLocation head = SkinHeadCache.get(acc.getUsername(), acc.getUuid());
      if (head != null) {
        if (unavailable) GlStateManager.color(0.45f, 0.45f, 0.45f, 1f);
        else             GlStateManager.color(1f, 1f, 1f, 1f);
        mc.getTextureManager().bindTexture(head);
        Gui.drawScaledCustomSizeModalRect(headX, headY, 0, 0, 64, 64, headSize, headSize, 64f, 64f);
        GlStateManager.color(1f, 1f, 1f, 1f);
      } else {
        Gui.drawRect(headX, headY, headX + headSize, headY + headSize, 0xFF222222);
        Gui.drawRect(headX + 1, headY + 1, headX + headSize - 1, headY + headSize - 1, 0xFF3A3A3A);
        drawCenteredString(fr, "?", headX + headSize / 2, headY + headSize / 2 - 4, 0xFF888888);
      }

      // Username (replaced with HIDDEN_LABEL in streamer mode, but keep status colors).
      String rawName = acc.getUsername();
      boolean isActiveByToken = acc.getAccessToken() != null && acc.getAccessToken().equals(active.getToken());
      boolean isActiveByName = rawName != null && rawName.equals(active.getUsername());
      String shown = UniversalAccountManager.streamerMode
        ? UniversalAccountManager.HIDDEN_LABEL
        : rawName;
      String username;
      if (StringUtils.isBlank(shown)) username = "&7&l?";
      else if (unavailable) username = "&8&m" + shown + "&r";
      else if (isActiveByToken) username = "&a&l" + shown;
      else if (isActiveByName) username = "&a" + shown;
      else username = shown;
      String pinPrefix = acc.isPinned() ? "&e★&r " : "";
      String usernameRendered = TextFormatting.translate(pinPrefix + "&r" + username + "&r");
      drawString(fr, usernameRendered, headX + headSize + 6, y + 4, -1);

      // Type badge
      String typeBadge;
      if (acc.isLauncher())    typeBadge = "&8[&fLAUNCHER&8]&r";
      else if (acc.isRefresh()) typeBadge = "&8[&3REFRESH&8]&r";
      else if (acc.isToken())  typeBadge = "&8[&dTOKEN&8]&r";
      else if (acc.isCookie()) typeBadge = "&8[&eCOOKIE&8]&r";
      else                     typeBadge = "&8[&bMS&8]&r";
      drawString(fr, TextFormatting.translate(typeBadge),
        headX + headSize + 6 + fr.getStringWidth(usernameRendered) + 6, y + 4, -1);

      // UUID line — also hidden in streamer mode.
      String uuid = acc.getUuid();
      if (uuid != null && uuid.length() >= 8) {
        String uuidShort = UniversalAccountManager.streamerMode
          ? "&8uuid: " + UniversalAccountManager.HIDDEN_LABEL + "&r"
          : "&8uuid: " + uuid.substring(0, 8) + "&r";
        drawString(fr, TextFormatting.translate(uuidShort),
          headX + headSize + 6, y + 16, -1);
      }
    }
  }
}
