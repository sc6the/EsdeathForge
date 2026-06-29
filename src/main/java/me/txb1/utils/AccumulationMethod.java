package me.txb1.utils;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class AccumulationMethod {
   private static final int multiplier = 60;
   private static long lastTimestampInGame;
   private static int i = 0;

   private static int om10(long var0, long var2) {
      long var4;
      return (var4 = var0 - var2) == 0L ? 0 : (var4 < 0L ? -1 : 1);
   }

   public static float getMultiplier() {
      float var10000;
      if (((getFps()) > (120))) {
         if (((getFps()) > (200))) {
            var10000 = 60.0F;
            
         } else {
            var10000 = 30.0F;
            
         }
      } else {
         var10000 = 0.0F;
      }

      return var10000;
   }

   private static int om11(float var0, float var1) {
      float var2;
      return (var2 = var0 - var1) == 0.0F ? 0 : (var2 < 0.0F ? -1 : 1);
   }

   public static void createAccumulation() {
      int var0 = Minecraft.getDebugFPS();
      if (((i) == 0)) {
         GL11.glAccum(257, 1.0F / (float)var0);
         if (((18 + 83 - -14 + 19 ^ 153 + 46 - 182 + 141) & (66 ^ 4 ^ 79 ^ 17 ^ -1))
            != ((58 + 96 - -46 + 3 ^ 27 + 92 - -9 + 26) & (108 ^ 70 ^ 29 ^ 102 ^ -1))) {
            return;
         }
      } else {
         GL11.glAccum(256, 1.0F / (float)var0);
      }

      i = i + 1;
      if (((i) >= (var0))) {
         i = 0;
         GL11.glAccum(258, 1.0F);
      }
   }

   public static float getAccumulationValue() {
      float var0 = getMultiplier() * 10.0F;
      lastTimestampInGame = System.currentTimeMillis();
      if (((om11(var0, 996.0F)) > 0)) {
         var0 = 996.0F;
      }

      if (((om11(var0, 990.0F)) > 0)) {
         var0 = 990.0F;
      }

      long var1 = System.currentTimeMillis() - lastTimestampInGame;
      if (((om10(var1, 10000L)) > 0)) {
         return 0.0F;
      } else {
         if (((om12(var0, 0.0F)) < 0)) {
            var0 = 0.0F;
         }

         return var0 / 1000.0F;
      }
   }

   public static int getFps() {
      return Minecraft.getDebugFPS();
   }

   private static int om12(float var0, float var1) {
      float var2;
      return (var2 = var0 - var1) == 0.0F ? 0 : (var2 < 0.0F ? -1 : 1);
   }
}
