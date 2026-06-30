package me.txb1.extras.labyconnect.gui;

import me.txb1.extras.labyconnect.LabyConnectManager;
import net.labymod.labyconnect.LabyConnect;
import net.labymod.labyconnect.log.MessageChatComponent;
import net.labymod.labyconnect.log.SingleChat;
import net.labymod.labyconnect.user.ChatUser;
import net.labymod.labyconnect.user.UserStatus;
import net.labymod.main.LabyMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

/**
 * "Chat" tab — Esdeath-styled LabyChat DM screen on the ported LabyConnect data.
 * Left = searchable friend list (presence + unread badge); right = selected DM thread + send box.
 * Top-right cogwheel sets your LabyMod online status (Online/Away/Busy/Offline).
 */
public class LabyChatGui extends GuiScreen {

    private static final int BTN_SEND = 1;
    private static final int BTN_DONE = 0;

    private static final int ROW_H = 18;
    private static final int LIST_X = 10;
    private static final int LIST_W = 150;
    private static final int TOP = 34; // below the tab bar

    private int listTop;
    private int listBottom;
    private int listScroll = 0;

    private int chatX;
    private int chatTop;
    private int chatBottom;
    private int chatScroll = 0;

    private GuiTextField input;
    private GuiTextField search;
    private ChatUser selected;

    // status cogwheel dropdown
    private boolean statusOpen = false;
    private int gearX, gearY, gearW, gearH;

    // image embeds
    private int chatMaxScroll = 0;
    private volatile String uploadStatus = null;
    private final List<EmbedHit> embedHits = new ArrayList<EmbedHit>();

    private static final class EmbedHit {
        final int x, y, w, h;
        final String url;
        EmbedHit(int x, int y, int w, int h, String url) { this.x = x; this.y = y; this.w = w; this.h = h; this.url = url; }
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.listTop = TOP + 20; // leave room for the search box
        this.listBottom = this.height - 38; // leave room for the Done button below the list
        this.chatX = LIST_X + LIST_W + 10;
        this.chatTop = TOP + 8;
        this.chatBottom = this.height - 40;

        this.search = new GuiTextField(1, this.fontRendererObj, LIST_X + 2, TOP, LIST_W - 4, 16);
        this.search.setMaxStringLength(32);
        this.search.setEnableBackgroundDrawing(false);

        this.buttonList.clear();
        int inputX = this.chatX;
        int inputW = this.width - this.chatX - 70;
        this.input = new GuiTextField(0, this.fontRendererObj, inputX + 2, this.height - 34, inputW - 4, 16);
        this.input.setMaxStringLength(256);
        this.input.setEnableBackgroundDrawing(false);
        this.buttonList.add(new me.txb1.forge.gui.ThemedButton(BTN_SEND, this.width - 66, this.height - 36, 56, 20, "Send"));
        this.buttonList.add(new me.txb1.forge.gui.ThemedButton(BTN_DONE, LIST_X, this.height - 32, 70, 20, "Done"));

        LabyConnect lc = LabyConnectManager.get();
        if (lc != null) {
            try { lc.sortFriendList(0); } catch (Throwable ignored) {}
            List<ChatUser> friends = friends();
            for (ChatUser u : friends) {
                if (u.getUnreadMessages() > 0) { select(u); break; }
            }
            if (selected == null && !friends.isEmpty()) select(friends.get(0));
        }
    }

    private List<ChatUser> allFriends() {
        LabyConnect lc = LabyConnectManager.get();
        if (lc == null) return new ArrayList<ChatUser>();
        List<ChatUser> sorted = lc.getSortFriends();
        if (sorted != null && !sorted.isEmpty()) return sorted;
        return lc.getFriends() == null ? new ArrayList<ChatUser>() : lc.getFriends();
    }

    /** allFriends() filtered by the search box. */
    private List<ChatUser> friends() {
        String q = this.search == null ? "" : this.search.getText().trim().toLowerCase();
        if (q.isEmpty()) return allFriends();
        List<ChatUser> out = new ArrayList<ChatUser>();
        for (ChatUser u : allFriends()) {
            String name = u.getGameProfile() == null ? "" : u.getGameProfile().getName();
            if (name != null && name.toLowerCase().contains(q)) out.add(u);
        }
        return out;
    }

    private void select(ChatUser user) {
        this.selected = user;
        this.chatScroll = 0;
        if (user != null) user.setUnreadMessages(0);
    }

    private SingleChat chatOf(ChatUser user) {
        LabyConnect lc = LabyConnectManager.get();
        if (lc == null || user == null) return null;
        return lc.getChatlogManager().getChat(user);
    }

    private UserStatus myStatus() {
        LabyConnect lc = LabyConnectManager.get();
        if (lc == null) return UserStatus.ONLINE;
        try { return lc.getClientProfile().getUserStatus(); } catch (Throwable t) { return UserStatus.ONLINE; }
    }

    private void setStatus(UserStatus s) {
        LabyConnect lc = LabyConnectManager.get();
        if (lc == null) return;
        try {
            lc.getClientProfile().setUserStatus(s);
            lc.getClientProfile().sendSettingsToServer();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        this.statusOpen = false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        LabyConnect lc = LabyConnectManager.get();

        ChatTabBar.draw(this, this.fontRendererObj, mouseX, mouseY, ChatTabBar.Tab.CHAT);
        drawStatusGear(mouseX, mouseY);

        // search box
        drawRect(LIST_X, TOP - 2, LIST_X + LIST_W, TOP + 16, 0x66000000);
        if (this.search.getText().isEmpty() && !this.search.isFocused()) {
            this.fontRendererObj.drawString("Search…", LIST_X + 4, TOP + 4, 0xFF888888);
        }
        this.search.drawTextBox();

        // panels
        drawRect(LIST_X, this.listTop, LIST_X + LIST_W, this.listBottom, 0x66000000);
        drawRect(this.chatX, this.chatTop, this.width - 10, this.chatBottom, 0x66000000);

        drawFriends(mouseX, mouseY);
        drawChat();

        if (this.selected != null) {
            drawRect(this.chatX, this.height - 38, this.width - 70, this.height - 18, 0x88000000);
            if (this.input.getText().isEmpty() && !this.input.isFocused()) {
                this.fontRendererObj.drawString("Message…  (Ctrl+V to paste an image)",
                    this.chatX + 4, this.height - 30, 0xFF777777);
            }
            this.input.drawTextBox();
        } else if (lc != null) {
            this.drawCenteredString(this.fontRendererObj, "Select a friend to chat",
                (this.chatX + this.width - 10) / 2, (this.chatTop + this.chatBottom) / 2, 0xFFAAAAAA);
        }

        if (lc != null && friends().isEmpty()) {
            this.drawCenteredString(this.fontRendererObj,
                LabyConnectManager.isOnline() ? "No friends" : "Loading…",
                LIST_X + LIST_W / 2, this.listTop + 10, 0xFFAAAAAA);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.statusOpen) drawStatusDropdown(mouseX, mouseY);
    }

    private void drawStatusGear(int mouseX, int mouseY) {
        UserStatus s = myStatus();
        String label = "⚙ " + statusName(s);
        gearW = this.fontRendererObj.getStringWidth(label) + 10;
        gearH = 16;
        gearX = this.width - 10 - gearW;
        gearY = 14;
        boolean hov = mouseX >= gearX && mouseX <= gearX + gearW && mouseY >= gearY && mouseY <= gearY + gearH;
        drawRect(gearX, gearY, gearX + gearW, gearY + gearH, hov ? 0x88000000 : 0x66000000);
        this.fontRendererObj.drawStringWithShadow("§" + s.getChatColor() + label, gearX + 5, gearY + 4, 0xFFFFFFFF);
    }

    // dropdown geometry (set in draw, read in click so they stay in sync)
    private int ddX, ddY, ddW, ddRowH;

    private void drawStatusDropdown(int mouseX, int mouseY) {
        UserStatus[] all = { UserStatus.ONLINE, UserStatus.AWAY, UserStatus.BUSY, UserStatus.OFFLINE };
        boolean alerts = LabyMod.getSettings().alertsOnlineStatus;
        String alertLabel = "Alerts: " + (alerts ? "§aOn" : "§7Off");
        int rh = 14;
        // widen enough for the alerts row, right-align under the gear
        int w = Math.max(gearW, this.fontRendererObj.getStringWidth("Alerts: Off") + 12);
        int x = gearX + gearW - w;
        int y = gearY + gearH + 2;
        this.ddX = x; this.ddY = y; this.ddW = w; this.ddRowH = rh;
        int rows = all.length + 1; // statuses + alerts toggle
        drawRect(x, y, x + w, y + rh * rows, 0xEE101010);
        for (int i = 0; i < all.length; i++) {
            int ry = y + i * rh;
            boolean hov = mouseX >= x && mouseX <= x + w && mouseY >= ry && mouseY < ry + rh;
            if (hov) drawRect(x, ry, x + w, ry + rh, 0x44FFFFFF);
            this.fontRendererObj.drawStringWithShadow("§" + all[i].getChatColor() + statusName(all[i]), x + 5, ry + 3, 0xFFFFFFFF);
        }
        // alerts toggle row (separated by a thin divider)
        int ry = y + all.length * rh;
        drawRect(x, ry, x + w, ry + 1, 0x55FFFFFF);
        boolean hov = mouseX >= x && mouseX <= x + w && mouseY >= ry && mouseY < ry + rh;
        if (hov) drawRect(x, ry + 1, x + w, ry + rh, 0x44FFFFFF);
        this.fontRendererObj.drawStringWithShadow("§f" + alertLabel, x + 5, ry + 3, 0xFFFFFFFF);
    }

    private static String statusName(UserStatus s) {
        switch (s) {
            case ONLINE: return "Online";
            case AWAY: return "Away";
            case BUSY: return "Busy";
            case OFFLINE: return "Invisible";
            default: return s.name();
        }
    }

    private void drawFriends(int mouseX, int mouseY) {
        List<ChatUser> friends = friends();
        int y = this.listTop + 2 - this.listScroll;
        enableScissor(LIST_X, this.listTop, LIST_W, this.listBottom - this.listTop);
        for (ChatUser u : friends) {
            if (y + ROW_H >= this.listTop && y <= this.listBottom) {
                boolean hovered = mouseX >= LIST_X && mouseX <= LIST_X + LIST_W && mouseY >= y && mouseY < y + ROW_H;
                if (u == this.selected) drawRect(LIST_X, y, LIST_X + LIST_W, y + ROW_H, 0x66FFFFFF);
                else if (hovered) drawRect(LIST_X, y, LIST_X + LIST_W, y + ROW_H, 0x44FFFFFF);

                drawRect(LIST_X + 4, y + ROW_H / 2 - 2, LIST_X + 8, y + ROW_H / 2 + 2,
                    u.isOnline() ? 0xFF4CC94C : 0xFF808080);

                String name = u.getGameProfile() == null ? "?" : u.getGameProfile().getName();
                this.fontRendererObj.drawStringWithShadow(
                    this.fontRendererObj.trimStringToWidth(name, LIST_W - 40),
                    LIST_X + 12, y + 5, u.isOnline() ? 0xFFFFFFFF : 0xFFBBBBBB);

                int unread = u.getUnreadMessages();
                if (unread > 0) {
                    String b = unread > 99 ? "99+" : String.valueOf(unread);
                    int bw = this.fontRendererObj.getStringWidth(b) + 6;
                    int bx = LIST_X + LIST_W - bw - 3;
                    drawRect(bx, y + 4, bx + bw, y + 14, 0xFFD03030);
                    this.fontRendererObj.drawString(b, bx + 3, y + 5, 0xFFFFFFFF);
                }
            }
            y += ROW_H;
        }
        disableScissor();
    }

    /** A rendered block: either a text line or an image embed. */
    private static final class Item {
        String text;          // non-null for text
        String imgUrl;        // non-null for embed
        int drawW, drawH;     // embed draw size
        int height;           // total block height
    }

    private void drawChat() {
        this.embedHits.clear();
        if (this.selected == null) return;
        SingleChat chat = chatOf(this.selected);
        String header = (this.selected.getGameProfile() == null ? "?" : this.selected.getGameProfile().getName())
            + (this.selected.isOnline() ? "  §a● online" : "  §7● offline");
        this.fontRendererObj.drawStringWithShadow("§l" + header, this.chatX + 4, this.chatTop - 12, 0xFFFFFFFF);

        // upload status / hint line under the header
        if (this.uploadStatus != null) {
            this.fontRendererObj.drawString(this.uploadStatus, this.chatX + 4, this.chatTop - 2, 0xFFCCCC55);
        }

        if (chat == null) return;
        List<MessageChatComponent> messages = new ArrayList<MessageChatComponent>(chat.getMessages());
        String me = LabyMod.getInstance().getPlayerName();
        int lineH = this.fontRendererObj.FONT_HEIGHT + 2;
        int areaX = this.chatX + 6;
        int areaTop = this.chatTop + 4;
        int areaH = this.chatBottom - 40 - this.chatTop;
        int wrapW = this.width - 10 - this.chatX - 12;
        int maxW = Math.min(160, wrapW);
        int maxH = 120;

        // Build display blocks.
        List<Item> items = new ArrayList<Item>();
        int totalH = 0;
        for (MessageChatComponent m : messages) {
            boolean self = me != null && me.equalsIgnoreCase(m.getSender());
            String prefix = self ? "§b" + m.getSender() + "§f: " : "§a" + m.getSender() + "§f: ";
            String imgUrl = ImageEmbedCache.findImageUrl(m.getMessage());
            if (imgUrl != null) {
                Item label = new Item();
                label.text = prefix + "§7[image]";
                label.height = lineH;
                items.add(label);
                totalH += lineH;

                Item emb = new Item();
                emb.imgUrl = imgUrl;
                ImageEmbedCache.Embed e = ImageEmbedCache.get(imgUrl);
                if (e.location != null && e.width > 0 && e.height > 0) {
                    int dw = Math.min(maxW, e.width);
                    int dh = e.height * dw / e.width;
                    if (dh > maxH) { dh = maxH; dw = e.width * dh / e.height; }
                    emb.drawW = dw; emb.drawH = dh;
                } else {
                    emb.drawW = maxW; emb.drawH = 14; // loading/failed placeholder
                }
                emb.height = emb.drawH + 3;
                items.add(emb);
                totalH += emb.height;
            } else {
                for (String line : this.fontRendererObj.listFormattedStringToWidth(prefix + m.getMessage(), wrapW)) {
                    Item it = new Item();
                    it.text = line;
                    it.height = lineH;
                    items.add(it);
                    totalH += lineH;
                }
            }
        }

        this.chatMaxScroll = Math.max(0, totalH - areaH);
        if (this.chatScroll > this.chatMaxScroll) this.chatScroll = this.chatMaxScroll;
        int scrollPixels = this.chatMaxScroll - this.chatScroll; // 0 = top, max = bottom default

        enableScissor(this.chatX, this.chatTop, this.width - 10 - this.chatX, areaH);
        int y = areaTop - scrollPixels;
        for (Item it : items) {
            if (y + it.height >= areaTop && y <= areaTop + areaH) {
                if (it.text != null) {
                    this.fontRendererObj.drawStringWithShadow(it.text, areaX, y, 0xFFFFFFFF);
                } else {
                    ImageEmbedCache.Embed e = ImageEmbedCache.get(it.imgUrl);
                    if (e.location != null) {
                        net.minecraft.client.renderer.GlStateManager.color(1f, 1f, 1f, 1f);
                        this.mc.getTextureManager().bindTexture(e.location);
                        net.minecraft.client.gui.Gui.drawScaledCustomSizeModalRect(
                            areaX, y, 0, 0, e.width, e.height, it.drawW, it.drawH, e.width, e.height);
                        this.embedHits.add(new EmbedHit(areaX, y, it.drawW, it.drawH, it.imgUrl));
                    } else {
                        this.fontRendererObj.drawString(e.failed ? "§c[image failed to load]" : "§7[loading image…]",
                            areaX, y, 0xFFFFFFFF);
                    }
                }
            }
            y += it.height;
        }
        disableScissor();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_SEND) sendCurrent();
        else if (button.id == BTN_DONE) this.mc.displayGuiScreen(null);
    }

    private void sendCurrent() {
        if (this.selected == null) return;
        String text = this.input.getText().trim();
        if (text.isEmpty()) return;
        SingleChat chat = chatOf(this.selected);
        if (chat == null) return;
        try {
            chat.addMessage(new MessageChatComponent(LabyMod.getInstance().getPlayerName(),
                System.currentTimeMillis(), text));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        this.input.setText("");
        this.chatScroll = 0;
    }

    /** Returns true if an image was found on the clipboard and an upload was started. */
    private boolean tryPasteImage() {
        try {
            java.awt.datatransfer.Transferable t =
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (t == null || !t.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.imageFlavor)) {
                return false;
            }
            java.awt.Image raw = (java.awt.Image) t.getTransferData(java.awt.datatransfer.DataFlavor.imageFlavor);
            if (raw == null) return false;
            java.awt.image.BufferedImage img = toBuffered(raw);
            uploadAndSend(img);
            return true;
        } catch (Throwable e) {
            this.uploadStatus = "§cclipboard read failed";
            return false;
        }
    }

    private static java.awt.image.BufferedImage toBuffered(java.awt.Image img) {
        int w = Math.max(1, img.getWidth(null));
        int h = Math.max(1, img.getHeight(null));
        java.awt.image.BufferedImage bi =
            new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = bi.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return bi;
    }

    private void uploadAndSend(final java.awt.image.BufferedImage img) {
        if (this.selected == null) { this.uploadStatus = "§cselect a friend first"; return; }
        if (!ImgurUploader.isConfigured()) {
            this.uploadStatus = "§cSet Imgur Client-ID in .minecraft/EsdeathClient/imgur_clientid.txt";
            return;
        }
        final ChatUser target = this.selected;
        final String myName = LabyMod.getInstance().getPlayerName();
        this.uploadStatus = "§euploading image…";
        Thread up = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String url = ImgurUploader.upload(img);
                    net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                        @Override
                        public void run() {
                            SingleChat chat = chatOf(target);
                            if (chat != null) {
                                chat.addMessage(new MessageChatComponent(myName, System.currentTimeMillis(), url));
                            }
                            LabyChatGui.this.uploadStatus = null;
                            LabyChatGui.this.chatScroll = 0;
                        }
                    });
                } catch (Throwable e) {
                    LabyChatGui.this.uploadStatus = "§cupload failed: " + e.getMessage();
                }
            }
        }, "ImgurUpload");
        up.setDaemon(true);
        up.start();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        // status dropdown first (overlays everything)
        if (this.statusOpen) {
            UserStatus[] all = { UserStatus.ONLINE, UserStatus.AWAY, UserStatus.BUSY, UserStatus.OFFLINE };
            int x = ddX, y = ddY, w = ddW, rh = ddRowH;
            if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY < y + rh * all.length) {
                setStatus(all[(mouseY - y) / rh]);
                return;
            }
            // alerts toggle row (row index = all.length)
            int ay = y + all.length * rh;
            if (mouseX >= x && mouseX <= x + w && mouseY >= ay && mouseY < ay + rh) {
                LabyMod.getSettings().alertsOnlineStatus = !LabyMod.getSettings().alertsOnlineStatus;
                LabyMod.getSettings().save();
                return;
            }
            this.statusOpen = false;
        }
        if (mouseX >= gearX && mouseX <= gearX + gearW && mouseY >= gearY && mouseY <= gearY + gearH) {
            this.statusOpen = !this.statusOpen;
            return;
        }
        // tab bar
        ChatTabBar.Tab t = ChatTabBar.clicked(this, this.fontRendererObj, mouseX, mouseY);
        if (t != null && t != ChatTabBar.Tab.CHAT) { ChatTabBar.open(this.mc, t); return; }

        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.input.mouseClicked(mouseX, mouseY, mouseButton);
        this.search.mouseClicked(mouseX, mouseY, mouseButton);

        // click an image embed -> open it in the browser
        for (EmbedHit h : this.embedHits) {
            if (mouseX >= h.x && mouseX <= h.x + h.w && mouseY >= h.y && mouseY <= h.y + h.h) {
                LabyMod.getInstance().openWebpage(h.url, false);
                return;
            }
        }

        if (mouseX >= LIST_X && mouseX <= LIST_X + LIST_W && mouseY >= this.listTop && mouseY <= this.listBottom) {
            int idx = (mouseY - (this.listTop + 2) + this.listScroll) / ROW_H;
            List<ChatUser> friends = friends();
            if (idx >= 0 && idx < friends.size()) select(friends.get(idx));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws java.io.IOException {
        if (this.search.isFocused()) {
            if (keyCode == Keyboard.KEY_ESCAPE) { this.search.setFocused(false); return; }
            if (this.search.textboxKeyTyped(typedChar, keyCode)) return;
        }
        if (this.input.isFocused() && (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER)) {
            sendCurrent();
            return;
        }
        // Ctrl+V with an image on the clipboard -> upload + send as an embed (LabyMod-style).
        if (this.input.isFocused() && keyCode == Keyboard.KEY_V && isCtrlKeyDown()) {
            if (tryPasteImage()) return; // consumed; otherwise fall through to normal text paste
        }
        if (keyCode == Keyboard.KEY_ESCAPE) { this.mc.displayGuiScreen(null); return; }
        if (this.input.textboxKeyTyped(typedChar, keyCode)) return;
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws java.io.IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;
        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        if (mx <= LIST_X + LIST_W) {
            int contentH = friends().size() * ROW_H;
            int maxScroll = Math.max(0, contentH - (this.listBottom - this.listTop));
            this.listScroll = Math.max(0, Math.min(maxScroll, this.listScroll + (wheel > 0 ? -ROW_H : ROW_H)));
        } else {
            this.chatScroll = Math.max(0, Math.min(this.chatMaxScroll, this.chatScroll + (wheel > 0 ? 20 : -20)));
        }
    }

    @Override
    public void updateScreen() {
        this.input.updateCursorCounter();
        this.search.updateCursorCounter();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void enableScissor(int x, int y, int w, int h) {
        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(this.mc);
        int scale = sr.getScaleFactor();
        org.lwjgl.opengl.GL11.glEnable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
        org.lwjgl.opengl.GL11.glScissor(x * scale, this.mc.displayHeight - (y + h) * scale, w * scale, h * scale);
    }

    private void disableScissor() {
        org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_SCISSOR_TEST);
    }
}
