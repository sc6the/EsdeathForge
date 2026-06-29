package me.txb1.utils;

import java.awt.image.BufferedImage;

public class ImageFrame {
   private final int height;
   private final int delay;
   private final String disposal;
   private final BufferedImage image;
   private final int width;

   public ImageFrame(BufferedImage var1) {
      this.image = var1;
      this.delay = -1;
      this.disposal = null;
      this.width = -1;
      this.height = -1;
   }

   public int getHeight() {
      return this.height;
   }

   public BufferedImage getImage() {
      return this.image;
   }

   public ImageFrame(BufferedImage var1, int var2, String var3, int var4, int var5) {
      this.image = var1;
      this.delay = var2;
      this.disposal = var3;
      this.width = var4;
      this.height = var5;
   }

   public String getDisposal() {
      return this.disposal;
   }

   public int getWidth() {
      return this.width;
   }

   public int getDelay() {
      return this.delay;
   }
}
