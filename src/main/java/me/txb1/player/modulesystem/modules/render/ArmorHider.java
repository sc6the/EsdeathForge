package me.txb1.player.modulesystem.modules.render;

import me.txb1.EsdeathClient;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;

// ArmorHider (ported from ArmorHider-1.0): hides armor layers on all players. The actual hide is
// MixinLayerArmorBase cancelling renderLayer for the configured slots; this module holds the live
// config + a settings GUI (4 piece toggles). Slot ids match LayerArmorBase: 1=boots 2=legs 3=chest
// 4=helmet.
public class ArmorHider extends Module {
   public static boolean active;
   public static boolean hideHelmet = true;
   public static boolean hideChest = true;
   public static boolean hideLegs = true;
   public static boolean hideBoots = true;

   private boolean loaded;

   public ArmorHider() {
      super("ArmorHider", "ArmorHider", Category.RENDER, true);
   }

   public static boolean isHidden(int armorSlot) {
      if (!active) {
         return false;
      }
      switch (armorSlot) {
         case 1:
            return hideBoots;
         case 2:
            return hideLegs;
         case 3:
            return hideChest;
         case 4:
            return hideHelmet;
         default:
            return false;
      }
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
      int sx = x + 10;
      int sy = y + 22;
      Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("§7Hide armor pieces", (float) sx, (float) (y + 8), -1);
      row(sx, sy, "Helmet", hideHelmet);
      row(sx, sy + 14, "Chestplate", hideChest);
      row(sx, sy + 28, "Leggings", hideLegs);
      row(sx, sy + 42, "Boots", hideBoots);
   }

   private void row(int x, int y, String label, boolean on) {
      Minecraft.getMinecraft().fontRendererObj
         .drawStringWithShadow((on ? "§a[ON] " : "§c[OFF] ") + "§7" + label, (float) x, (float) y, -1);
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int button, int x, int y, int height, int width) {
      if (button != 0) {
         return;
      }
      int sx = x + 10;
      int sy = y + 22;
      int w = Minecraft.getMinecraft().fontRendererObj.getStringWidth("[OFF] Chestplate") + 10;
      if (mouseX < sx || mouseX > sx + w) {
         return;
      }
      if (within(mouseY, sy)) {
         hideHelmet = !hideHelmet;
      } else if (within(mouseY, sy + 14)) {
         hideChest = !hideChest;
      } else if (within(mouseY, sy + 28)) {
         hideLegs = !hideLegs;
      } else if (within(mouseY, sy + 42)) {
         hideBoots = !hideBoots;
      } else {
         return;
      }
      save();
   }

   private boolean within(int my, int rowY) {
      return my >= rowY && my <= rowY + 9;
   }

   private void load() {
      if (this.loaded) {
         return;
      }
      try {
         Object o = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("armorhider_cfg");
         if (o != null) {
            String[] p = String.valueOf(o).split(",");
            if (p.length == 4) {
               hideHelmet = Boolean.parseBoolean(p[0]);
               hideChest = Boolean.parseBoolean(p[1]);
               hideLegs = Boolean.parseBoolean(p[2]);
               hideBoots = Boolean.parseBoolean(p[3]);
            }
         }
      } catch (Exception ignored) {
      }
      this.loaded = true;
   }

   private void save() {
      try {
         EsdeathClient.getInstance().getFireDB().getDataBase()
            .saveObject("armorhider_cfg", hideHelmet + "," + hideChest + "," + hideLegs + "," + hideBoots);
         EsdeathClient.getInstance().getFireDB().getDataBase().push();
      } catch (Exception ignored) {
      }
   }
}
