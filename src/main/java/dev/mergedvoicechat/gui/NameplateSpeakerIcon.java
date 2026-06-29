package dev.mergedvoicechat.gui;

import net.labymod.addons.voicechat.VoiceChat;
import net.labymod.addons.voicechat.audio.surround.SurroundManager;
import net.labymod.addons.voicechat.client.Mute;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.UUID;

/**
 * 3D billboard speaker icon above the player's nameplate. Uses the original
 * VoiceChat speaker_*.png textures. Subscribed to RenderLivingEvent.Specials.Post
 * with LOWEST priority so it draws on top of other nametag mods.
 */
public final class NameplateSpeakerIcon {

    private static final int SIZE = 7;
    private static final float NAME_TAG_SCALE = 0.02666667F;
    // Simple Voice Chat-style icons (white, drawn untinted)
    private static final ResourceLocation SVC_SPEAKER = new ResourceLocation("mergedvoicechat", "textures/svc/speaker.png");
    private static final ResourceLocation SVC_SPEAKER_OFF = new ResourceLocation("mergedvoicechat", "textures/svc/speaker_off.png");

    private final VoiceChat vc;

    public NameplateSpeakerIcon(VoiceChat vc) { this.vc = vc; }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderPlayer(RenderLivingEvent.Specials.Post<EntityLivingBase> event) {
        if (!vc.isEnabled()) return;
        if (!(event.entity instanceof AbstractClientPlayer)) return;
        AbstractClientPlayer player = (AbstractClientPlayer) event.entity;
        if (player.isInvisible()) return;

        UUID self = LabyMod.getInstance().getPlayerUUID();
        UUID uuid = player.getUniqueID();
        boolean isSelf = self != null && self.equals(uuid);

        SurroundManager sm = vc.getSurroundManager();
        if (sm == null) return;
        // self: drive off your own PTT/talk state so the icon is verifiable in third person.
        boolean talking;
        if (isSelf) {
            talking = vc.pushToTalkPressed;
        } else {
            if (!sm.isListening(uuid)) return;
            if (vc.getVolume(uuid) == 0 && !vc.serverMutes.contains(uuid)) return; // locally muted: no icon
            talking = sm.isTalking(uuid);
        }
        boolean muted = isMuted(uuid);

        // SVC look: white speaker when talking, speaker_off when muted, nothing when idle/listening.
        ResourceLocation tex;
        if (talking) {
            tex = SVC_SPEAKER;
        } else if (muted) {
            tex = SVC_SPEAKER_OFF;
        } else {
            return;
        }

        drawIconAboveName(player, event.x, event.y, event.z, tex, talking, muted);
    }

    private boolean isMuted(UUID uuid) {
        if (vc.serverMutes.contains(uuid)) return true;
        if (vc.getVoiceClientListener() != null) {
            Mute m = vc.getVoiceClientListener().getGlobalMuted().get(uuid);
            if (m != null && !m.isExpired()) return true;
        }
        return false;
    }

    private void drawIconAboveName(AbstractClientPlayer player, double x, double y, double z,
                                   ResourceLocation tex, boolean talking, boolean muted) {
        Minecraft mc = Minecraft.getMinecraft();
        RenderManager rm = mc.getRenderManager();

        EntityPlayer self = mc.thePlayer;
        if (self != null && player.getDistanceSqToEntity(self) > 64.0 * 64.0) return;

        // Vanilla nameplate renders at entity height + 0.5; sit at the same height (no upward offset).
        double tagY = y + (double) player.height + 0.5D;
        if (player.isSneaking()) tagY -= 0.25D;

        // Width of the name string we'll be sitting next to.
        String name = player.getDisplayName().getFormattedText();
        int nameWidth = mc.fontRendererObj.getStringWidth(name);
        int xOffset = nameWidth / 2 - 5;       // sits just inside the right edge of the name
        float yOffset = -0.5F;                  // half a px above the name text
        if ("deadmau5".equals(player.getName())) yOffset += -10; // matches vanilla quirk

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, tagY, z);
        GlStateManager.rotate(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(rm.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-NAME_TAG_SCALE, -NAME_TAG_SCALE, NAME_TAG_SCALE);

        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 770, 771);

        // SVC icons are already styled (white speaker); draw the raw texture untinted.
        mc.getTextureManager().bindTexture(tex);
        // NEAREST keeps the 16x16 pixel-art icon crisp when scaled up close to the camera (LINEAR blurs it)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawQuad(xOffset, yOffset, SIZE, SIZE);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawQuad(float x, float y, float w, float h) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0F, 0F); GL11.glVertex2f(x,     y);
        GL11.glTexCoord2f(0F, 1F); GL11.glVertex2f(x,     y + h);
        GL11.glTexCoord2f(1F, 1F); GL11.glVertex2f(x + w, y + h);
        GL11.glTexCoord2f(1F, 0F); GL11.glVertex2f(x + w, y);
        GL11.glEnd();
    }
}
