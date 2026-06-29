package me.txb1.player.buttons;

import java.awt.Color;
import me.txb1.EsdeathClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class CosmeticButton extends Gui {
   int fade;
   protected boolean hovered;
   public boolean enabled;
   int i;
   protected static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");
   public int xPosition;
   int dot;
   protected int height;
   public int id;
   public String displayString;
   public int yPosition;
   public boolean visible;
   protected int width;

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

   public void drawButtonForegroundLayer(int var1, int var2) {
   }

   public void setWidth(int var1) {
      this.width = var1;
   }

   public boolean isMouseOver() {
      return this.hovered;
   }

   public void mouseReleased(int var1, int var2) {
   }

   public int getButtonWidth() {
      return this.width;
   }

   public CosmeticButton(int var1, int var2, int var3, int var4, int var5, String var6) {
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

   public CosmeticButton(int var1, int var2, int var3, String var4) {
      this(var1, var2, var3, 200, 20, var4);
   }

   public void drawButton(Minecraft var1, int var2, int var3) {
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
         if (!(this.enabled)) {
            var6 = 10526880;
            
         } else if ((this.hovered)) {
            var6 = 16777120;
            if (((this.fade) < (80))) {
               this.fade = this.fade + 10;
               if (((202 + 98 - 175 + 96 ^ 137 + 107 - 179 + 82) & (3 + 3 - -65 + 160 ^ 150 + 131 - 230 + 118 ^ -1))
                  != ((83 ^ 75 ^ 59 ^ 42) & (120 ^ 116 ^ 192 ^ 197 ^ -1))) {
                  return;
               }
            }
         } else if (((this.fade) > (30))) {
            this.fade = this.fade - 3;
         }

         if ((EsdeathClient.getInstance().getPlayer(var1.thePlayer.getUniqueID().toString()).getCosmetics().contains(this.displayString.toLowerCase()))) {
            this.drawCenteredString(
               var4,
               String.valueOf(new StringBuilder().append("§a").append(this.displayString)),
               this.xPosition + this.width / 2,
               this.yPosition + (this.height - 8) / 2,
               EsdeathClient.getInstance().rainbow(200)
            );
            
         } else {
            this.drawCenteredString(
               var4,
               String.valueOf(new StringBuilder().append("§c").append(this.displayString)),
               this.xPosition + this.width / 2,
               this.yPosition + (this.height - 8) / 2,
               EsdeathClient.getInstance().rainbow(200)
            );
         }

         if ((this.hovered)) {
            this.drawHorizontalLine(
               this.xPosition + 1,
               this.xPosition - 2 + this.width,
               this.yPosition + this.height - 2,
               EsdeathClient.getInstance().rainbow(400)
            );
            this.drawVerticalLine(
               this.xPosition + 1, this.yPosition + 1, this.yPosition - 2 + this.height, EsdeathClient.getInstance().rainbow(400)
            );
            this.drawVerticalLine(
               this.xPosition - 2 + this.width,
               this.yPosition + 1,
               this.yPosition - 2 + this.height,
               EsdeathClient.getInstance().rainbow(400)
            );
            this.drawHorizontalLine(
               this.xPosition + 1, this.xPosition - 2 + this.width, this.yPosition + 1, EsdeathClient.getInstance().rainbow(400)
            );
         }

         Color var7 = new Color(100, 100, 40, this.fade);
         Gui.drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, var7.getRGB());
      }
   }

   public void playPressSound(SoundHandler var1) {
      var1.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
   }

   protected void mouseDragged(Minecraft var1, int var2, int var3) {
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
}
