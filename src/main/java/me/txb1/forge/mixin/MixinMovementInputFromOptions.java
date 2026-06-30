package me.txb1.forge.mixin;

import me.txb1.player.modulesystem.modules.utils.NullMove;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// NullMove ("snap tap"): after vanilla computes the move state, if both opposite movement keys are
// held, override the axis to follow the most-recently-pressed key instead of cancelling to zero.
// moveForward/moveStrafe/sneak are public fields on MovementInput, so we access them via a cast (no
// inherited @Shadow needed).
@Mixin(MovementInputFromOptions.class)
public abstract class MixinMovementInputFromOptions {

   @Inject(method = "updatePlayerMoveState", at = @At("TAIL"))
   private void esdeath$nullMove(CallbackInfo ci) {
      if (!NullMove.active) {
         return;
      }
      GameSettings gs = Minecraft.getMinecraft().gameSettings;
      boolean f = gs.keyBindForward.isKeyDown();
      boolean b = gs.keyBindBack.isKeyDown();
      boolean l = gs.keyBindLeft.isKeyDown();
      boolean r = gs.keyBindRight.isKeyDown();
      NullMove.update(f, b, l, r);

      MovementInput mi = (MovementInput) (Object) this;
      float sneakMul = mi.sneak ? 0.3F : 1.0F;
      if (f && b) {
         mi.moveForward = (NullMove.forwardLatest ? 1.0F : -1.0F) * sneakMul;
      }
      if (l && r) {
         mi.moveStrafe = (NullMove.leftLatest ? 1.0F : -1.0F) * sneakMul;
      }
   }
}
