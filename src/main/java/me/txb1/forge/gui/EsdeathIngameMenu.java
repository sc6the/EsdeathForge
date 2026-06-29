package me.txb1.forge.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import me.txb1.EsdeathClient;
import me.txb1.EsdeathGuiScreen;
import me.txb1.extras.capes.CapeGui;
import me.txb1.extras.cosmetics.gui.CosmeticGui;
import me.txb1.extras.settings.SettingsGui;
import me.txb1.extras.settings.SettingsUtil;
import me.txb1.extras.settings.guisettings.GuiGui;
import me.txb1.extras.settings.link.LinkGui;
import me.txb1.extras.settings.theme.ThemeGui;
import me.txb1.extras.status.StatusGui;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import me.txb1.player.modulesystem.ModuleButton;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

// Clean Forge reconstruction of the standalone's in-game module menu (the obfuscated
// GuiIngameMenu takeover). Reuses the original textured ModuleButton (renders the
// EsdeathButton.png buttons with the green check / red cross + edit pencil). Opened in place
// of the vanilla ESC menu via GuiOpenEvent in ForgeEventBridge.
public class EsdeathIngameMenu extends EsdeathGuiScreen {
   // themed button background (black with wing ornaments) shared with the main menu
   private static final ResourceLocation BUTTON = new ResourceLocation("EsdeathClient/button.jpg");
   // bordered module box geometry (recomputed each frame from screen size)
   private int boxX, boxY, boxRight, boxBottom;
   // left-hand side panel: clickable dark "buttons"
   private final List<EsdeathMenuPanel> panels = new ArrayList<EsdeathMenuPanel>();


   // panel actions
   private static final int A_CAPES = 0, A_DISPLAY = 1, A_COSMETICS = 2, A_STATUS = 3,
      A_BACK_GAME = 4, A_OPTIONS = 5, A_BACK_MENU = 6, A_THEME = 7, A_RELOAD = 9, A_MODS = 10,
      A_SKIN = 11;

   @Override
   public void initGui() {
      super.initGui();
      buildPanels();
      setUpButtons();
      this.lastCat = EsdeathClient.getInstance().cat;
   }

   private void buildPanels() {
      this.panels.clear();
      int px = 20;
      int w = 75, h = 18, gap = 4;
      int py = this.height / 4;
      // 2x2 quick-access row
      this.panels.add(new EsdeathMenuPanel(px, py, w, h, "Capes", A_CAPES));
      this.panels.add(new EsdeathMenuPanel(px + w + gap, py, w, h, "Display", A_DISPLAY));
      this.panels.add(new EsdeathMenuPanel(px, py + h + gap, w, h, "Cosmetics", A_COSMETICS));
      this.panels.add(new EsdeathMenuPanel(px + w + gap, py + h + gap, w, h, "Status", A_STATUS));
      // full-width stacked controls
      int fw = w * 2 + gap;
      int sy = py + (h + gap) * 2 + 14;
      int step = h + gap;
      this.panels.add(new EsdeathMenuPanel(px, sy, fw, h, "Skinchanger", A_SKIN));
      this.panels.add(new EsdeathMenuPanel(px, sy + step, fw, h, "Back to Game", A_BACK_GAME));
      this.panels.add(new EsdeathMenuPanel(px, sy + step * 2, fw, h, "Theme", A_THEME));
      this.panels.add(new EsdeathMenuPanel(px, sy + step * 3, fw, h, "Reload Resources", A_RELOAD));
      this.panels.add(new EsdeathMenuPanel(px, sy + step * 4, fw, h, "Mods", A_MODS));
      // Options + Quit (Back to Menu) pinned to the bottom.
      this.panels.add(new EsdeathMenuPanel(px, sy + step * 5 + 12, fw, h, "Options", A_OPTIONS));
      this.panels.add(new EsdeathMenuPanel(px, sy + step * 6 + 12, fw, h, "Back to Menu", A_BACK_MENU));
   }

   // lay the current category's modules out as a 4-column grid of textured ModuleButtons
   private void setUpButtons() {
      this.moduleButtonList.clear();
      computeBox();
      Category current = EsdeathClient.getInstance().getCategories().get(EsdeathClient.getInstance().cat);

      int cols = 4;
      int pad = 10;
      int gridX = this.boxX + pad;
      int gridTop = this.boxY + 22;
      int cellGapX = 8, cellGapY = 8;
      int cellW = (this.boxRight - this.boxX - pad * 2 - cellGapX * (cols - 1)) / cols;
      int cellH = 58;

      String q = this.search.toLowerCase().trim();
      int id = 0, col = 0, row = 0;
      for (Module m : EsdeathClient.getInstance().getModuleManager().getModules()) {
         if (q.isEmpty()) {
            // no search: show only the active category
            if (!m.getCategory().equals(current)) {
               continue;
            }
         } else {
            // searching: match across ALL categories
            if (!m.getDisplayName().toLowerCase().contains(q) && !m.getName().toLowerCase().contains(q)) {
               continue;
            }
         }
         int bx = gridX + col * (cellW + cellGapX);
         int by = gridTop + row * (cellH + cellGapY);
         this.moduleButtonList.add(new ModuleButton(id++, bx, by, cellW, cellH, m.getDisplayName()));
         if (++col >= cols) {
            col = 0;
            row++;
         }
      }
   }

   private void computeBox() {
      this.boxX = this.width / 3;
      this.boxY = this.height / 6;
      this.boxRight = (int) (this.boxX + this.width / 1.9);
      if (this.boxRight > this.width - 10) {
         this.boxRight = this.width - 10;
      }
      this.boxBottom = this.height - 20;
      // GuiGui ("Display") draws its box + controls at these coords; the standalone module menu
      // stored them here, so we must publish them too or GuiGui renders blank.
      SettingsUtil.vals.put("x", this.boxX);
      SettingsUtil.vals.put("y", this.boxY);
      SettingsUtil.vals.put("right", this.boxRight);
      SettingsUtil.vals.put("bottom", this.boxBottom);
   }

   private int lastCat = -1;
   private String search = "";

   @Override
   protected void keyTyped(char typedChar, int keyCode) throws IOException {
      if (keyCode == 1) { // ESC closes
         super.keyTyped(typedChar, keyCode);
         return;
      }
      if (keyCode == 14) { // backspace
         if (this.search.length() > 0) {
            this.search = this.search.substring(0, this.search.length() - 1);
            setUpButtons();
         }
         return;
      }
      if (typedChar >= 32 && this.search.length() < 24) {
         this.search += typedChar;
         setUpButtons();
         return;
      }
      super.keyTyped(typedChar, keyCode);
   }

   @Override
   public void updateScreen() {
      // only rebuild the button grid when the category actually changes — rebuilding every tick
      // reallocated every ModuleButton 20x/sec and reset their hover fade (laggy/flickery feel).
      int cat = EsdeathClient.getInstance().cat;
      if (cat != this.lastCat) {
         this.lastCat = cat;
         setUpButtons();
      }
      super.updateScreen();
   }

   @Override
   protected void actionPerformed(ModuleButton button) throws IOException {
      Module m = EsdeathClient.getInstance().getModuleManager().getModuleByName(button.displayString);
      boolean onPencil = m.isSetting()
         && button.mouseX > button.xPosition + button.width / 4 * 3
         && button.mouseY > button.yPosition + button.height / 3 * 2
         && button.mouseX < button.xPosition + button.width
         && button.mouseY < button.yPosition + button.height;
      // Move handle (bottom-left) on VISUAL modules opens the single-element drag editor.
      boolean onMove = m.isCategory(Category.VISUAL)
         && button.mouseX >= button.xPosition
         && button.mouseX < button.xPosition + button.width / 4
         && button.mouseY > button.yPosition + button.height / 3 * 2
         && button.mouseY < button.yPosition + button.height;
      if (onMove) {
         this.mc.displayGuiScreen(new me.txb1.extras.settings.anzeige.ModuleMoveGui(m, this));
      } else if (onPencil) {
         net.minecraft.client.gui.GuiScreen custom = m.getCustomSettingsGui();
         this.mc.displayGuiScreen(custom != null ? custom : new SettingsGui(m));
      } else {
         m.toggle();
      }
   }

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      computeBox();

      // ---- module box ----
      Gui.drawRect(this.boxX, this.boxY, this.boxRight, this.boxBottom, new Color(0, 0, 0, 120).getRGB());
      int border = EsdeathClient.getInstance().rainbow(800);
      Gui.drawRect(this.boxX - 2, this.boxY, this.boxX, this.boxBottom, border);
      Gui.drawRect(this.boxX - 2, this.boxY - 2, this.boxRight + 2, this.boxY, border);
      Gui.drawRect(this.boxRight, this.boxY, this.boxRight + 2, this.boxBottom, border);
      Gui.drawRect(this.boxX - 2, this.boxBottom, this.boxRight + 2, this.boxBottom + 2, border);

      String cat = EsdeathClient.getInstance().getCategories().get(EsdeathClient.getInstance().cat).name();
      this.fontRendererObj.drawStringWithShadow(
         "§lCategory : " + cat + " §7(Click to change, right-click back)",
         this.boxX + 8, this.boxY + 7, border);

      // search box (top-right of the module box header) — type to filter the visible modules
      int sbW = 110;
      int sbX = this.boxRight - sbW - 6;
      int sbY = this.boxY + 5;
      Gui.drawRect(sbX, sbY, sbX + sbW, sbY + 12, 0x80000000);
      Gui.drawRect(sbX, sbY, sbX + sbW, sbY + 1, 0xFF555555);
      String shown = this.search.isEmpty() ? "§7Search..." : this.search + (System.currentTimeMillis() % 1000 < 500 ? "_" : "");
      this.fontRendererObj.drawStringWithShadow(shown, sbX + 4, sbY + 2, -1);

      // ---- side panel ----
      for (EsdeathMenuPanel p : this.panels) {
         if (p.w == 0) {
            continue;
         }
         boolean hov = p.hovered(mouseX, mouseY);
         drawThemedButton(this.mc, BUTTON, p.x, p.y, p.w, p.h, hov);
         if (hov && !ButtonStyle.vanilla) { // vanilla style shows its own hover texture
            Gui.drawRect(p.x, p.y, p.x + p.w, p.y + p.h, new Color(40, 40, 40, 150).getRGB());
         }
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         this.fontRendererObj.drawStringWithShadow(p.label,
            p.x + p.w / 2.0F - this.fontRendererObj.getStringWidth(p.label) / 2.0F, p.y + (p.h - 8) / 2.0F, 0xBBBBBB);
      }

      // draws moduleButtonList (textured ModuleButtons) via EsdeathGuiScreen
      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   @Override
   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      // search box swallows its own clicks (don't let them fall through to the category switch)
      int sbW = 110;
      int sbX = this.boxRight - sbW - 6;
      int sbY = this.boxY + 5;
      if (mouseX >= sbX && mouseX <= sbX + sbW && mouseY >= sbY && mouseY <= sbY + 12) {
         return;
      }

      // category header → cycle category (left-click forward, right-click back)
      if (mouseX >= this.boxX && mouseX <= this.boxRight && mouseY >= this.boxY && mouseY <= this.boxY + 20) {
         EsdeathClient esd = EsdeathClient.getInstance();
         int n = esd.getCategories().size();
         esd.cat = mouseButton == 1 ? (esd.cat - 1 + n) % n : (esd.cat + 1) % n;
         this.search = "";
         setUpButtons();
         return;
      }
      // side panel
      for (EsdeathMenuPanel p : this.panels) {
         if (p.w != 0 && p.hovered(mouseX, mouseY)) {
            handlePanel(p.action);
            return;
         }
      }
      super.mouseClicked(mouseX, mouseY, mouseButton);
   }

   private void handlePanel(int action) {
      switch (action) {
         case A_CAPES: this.mc.displayGuiScreen(new me.proxycracked.capemod.gui.GuiChangeCape(this)); break;
         case A_DISPLAY: this.mc.displayGuiScreen(new me.txb1.extras.settings.anzeige.HudEditorGui(this)); break;
         case A_COSMETICS: this.mc.displayGuiScreen(new CosmeticGui()); break;
         case A_STATUS: this.mc.displayGuiScreen(new StatusGui()); break;
         case A_SKIN: this.mc.displayGuiScreen(new me.txb1.extras.skin.EsdeathSkinGui(this)); break;
         case A_BACK_GAME: this.mc.displayGuiScreen(null); this.mc.setIngameFocus(); break;
         case A_OPTIONS: this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiOptions(this, this.mc.gameSettings)); break;
         case A_BACK_MENU:
            this.mc.theWorld.sendQuittingDisconnectingPacket();
            this.mc.loadWorld(null);
            this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiMainMenu());
            break;
         case A_THEME: this.mc.displayGuiScreen(new ThemeGui(this)); break;
         case A_RELOAD: this.mc.refreshResources(); break;
         case A_MODS: this.mc.displayGuiScreen(new net.minecraftforge.fml.client.GuiModList(this)); break;
         default: break;
      }
   }

   @Override
   public boolean doesGuiPauseGame() {
      return true;
   }

   // shared themed-button renderer. FANCY = button.jpg (black w/ wing ornaments); SEMI_TRANSPARENT =
   // a plain black slab; VANILLA = the real Minecraft button from widgets.png. Chosen in the Theme menu.
   static void drawThemedButton(net.minecraft.client.Minecraft mc, ResourceLocation tex, int x, int y, int w, int h) {
      drawThemedButton(mc, tex, x, y, w, h, false);
   }

   static void drawThemedButton(net.minecraft.client.Minecraft mc, ResourceLocation tex, int x, int y, int w, int h, boolean hovered) {
      if (ButtonStyle.vanilla) {
         drawVanillaButton(mc, x, y, w, h, hovered);
         return;
      }
      if (ButtonStyle.semiTransparent) {
         Gui.drawRect(x, y, x + w, y + h, new Color(0, 0, 0, ButtonStyle.opacity).getRGB());
         return;
      }
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.enableBlend();
      try {
         mc.getTextureManager().bindTexture(tex);
         Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, w, h, (float) w, (float) h);
      } catch (Throwable t) {
         Gui.drawRect(x, y, x + w, y + h, new Color(8, 8, 14, 235).getRGB());
      }
   }

   // the vanilla button background (widgets.png, idle "enabled" state at v=66), split-blitted as two
   // halves so arbitrary widths look right — exactly how GuiButton renders. Callers add their own hover.
   private static final ResourceLocation WIDGETS = new ResourceLocation("textures/gui/widgets.png");

   private static void drawVanillaButton(net.minecraft.client.Minecraft mc, int x, int y, int w, int h, boolean hovered) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.enableBlend();
      mc.getTextureManager().bindTexture(WIDGETS);
      int v = hovered ? 86 : 66; // 46 + state*20: state 1 = idle, state 2 = hovered
      int half = w / 2;
      Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, (float) v, half, h, 256.0F, 256.0F);
      Gui.drawModalRectWithCustomSizedTexture(x + half, y, (float) (200 - (w - half)), (float) v, w - half, h, 256.0F, 256.0F);
   }
}
