package me.txb1.player.modulesystem.modules.render.crosshair;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.awt.Color;
import me.txb1.player.events.EventRender;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

// Ported from CrosshairMod-1.0.0 (com.customxhair). Renders a grid-painted custom
// crosshair in place of vanilla. Vanilla suppression is an ASM gate on
// GuiIngame.showCrosshair()->false (patch-asm/gates.tsv) keyed on `active`.
// The CrosshairRenderer texture logic is folded in here.
public class CrosshairMod extends Module {
   public static boolean active = false;
   public static CrosshairMod instance;

   public final CrosshairConfig config = new CrosshairConfig();

   private static final int RENDER_PX = 15;
   private DynamicTexture texture;
   private ResourceLocation textureLocation;
   private int builtForSize = -1;
   private long builtSignature = -1L;

   public CrosshairMod() {
      super("CrosshairMod", "CrosshairMod", Category.RENDER, true);
      instance = this;
      this.config.attach(Minecraft.getMinecraft().mcDataDir);
   }

   @Override
   public void onEnable() {
      active = true;
      EventManager.register(this);
   }

   @Override
   public void onDisable() {
      active = false;
      EventManager.unregister(this);
   }

   public void invalidate() {
      this.builtForSize = -1;
      this.builtSignature = -1L;
   }

   @EventTarget
   public void onRender(EventRender event) {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.thePlayer == null || mc.currentScreen != null) {
         return;
      }
      int gridSize = this.config.gridSize;
      this.ensureTexture(gridSize);
      ScaledResolution sr = new ScaledResolution(mc);
      int sw = sr.getScaledWidth();
      int sh = sr.getScaledHeight();
      int hudFactor = Math.max(1, sr.getScaleFactor());
      int targetFactor = resolveScaleFactor(this.config.scale, hudFactor);
      double sizeScaled = 15.0 * (double)targetFactor / (double)hudFactor;
      double left = (double)sw / 2.0 - sizeScaled / 2.0;
      double top = (double)sh / 2.0 - sizeScaled / 2.0;
      mc.getTextureManager().bindTexture(this.textureLocation);
      GL11.glTexParameteri(3553, 10241, 9728);
      GL11.glTexParameteri(3553, 10240, 9728);
      GlStateManager.enableBlend();
      if (this.config.vanillaBlending) {
         GlStateManager.tryBlendFuncSeparate(775, 769, 1, 0);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      } else {
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         GlStateManager.color(
            (float)(this.config.colorARGB >> 16 & 0xFF) / 255.0F,
            (float)(this.config.colorARGB >> 8 & 0xFF) / 255.0F,
            (float)(this.config.colorARGB & 0xFF) / 255.0F,
            (float)(this.config.colorARGB >>> 24 & 0xFF) / 255.0F
         );
      }
      GlStateManager.enableAlpha();
      drawQuad(left, top, sizeScaled, sizeScaled);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
   }

   private static int resolveScaleFactor(int cfgScale, int hudFactor) {
      switch (cfgScale) {
         case 1:
            return 1;
         case 2:
            return 2;
         case 3:
            return 3;
         case 0:
         default:
            return hudFactor;
      }
   }

   public void ensureTexture(int size) {
      long sig = signature(this.config.activeGrid(), this.config.colorARGB);
      if (this.texture == null || size != this.builtForSize || sig != this.builtSignature) {
         if (this.texture != null) {
            Minecraft.getMinecraft().getTextureManager().deleteTexture(this.textureLocation);
            this.texture = null;
         }
         int[] data = new int[size * size];
         boolean[][] g = this.config.activeGrid();
         int onPixel = -1;
         for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
               data[y * size + x] = g[y][x] ? onPixel : 0;
            }
         }
         this.texture = new DynamicTexture(size, size);
         int[] dst = this.texture.getTextureData();
         System.arraycopy(data, 0, dst, 0, data.length);
         this.texture.updateDynamicTexture();
         this.textureLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("crosshairmod_" + size, this.texture);
         this.builtForSize = size;
         this.builtSignature = sig;
      }
   }

   private static long signature(boolean[][] g, int colorARGB) {
      long h = 1469598103934665603L;
      for (int y = 0; y < g.length; y++) {
         for (int x = 0; x < g[y].length; x++) {
            h = (h ^ (long)(g[y][x] ? 1 : 0)) * 1099511628211L;
         }
      }
      return (h ^ (long)colorARGB) * 1099511628211L;
   }

   private static void drawQuad(double x, double y, double w, double h) {
      Tessellator t = Tessellator.getInstance();
      WorldRenderer wr = t.getWorldRenderer();
      wr.begin(7, DefaultVertexFormats.POSITION_TEX);
      wr.pos(x, y + h, 0.0).tex(0.0, 1.0).endVertex();
      wr.pos(x + w, y + h, 0.0).tex(1.0, 1.0).endVertex();
      wr.pos(x + w, y, 0.0).tex(1.0, 0.0).endVertex();
      wr.pos(x, y, 0.0).tex(0.0, 0.0).endVertex();
      t.draw();
   }

   // --- settings panel: an "Edit" button that opens the grid editor ---
   @Override
   public void onSettingsDrawScreen(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      Gui.drawRect(var3 + 5, var4 + 30, var3 + 70, var4 + 45, Color.GREEN.getRGB());
      Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("Edit", var3 + 18, var4 + 30 + 4, Color.BLACK.getRGB());
      super.onSettingsDrawScreen(var1, var2, var3, var4, var5, var6, var7, var8);
   }

   @Override
   public void mouseClicked(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      if (var1 > var4 + 5 && var1 < var4 + 70 && var2 > var5 + 30 && var2 < var5 + 45) {
         Minecraft.getMinecraft().displayGuiScreen(new GuiCrosshairEditor(this));
         Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
      }
      super.mouseClicked(var1, var2, var3, var4, var5, var6, var7);
   }
}
