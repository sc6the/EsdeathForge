package me.txb1.player.modulesystem.modules.visuals;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import java.util.Iterator;
import me.txb1.EsdeathClient;
import me.txb1.extras.settings.SettingsUtil;
import me.txb1.extras.settings.anzeige.AnzeigeSettings;
import me.txb1.extras.settings.anzeige.Cordinates;
import me.txb1.player.events.EventClickMouse;
import me.txb1.player.events.EventRender;
import me.txb1.player.events.EventRightClickMouse;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class CPS extends Module {
   int clicks;
   private static ArrayList<Long> arrright = new ArrayList<>();
   private static ArrayList<Long> arr = new ArrayList<>();
   int clicksright;

   @EventTarget
   public void OnHit(EventRightClickMouse var1) {
      if ((EsdeathClient.getInstance().getModuleManager().getModuleByName("CPS").isEnabled())) {
         this.clicksright = this.clicksright + 1;
         arrright.add(System.currentTimeMillis());
      }
   }

   private static int om8(long var0, long var2) {
      long var4;
      return (var4 = var0 - var2) == 0L ? 0 : (var4 < 0L ? -1 : 1);
   }

   @Override
   public void onDisable() {
      EventManager.unregister(this);
   }

   public CPS() {
      super("CPS", "CPS", Category.VISUAL, true);
      this.clicks = 0;
      this.clicksright = 0;
   }

   @EventTarget
   public void OnHit(EventClickMouse var1) {
      if ((EsdeathClient.getInstance().getModuleManager().getModuleByName("CPS").isEnabled())) {
         this.clicks = this.clicks + 1;
         arr.add(System.currentTimeMillis());
      }
   }

   @Override
   public void mouseClicked(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      SettingsUtil.mouseClickVisual(var1, var2, var3, var4, var5, var6, var7, this);
      super.mouseClicked(var1, var2, var3, var4, var5, var6, var7);
   }

   @Override
   public void onSettingsDrawScreen(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      SettingsUtil.drawVisual(var1, var2, var3, var4, var5, var6, var7, var8);
      super.onSettingsDrawScreen(var1, var2, var3, var4, var5, var6, var7, var8);
   }

   @Override
   public void onEnable() {
      EventManager.register(this);
   }

   @EventTarget
   public void OnRender(EventRender var1) {
      Cordinates var2 = AnzeigeSettings.getCords(this.getName().toLowerCase());
      FontRenderer var3 = Minecraft.getMinecraft().fontRendererObj;
      var3.drawStringWithShadow(
         String.valueOf(new StringBuilder().append("CPS ").append(this.clicks).append(" / ").append(this.clicksright)),
         var2.getX(),
         var2.getY(),
         EsdeathClient.getInstance().rainbow(500)
      );
      Iterator var4 = arr.iterator();

      while ((var4.hasNext())) {
         Long var5 = (Long)var4.next();
         if (((om8(System.currentTimeMillis() - var5, 1200L)) > 0)) {
            this.clicks = this.clicks - 1;
            arr.remove(var5);
         }

         if (((153 + 9 - 111 + 133 ^ 97 + 84 - 29 + 26) & (117 ^ 120 ^ 144 ^ 151 ^ -1))
            > ((63 ^ 43 ^ 31 ^ 14) & (29 + 136 - 72 + 56 ^ 39 + 47 - -28 + 30 ^ -1))) {
            return;
         }
      }

      var4 = arrright.iterator();

      while ((var4.hasNext())) {
         Long var7 = (Long)var4.next();
         if (((om8(System.currentTimeMillis() - var7, 1200L)) > 0)) {
            this.clicksright = this.clicksright - 1;
            arrright.remove(var7);
         }

      }
   }

}
