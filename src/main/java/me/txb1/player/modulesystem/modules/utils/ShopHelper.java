package me.txb1.player.modulesystem.modules.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

// ShopHelper: in the BedWars shop, highlights items you can currently afford by drawing a slot
// background in the cost resource's colour. The cost is read from the item's lore ("2 Iron",
// "5 Gold", …). Slot rendering is done in MixinGuiContainer#esdeath$shopHelper.
public class ShopHelper extends Module {
   public static boolean active;
   public static int opacity = 110; // slot background alpha (0..255)
   public static boolean highlightAffordable = true;

   private static final Pattern COST = Pattern.compile("(\\d+)\\s+(iron|gold|diamond|emerald)");

   public ShopHelper() {
      super("ShopHelper", "ShopHelper", Category.UTILS, true);
   }

   @Override
   public net.minecraft.client.gui.GuiScreen getCustomSettingsGui() {
      return new UtilSettingsGui(this, new me.txb1.forge.gui.EsdeathIngameMenu())
         .toggle("Highlight Affordable", () -> highlightAffordable, v -> highlightAffordable = v)
         .slider("Opacity", 0, 255, () -> opacity, v -> opacity = v);
   }

   @Override
   public void onEnable() {
      active = true;
   }

   @Override
   public void onDisable() {
      active = false;
   }

   // The slot background colour (RGB, no alpha) if the item has a parseable cost the player can afford;
   // returns 0 otherwise. Colour matches the resource type.
   public static int affordableColor(ItemStack stack) {
      if (stack == null || !stack.hasTagCompound()) {
         return 0;
      }
      try {
         NBTTagCompound disp = stack.getTagCompound().getCompoundTag("display");
         NBTTagList lore = disp.getTagList("Lore", 8);
         for (int i = 0; i < lore.tagCount(); i++) {
            String line = lore.getStringTagAt(i).replaceAll("§.", "").toLowerCase();
            Matcher m = COST.matcher(line);
            if (m.find()) {
               int amt = Integer.parseInt(m.group(1));
               String res = m.group(2);
               Item item;
               int color;
               if (res.equals("iron")) {
                  item = Items.iron_ingot;
                  color = 0xDDDDDD;
               } else if (res.equals("gold")) {
                  item = Items.gold_ingot;
                  color = 0xFFD700;
               } else if (res.equals("diamond")) {
                  item = Items.diamond;
                  color = 0x33CCFF;
               } else {
                  item = Items.emerald;
                  color = 0x33DD33;
               }
               return count(item) >= amt ? color : 0;
            }
         }
      } catch (Throwable ignored) {
      }
      return 0;
   }

   private static int count(Item item) {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.thePlayer == null) {
         return 0;
      }
      int n = 0;
      for (ItemStack s : mc.thePlayer.inventory.mainInventory) {
         if (s != null && s.getItem() == item) {
            n += s.stackSize;
         }
      }
      return n;
   }
}
