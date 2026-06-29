package me.txb1.player.buttons;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class CustomButton extends Gui {
   public int id;
   public int yPosition;
   protected boolean hovered;
   public boolean enabled;
   int fade;
   int i;
   int dot;
   public boolean visible;
   public int xPosition;
   protected int height;
   public String displayString;
   protected int width;

   public void drawButtonForegroundLayer(int var1, int var2) {
   }

   public void playPressSound(SoundHandler var1) {
      var1.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
   }

   public String getDisplayString() {
      return this.displayString;
   }

   public void mouseReleased(int var1, int var2) {
   }

   public CustomButton(int var1, int var2, int var3, String var4) {
      this(var1, var2, var3, 200, 20, var4);
   }

   public int getButtonWidth() {
      return this.width;
   }

   public void setWidth(int var1) {
      this.width = var1;
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

   public void drawButton(Minecraft var1, int var2, int var3) {
      if ((this.visible)) {
         String var4 = this.displayString;
         this.dot = this.dot + 1;
         if (((this.dot) > (5))) {
            this.dot = 0;
         }

         FontRenderer var5 = var1.fontRendererObj;
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
         int var6 = this.getHoverState(this.hovered);
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         GlStateManager.blendFunc(770, 771);
         this.mouseDragged(var1, var2, var3);
         if ((this.hovered)) {
            if (((this.fade) < (90))) {
               this.fade = this.fade + 10;
               
            }
         } else if (((this.fade) > (70))) {
            this.fade = this.fade - 3;
         }

         GlStateManager.pushMatrix();
         GlStateManager.scale(0.5F, 0.5F, 0.5F);
         this.drawCenteredString(
            var5,
            this.displayString,
            (this.xPosition + this.width / 2) * 2,
            2 * (this.yPosition + (this.height - 8) / 2 - 8) + 20,
            1631899
         );
         GlStateManager.popMatrix();
         Color var7 = new Color(100, 100, 150, this.fade);
         Gui.drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, var7.getRGB());
      }
   }

   protected void mouseDragged(Minecraft var1, int var2, int var3) {
   }

   public boolean isMouseOver() {
      return this.hovered;
   }

   public CustomButton(int var1, int var2, int var3, int var4, int var5, String var6) {
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
