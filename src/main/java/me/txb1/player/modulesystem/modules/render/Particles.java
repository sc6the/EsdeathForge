package me.txb1.player.modulesystem.modules.render;

import me.txb1.EsdeathClient;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

// Particles: the merge of the old MoreParticles + Sharpness modules, modelled on ParticleMultiplier.
//   - multiplier      scales the CRIT_MAGIC particle count (MoreParticles' "more sparkles")
//   - alwaysSharpness emits the enchant sparkle on every melee hit (Sharpness), de-duped against
//                     the server's own sharpness particle so it isn't doubled
//   - cleanView       hides your own crit particles
// The three native mixins (MixinEntityParticleEmitter / MixinNetHandlerPlayClient /
// MixinPlayerControllerMP) read the effective* getters, which return vanilla behaviour while the
// module is OFF. Settings (edit pencil) expose a slider + two toggles.
public class Particles extends Module {
   // live config read by the mixins
   public static boolean active;
   public static double multiplier = 2.0;
   public static boolean alwaysSharpness = true;
   public static boolean cleanView = false;

   // self-emitted-sharpness de-dupe window (so alwaysSharpness + server packet don't stack)
   private static final long SELF_SHARP_WINDOW_MS = 750L;
   private static volatile int selfSharpEntityId = Integer.MIN_VALUE;
   private static volatile long selfSharpTime = Long.MIN_VALUE;

   // effective getters: neutral/vanilla when the module is disabled
   public static double effectiveMultiplier() {
      return active ? multiplier : 1.0;
   }

   public static boolean effectiveAlwaysSharpness() {
      return active && alwaysSharpness;
   }

   public static boolean effectiveCleanView() {
      return active && cleanView;
   }

   public static void markSelfSharpness(int entityId) {
      selfSharpEntityId = entityId;
      selfSharpTime = System.currentTimeMillis();
   }

   public static boolean isDuplicateServerSharpness(int entityId) {
      return entityId == selfSharpEntityId && System.currentTimeMillis() - selfSharpTime <= SELF_SHARP_WINDOW_MS;
   }

   // ---- module ----
   private boolean loaded;
   private boolean dragging;
   private static final double MULT_MIN = 0.0;
   private static final double MULT_MAX = 10.0;
   private static final int SL_DX = 10;
   private static final int SL_W = 150;
   private static final int SL_H = 10;
   private static final int SL_KNOB = 6;
   private static final int SL_DY = 30;

   public Particles() {
      super("Particles", "Particles", Category.RENDER, true);
   }

   @Override
   public void onEnable() {
      load();
      active = true;
   }

   @Override
   public void onDisable() {
      active = false;
   }

   @Override
   public void onSettingsDrawScreen(int mouseX, int mouseY, int x, int y, int right, int bottom, int height, int width) {
      int sx = x + SL_DX;
      int sy = y + SL_DY;
      boolean down = Mouse.isButtonDown(0);

      Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("§7Particle multiplier", (float) sx, (float) (y + 18), -1);

      // multiplier slider
      boolean inTrack = mouseX >= sx && mouseX <= sx + SL_W && mouseY >= sy - 2 && mouseY <= sy + SL_H + 2;
      if (down && (this.dragging || inTrack)) {
         this.dragging = true;
         float frac = (float) (mouseX - sx) / (float) (SL_W - SL_KNOB);
         if (frac < 0.0F) {
            frac = 0.0F;
         } else if (frac > 1.0F) {
            frac = 1.0F;
         }
         multiplier = MULT_MIN + frac * (MULT_MAX - MULT_MIN);
      }
      if (!down && this.dragging) {
         this.dragging = false;
         save();
      }
      float frac = (float) ((multiplier - MULT_MIN) / (MULT_MAX - MULT_MIN));
      Gui.drawRect(sx, sy, sx + SL_W, sy + SL_H, 0x80000000);
      Gui.drawRect(sx, sy, sx + SL_W, sy + 1, 0xFF555555);
      int knobX = sx + (int) (frac * (SL_W - SL_KNOB));
      Gui.drawRect(knobX, sy, knobX + SL_KNOB, sy + SL_H, 0xFFAAAAAA);
      Minecraft.getMinecraft().fontRendererObj
         .drawStringWithShadow(String.format("§7x §f%.2f", multiplier), (float) (sx + SL_W + 8), (float) (sy + 1), -1);

      // two toggles below the slider
      drawToggle(sx, sy + 22, mouseX, mouseY, "Always Sharpness", alwaysSharpness);
      drawToggle(sx, sy + 38, mouseX, mouseY, "Clean View (hide own)", cleanView);
      // entry into the bundled colour/scale/opacity editor
      Minecraft.getMinecraft().fontRendererObj
         .drawStringWithShadow("§b» Customize colours / scale / opacity", (float) sx, (float) (sy + 56), -1);
   }

   private void drawToggle(int tx, int ty, int mouseX, int mouseY, String label, boolean on) {
      Minecraft.getMinecraft().fontRendererObj
         .drawStringWithShadow((on ? "§a[ON] " : "§c[OFF] ") + "§7" + label, (float) tx, (float) ty, -1);
   }

   // toggles are click-driven (SettingsGui forwards mouseClicked); the slider is drag-polled above
   @Override
   public void mouseClicked(int mouseX, int mouseY, int button, int x, int y, int height, int width) {
      if (button != 0) {
         return;
      }
      int sx = x + SL_DX;
      int sy = y + SL_DY;
      int rowH = 9;
      int aY = sy + 22, cY = sy + 38, eY = sy + 56;
      int w = Minecraft.getMinecraft().fontRendererObj.getStringWidth("[OFF] Always Sharpness");
      if (mouseX >= sx && mouseX <= sx + w + 30 && mouseY >= aY && mouseY <= aY + rowH) {
         alwaysSharpness = !alwaysSharpness;
         save();
      } else if (mouseX >= sx && mouseX <= sx + w + 60 && mouseY >= cY && mouseY <= cY + rowH) {
         cleanView = !cleanView;
         save();
      } else if (mouseY >= eY && mouseY <= eY + rowH
            && mouseX >= sx && mouseX <= sx + Minecraft.getMinecraft().fontRendererObj.getStringWidth("» Customize colours / scale / opacity")) {
         me.txb1.forge.ParticleCustomizerHolder.openEditor();
      }
   }

   private void load() {
      if (this.loaded) {
         return;
      }
      try {
         Object o = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("particles_cfg");
         if (o != null) {
            String[] p = String.valueOf(o).split(",");
            if (p.length == 3) {
               multiplier = Double.parseDouble(p[0]);
               alwaysSharpness = Boolean.parseBoolean(p[1]);
               cleanView = Boolean.parseBoolean(p[2]);
            }
         }
      } catch (Exception ignored) {
      }
      this.loaded = true;
   }

   private void save() {
      try {
         EsdeathClient.getInstance().getFireDB().getDataBase()
            .saveObject("particles_cfg", multiplier + "," + alwaysSharpness + "," + cleanView);
         EsdeathClient.getInstance().getFireDB().getDataBase().push();
      } catch (Exception ignored) {
      }
   }
}
