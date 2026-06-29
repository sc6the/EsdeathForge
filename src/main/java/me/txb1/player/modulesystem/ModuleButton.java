package me.txb1.player.modulesystem;

import java.awt.Color;
import me.txb1.EsdeathClient;
import me.txb1.extras.settings.guisettings.GuiSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ModuleButton extends Gui {
   protected boolean hovered;
   int i;
   public int xPosition;
   public int height;
   int dot;
   int fade;
   public int mouseY;
   public String displayString;
   private Module cachedModule; // resolved once (drawButton looks it up twice per frame)
   public int width;
   public int yPosition;
   public int id;
   public int mouseX;
   public boolean visible;
   public boolean enabled;

   public void drawButtonForegroundLayer(int var1, int var2) {
   }

   private Module module() {
      if (this.cachedModule == null) {
         this.cachedModule = EsdeathClient.getInstance().getModuleManager().getModuleByName(this.displayString);
      }
      return this.cachedModule;
   }

   public ModuleButton(int var1, int var2, int var3, int var4, int var5, String var6) {
      this.dot = 0;
      this.i = 0;
      this.fade = 30;
      this.width = 200;
      this.height = 20;
      this.enabled = true;
      this.visible = true;
      this.id = var1;
      this.xPosition = var2;
      this.yPosition = var3;
      this.width = var4;
      this.height = var5;
      this.displayString = var6;
   }

   public void mouseReleased(int var1, int var2) {
   }

   public int getButtonWidth() {
      return this.width;
   }

   public void playPressSound(SoundHandler var1) {
      var1.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
   }

   protected void mouseDragged(Minecraft var1, int var2, int var3) {
   }

   public boolean isMouseOver() {
      return this.hovered;
   }

   public ModuleButton(int var1, int var2, int var3, String var4) {
      this(var1, var2, var3, 200, 20, var4);
   }

   public void drawButton(Minecraft var1, int var2, int var3) {
      this.mouseX = var2;
      this.mouseY = var3;
      if ((this.visible)) {
         this.dot = this.dot + 1;
         if (((this.dot) > (5))) {
            this.dot = 0;
         }

         FontRenderer var4 = var1.fontRendererObj;
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         int var10001;
         if (((var2) >= (this.xPosition))
            && ((var3) >= (this.yPosition))
            && ((var2) < (this.xPosition + this.width))
            && ((var3) < (this.yPosition + this.height))) {
            var10001 = 1;
            
         } else {
            var10001 = 0;
         }

         this.hovered = (var10001 != 0);
         int var5 = this.getHoverState(this.hovered);
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         GlStateManager.blendFunc(770, 771);
         this.mouseDragged(var1, var2, var3);
         int var6 = 14737632;
         boolean var7 = this.module().isSetting();
         int var8 = 0;
         if ((var7)
            && ((var2) > (this.xPosition + this.width / 4 * 3))
            && ((var3) > (this.yPosition + this.height / 3 * 2))
            && ((var2) < (this.xPosition + this.width))
            && ((var3) < (this.yPosition + this.height))) {
            var8 = 1;
         }

         if (!(this.enabled)) {
            var6 = 10526880;
            
         } else if ((this.hovered) && ((var8) == 0)) {
            var6 = 16777120;
            if (((this.fade) < (80))) {
               this.fade = this.fade + 5;
               
            }
         } else if (((this.fade) > (30))) {
            this.fade = this.fade - 1;
         }

         // Plain dark fill — no textured black border (the status outline below is the only border now).
         Gui.drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height,
            new Color(12, 12, 18, 235).getRGB());

         // Status outline (replaces the old check/cross symbols): grey when inactive, green when active.
         int ox = this.xPosition, oy = this.yPosition, ow = this.width, oh = this.height;
         int outline = this.module().isEnabled() ? 0xFF55DD55 : 0xFF888888;
         Gui.drawRect(ox, oy, ox + ow, oy + 1, outline);          // top
         Gui.drawRect(ox, oy + oh - 1, ox + ow, oy + oh, outline); // bottom
         Gui.drawRect(ox, oy, ox + 1, oy + oh, outline);          // left
         Gui.drawRect(ox + ow - 1, oy, ox + ow, oy + oh, outline); // right

         // Hover: a grey semitransparent overlay over the button (also tints the outline grey).
         if (this.hovered) {
            Gui.drawRect(ox, oy, ox + ow, oy + oh, 0x55AAAAAA);
         }

         // Module name: white, light grey on hover. Centered, NO vertical shift on hover.
         int textColor = this.hovered ? 0xFFBBBBBB : 0xFFFFFFFF;
         GlStateManager.pushMatrix();
         GlStateManager.scale(0.8F, 0.8F, 0.8F);
         this.drawCenteredString(
            var4,
            this.displayString,
            (int) ((this.xPosition + this.width / 2) / 0.8F),
            (int) ((this.yPosition + (this.height - 8) / 2) / 0.8F),
            textColor
         );
         GlStateManager.popMatrix();

         // Move handle (bottom-left): a 4-arrow "move" icon shown on positionable VISUAL modules.
         // Clicking it opens the single-element move editor; the pencil (bottom-right) opens config.
         if (this.module().isCategory(Category.VISUAL)) {
            drawMoveIcon(this.xPosition + 3, this.yPosition + this.height - 12, EsdeathClient.getInstance().rainbow(500));
         }

         // Edit pencil (bottom-right) on configurable modules — replaces the one the old texture drew.
         if (var7) {
            drawPencilIcon(this.xPosition + this.width - 12, this.yPosition + this.height - 12, var8 != 0);
         }

         new Color(66, 66, 146, this.fade + 25);
      }
   }

   // a small pencil icon (diagonal red body + light tip) with a 1px drop shadow. Brighter on hover.
   private static void drawPencilIcon(int ox, int oy, boolean hovered) {
      pencilPass(ox + 1, oy + 1, 0xFF000000, 0xFF000000); // shadow
      pencilPass(ox, oy, hovered ? 0xFFFF6464 : 0xFFD83A3A, 0xFFEEEEEE);
   }

   private static void pencilPass(int ox, int oy, int body, int tip) {
      // diagonal body from top-right to bottom-left (2px thick)
      for (int k = 0; k < 7; k++) {
         int cx = 6 - k;
         int cy = k;
         Gui.drawRect(ox + cx, oy + cy, ox + cx + 2, oy + cy + 2, body);
      }
      // writing tip highlight at the bottom-left end
      Gui.drawRect(ox, oy + 7, ox + 2, oy + 9, tip);
   }

   // a recognisable 9x9 four-directional move icon (cross + arrowheads), with a 1px drop shadow for
   // contrast against any button background. Drawn with rects so it never depends on font glyphs.
   private static void drawMoveIcon(int ox, int oy, int color) {
      moveIconPass(ox + 1, oy + 1, 0xFF000000); // shadow
      moveIconPass(ox, oy, color);
   }

   private static void moveIconPass(int ox, int oy, int c) {
      Gui.drawRect(ox + 4, oy, ox + 5, oy + 9, c);     // vertical stem
      Gui.drawRect(ox, oy + 4, ox + 9, oy + 5, c);     // horizontal stem
      // arrowhead wings (1px each)
      px(ox, oy, 3, 1, c); px(ox, oy, 5, 1, c);        // up
      px(ox, oy, 3, 7, c); px(ox, oy, 5, 7, c);        // down
      px(ox, oy, 1, 3, c); px(ox, oy, 1, 5, c);        // left
      px(ox, oy, 7, 3, c); px(ox, oy, 7, 5, c);        // right
   }

   private static void px(int ox, int oy, int cx, int cy, int c) {
      Gui.drawRect(ox + cx, oy + cy, ox + cx + 1, oy + cy + 1, c);
   }

   protected int getHoverState(boolean var1) {
      int var2 = 1;
      if (!(this.enabled)) {
         var2 = 0;
         
      } else if ((var1)) {
         var2 = 2;
      }

      return var2;
   }

   public void setWidth(int var1) {
      this.width = var1;
   }

   public String getDisplayString() {
      return this.displayString;
   }

   public boolean mousePressed(Minecraft var1, int var2, int var3) {
      int var10000;
      if ((this.enabled)
         && (this.visible)
         && ((var2) >= (this.xPosition))
         && ((var3) >= (this.yPosition))
         && ((var2) < (this.xPosition + this.width))
         && ((var3) < (this.yPosition + this.height))) {
         var10000 = 1;
         
      } else {
         var10000 = 0;
      }

      return (var10000 != 0);
   }

}
