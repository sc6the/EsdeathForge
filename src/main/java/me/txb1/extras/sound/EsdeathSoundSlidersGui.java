package me.txb1.extras.sound;

import com.example.soundsliders.SoundSliders;
import dev.mergedvoicechat.MergedVoiceChat;
import net.labymod.addons.voicechat.VoiceChat;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

// Esdeath sound-sliders screen (replaces the bundled SoundSliders GUI). Adds Redstone + Voice Chat
// sliders alongside the original Damage + Footsteps ones:
//   * Damage / Footsteps -> the bundled SoundSliders.CONFIG (its handler keeps scaling them)
//   * Redstone           -> RedstoneVolume (scaled by RedstoneSoundHandler)
//   * Voice Chat         -> VoiceChat.surroundVolume (the merged voice-chat surround volume)
// Sliders are custom-drawn (no GuiSlider) and indexed so there are no inner/anonymous classes for
// raven's transformer to choke on.
public class EsdeathSoundSlidersGui extends GuiScreen {
   private static final int ID_DONE = 0, ID_RESET = 1;
   private static final int SLIDER_W = 240, SLIDER_H = 8, HANDLE_W = 6, HANDLE_H = 16;

   private final GuiScreen parent;
   private final String[] labels = {"Damage", "Footsteps", "Redstone", "Voice Chat"};
   private int sliderX, firstY, rowGap;
   private int dragging = -1;

   public EsdeathSoundSlidersGui(GuiScreen parent) {
      this.parent = parent;
   }

   private static VoiceChat vc() {
      MergedVoiceChat m = MergedVoiceChat.INSTANCE;
      return m == null ? null : m.voiceChat;
   }

   // 0-200% for the vanilla-sound sliders, 0-500% for voice (matches its own settings range).
   private float maxVal(int i) {
      return i == 3 ? 5.0F : 2.0F;
   }

   private float getVal(int i) {
      switch (i) {
         case 0: return SoundSliders.CONFIG.damageVolume;
         case 1: return SoundSliders.CONFIG.stepVolume;
         case 2: return RedstoneVolume.get();
         case 3: VoiceChat v = vc(); return v == null ? 1.0F : v.surroundVolume / 100.0F;
         default: return 1.0F;
      }
   }

   private void setVal(int i, float val) {
      switch (i) {
         case 0: SoundSliders.CONFIG.damageVolume = val; break;
         case 1: SoundSliders.CONFIG.stepVolume = val; break;
         case 2: RedstoneVolume.set(val); break;
         case 3: VoiceChat v = vc(); if (v != null) v.surroundVolume = Math.round(val * 100.0F); break;
         default: break;
      }
   }

   @Override
   public void initGui() {
      this.buttonList.clear();
      this.sliderX = this.width / 2 - SLIDER_W / 2;
      this.rowGap = 38;
      this.firstY = this.height / 5 + 10;
      int by = this.firstY + this.labels.length * this.rowGap + 14;
      this.buttonList.add(new GuiButton(ID_RESET, this.width / 2 - 100, by, 95, 20, "Reset"));
      this.buttonList.add(new GuiButton(ID_DONE, this.width / 2 + 5, by, 95, 20, "Done"));
   }

   private int rowY(int i) {
      return this.firstY + i * this.rowGap;
   }

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRendererObj, "Sound Sliders", this.width / 2, this.firstY - 28, 0xFFFFFFFF);
      for (int i = 0; i < this.labels.length; i++) {
         drawSlider(i, mouseX, mouseY);
      }
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   private void drawSlider(int i, int mouseX, int mouseY) {
      int y = rowY(i);
      float max = maxVal(i);
      float frac = Math.max(0.0F, Math.min(1.0F, getVal(i) / max));
      int pct = Math.round(getVal(i) * 100.0F);
      this.fontRendererObj.drawStringWithShadow(this.labels[i] + ": " + pct + "%", this.sliderX, y - 10, 0xFFCCCCCC);

      int railY = y + (HANDLE_H - SLIDER_H) / 2;
      Gui.drawRect(this.sliderX, railY, this.sliderX + SLIDER_W, railY + SLIDER_H, 0xFF202020);
      int fillW = (int) (frac * (SLIDER_W - HANDLE_W));
      Gui.drawRect(this.sliderX, railY, this.sliderX + fillW + HANDLE_W, railY + SLIDER_H, 0xFF3F7FBF);
      int hx = this.sliderX + fillW;
      boolean hov = overSlider(i, mouseX, mouseY) || this.dragging == i;
      Gui.drawRect(hx, y, hx + HANDLE_W, y + HANDLE_H, hov ? 0xFFFFFFFF : 0xFFBBBBBB);
   }

   private boolean overSlider(int i, int mouseX, int mouseY) {
      int y = rowY(i);
      return mouseX >= this.sliderX && mouseX <= this.sliderX + SLIDER_W && mouseY >= y && mouseY <= y + HANDLE_H;
   }

   private float valueFromMouse(int i, int mouseX) {
      float frac = (float) (mouseX - this.sliderX) / (float) (SLIDER_W - HANDLE_W);
      frac = Math.max(0.0F, Math.min(1.0F, frac));
      return frac * maxVal(i);
   }

   @Override
   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      if (mouseButton == 0) {
         for (int i = 0; i < this.labels.length; i++) {
            if (overSlider(i, mouseX, mouseY)) {
               this.dragging = i;
               setVal(i, valueFromMouse(i, mouseX));
               return;
            }
         }
      }
      super.mouseClicked(mouseX, mouseY, mouseButton);
   }

   @Override
   protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long time) {
      if (this.dragging >= 0) {
         setVal(this.dragging, valueFromMouse(this.dragging, mouseX));
      }
   }

   @Override
   protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
      this.dragging = -1;
   }

   @Override
   protected void actionPerformed(GuiButton button) throws IOException {
      switch (button.id) {
         case ID_RESET:
            for (int i = 0; i < this.labels.length; i++) {
               setVal(i, 1.0F);
            }
            break;
         case ID_DONE:
            persist();
            this.mc.displayGuiScreen(this.parent);
            break;
         default:
            break;
      }
   }

   @Override
   public void onGuiClosed() {
      persist();
   }

   private void persist() {
      try {
         SoundSliders.CONFIG.save();
      } catch (Throwable ignored) {
      }
      RedstoneVolume.save();
      VoiceChat v = vc();
      if (v != null) {
         dev.mergedvoicechat.Config.save(v);
      }
   }

   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }
}
