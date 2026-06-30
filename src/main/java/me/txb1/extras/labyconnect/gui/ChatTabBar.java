package me.txb1.extras.labyconnect.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

/**
 * Shared top navigation bar: [Menu] [Chat] [Friends] [Voicechat].
 * Rendered by the pause menu and the chat/friends screens so they behave like tabs.
 */
public final class ChatTabBar {

    public enum Tab {
        PAUSEMENU("Menu"),
        CHAT("Chat"),
        FRIENDS("Friends"),
        VOICE("Voicechat");

        public final String label;
        Tab(String label) { this.label = label; }
    }

    private static final int Y = 16;
    private static final int H = 16;
    private static final int GAP = 26;

    private ChatTabBar() {}

    /** x of each tab's left edge, centered as a group. */
    private static int[] layout(GuiScreen gui, FontRenderer fr) {
        Tab[] tabs = Tab.values();
        int total = 0;
        int[] w = new int[tabs.length];
        for (int i = 0; i < tabs.length; i++) {
            w[i] = fr.getStringWidth(tabs[i].label);
            total += w[i];
        }
        total += GAP * (tabs.length - 1);
        int[] x = new int[tabs.length];
        int cx = (gui.width - total) / 2;
        for (int i = 0; i < tabs.length; i++) {
            x[i] = cx;
            cx += w[i] + GAP;
        }
        return x;
    }

    public static void draw(GuiScreen gui, FontRenderer fr, int mouseX, int mouseY, Tab active) {
        int[] x = layout(gui, fr);
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            int w = fr.getStringWidth(tabs[i].label);
            boolean hovered = mouseX >= x[i] - 4 && mouseX <= x[i] + w + 4 && mouseY >= Y - 4 && mouseY <= Y + H - 4;
            int color = tabs[i] == active ? 0xFFFFFFFF : hovered ? 0xFFFFFFFF : 0xFFAAAAAA;
            fr.drawStringWithShadow(tabs[i].label, x[i], Y, color);
            if (tabs[i] == active) {
                Gui.drawRect(x[i], Y + 11, x[i] + w, Y + 12, 0xFFFFFFFF);
            }
        }
    }

    /** Returns the clicked tab, or null. */
    public static Tab clicked(GuiScreen gui, FontRenderer fr, int mouseX, int mouseY) {
        int[] x = layout(gui, fr);
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            int w = fr.getStringWidth(tabs[i].label);
            if (mouseX >= x[i] - 4 && mouseX <= x[i] + w + 4 && mouseY >= Y - 4 && mouseY <= Y + H - 4) {
                return tabs[i];
            }
        }
        return null;
    }

    public static void open(Minecraft mc, Tab tab) {
        switch (tab) {
            case PAUSEMENU:
                mc.displayGuiScreen(new me.txb1.forge.gui.EsdeathIngameMenu());
                break;
            case CHAT:
                mc.displayGuiScreen(new LabyChatGui());
                break;
            case FRIENDS:
                mc.displayGuiScreen(new FriendsGui());
                break;
            case VOICE:
                GuiScreen g = voiceSettings();
                if (g != null) {
                    mc.displayGuiScreen(g);
                }
                break;
        }
    }

    private static GuiScreen voiceSettings() {
        try {
            me.txb1.player.modulesystem.Module m =
                me.txb1.EsdeathClient.getInstance().getModuleManager().getModuleByName("VoiceChat");
            if (m != null) {
                return m.getCustomSettingsGui();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
