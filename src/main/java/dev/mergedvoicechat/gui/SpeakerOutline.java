package dev.mergedvoicechat.gui;

import dev.mergedvoicechat.gui.shader.GlowShader;
import dev.mergedvoicechat.gui.shader.OutlineShader;
import dev.mergedvoicechat.mixin.IAccessorEntityRenderer;
import net.labymod.addons.voicechat.VoiceChat;
import net.labymod.addons.voicechat.audio.surround.SurroundManager;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Glowing outline + low-opacity fill over speaking players (and an optional gray idle outline),
 * replacing the old floating speaker icon. The rendering technique is ported from Raven-bS's
 * PlayerESP "outline" mode: each target is drawn into an offscreen framebuffer with a flat
 * {@link GlowShader}, then an edge-detect {@link OutlineShader} composites a glow rim (+ faint fill)
 * back over the scene. Colour fades in/out per player for a smooth transition.
 */
public final class SpeakerOutline {

    // Set true only while drawing into the glow buffer, so the nameplate mixin can suppress names.
    public static volatile boolean renderingOutlinePass = false;

    private static final float FADE_PER_SEC = 5.5F;
    private static final float FILL = 0.22F;       // speaking fill opacity (gray idle has no fill)
    private static final int[] GREEN = {51, 255, 77};
    private static final int[] GRAY = {184, 184, 184};

    private final VoiceChat vc;
    private final Map<UUID, State> states = new HashMap<UUID, State>();

    private GlowShader glowShader;
    private OutlineShader outlineShader;
    private boolean shaderInit;
    private Framebuffer framebuffer;

    public SpeakerOutline(VoiceChat vc) {
        this.vc = vc;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderWorldLast(RenderWorldLastEvent e) {
        if (!vc.isEnabled() || (!vc.speakingGlow && !vc.idleOutline)) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;
        SurroundManager sm = vc.getSurroundManager();
        if (sm == null) return;

        ensureShaders();
        if (glowShader == null || outlineShader == null
            || !glowShader.isValid() || !outlineShader.isValid()) return;

        List<Target> green = new ArrayList<Target>();
        List<Target> gray = new ArrayList<Target>();
        UUID self = LabyMod.getInstance().getPlayerUUID();

        for (Object o : new ArrayList<Object>(mc.theWorld.playerEntities)) {
            if (!(o instanceof AbstractClientPlayer)) continue;
            EntityPlayer p = (EntityPlayer) o;
            if (p.isInvisible() || p.isDead) continue;

            UUID uuid = p.getUniqueID();
            boolean isSelf = self != null && self.equals(uuid);
            if (isSelf && mc.gameSettings.thirdPersonView == 0) continue; // don't draw own model in 1st person

            boolean participant;
            boolean talking;
            if (isSelf) {
                participant = true;
                talking = vc.pushToTalkPressed;
            } else {
                boolean locallyMuted = vc.getVolume(uuid) == 0 && !vc.serverMutes.contains(uuid);
                participant = sm.isListening(uuid) && !locallyMuted;
                talking = participant && sm.isTalking(uuid);
            }

            float greenTarget = (vc.speakingGlow && talking) ? 1F : 0F;
            float grayTarget = (vc.idleOutline && participant && !talking) ? 1F : 0F;
            float[] a = advance(uuid, greenTarget, grayTarget);
            if (a[0] > 0.01F) green.add(new Target(p, a[0]));
            if (a[1] > 0.01F) gray.add(new Target(p, a[1]));
        }

        if (green.isEmpty() && gray.isEmpty()) return;

        framebuffer = ensureFramebuffer(mc, framebuffer);
        if (framebuffer == null) return;

        // Save BOTH matrices: the pass runs setupCameraTransform/setupOverlayRendering which clobber
        // the projection matrix in place. Without restoring it, anything drawn after us on
        // RenderWorldLastEvent (e.g. a nametag-editor's 3D nametags) inherits a broken projection.
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        try {
            if (!green.isEmpty()) runPass(mc, e.partialTicks, green, GREEN, FILL, vc.glowOpacity / 100.0F);
            if (!gray.isEmpty()) runPass(mc, e.partialTicks, gray, GRAY, 0F, vc.idleOpacity / 100.0F);
        } catch (Throwable t) {
            renderingOutlinePass = false;
            // make sure we never leave a shader program bound on failure
            try { glowShader.stop(); } catch (Throwable ignored) {}
            try { outlineShader.stop(); } catch (Throwable ignored) {}
            mc.getFramebuffer().bindFramebuffer(false);
        } finally {
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.popMatrix();
            GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
        }
    }

    private void runPass(Minecraft mc, float partialTicks, List<Target> targets, int[] rgb, float fill, float opacity) {
        if (opacity < 0F) opacity = 0F;
        if (opacity > 1F) opacity = 1F;
        GlStateManager.pushAttrib();

        framebuffer.framebufferClear();           // clears to (…,0) and leaves fb unbound
        framebuffer.bindFramebuffer(false);
        ((IAccessorEntityRenderer) mc.entityRenderer).callSetupCameraTransform(partialTicks, 0);

        boolean shadows = mc.gameSettings.entityShadows;
        mc.gameSettings.entityShadows = false;
        GlStateManager.disableBlend();

        renderingOutlinePass = true;
        glowShader.use();
        for (Target t : targets) {
            glowShader.setColor(rgb[0], rgb[1], rgb[2], (int) (255 * t.alpha * opacity));
            // Render only the base body for the outline — hide the 2nd skin layer (jacket/sleeves/
            // pants/hat) so the glow rim follows the body silhouette instead of the puffy overlay.
            byte savedParts = hideSkinOverlay(t.player);
            mc.getRenderManager().renderEntityStatic(t.player, partialTicks, true);
            restoreSkinOverlay(t.player, savedParts);
        }
        glowShader.stop();
        renderingOutlinePass = false;

        mc.gameSettings.entityShadows = shadows;
        mc.entityRenderer.disableLightmap();
        mc.entityRenderer.setupOverlayRendering();

        // composite the glow buffer back over the scene
        mc.getFramebuffer().bindFramebuffer(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        GlStateManager.color(1F, 1F, 1F, 1F);
        outlineShader.setFill(fill * opacity);
        outlineShader.setTint(rgb[0], rgb[1], rgb[2]); // recolour from uniform -> ignores any white the cosmetics wrote
        outlineShader.use();
        drawFramebufferFullscreen(mc, framebuffer);
        outlineShader.stop();
        GlStateManager.enableAlpha();

        mc.getFramebuffer().bindFramebuffer(false);
        GlStateManager.popAttrib();
    }

    // During the outline pass, hide the 2nd skin layer (jacket/sleeves/pants/hat) but KEEP the cape,
    // so the glow silhouette follows the cape (and the cape occludes the body outline behind it).
    // DataWatcher index 10 is the displayed-skin-parts mask; bit 0 (0x01) is the cape.
    private static final byte CAPE_BIT = 0x01;

    private static byte hideSkinOverlay(EntityPlayer p) {
        try {
            byte old = p.getDataWatcher().getWatchableObjectByte(10);
            byte capeOnly = (byte) (old & CAPE_BIT);
            if (capeOnly != old) {
                p.getDataWatcher().updateObject(10, Byte.valueOf(capeOnly));
            }
            return old;
        } catch (Throwable t) {
            return 0;
        }
    }

    private static void restoreSkinOverlay(EntityPlayer p, byte old) {
        try {
            byte cur = p.getDataWatcher().getWatchableObjectByte(10);
            if (cur != old) {
                p.getDataWatcher().updateObject(10, Byte.valueOf(old));
            }
        } catch (Throwable ignored) {
        }
    }

    private void ensureShaders() {
        if (shaderInit) return;
        shaderInit = true;
        try {
            glowShader = new GlowShader();
            outlineShader = new OutlineShader();
        } catch (Throwable t) {
            glowShader = null;
            outlineShader = null;
        }
    }

    private static Framebuffer ensureFramebuffer(Minecraft mc, Framebuffer fb) {
        if (fb == null || fb.framebufferWidth != mc.displayWidth || fb.framebufferHeight != mc.displayHeight) {
            if (fb != null) fb.deleteFramebuffer();
            return new Framebuffer(mc.displayWidth, mc.displayHeight, false);
        }
        return fb;
    }

    private static void drawFramebufferFullscreen(Minecraft mc, Framebuffer fb) {
        ScaledResolution sr = new ScaledResolution(mc);
        GlStateManager.bindTexture(fb.framebufferTexture);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2d(0.0, 1.0); GL11.glVertex2d(0.0, 0.0);
        GL11.glTexCoord2d(0.0, 0.0); GL11.glVertex2d(0.0, sr.getScaledHeight());
        GL11.glTexCoord2d(1.0, 0.0); GL11.glVertex2d(sr.getScaledWidth(), sr.getScaledHeight());
        GL11.glTexCoord2d(1.0, 1.0); GL11.glVertex2d(sr.getScaledWidth(), 0.0);
        GL11.glEnd();
    }

    private float[] advance(UUID uuid, float greenTarget, float grayTarget) {
        long now = System.nanoTime();
        State s = states.get(uuid);
        if (s == null) {
            s = new State();
            s.t = now;
            states.put(uuid, s);
        }
        float dt = (now - s.t) / 1.0E9F;
        s.t = now;
        if (dt < 0F) dt = 0F;
        if (dt > 0.1F) dt = 0.1F;
        float step = FADE_PER_SEC * dt;
        s.green = approach(s.green, greenTarget, step);
        s.gray = approach(s.gray, grayTarget, step);
        if (s.green <= 0.001F && s.gray <= 0.001F && greenTarget == 0F && grayTarget == 0F) {
            states.remove(uuid);
        }
        return new float[]{s.green, s.gray};
    }

    private static float approach(float cur, float target, float step) {
        if (cur < target) return Math.min(target, cur + step);
        if (cur > target) return Math.max(target, cur - step);
        return cur;
    }

    private static final class Target {
        final EntityPlayer player;
        final float alpha;
        Target(EntityPlayer player, float alpha) {
            this.player = player;
            this.alpha = alpha;
        }
    }

    private static final class State {
        float green;
        float gray;
        long t;
    }
}
