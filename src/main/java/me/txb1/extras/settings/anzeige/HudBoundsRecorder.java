package me.txb1.extras.settings.anzeige;

import java.util.ArrayList;
import java.util.List;

// While the HUD editor renders the live HUD, this records the screen rectangle of every string drawn
// so the editor can outline each element's actual extent (instead of a placeholder label). Each entry
// is {x, y, w, h}. MixinFontRenderer reports into it; only active during the editor's measure pass.
public final class HudBoundsRecorder {
   public static boolean recording;
   public static final List<int[]> rects = new ArrayList<int[]>();

   private HudBoundsRecorder() {
   }

   public static void report(int x, int y, int w, int h) {
      if (recording && w > 0) {
         rects.add(new int[] {x, y, w, h});
      }
   }
}
