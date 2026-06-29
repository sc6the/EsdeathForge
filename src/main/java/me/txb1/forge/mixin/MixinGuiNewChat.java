package me.txb1.forge.mixin;

import java.util.List;
import me.txb1.extras.settings.anzeige.AnzeigeSettings;
import me.txb1.extras.settings.anzeige.Cordinates;
import me.txb1.player.modulesystem.modules.render.CleanChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// CleanChat: chat slide-in animation + draggable chat offset + optional background removal. The
// background removal re-renders drawChat ourselves (skipping the per-line dark rect) and cancels the
// vanilla method — this is robust to OptiFine, which patches drawChat at runtime and otherwise keeps
// drawing its own background even when we redirect Gui.drawRect.
@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat {

   @Shadow @Final private Minecraft mc;
   @Shadow @Final private List<ChatLine> drawnChatLines;
   @Shadow private int scrollPos;
   @Shadow private boolean isScrolled;

   @Shadow public abstract int getLineCount();
   @Shadow public abstract boolean getChatOpen();
   @Shadow public abstract float getChatScale();
   @Shadow public abstract int getChatWidth();

   @Unique private boolean esdeath$slid;

   // record when a real (non-refresh) line is added so drawChat can animate from there
   @Inject(method = "setChatLine", at = @At("HEAD"))
   private void esdeath$onChatLine(IChatComponent component, int chatLineId, int updateCounter, boolean displayOnly, CallbackInfo ci) {
      if (!displayOnly) {
         CleanChat.lastMessage = System.currentTimeMillis();
      }
   }

   @Inject(method = "drawChat", at = @At("HEAD"), cancellable = true)
   private void esdeath$drawChatHead(int updateCounter, CallbackInfo ci) {
      CleanChat.ensureConfig();
      this.esdeath$slid = false;

      Cordinates off = AnzeigeSettings.getCords("chat");
      float slideY = 0.0F;
      if (CleanChat.active && !(Minecraft.getMinecraft().currentScreen instanceof GuiChat)) {
         long elapsed = System.currentTimeMillis() - CleanChat.lastMessage;
         if (elapsed >= 0 && elapsed < CleanChat.slideMs) {
            float frac = (float) elapsed / (float) CleanChat.slideMs;
            slideY = (1.0F - frac) * (1.0F - frac) * 9.0F;
         }
      }

      if (CleanChat.removeBackground) {
         // fully take over the draw (no per-line background), with offset + slide folded in
         esdeath$drawChatNoBg(updateCounter, off.getX(), (float) off.getY() + slideY);
         ci.cancel();
         return;
      }

      // normal path: just translate the vanilla render by the chat offset + slide
      if (off.getX() != 0 || off.getY() != 0 || slideY != 0.0F) {
         GlStateManager.pushMatrix();
         GlStateManager.translate((float) off.getX(), (float) off.getY() + slideY, 0.0F);
         this.esdeath$slid = true;
      }
   }

   @Inject(method = "drawChat", at = @At("RETURN"))
   private void esdeath$drawChatReturn(int updateCounter, CallbackInfo ci) {
      if (this.esdeath$slid) {
         GlStateManager.popMatrix();
         this.esdeath$slid = false;
      }
   }

   // Faithful re-implementation of GuiNewChat.drawChat minus the per-line background rect.
   @Unique
   private void esdeath$drawChatNoBg(int updateCounter, int offX, float offY) {
      if (this.mc.gameSettings.chatVisibility == EntityPlayer.EnumChatVisibility.HIDDEN) {
         return;
      }
      int i = this.getLineCount();
      boolean flag = this.getChatOpen();
      int j = 0;
      int k = this.drawnChatLines.size();
      float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;
      if (k <= 0) {
         return;
      }
      float f1 = this.getChatScale();
      int l = MathHelper.ceiling_float_int((float) this.getChatWidth() / f1);
      GlStateManager.pushMatrix();
      GlStateManager.translate(2.0F + offX, 20.0F + offY, 0.0F);
      GlStateManager.scale(f1, f1, 1.0F);

      for (int i1 = 0; i1 + this.scrollPos < this.drawnChatLines.size() && i1 < i; ++i1) {
         ChatLine chatline = this.drawnChatLines.get(i1 + this.scrollPos);
         if (chatline != null) {
            int j1 = updateCounter - chatline.getUpdatedCounter();
            if (j1 < 200 || flag) {
               double d0 = (double) j1 / 200.0D;
               d0 = 1.0D - d0;
               d0 = d0 * 10.0D;
               d0 = MathHelper.clamp_double(d0, 0.0D, 1.0D);
               d0 = d0 * d0;
               int l1 = (int) (255.0D * d0);
               if (flag) {
                  l1 = 255;
               }
               l1 = (int) ((float) l1 * f);
               ++j;
               if (l1 > 3) {
                  int j2 = -i1 * 9;
                  // (per-line background rect intentionally omitted)
                  String s = chatline.getChatComponent().getFormattedText();
                  GlStateManager.enableBlend();
                  this.mc.fontRendererObj.drawStringWithShadow(s, 0.0F, (float) (j2 - 8), 16777215 + (l1 << 24));
                  GlStateManager.disableAlpha();
                  GlStateManager.disableBlend();
               }
            }
         }
      }

      if (flag) {
         int k2 = this.mc.fontRendererObj.FONT_HEIGHT;
         GlStateManager.translate(-3.0F, 0.0F, 0.0F);
         int l2 = k * k2 + k;
         int i3 = j * k2 + j;
         int j3 = this.scrollPos * i3 / k;
         int k1 = i3 * i3 / l2;
         if (l2 != i3) {
            int k3 = j3 > 0 ? 170 : 96;
            int l3 = this.isScrolled ? 13382451 : 3355562;
            Gui.drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
            Gui.drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
         }
      }

      GlStateManager.popMatrix();
   }
}
