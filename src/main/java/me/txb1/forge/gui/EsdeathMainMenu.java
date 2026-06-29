package me.txb1.forge.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.txb1.EsdeathClient;
import me.txb1.extras.accountmanager.AccountManager;
import me.txb1.extras.settings.theme.ThemeGui;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

// Clean Forge reconstruction of the standalone's themed main menu (the obfuscated GuiMainMenu
// takeover). Swapped in for the vanilla main menu via GuiOpenEvent. Buttons are drawn in the
// same dark Esdeath style as the in-game module menu.
public class EsdeathMainMenu extends GuiScreen {
   private static final ResourceLocation BG = new ResourceLocation("EsdeathClient/MainBackground.jpg");
   private static final ResourceLocation BUTTON_TEX = new ResourceLocation("EsdeathClient/button.jpg");
   private final List<Panel> panels = new ArrayList<Panel>();

   private static final int A_SINGLE = 0, A_MULTI = 1, A_OPTIONS = 2, A_QUIT = 3,
      A_THEME = 4, A_ART = 5, A_ACCOUNTS = 6, A_MODS = 7;

   private static final class Panel {
      final int x, y, w, h;
      final String label;
      final int action;
      int fade = 30;

      Panel(int x, int y, int w, int h, String label, int action) {
         this.x = x; this.y = y; this.w = w; this.h = h; this.label = label; this.action = action;
      }

      boolean hovered(int mx, int my) {
         return mx >= x && my >= y && mx < x + w && my < y + h;
      }
   }

   @Override
   public void initGui() {
      // re-roll a random favourite background each time the home screen opens (if random mode is on)
      me.txb1.forge.gui.art.BackgroundManager.get().maybeRollRandom();
      this.panels.clear();
      int x = this.width / 8;
      int w = 150, h = 20, gap = 6;
      int y = this.height / 4;
      this.panels.add(new Panel(x, y, w, h, "SINGLEPLAYER", A_SINGLE));
      this.panels.add(new Panel(x, y + (h + gap), w, h, "MULTIPLAYER", A_MULTI));
      int y2 = y + (h + gap) * 2 + 14;
      this.panels.add(new Panel(x, y2, w, h, "ACCOUNT MANAGER", A_ACCOUNTS));
      this.panels.add(new Panel(x, y2 + (h + gap), w, h, "MODS", A_MODS));
      this.panels.add(new Panel(x, y2 + (h + gap) * 2, w, h, "OPTIONS", A_OPTIONS));
      this.panels.add(new Panel(x, y2 + (h + gap) * 3, w, h, "QUIT GAME", A_QUIT));
      // Theme + Art below Quit Game.
      int y3 = y2 + (h + gap) * 4 + 14;
      int half = (w - gap) / 2;
      this.panels.add(new Panel(x, y3, half, h, "THEME", A_THEME));
      this.panels.add(new Panel(x + half + gap, y3, half, h, "ART", A_ART));
   }

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      // background art stretched to the full screen
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      try {
         ResourceLocation bg = me.txb1.forge.gui.art.BackgroundManager.get().getSelectedTexture();
         this.mc.getTextureManager().bindTexture(bg != null ? bg : BG);
         Gui.drawModalRectWithCustomSizedTexture(0, 0, 0.0F, 0.0F, this.width, this.height, (float) this.width, (float) this.height);
      } catch (Throwable t) {
         this.drawDefaultBackground();
      }

      // falling-snow overlay (over the background, behind the buttons) — toggled in Theme -> Snow
      if (me.txb1.extras.snow.SnowSettings.menuEnabled) {
         me.txb1.extras.snow.SnowRenderer.render(this.width, this.height, mouseX, mouseY);
      }

      for (Panel p : this.panels) {
         boolean hov = p.hovered(mouseX, mouseY);
         EsdeathIngameMenu.drawThemedButton(this.mc, BUTTON_TEX, p.x, p.y, p.w, p.h, hov);
         if (hov && !me.txb1.forge.gui.ButtonStyle.vanilla) { // vanilla style shows its own hover texture
            Gui.drawRect(p.x, p.y, p.x + p.w, p.y + p.h, new Color(40, 40, 40, 150).getRGB());
         }
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         this.fontRendererObj.drawStringWithShadow(p.label,
            p.x + p.w / 2.0F - this.fontRendererObj.getStringWidth(p.label) / 2.0F, p.y + (p.h - 8) / 2.0F, 0xBBBBBB);
      }

      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   @Override
   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      for (Panel p : this.panels) {
         if (p.hovered(mouseX, mouseY)) {
            handle(p.action);
            return;
         }
      }
      super.mouseClicked(mouseX, mouseY, mouseButton);
   }

   private void handle(int action) {
      switch (action) {
         case A_SINGLE: this.mc.displayGuiScreen(new GuiSelectWorld(this)); break;
         case A_MULTI: this.mc.displayGuiScreen(new me.txb1.forge.gui.server.EsdeathServerListGui(this)); break;
         case A_OPTIONS: this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings)); break;
         case A_QUIT: this.mc.shutdown(); break;
         case A_THEME: this.mc.displayGuiScreen(new ThemeGui(this)); break;
         case A_ART: this.mc.displayGuiScreen(new me.txb1.forge.gui.art.EsdeathArtGui(this)); break;
         case A_ACCOUNTS: this.mc.displayGuiScreen(new AccountManager()); break;
         case A_MODS: this.mc.displayGuiScreen(new net.minecraftforge.fml.client.GuiModList(this)); break;
         default: break;
      }
   }

   @Override
   public boolean doesGuiPauseGame() {
      return false;
   }
}
