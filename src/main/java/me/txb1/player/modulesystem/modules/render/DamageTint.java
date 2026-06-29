package me.txb1.player.modulesystem.modules.render;

import me.txb1.EsdeathClient;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

// DamageTint (ported from Damage_Tint_1.2): recolours the entity hurt-flash (the red overlay
// drawn on a living entity while hurtTime>0). The vanilla colour (1,0,0,0.3 RGBA) is loaded as
// 4 constants into RendererLivingEntity.brightnessBuffer; an ASM op replaces those 4 operands
// with GETSTATIC reads of the live fields below (patch-asm/colors.tsv). When the module is OFF
// the fields hold the vanilla values, so the flash looks stock.
public class DamageTint extends Module {
   // live values read by the patched bytecode (default = vanilla red flash)
   public static float red = 1.0F;
   public static float green = 0.0F;
   public static float blue = 0.0F;
   public static float alpha = 0.3F;

   // user-configured values (applied while enabled, persisted)
   private float cRed = 1.0F;
   private float cGreen = 0.0F;
   private float cBlue = 0.0F;
   private float cAlpha = 0.3F;
   private boolean loaded;
   private int dragIdx = -1;   // which slider is being dragged (0..3), -1 none

   private static final int SL_DX = 10;
   private static final int SL_W = 150;
   private static final int SL_H = 10;
   private static final int SL_KNOB = 6;
   private static final int ROW_H = 16;
   private static final int TOP_DY = 30;
   private static final String[] LABELS = {"R", "G", "B", "A"};

   public DamageTint() {
      super("DamageTint", "DamageTint", Category.RENDER, true);
   }

   @Override
   public void onEnable() {
      this.loadColor();
      this.applyLive();
   }

   @Override
   public void onDisable() {
      // revert to the vanilla red flash so the hurt overlay looks stock when off
      red = 1.0F;
      green = 0.0F;
      blue = 0.0F;
      alpha = 0.3F;
   }

   private void applyLive() {
      red = this.cRed;
      green = this.cGreen;
      blue = this.cBlue;
      alpha = this.cAlpha;
   }

   private float comp(int i) {
      switch (i) {
         case 1:
            return this.cGreen;
         case 2:
            return this.cBlue;
         case 3:
            return this.cAlpha;
         default:
            return this.cRed;
      }
   }

   private void setComp(int i, float v) {
      switch (i) {
         case 1:
            this.cGreen = v;
            break;
         case 2:
            this.cBlue = v;
            break;
         case 3:
            this.cAlpha = v;
            break;
         default:
            this.cRed = v;
      }
      this.applyLive();
   }

   @Override
   public void onSettingsDrawScreen(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      int sx = var3 + SL_DX;
      boolean down = Mouse.isButtonDown(0);

      Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("§7Hurt-flash colour (RGBA)", (float)sx, (float)(var4 + 18), -1);

      for (int i = 0; i < 4; i++) {
         int sy = var4 + TOP_DY + i * ROW_H;
         boolean inTrack = var1 >= sx && var1 <= sx + SL_W && var2 >= sy - 2 && var2 <= sy + SL_H + 2;
         if (down && (this.dragIdx == i || (this.dragIdx == -1 && inTrack))) {
            this.dragIdx = i;
            float frac = (float)(var1 - sx) / (float)(SL_W - SL_KNOB);
            if (frac < 0.0F) {
               frac = 0.0F;
            } else if (frac > 1.0F) {
               frac = 1.0F;
            }
            this.setComp(i, frac);
         }
         float frac = this.comp(i);
         Gui.drawRect(sx, sy, sx + SL_W, sy + SL_H, 0x80000000);
         Gui.drawRect(sx, sy, sx + SL_W, sy + 1, 0xFF555555);
         int knobX = sx + (int)(frac * (SL_W - SL_KNOB));
         Gui.drawRect(knobX, sy, knobX + SL_KNOB, sy + SL_H, 0xFFAAAAAA);
         Minecraft.getMinecraft().fontRendererObj
            .drawStringWithShadow(String.format("§7%s §f%.2f", LABELS[i], frac), (float)(sx + SL_W + 8), (float)(sy + 1), -1);
      }

      if (!down && this.dragIdx != -1) {
         this.dragIdx = -1;
         this.saveColor();
      }

      super.onSettingsDrawScreen(var1, var2, var3, var4, var5, var6, var7, var8);
   }

   private void loadColor() {
      if (this.loaded) {
         return;
      }
      try {
         Object o = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("damagetint_color");
         if (o != null) {
            String[] p = String.valueOf(o).split(",");
            if (p.length == 4) {
               this.cRed = Float.parseFloat(p[0]);
               this.cGreen = Float.parseFloat(p[1]);
               this.cBlue = Float.parseFloat(p[2]);
               this.cAlpha = Float.parseFloat(p[3]);
            }
         }
      } catch (Exception ignored) {
      }
      this.loaded = true;
   }

   private void saveColor() {
      try {
         EsdeathClient.getInstance().getFireDB().getDataBase()
            .saveObject("damagetint_color", this.cRed + "," + this.cGreen + "," + this.cBlue + "," + this.cAlpha);
         EsdeathClient.getInstance().getFireDB().getDataBase().push();
      } catch (Exception ignored) {
      }
   }
}
