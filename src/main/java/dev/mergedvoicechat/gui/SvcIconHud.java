package dev.mergedvoicechat.gui;

import net.labymod.addons.voicechat.VoiceChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Simple Voice Chat-style corner microphone-state icon. Shows the SVC icon for the current voice
 * state (talking / muted / disabled / disconnected) in the top-left, matching SVC's HUD look,
 * driven by mergedvoicechat's own voice state. Only appears when there's something to show.
 */
public class SvcIconHud {

    private static final ResourceLocation MIC = tex("microphone");
    private static final ResourceLocation MIC_OFF = tex("microphone_off");
    private static final ResourceLocation SPEAKER_OFF = tex("speaker_off");
    private static final ResourceLocation DISCONNECTED = tex("disconnected");

    private static ResourceLocation tex(String n) {
        return new ResourceLocation("mergedvoicechat", "textures/svc/" + n + ".png");
    }

    private final VoiceChat vc;

    public SvcIconHud(VoiceChat vc) {
        this.vc = vc;
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post e) {
        if (e.type != RenderGameOverlayEvent.ElementType.HOTBAR) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.currentScreen != null) {
            return;
        }
        ResourceLocation icon = pick();
        if (icon == null) {
            return;
        }
        ScaledResolution sr = new ScaledResolution(mc);
        int x = sr.getScaledWidth() - 16 - 4;   // bottom-right corner
        int y = sr.getScaledHeight() - 16 - 4;
        mc.getTextureManager().bindTexture(icon);
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0F, 0F, 16, 16, 16F, 16F);
        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    // SVC priority: disconnected > disabled > muted > talking > (idle: nothing)
    private ResourceLocation pick() {
        if (!vc.enabled) {
            return SPEAKER_OFF;
        }
        if (!vc.isConnected()) {
            return DISCONNECTED;
        }
        if (vc.pushToTalkPressed) {
            return MIC;
        }
        return null;
    }
}
