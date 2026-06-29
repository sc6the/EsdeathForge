package me.txb1.player.modulesystem.modules.visuals;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import java.util.Iterator;
import me.txb1.EsdeathClient;
import me.txb1.extras.settings.SettingsUtil;
import me.txb1.extras.settings.anzeige.AnzeigeSettings;
import me.txb1.extras.settings.anzeige.Cordinates;
import me.txb1.player.events.EventRender;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class XYZ extends Module {

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

   @EventTarget
   public void OnRender(EventRender var1) {
      Cordinates var2 = AnzeigeSettings.getCords(this.getName().toLowerCase());
      FontRenderer var3 = Minecraft.getMinecraft().fontRendererObj;
      ArrayList var4 = new ArrayList();
      StringBuilder var10001 = new StringBuilder().append("X ");
      var4.add(String.valueOf(var10001.append(String.valueOf((short)((int)mc.thePlayer.posX)))));
      var10001 = new StringBuilder().append("Y ");
      var4.add(String.valueOf(var10001.append(String.valueOf((short)((int)mc.thePlayer.posY)))));
      var10001 = new StringBuilder().append("Z ");
      var4.add(String.valueOf(var10001.append(String.valueOf((short)((int)mc.thePlayer.posZ)))));
      int var5 = 0;
      Iterator var6 = var4.iterator();

      while ((var6.hasNext())) {
         String var7 = (String)var6.next();
         var3.drawStringWithShadow(var7, var2.getX(), var2.getY() + 10 * var5, EsdeathClient.getInstance().rainbow(500));
         var5++;
         
      }
   }

   @Override
   public void onDisable() {
      EventManager.unregister(this);
   }

   public XYZ() {
      super("XYZ", "XYZ", Category.VISUAL, true);
   }

   @Override
   public void onEnable() {
      EventManager.register(this);
   }

}
