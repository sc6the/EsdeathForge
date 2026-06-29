package me.txb1.forge.gui;

// Side-panel "button" for EsdeathIngameMenu. Kept as a top-level class (not an inner class of
// EsdeathIngameMenu) because the co-loaded raven transformer NPEs when it lazily loads certain
// inner/synthetic classes (EsdeathIngameMenu$Panel) -> NoClassDefFoundError opening the menu.
final class EsdeathMenuPanel {
   final int x, y, w, h;
   final String label;
   final int action;
   int fade = 30;

   EsdeathMenuPanel(int x, int y, int w, int h, String label, int action) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      this.label = label;
      this.action = action;
   }

   boolean hovered(int mx, int my) {
      return mx >= x && my >= y && mx < x + w && my < y + h;
   }
}
