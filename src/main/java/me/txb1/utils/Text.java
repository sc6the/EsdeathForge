package me.txb1.utils;

import java.io.Serializable;

public class Text implements Serializable {
   private int key;
   private String text;

   public int getKey() {
      return this.key;
   }

   public String getText() {
      return this.text;
   }

   public Text(int var1, String var2) {
      this.key = var1;
      this.text = var2;
   }
}
