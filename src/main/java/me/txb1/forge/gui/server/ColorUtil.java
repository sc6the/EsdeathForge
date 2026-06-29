package me.txb1.forge.gui.server;

// Translate '&' colour codes to the '§' control char (so users can colour server names with & instead
// of the paragraph symbol). Only valid code characters (0-9 a-f k-o r) are converted.
public final class ColorUtil {
   private static final String CODES = "0123456789abcdefklmnorABCDEFKLMNOR";

   private ColorUtil() {
   }

   public static String translate(String s) {
      if (s == null || s.indexOf('&') < 0) {
         return s;
      }
      char[] c = s.toCharArray();
      for (int i = 0; i < c.length - 1; i++) {
         if (c[i] == '&' && CODES.indexOf(c[i + 1]) > -1) {
            c[i] = '§';
            c[i + 1] = Character.toLowerCase(c[i + 1]);
         }
      }
      return new String(c);
   }
}
