package me.txb1.player.modulesystem;

import java.net.UnknownHostException;
import net.minecraft.client.Minecraft;

public class Module {
   private Category category;
   private boolean setting;
   public boolean visible;
   private String displayName;
   public static boolean colormode = false;
   private boolean toggled;
   public static Minecraft mc = Minecraft.getMinecraft();
   private String name;

   public boolean isEnabled() {
      return this.toggled;
   }

   public void setCategory(Category var1) {
      this.category = var1;
   }

   public void onSettingsKeyType(char var1, int var2) {
   }

   public void onDisable() {
   }

   public void mouseClicked(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
   }

   public boolean isSetting() {
      return this.setting;
   }

   public void toggle() {
      if ((this.toggled)) {
         this.toggled = false;
         this.onDisable();
         
      } else {
         this.toggled = true;

         try {
            this.onEnable();
         } catch (UnknownHostException var2) {
            var2.printStackTrace();
            return;
         }

      }
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public void onSettings() {
   }

   // Modules that wrap a bundled mod's own config screen return it here; the edit pencil opens it
   // instead of the generic SettingsGui. Default null = use SettingsGui.
   public net.minecraft.client.gui.GuiScreen getCustomSettingsGui() {
      return null;
   }

   public String getName() {
      return this.name;
   }

   public void setDisplayName(String var1) {
      this.displayName = var1;
   }

   public Category getCategory() {
      return this.category;
   }

   public void setName(String var1) {
      this.name = var1;
   }

   public Module(String var1, String var2, Category var3, boolean var4) {
      this.name = var1;
      this.displayName = var2;
      this.category = var3;
      this.visible = true;
      this.setting = var4;
   }

   public boolean isCategory(Category var1) {
      int var10000;
      if (((this.category) == (var1))) {
         var10000 = 1;
         
      } else {
         var10000 = 0;
      }

      return (var10000 != 0);
   }

   public void onSettingsDrawScreen(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
   }

   public void onEnable() throws UnknownHostException {
   }

}
