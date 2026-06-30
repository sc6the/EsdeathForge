package me.txb1.extras.labyconnect.gui;

import me.txb1.extras.labyconnect.LabyConnectManager;
import net.labymod.labyconnect.LabyConnect;
import net.labymod.labyconnect.packets.PacketPlayDenyFriendRequest;
import net.labymod.labyconnect.packets.PacketPlayRequestAddFriend;
import net.labymod.labyconnect.user.ChatRequest;
import net.labymod.labyconnect.user.ChatUser;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

/**
 * "Friends" tab — add friends by name, accept/deny incoming requests, and view the friend list,
 * all over the ported LabyConnect socket.
 */
public class FriendsGui extends GuiScreen {

    private static final int BTN_ADD = 1;
    private static final int BTN_DONE = 0;
    private static final int TOP = 34;
    private static final int ROW_H = 18;

    private GuiTextField addField;
    private int listTop, listBottom, listScroll = 0;

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.listTop = TOP + 26;
        this.listBottom = this.height - 38;

        this.addField = new GuiTextField(0, this.fontRendererObj, 12 + 2, TOP, 200, 16);
        this.addField.setMaxStringLength(16);
        this.addField.setEnableBackgroundDrawing(false);

        this.buttonList.clear();
        this.buttonList.add(new me.txb1.forge.gui.ThemedButton(BTN_ADD, 220, TOP - 4, 60, 20, "Add"));
        this.buttonList.add(new me.txb1.forge.gui.ThemedButton(BTN_DONE, 12, this.height - 32, 70, 20, "Done"));
    }

    private LabyConnect lc() {
        return LabyConnectManager.get();
    }

    private List<ChatRequest> requests() {
        LabyConnect lc = lc();
        if (lc == null || lc.getRequests() == null) return new ArrayList<ChatRequest>();
        return lc.getRequests();
    }

    private List<ChatUser> friends() {
        LabyConnect lc = lc();
        if (lc == null) return new ArrayList<ChatUser>();
        List<ChatUser> sorted = lc.getSortFriends();
        if (sorted != null && !sorted.isEmpty()) return sorted;
        return lc.getFriends() == null ? new ArrayList<ChatUser>() : lc.getFriends();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        ChatTabBar.draw(this, this.fontRendererObj, mouseX, mouseY, ChatTabBar.Tab.FRIENDS);

        // add-friend row
        this.fontRendererObj.drawStringWithShadow("Add friend:", 12, TOP - 12, 0xFFFFFFFF);
        drawRect(12, TOP - 2, 214, TOP + 16, 0x66000000);
        if (this.addField.getText().isEmpty() && !this.addField.isFocused()) {
            this.fontRendererObj.drawString("player name", 16, TOP + 4, 0xFF888888);
        }
        this.addField.drawTextBox();

        LabyConnect lc = lc();
        if (lc != null) {
            String resp = lc.getClientConnection() == null ? null : lc.getClientConnection().lastAddFriendResponse;
            if (resp != null) {
                String msg = resp.equals("true") ? "§aRequest sent!" : "§c" + resp;
                this.fontRendererObj.drawStringWithShadow(msg, 290, TOP + 2, 0xFFFFFFFF);
            }
        }

        // list area
        drawRect(12, this.listTop, this.width - 12, this.listBottom, 0x55000000);

        int y = this.listTop + 4 - this.listScroll;
        enableScissor(12, this.listTop, this.width - 24, this.listBottom - this.listTop);

        List<ChatRequest> reqs = requests();
        if (!reqs.isEmpty()) {
            this.fontRendererObj.drawStringWithShadow("§eIncoming requests (" + reqs.size() + ")", 18, y, 0xFFFFFFFF);
            y += 14;
            for (ChatRequest r : reqs) {
                String name = r.getGameProfile() == null ? "?" : r.getGameProfile().getName();
                this.fontRendererObj.drawStringWithShadow(name, 22, y + 4, 0xFFFFFFFF);
                drawMiniButton(this.width - 150, y, 60, "§aAccept", mouseX, mouseY);
                drawMiniButton(this.width - 84, y, 60, "§cDeny", mouseX, mouseY);
                y += ROW_H;
            }
            y += 6;
        }

        List<ChatUser> friends = friends();
        this.fontRendererObj.drawStringWithShadow("§7Friends (" + friends.size() + ")", 18, y, 0xFFFFFFFF);
        y += 14;
        for (ChatUser u : friends) {
            drawRect(18, y + ROW_H / 2 - 2, 22, y + ROW_H / 2 + 2, u.isOnline() ? 0xFF4CC94C : 0xFF808080);
            String name = u.getGameProfile() == null ? "?" : u.getGameProfile().getName();
            this.fontRendererObj.drawStringWithShadow(name, 26, y + 4, u.isOnline() ? 0xFFFFFFFF : 0xFFBBBBBB);
            String sm = u.getStatusMessage();
            if (sm != null && !sm.isEmpty()) {
                this.fontRendererObj.drawString("§7" + this.fontRendererObj.trimStringToWidth(sm, 220),
                    26 + this.fontRendererObj.getStringWidth(name) + 8, y + 4, 0xFF999999);
            }
            y += ROW_H;
        }
        disableScissor();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawMiniButton(int x, int y, int w, String label, int mouseX, int mouseY) {
        boolean hov = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 14;
        drawRect(x, y, x + w, y + 14, hov ? 0x88000000 : 0x55000000);
        this.drawCenteredString(this.fontRendererObj, label, x + w / 2, y + 3, 0xFFFFFFFF);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_ADD) addFriend(this.addField.getText().trim());
        else if (button.id == BTN_DONE) this.mc.displayGuiScreen(null);
    }

    private void addFriend(String name) {
        if (name.isEmpty()) return;
        LabyConnect lc = lc();
        if (lc == null || lc.getClientConnection() == null) return;
        lc.getClientConnection().sendPacket(new PacketPlayRequestAddFriend(name));
        this.addField.setText("");
    }

    private void accept(ChatRequest r) {
        LabyConnect lc = lc();
        if (lc == null || lc.getClientConnection() == null || r.getGameProfile() == null) return;
        // Accepting = sending the reciprocal add-friend request.
        lc.getClientConnection().sendPacket(new PacketPlayRequestAddFriend(r.getGameProfile().getName()));
        lc.getRequests().remove(r);
    }

    private void deny(ChatRequest r) {
        LabyConnect lc = lc();
        if (lc == null || lc.getClientConnection() == null) return;
        lc.getClientConnection().sendPacket(new PacketPlayDenyFriendRequest(r));
        lc.getRequests().remove(r);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        ChatTabBar.Tab t = ChatTabBar.clicked(this, this.fontRendererObj, mouseX, mouseY);
        if (t != null && t != ChatTabBar.Tab.FRIENDS) { ChatTabBar.open(this.mc, t); return; }

        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.addField.mouseClicked(mouseX, mouseY, mouseButton);

        // Accept/Deny hit-test against the same row layout used in drawScreen.
        int y = this.listTop + 4 - this.listScroll;
        List<ChatRequest> reqs = requests();
        if (!reqs.isEmpty()) {
            y += 14;
            for (ChatRequest r : new ArrayList<ChatRequest>(reqs)) {
                if (hit(this.width - 150, y, 60, mouseX, mouseY)) { accept(r); return; }
                if (hit(this.width - 84, y, 60, mouseX, mouseY)) { deny(r); return; }
                y += ROW_H;
            }
        }
    }

    private boolean hit(int x, int y, int w, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 14;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws java.io.IOException {
        if (this.addField.isFocused() && (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER)) {
            addFriend(this.addField.getText().trim());
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) { this.mc.displayGuiScreen(null); return; }
        if (this.addField.textboxKeyTyped(typedChar, keyCode)) return;
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws java.io.IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;
        this.listScroll = Math.max(0, this.listScroll + (wheel > 0 ? -ROW_H : ROW_H));
    }

    @Override
    public void updateScreen() {
        this.addField.updateCursorCounter();
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
