package me.txb1.forge.gui;

import java.io.File;
import java.nio.file.Files;
import net.minecraft.client.Minecraft;

// Global menu button style, chosen in the Theme menu and read by EsdeathIngameMenu.drawThemedButton.
//   FANCY (default)  = the button.jpg texture (black w/ wing ornaments)
//   SEMI_TRANSPARENT = a plain black slab at 40% opacity
// Persisted to .minecraft/EsdeathClient/button_style.txt so it survives restarts.
public final class ButtonStyle {
   // styles: FANCY (button.jpg texture), SEMI_TRANSPARENT (black slab), VANILLA (widgets.png buttons)
   public static final int FANCY = 0, SEMI = 1, VANILLA = 2;

   public static boolean semiTransparent; // == (style == SEMI)
   public static boolean vanilla;         // == (style == VANILLA)
   // alpha (0..255) of the SEMI_TRANSPARENT slab — adjustable via the Theme menu slider. 102 ≈ 40%.
   public static int opacity = 102;

   private static final File FILE = new File(Minecraft.getMinecraft().mcDataDir, "EsdeathClient/button_style.txt");

   private ButtonStyle() {
   }

   static {
      // file format: "<style>" or "<style>:<opacity>" (style = fancy | semi | vanilla). Old files load fine.
      try {
         if (FILE.isFile()) {
            String s = new String(Files.readAllBytes(FILE.toPath()), "UTF-8").trim();
            String[] parts = s.split(":");
            semiTransparent = "semi".equals(parts[0]);
            vanilla = "vanilla".equals(parts[0]);
            if (parts.length > 1) {
               opacity = clampOpacity(Integer.parseInt(parts[1].trim()));
            }
         }
      } catch (Throwable ignored) {
      }
   }

   public static int clampOpacity(int v) {
      return Math.max(0, Math.min(255, v));
   }

   public static void setOpacity(int v) {
      opacity = clampOpacity(v);
      save();
   }

   public static int styleIndex() {
      return vanilla ? VANILLA : (semiTransparent ? SEMI : FANCY);
   }

   public static void setStyle(int style) {
      semiTransparent = style == SEMI;
      vanilla = style == VANILLA;
      save();
   }

   // kept for compatibility: true = semi-transparent, false = fancy
   public static void set(boolean semi) {
      setStyle(semi ? SEMI : FANCY);
   }

   private static void save() {
      String token = vanilla ? "vanilla" : (semiTransparent ? "semi" : "fancy");
      try {
         FILE.getParentFile().mkdirs();
         Files.write(FILE.toPath(), (token + ":" + opacity).getBytes("UTF-8"));
      } catch (Throwable ignored) {
      }
   }
}
