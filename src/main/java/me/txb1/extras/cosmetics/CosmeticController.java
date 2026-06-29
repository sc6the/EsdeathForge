package me.txb1.extras.cosmetics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import me.txb1.EsdeathClient;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;

public class CosmeticController {
   private static ArrayList<String> cosmetics = new ArrayList<>();
   // names (lowercase) that come from the bundled OldAnimationsMod (OAM) framework, used to
   // split the cosmetic GUI into Esdeath vs OAM columns.
   private static final LinkedHashSet<String> oam = new LinkedHashSet<>();
   // OFFLINE: local cosmetic state (website backend is dead). Equipped set + per-cosmetic custom color.
   private static final LinkedHashSet<String> active = new LinkedHashSet<>();      // lowercase names
   private static final HashMap<String, int[]> colors = new HashMap<>();           // lowercase -> {r,g,b}
   private static final HashMap<String, float[]> offsets = new HashMap<>();        // lowercase -> {x,y,z}
   private static final HashMap<String, Float> scales = new HashMap<>();           // lowercase -> scale factor
   private static final HashMap<String, Float> alphas = new HashMap<>();           // lowercase -> alpha 0..1
   private static final java.util.HashSet<String> multicolor = new java.util.HashSet<>(); // lowercase names cycling rainbow
   private static final HashMap<String, String> options = new HashMap<>();                 // "name|key" -> chosen value (OAM variant)
   public static final float SCALE_MIN = 0.25F;
   public static final float SCALE_MAX = 3.0F;
   public static final float ALPHA_MIN = 0.1F;
   private static boolean loaded = false;

   public static boolean shouldRenderTopHat(AbstractClientPlayer var0) {
      return true;
   }

   public static void addCos(String var0) {
      if (((cosmetics.size()) > 0) && !(cosmetics.contains(var0)) || ((cosmetics.size()) == 0)) {
         cosmetics.add(var0);
      }
   }

   public static ArrayList<String> getCosmetics() {
      return cosmetics;
   }

   // mark a registered cosmetic as OAM-sourced (called from OamCosmeticLayer)
   public static void markOam(String var0) {
      oam.add(var0.toLowerCase());
   }

   public static boolean isOam(String var0) {
      return oam.contains(var0.toLowerCase());
   }

   // names sourced from LabyMod (currently capes); shown under the "Labymod" category in the GUI.
   private static final LinkedHashSet<String> labymod = new LinkedHashSet<>();

   public static void markLabymod(String var0) {
      labymod.add(var0.toLowerCase());
   }

   public static boolean isLabymod(String var0) {
      return labymod.contains(var0.toLowerCase());
   }

   // Category bucket for the cosmetic browser: "Labymod", "OAM", or "Esdeath" (the default).
   public static String categoryOf(String name) {
      if (isLabymod(name)) {
         return "Labymod";
      }
      if (isOam(name)) {
         return "OAM";
      }
      return "Esdeath";
   }

   public static Float[] getTopHatColor(AbstractClientPlayer var0) {
      Float[] var10000 = new Float[3];
      var10000[0] = 3.0F;
      var10000[1] = 1.0F;
      var10000[2] = 2.0F;
      return var10000;
   }

   // ---- toggle (equip) ----
   public static boolean isActive(String name) {
      return active.contains(name.toLowerCase());
   }

   public static ArrayList<String> getActive() {
      return new ArrayList<>(active);
   }

   public static void setActive(String name, boolean on) {
      String n = name.toLowerCase();
      if (on) {
         active.add(n);
      } else {
         active.remove(n);
      }
      save();
   }

   public static void toggle(String name) {
      setActive(name, !isActive(name));
   }

   // Reset a cosmetic's appearance tweaks back to defaults: scale (1.0), colour (white / no override,
   // incl. the multicolor + overlay toggles), opacity (1.0) and position offset (0,0,0). Leaves the
   // equip state and any OAM style/variant options alone. One save at the end.
   public static void resetCosmetic(String name) {
      String n = name.toLowerCase();
      scales.remove(n);
      colors.remove(n);
      multicolor.remove(n);
      overlay.remove(n);
      alphas.remove(n);
      offsets.remove(n);
      save();
   }

   // ---- recolor ----
   public static boolean hasColor(String name) {
      return colors.containsKey(name.toLowerCase());
   }

   public static int[] getColor(String name) {
      return colors.get(name.toLowerCase());
   }

   public static void setColor(String name, int r, int g, int b) {
      colors.put(name.toLowerCase(), new int[]{r & 255, g & 255, b & 255});
      save();
   }

   public static void clearColor(String name) {
      colors.remove(name.toLowerCase());
      save();
   }

   // "RRGGBB" of the current color (default white)
   public static String getColorHex(String name) {
      int[] c = colors.get(name.toLowerCase());
      if (c == null) {
         return "FFFFFF";
      }
      return String.format("%02X%02X%02X", c[0], c[1], c[2]);
   }

   public static void setColorHex(String name, String hex) {
      String h = hex.replace("#", "").trim();
      if (h.length() != 6) {
         return;
      }
      try {
         int rgb = Integer.parseInt(h, 16);
         setColor(name, (rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255);
      } catch (NumberFormatException ignored) {
      }
   }

   // ---- position (X/Y/Z offset in player-model space) ----
   public static float getOffsetX(String name) {
      float[] o = offsets.get(name.toLowerCase());
      return o == null ? 0.0F : o[0];
   }

   public static float getOffsetY(String name) {
      float[] o = offsets.get(name.toLowerCase());
      return o == null ? 0.0F : o[1];
   }

   public static float getOffsetZ(String name) {
      float[] o = offsets.get(name.toLowerCase());
      return o == null ? 0.0F : o[2];
   }

   public static void setOffset(String name, float x, float y, float z) {
      offsets.put(name.toLowerCase(), new float[]{x, y, z});
      save();
   }

   // ---- scale (uniform, per cosmetic) ----
   public static float getScale(String name) {
      Float s = scales.get(name.toLowerCase());
      return s == null ? 1.0F : s;
   }

   public static void setScale(String name, float s) {
      if (s < SCALE_MIN) {
         s = SCALE_MIN;
      } else if (s > SCALE_MAX) {
         s = SCALE_MAX;
      }
      scales.put(name.toLowerCase(), s);
      save();
   }

   // ---- colour blend mode (per cosmetic): false = Multiply (tint), true = Overlay (additive glow) ----
   private static final java.util.HashSet<String> overlay = new java.util.HashSet<>();

   public static boolean isOverlay(String name) {
      return overlay.contains(name.toLowerCase());
   }

   public static void setOverlay(String name, boolean on) {
      if (on) {
         overlay.add(name.toLowerCase());
      } else {
         overlay.remove(name.toLowerCase());
      }
      save();
   }

   public static String blendName(String name) {
      return isOverlay(name) ? "Overlay" : "Multiply";
   }

   // ---- multicolor (OAM-style rainbow cycle) ----
   public static boolean isMulticolorCosmetic(String name) {
      return multicolor.contains(name.toLowerCase());
   }

   public static void setMulticolor(String name, boolean on) {
      if (on) {
         multicolor.add(name.toLowerCase());
      } else {
         multicolor.remove(name.toLowerCase());
      }
      save();
   }

   // ---- OAM variant options (per cosmetic, e.g. Konoha "renegade", Nine Tails count) ----
   public static String getOption(String name, String key, String def) {
      String v = options.get(name.toLowerCase() + "|" + key.toLowerCase());
      return v == null ? def : v;
   }

   public static void setOption(String name, String key, String value) {
      options.put(name.toLowerCase() + "|" + key.toLowerCase(), value);
      save();
   }

   // ---- transparency (per cosmetic, 0.1 .. 1.0) ----
   public static float getAlpha(String name) {
      Float a = alphas.get(name.toLowerCase());
      return a == null ? 1.0F : a;
   }

   public static void setAlpha(String name, float a) {
      if (a < ALPHA_MIN) {
         a = ALPHA_MIN;
      } else if (a > 1.0F) {
         a = 1.0F;
      }
      alphas.put(name.toLowerCase(), a);
      save();
   }

   public static void translate(String name) {
      float[] o = offsets.get(name.toLowerCase());
      if (o != null) {
         // offsets are in pixels: 1 unit = 1 model pixel = 0.0625 blocks.
         // layer space has +Y pointing down, so negate Y -> +1 = up, -1 = down.
         // +Z pushes the cosmetic away from the camera-facing front of the model.
         GlStateManager.translate(o[0] * 0.0625F, -o[1] * 0.0625F, o[2] * 0.0625F);
      }
      float s = getScale(name);
      if (s != 1.0F) {
         GlStateManager.scale(s, s, s);
      }
   }

   // translate + color in one call (used at each cosmetic's render insertion point)
   public static void apply(String name) {
      apply(name, 1.0F, 1.0F, 1.0F);
   }

   // Variant for cosmetics that have a non-white default tint (e.g. Susanoo magenta).
   // The default (dr,dg,db) is used unless the player set a custom color.
   public static void apply(String name, float dr, float dg, float db) {
      translate(name);
      color(name, dr, dg, db);
   }

   public static void color(String name) {
      color(name, 1.0F, 1.0F, 1.0F);
   }

   // Apply the custom color tint (or the supplied default) plus the per-cosmetic transparency.
   public static void color(String name, float dr, float dg, float db) {
      String n = name.toLowerCase();
      int[] c = colors.get(n);
      float a = getAlpha(n);
      float r = dr;
      float g = dg;
      float b = db;
      if (c != null) {
         r = c[0] / 255.0F;
         g = c[1] / 255.0F;
         b = c[2] / 255.0F;
      }
      if (isOverlay(n)) {
         // Overlay: additive blend so the tint reads as a glow over the texture instead of darkening it.
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(770, 1); // SRC_ALPHA, ONE
      } else if (a < 1.0F) {
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(770, 771); // SRC_ALPHA, ONE_MINUS_SRC_ALPHA
      }
      GlStateManager.color(r, g, b, a);
   }

   // Effect overlays (creeper/wither) use additive blend (GL_ONE,GL_ONE), so transparency is
   // achieved by scaling brightness with alpha. brightness keeps their built-in dimming (~0.5).
   public static void applyEffect(String name, float brightness) {
      String n = name.toLowerCase();
      int[] c = colors.get(n);
      float a = getAlpha(n);
      float r = brightness;
      float g = brightness;
      float b = brightness;
      if (c != null) {
         r = c[0] / 255.0F * brightness;
         g = c[1] / 255.0F * brightness;
         b = c[2] / 255.0F * brightness;
      }
      GlStateManager.color(r * a, g * a, b * a, 1.0F);
   }

   // Restore default GL color/blend after a cosmetic renders so state doesn't leak onto the
   // next layer/entity (called centrally from CosmeticBase.doRenderLayer).
   public static void resetRenderState() {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.disableBlend();
   }

   // ---- persistence (FireDB local storage) ----
   // The Labymod cape is no longer a category entry; it is controlled by the cape-priority button in
   // the capes menu (CapePriority) and rendered via getLocationCape when priority == Labymod.
   public static void registerLabymodEntries() {
   }

   @SuppressWarnings("unchecked")
   public static void load() {
      registerLabymodEntries();
      try {
         Object a = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("cosmetic_active");
         active.clear();
         if (a instanceof ArrayList) {
            for (Object o : (ArrayList<Object>) a) {
               active.add(String.valueOf(o).toLowerCase());
            }
         }
         Object c = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("cosmetic_colors");
         colors.clear();
         if (c instanceof ArrayList) {
            for (Object o : (ArrayList<Object>) c) {
               String s = String.valueOf(o);            // "name=RRGGBB"
               int eq = s.indexOf('=');
               if (eq <= 0 || eq + 7 > s.length()) {
                  continue;
               }
               String nm = s.substring(0, eq).toLowerCase();
               int rgb = Integer.parseInt(s.substring(eq + 1, eq + 7), 16);
               colors.put(nm, new int[]{(rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255});
            }
         }
         Object off = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("cosmetic_offsets");
         offsets.clear();
         if (off instanceof ArrayList) {
            for (Object o : (ArrayList<Object>) off) {
               String s = String.valueOf(o);                // "name=x,y" (legacy) or "name=x,y,z"
               int eq = s.indexOf('=');
               if (eq <= 0) {
                  continue;
               }
               String nm = s.substring(0, eq).toLowerCase();
               String[] xyz = s.substring(eq + 1).split(",");
               if (xyz.length >= 2) {
                  try {
                     float z = xyz.length >= 3 ? Float.parseFloat(xyz[2]) : 0.0F;
                     offsets.put(nm, new float[]{Float.parseFloat(xyz[0]), Float.parseFloat(xyz[1]), z});
                  } catch (NumberFormatException ignored) {
                  }
               }
            }
         }
         Object sc = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("cosmetic_scales");
         scales.clear();
         if (sc instanceof ArrayList) {
            for (Object o : (ArrayList<Object>) sc) {
               String s = String.valueOf(o);                // "name=scale"
               int eq = s.indexOf('=');
               if (eq <= 0) {
                  continue;
               }
               String nm = s.substring(0, eq).toLowerCase();
               try {
                  scales.put(nm, Float.parseFloat(s.substring(eq + 1)));
               } catch (NumberFormatException ignored) {
               }
            }
         }
         Object op = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("cosmetic_options");
         options.clear();
         if (op instanceof ArrayList) {
            for (Object o : (ArrayList<Object>) op) {
               String s = String.valueOf(o);             // "name|key=value"
               int eq = s.indexOf('=');
               if (eq <= 0) {
                  continue;
               }
               options.put(s.substring(0, eq).toLowerCase(), s.substring(eq + 1));
            }
         }
         Object mc = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("cosmetic_multicolor");
         multicolor.clear();
         if (mc instanceof ArrayList) {
            for (Object o : (ArrayList<Object>) mc) {
               multicolor.add(String.valueOf(o).toLowerCase());
            }
         }
         Object ov = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("cosmetic_overlay");
         overlay.clear();
         if (ov instanceof ArrayList) {
            for (Object o : (ArrayList<Object>) ov) {
               overlay.add(String.valueOf(o).toLowerCase());
            }
         }
         Object al = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("cosmetic_alphas");
         alphas.clear();
         if (al instanceof ArrayList) {
            for (Object o : (ArrayList<Object>) al) {
               String s = String.valueOf(o);                // "name=alpha"
               int eq = s.indexOf('=');
               if (eq <= 0) {
                  continue;
               }
               String nm = s.substring(0, eq).toLowerCase();
               try {
                  alphas.put(nm, Float.parseFloat(s.substring(eq + 1)));
               } catch (NumberFormatException ignored) {
               }
            }
         }
         loaded = true;
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static void save() {
      if (!loaded) {
         return; // don't overwrite saved state before it's been loaded
      }
      try {
         ArrayList<String> a = new ArrayList<>(active);
         ArrayList<String> c = new ArrayList<>();
         for (java.util.Map.Entry<String, int[]> e : colors.entrySet()) {
            int[] v = e.getValue();
            c.add(e.getKey() + "=" + String.format("%02X%02X%02X", v[0], v[1], v[2]));
         }
         ArrayList<String> off = new ArrayList<>();
         for (java.util.Map.Entry<String, float[]> e : offsets.entrySet()) {
            float[] v = e.getValue();
            off.add(e.getKey() + "=" + v[0] + "," + v[1] + "," + v[2]);
         }
         ArrayList<String> sc = new ArrayList<>();
         for (java.util.Map.Entry<String, Float> e : scales.entrySet()) {
            sc.add(e.getKey() + "=" + e.getValue());
         }
         ArrayList<String> al = new ArrayList<>();
         for (java.util.Map.Entry<String, Float> e : alphas.entrySet()) {
            al.add(e.getKey() + "=" + e.getValue());
         }
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("cosmetic_active", a);
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("cosmetic_colors", c);
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("cosmetic_offsets", off);
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("cosmetic_scales", sc);
         ArrayList<String> op = new ArrayList<>();
         for (java.util.Map.Entry<String, String> e : options.entrySet()) {
            op.add(e.getKey() + "=" + e.getValue());
         }
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("cosmetic_options", op);
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("cosmetic_multicolor", new ArrayList<>(multicolor));
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("cosmetic_overlay", new ArrayList<>(overlay));
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("cosmetic_alphas", al);
         EsdeathClient.getInstance().getFireDB().getDataBase().push();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
