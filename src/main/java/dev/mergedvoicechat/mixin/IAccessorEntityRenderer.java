package dev.mergedvoicechat.mixin;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// Exposes EntityRenderer#setupCameraTransform (private) so the outline pass can re-establish the
// world camera projection/modelview while rendering into the offscreen glow buffer.
@Mixin(EntityRenderer.class)
public interface IAccessorEntityRenderer {

    @Invoker("setupCameraTransform")
    void callSetupCameraTransform(float partialTicks, int pass);
}
