package dev.mergedvoicechat.mixin;

import dev.mergedvoicechat.MergedVoiceChat;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When the "Show all nameplates" toggle is on, force canRenderName to return true
 * for any player -- including the local player.
 *
 * Higher Mixin priority value = applied LATER = our setReturnValue overrides any
 * earlier injection (e.g. NoNicknames' setReturnValue(false)).
 */
@Mixin(value = RendererLivingEntity.class, priority = 2000)
public abstract class MixinRendererLivingEntity {

    @Inject(method = "canRenderName", at = @At("HEAD"), cancellable = true)
    private void mvc$showAll(EntityLivingBase entity, CallbackInfoReturnable<Boolean> cir) {
        if (MergedVoiceChat.INSTANCE == null || MergedVoiceChat.INSTANCE.voiceChat == null) return;
        // Suppress nameplates for entities being drawn into the outline glow buffer.
        if (dev.mergedvoicechat.gui.SpeakerOutline.renderingOutlinePass) {
            cir.setReturnValue(false);
            return;
        }
        if (!MergedVoiceChat.INSTANCE.voiceChat.showAllNameplates) return;
        if (entity instanceof EntityPlayer) {
            cir.setReturnValue(true);
        }
    }
}
