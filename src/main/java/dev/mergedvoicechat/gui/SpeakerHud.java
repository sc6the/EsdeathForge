package dev.mergedvoicechat.gui;

import net.labymod.addons.voicechat.VoiceChat;
import net.labymod.addons.voicechat.audio.surround.SurroundManager;
import net.labymod.addons.voicechat.client.Mute;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Renders a list of currently-speaking players. Each row: [head 8x8] [speaker icon] [tablist-colored name] [vol%]
 * Position from voiceChat.hudX/hudY. Background optional. Scale per voiceChat.hudScale (50-300%).
 */
public class SpeakerHud {

    public static final int LINE_HEIGHT = 12;
    public static final int PAD_X = 4;
    public static final int PAD_Y = 3;
    public static final int HEAD_SIZE = 8;
    // Match NameplateSpeakerIcon.SIZE so the HUD speaker icon visually matches the one floating next to the nametag.
    public static final int ICON_SIZE = 10;
    public static final int GAP = 3;
    public static final int BG = 0x80000000;
    public static final int MIN_WIDTH = 60;

    // SVC-style speaker icons (white), replacing the old green/red dots.
    private static final ResourceLocation SVC_SPEAKER = new ResourceLocation("mergedvoicechat", "textures/svc/speaker.png");
    private static final ResourceLocation SVC_SPEAKER_OFF = new ResourceLocation("mergedvoicechat", "textures/svc/speaker_off.png");

    private final VoiceChat vc;

    // Clickable row boxes from the last draw (scaled-GUI coords). Used to open the volume menu while
    // the chat screen is open. Rebuilt every frame in draw().
    private final List<Hit> hits = new ArrayList<>();

    public SpeakerHud(VoiceChat vc) { this.vc = vc; }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post e) {
        if (e.type != RenderGameOverlayEvent.ElementType.HOTBAR) return;
        if (!vc.hudEnabled) return;
        // Stay visible while the chat screen is open (it renders over the world+HUD), but hide for any
        // other full screen (inventory, our own menus, etc.).
        net.minecraft.client.gui.GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen != null && !(screen instanceof GuiChat)) return;
        draw(false);
    }

    // While the chat screen is up, clicking a speaker's name opens its volume menu.
    @SubscribeEvent
    public void onChatMouse(GuiScreenEvent.MouseInputEvent.Pre e) {
        if (!vc.hudEnabled) return;
        if (!(e.gui instanceof GuiChat)) return;
        if (Mouse.getEventButton() != 0 || !Mouse.getEventButtonState()) return; // left-press only

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        int mx = Mouse.getEventX() * sr.getScaledWidth() / mc.displayWidth;
        int my = sr.getScaledHeight() - Mouse.getEventY() * sr.getScaledHeight() / mc.displayHeight - 1;
        for (Hit hb : this.hits) {
            if (mx >= hb.x && mx <= hb.x + hb.w && my >= hb.y && my <= hb.y + hb.h) {
                mc.displayGuiScreen(new GuiVolume(this.vc, hb.uuid, hb.username));
                e.setCanceled(true);
                return;
            }
        }
    }

    public int draw(boolean editor) {
        Minecraft mc = Minecraft.getMinecraft();
        this.hits.clear();
        if (mc.theWorld == null) return 0;

        List<Row> rows = collectRows();
        int count = rows.size();
        if (count == 0 && !editor) return 0;

        int textOffset = HEAD_SIZE + GAP;
        int w = MIN_WIDTH;
        for (Row r : rows) {
            int rw = PAD_X + textOffset + mc.fontRendererObj.getStringWidth(r.displayLine) + PAD_X;
            w = Math.max(w, rw);
        }
        int h = PAD_Y * 2 + (count == 0 ? LINE_HEIGHT : count * LINE_HEIGHT);

        ScaledResolution sr = new ScaledResolution(mc);

        int x = clamp(vc.hudX, 0, sr.getScaledWidth() - w);
        int y = clamp(vc.hudY, 0, sr.getScaledHeight() - h);
        vc.hudX = x; vc.hudY = y;

        if (vc.hudBackground) {
            // scale the background's alpha (BG = 0x80 base) by the HUD opacity %, keeping RGB black
            int a = (int) ((BG >>> 24) * (vc.hudOpacity / 100.0F));
            Gui.drawRect(x, y, x + w, y + h, (Math.max(0, Math.min(255, a)) << 24));
        }

        int rowY = y + PAD_Y;
        if (count == 0) {
            mc.fontRendererObj.drawStringWithShadow("§7(silent)", x + PAD_X, rowY, 0xFF999999);
        } else {
            for (Row r : rows) {
                drawHead(r.skin, x + PAD_X, rowY);
                mc.fontRendererObj.drawStringWithShadow(
                    r.displayLine,
                    x + PAD_X + textOffset,
                    rowY + 1,
                    0xFFFFFFFF);
                this.hits.add(new Hit(r.uuid, r.username, x, rowY, w, LINE_HEIGHT));
                rowY += LINE_HEIGHT;
            }
        }

        return (h << 16) | (w & 0xFFFF);
    }

    public int[] computeBox() {
        Minecraft mc = Minecraft.getMinecraft();
        List<Row> rows = collectRows();
        int count = rows.size();
        int textOffset = HEAD_SIZE + GAP;
        int w = MIN_WIDTH;
        for (Row r : rows) {
            int rw = PAD_X + textOffset + mc.fontRendererObj.getStringWidth(r.displayLine) + PAD_X;
            w = Math.max(w, rw);
        }
        int h = PAD_Y * 2 + (count == 0 ? LINE_HEIGHT : count * LINE_HEIGHT);
        return new int[]{vc.hudX, vc.hudY, w, h};
    }

    private List<Row> collectRows() {
        List<Row> out = new ArrayList<>();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) return out;
        SurroundManager sm = vc.getSurroundManager();
        if (sm == null) return out;

        UUID self = LabyMod.getInstance().getPlayerUUID();
        if (self != null && sm.isTalking(self)) {
            out.add(buildRow(self, true));
        }
        // HUD lists currently-speaking players only (per the class javadoc), so non-talking and
        // muted users are filtered out and every row shows the activated SPEAKER_ON icon.
        for (UUID uuid : sm.getUserStreams().keySet()) {
            if (self != null && self.equals(uuid)) continue;
            if (vc.getVolume(uuid) == 0) continue;
            if (isMuted(uuid)) continue;
            if (!sm.isTalking(uuid)) continue;
            out.add(buildRow(uuid, true));
        }
        return out;
    }

    private Row buildRow(UUID uuid, boolean talking) {
        Minecraft mc = Minecraft.getMinecraft();
        Row r = new Row();
        r.uuid = uuid;
        NetworkPlayerInfo info = mc.getNetHandler() == null ? null : mc.getNetHandler().getPlayerInfo(uuid);

        String tablistFormatted;
        String plainName;
        if (info != null && info.getDisplayName() != null) {
            tablistFormatted = info.getDisplayName().getFormattedText();
            plainName = info.getGameProfile().getName();
        } else if (info != null) {
            plainName = info.getGameProfile().getName();
            tablistFormatted = ScorePlayerTeam.formatPlayerName(info.getPlayerTeam(), plainName);
        } else {
            EntityPlayer p = mc.theWorld.getPlayerEntityByUUID(uuid);
            plainName = p != null ? p.getName() : uuid.toString().substring(0, 8);
            tablistFormatted = plainName;
        }
        r.username = plainName;

        int volume = vc.getVolume(uuid);
        r.displayLine = tablistFormatted + (volume != 100 ? " §7(" + volume + "%)" : "");

        // Prefer AbstractClientPlayer.getLocationSkin() on the entity (handles fallback + legacy skin processing).
        EntityPlayer self = mc.thePlayer;
        EntityPlayer entityForSkin = (self != null && self.getUniqueID().equals(uuid))
            ? self
            : mc.theWorld.getPlayerEntityByUUID(uuid);
        if (entityForSkin instanceof net.minecraft.client.entity.AbstractClientPlayer) {
            r.skin = ((net.minecraft.client.entity.AbstractClientPlayer) entityForSkin).getLocationSkin();
        } else if (info != null && info.getLocationSkin() != null) {
            r.skin = info.getLocationSkin();
        } else {
            r.skin = DefaultPlayerSkin.getDefaultSkin(uuid);
        }

        boolean muted = isMuted(uuid);
        // SVC white speaker icon (untinted), same look as the nametag icon.
        r.iconTexture = muted ? SVC_SPEAKER_OFF : SVC_SPEAKER;
        r.iconColor = 0xFFFFFFFF;
        return r;
    }

    private boolean isMuted(UUID uuid) {
        if (vc.serverMutes.contains(uuid)) return true;
        if (vc.getVolume(uuid) == 0) return true;
        if (vc.getVoiceClientListener() != null) {
            Mute m = vc.getVoiceClientListener().getGlobalMuted().get(uuid);
            if (m != null && !m.isExpired()) return true;
        }
        return false;
    }

    private void drawHead(ResourceLocation skin, int x, int y) {
        if (skin == null) return;
        Minecraft.getMinecraft().getTextureManager().bindTexture(skin);
        // Reset state from any previous icon draw.
        GlStateManager.disableBlend();
        GlStateManager.color(1F, 1F, 1F, 1F);
        // Face only -- hat overlay sampling at (40,8) is wrong for legacy 64x32 skins
        // (samples right-arm region, causing weird recoloring) and we can't reliably
        // detect skin format here without async work. Skip the hat for stability.
        Gui.drawScaledCustomSizeModalRect(x, y, 8F, 8F, 8, 8, HEAD_SIZE, HEAD_SIZE, 64F, 64F);
    }

    private void drawIcon(ResourceLocation tex, int x, int y, int rgba) {
        if (tex == null) return;
        float r = ((rgba >> 16) & 0xFF) / 255F;
        float g = ((rgba >>  8) & 0xFF) / 255F;
        float b = ( rgba        & 0xFF) / 255F;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 770, 771);

        // Draw the raw 16x16 SVC icon (no baked outline), crisp via NEAREST.
        Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
        org.lwjgl.opengl.GL11.glTexParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER, org.lwjgl.opengl.GL11.GL_NEAREST);
        org.lwjgl.opengl.GL11.glTexParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER, org.lwjgl.opengl.GL11.GL_NEAREST);
        GlStateManager.color(r, g, b, 1F);
        Gui.drawScaledCustomSizeModalRect(x, y, 0F, 0F, 16, 16, ICON_SIZE, ICON_SIZE, 16F, 16F);

        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    private static int clamp(int v, int lo, int hi) {
        if (hi < lo) return lo;
        return Math.max(lo, Math.min(hi, v));
    }

    private static final class Row {
        UUID uuid;
        String username;
        String displayLine;
        ResourceLocation skin;
        ResourceLocation iconTexture;
        int iconColor;
    }

    private static final class Hit {
        final UUID uuid;
        final String username;
        final int x, y, w, h;
        Hit(UUID uuid, String username, int x, int y, int w, int h) {
            this.uuid = uuid; this.username = username;
            this.x = x; this.y = y; this.w = w; this.h = h;
        }
    }
}
