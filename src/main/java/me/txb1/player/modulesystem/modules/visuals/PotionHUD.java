package me.txb1.player.modulesystem.modules.visuals;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.util.Collection;
import me.txb1.extras.settings.SettingsUtil;
import me.txb1.extras.settings.anzeige.AnzeigeSettings;
import me.txb1.extras.settings.anzeige.Cordinates;
import me.txb1.player.events.EventRender;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import me.txb1.EsdeathClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

// PotionHUD (reimplementation of Powns' CheatbreakerHUD status-effect display): lists active potion
// effects with their inventory icon, translated name + amplifier, and remaining duration. Rendered
// through the Esdeath HUD system so it drags/positions like the other visual modules ("potionhud").
public class PotionHUD extends Module {
   private static final ResourceLocation INVENTORY = new ResourceLocation("textures/gui/container/inventory.png");

   // drawTexturedModalRect is protected in Gui; a tiny subclass exposes it for the icon blit
   private static final class Blit extends Gui {
      void icon(int x, int y, int u, int v, int w, int h) {
         this.drawTexturedModalRect(x, y, u, v, w, h);
      }
   }

   private static final Blit BLIT = new Blit();

   public static boolean compact; // compact mode: no icons, single tight line per effect
   private static boolean configLoaded;
   private boolean wasDown;

   public PotionHUD() {
      super("PotionHUD", "PotionHUD", Category.VISUAL, true);
   }

   public static void ensureConfig() {
      if (configLoaded) {
         return;
      }
      configLoaded = true;
      try {
         Object o = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("potionhud_compact");
         if (o != null) {
            compact = Boolean.parseBoolean(String.valueOf(o));
         }
      } catch (Exception ignored) {
      }
   }

   private static void save() {
      try {
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("potionhud_compact", String.valueOf(compact));
         EsdeathClient.getInstance().getFireDB().getDataBase().push();
      } catch (Exception ignored) {
      }
   }

   @Override
   public void onEnable() {
      ensureConfig();
      EventManager.register(this);
   }

   @Override
   public void onDisable() {
      EventManager.unregister(this);
   }

   @Override
   public void mouseClicked(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      SettingsUtil.mouseClickVisual(var1, var2, var3, var4, var5, var6, var7, this);
      super.mouseClicked(var1, var2, var3, var4, var5, var6, var7);
   }

   @Override
   public void onSettingsDrawScreen(int mouseX, int mouseY, int x, int y, int var5, int var6, int var7, int var8) {
      SettingsUtil.drawVisual(mouseX, mouseY, x, y, var5, var6, var7, var8);

      // Compact-mode toggle
      int tx = x + 5;
      int ty = y + 52;
      int tw = 150;
      int th = 14;
      Gui.drawRect(tx, ty, tx + tw, ty + th, compact ? 0xFF335533 : 0xFF333333);
      Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
         compact ? "§aCompact (no icons) ✔" : "§7Compact (no icons) ✘", (float) (tx + 4), (float) (ty + 3), -1);
      boolean down = Mouse.isButtonDown(0);
      if (down && !this.wasDown && mouseX >= tx && mouseX <= tx + tw && mouseY >= ty && mouseY <= ty + th) {
         compact = !compact;
         save();
      }
      this.wasDown = down;

      super.onSettingsDrawScreen(mouseX, mouseY, x, y, var5, var6, var7, var8);
   }

   @EventTarget
   public void onRender(EventRender event) {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.thePlayer == null) {
         return;
      }
      Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
      if (effects == null || effects.isEmpty()) {
         return;
      }
      ensureConfig();
      Cordinates pos = AnzeigeSettings.getCords("potionhud");
      int x = pos.getX();
      int y = pos.getY();
      int addY = 0;

      for (PotionEffect effect : effects) {
         Potion potion = Potion.potionTypes[effect.getPotionID()];
         if (potion == null || !potion.shouldRender(effect)) {
            continue;
         }

         // name + amplifier
         String name = I18n.format(potion.getName());
         int amp = effect.getAmplifier();
         if (amp == 1) {
            name = name + " " + I18n.format("enchantment.level.2");
         } else if (amp == 2) {
            name = name + " " + I18n.format("enchantment.level.3");
         } else if (amp == 3) {
            name = name + " " + I18n.format("enchantment.level.4");
         }

         if (compact) {
            // single tight line, no icon: "Name 1:23"
            mc.fontRendererObj.drawStringWithShadow(
               name + " §7" + Potion.getDurationString(effect), x, y + addY, 0xFFFFFF);
            addY += mc.fontRendererObj.FONT_HEIGHT + 2;
         } else {
            // potion icon (from the vanilla inventory sheet)
            if (potion.hasStatusIcon()) {
               GlStateManager.pushMatrix();
               GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
               mc.getTextureManager().bindTexture(INVENTORY);
               int idx = potion.getStatusIconIndex();
               BLIT.icon(x, y + addY, idx % 8 * 18, 198 + idx / 8 * 18, 18, 18);
               GlStateManager.popMatrix();
            }
            mc.fontRendererObj.drawStringWithShadow(name, x + 20, y + 2 + addY, 0xFFFFFF);
            mc.fontRendererObj.drawStringWithShadow(Potion.getDurationString(effect), x + 20, y + 2 + addY + (mc.fontRendererObj.FONT_HEIGHT + 1), 0xFF7F7F7F);
            addY += 22;
         }
      }
   }
}
