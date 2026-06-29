package me.txb1.player.modulesystem.modules.player;

import java.lang.reflect.Field;
import java.util.List;
import me.txb1.EsdeathClient;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

// MotionBlur: applies the Esdeath "phosphor" post-processing shader (EsdeathClient/phosphor.json,
// bundled from the standalone — main->swap->previous frame feedback, Phosphor 0.65). Strength is the
// Phosphor uniform (per-channel decay) and is configurable via the module settings slider. The
// shader is (re)loaded on enable and removed on disable.
public class MotionBlur extends Module {
   private static final ResourceLocation PHOSPHOR = new ResourceLocation("EsdeathClient/phosphor.json");
   private static final float MIN = 0.0F;
   private static final float MAX = 0.95F;

   private float strength = 0.65F;
   private boolean loaded;
   private boolean dragging;

   private static final int SL_DX = 10;
   private static final int SL_W = 150;
   private static final int SL_H = 10;
   private static final int SL_KNOB = 6;
   private static final int SL_DY = 26;

   public MotionBlur() {
      super("MotionBlur", "MotionBlur", Category.RENDER, true);
   }

   @Override
   public void onEnable() {
      load();
      try {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.entityRenderer != null) {
            mc.entityRenderer.loadShader(PHOSPHOR);
            applyStrength();
         }
      } catch (Throwable ignored) {
      }
   }

   @Override
   public void onDisable() {
      try {
         Minecraft.getMinecraft().entityRenderer.stopUseShader();
      } catch (Throwable ignored) {
      }
   }

   // push `strength` into the loaded phosphor shader's Phosphor uniform (vec3). Uses reflection for
   // the private shader-group plumbing.
   private void applyStrength() {
      try {
         Minecraft mc = Minecraft.getMinecraft();
         Field sgF = EntityRenderer.class.getDeclaredField("field_147707_d"); // theShaderGroup
         sgF.setAccessible(true);
         Object shaderGroup = sgF.get(mc.entityRenderer);
         if (shaderGroup == null) {
            return;
         }
         Field lsF = shaderGroup.getClass().getDeclaredField("field_148031_d"); // listShaders
         lsF.setAccessible(true);
         List<?> shaders = (List<?>) lsF.get(shaderGroup);
         for (Object shader : shaders) {
            Object sm = shader.getClass().getMethod("func_148043_c").invoke(shader);              // getShaderManager
            Object uni = sm.getClass().getMethod("func_147984_b", String.class).invoke(sm, "Phosphor"); // getShaderUniformOrDefault
            if (uni != null) {
               setUniform(uni, strength);
            }
         }
      } catch (Throwable ignored) {
      }
   }

   private void setUniform(Object uniform, float v) throws Exception {
      try {
         uniform.getClass().getMethod("func_148087_a", float.class, float.class, float.class).invoke(uniform, v, v, v);
      } catch (NoSuchMethodException e) {
         uniform.getClass().getMethod("func_148090_a", float.class, float.class, float.class).invoke(uniform, v, v, v);
      }
   }

   @Override
   public void onSettingsDrawScreen(int mouseX, int mouseY, int x, int y, int right, int bottom, int height, int width) {
      int sx = x + SL_DX;
      int sy = y + SL_DY;
      boolean down = Mouse.isButtonDown(0);
      Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("§7Motion blur strength", (float) sx, (float) (y + 12), -1);

      boolean inTrack = mouseX >= sx && mouseX <= sx + SL_W && mouseY >= sy - 2 && mouseY <= sy + SL_H + 2;
      if (down && (this.dragging || inTrack)) {
         this.dragging = true;
         float frac = (float) (mouseX - sx) / (float) (SL_W - SL_KNOB);
         if (frac < 0.0F) {
            frac = 0.0F;
         } else if (frac > 1.0F) {
            frac = 1.0F;
         }
         this.strength = MIN + frac * (MAX - MIN);
         if (this.isEnabled()) {
            applyStrength();
         }
      }
      if (!down && this.dragging) {
         this.dragging = false;
         save();
      }
      float frac = (this.strength - MIN) / (MAX - MIN);
      Gui.drawRect(sx, sy, sx + SL_W, sy + SL_H, 0x80000000);
      Gui.drawRect(sx, sy, sx + SL_W, sy + 1, 0xFF555555);
      int knobX = sx + (int) (frac * (SL_W - SL_KNOB));
      Gui.drawRect(knobX, sy, knobX + SL_KNOB, sy + SL_H, 0xFFAAAAAA);
      Minecraft.getMinecraft().fontRendererObj
         .drawStringWithShadow(String.format("§f%.2f", this.strength), (float) (sx + SL_W + 8), (float) (sy + 1), -1);
   }

   private void load() {
      if (this.loaded) {
         return;
      }
      try {
         Object o = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("motionblur_strength");
         if (o != null) {
            this.strength = Float.parseFloat(String.valueOf(o));
         }
      } catch (Exception ignored) {
      }
      this.loaded = true;
   }

   private void save() {
      try {
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("motionblur_strength", String.valueOf(this.strength));
         EsdeathClient.getInstance().getFireDB().getDataBase().push();
      } catch (Exception ignored) {
      }
   }
}
