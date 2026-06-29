package me.txb1.extras.settings.anzeige;

import java.io.Serializable;

public class Cordinates implements Serializable {
   private int y;
   private int x;

   public void decreaseX() {
      this.x = this.x - 1;
   }

   public Cordinates() {
      this.x = 0;
      this.y = 0;
   }

   public int getX() {
      return this.x;
   }

   public void setY(int var1) {
      this.y = var1;
   }

   public void increaseX() {
      this.x = this.x + 1;
   }

   public void setX(int var1) {
      this.x = var1;
   }

   public Cordinates(int var1, int var2) {
      this.x = 0;
      this.y = 0;
      this.x = var1;
      this.y = var2;
   }

   public int getY() {
      return this.y;
   }

   public void increaseY() {
      this.y = this.y + 1;
   }

   public void decreaseY() {
      this.y = this.y - 1;
   }

}
