package me.txb1.player.modulesystem.modules.visuals;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import me.txb1.EsdeathClient;
import me.txb1.extras.settings.SettingsUtil;
import me.txb1.extras.settings.anzeige.AnzeigeSettings;
import me.txb1.extras.settings.anzeige.Cordinates;
import me.txb1.player.events.EventRender;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import me.txb1.player.modulesystem.modules.player.PingTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class Ping extends Module {

   @Override
   public void mouseClicked(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      SettingsUtil.mouseClickVisual(var1, var2, var3, var4, var5, var6, var7, this);
      super.mouseClicked(var1, var2, var3, var4, var5, var6, var7);
   }

   @EventTarget
   public void OnRender(EventRender var1) {
      Cordinates var2 = AnzeigeSettings.getCords(this.getName().toLowerCase());
      FontRenderer var3 = Minecraft.getMinecraft().fontRendererObj;
      var3.drawStringWithShadow(
         String.valueOf(
            new StringBuilder()
               .append("Ping: ")
               .append(PingTag.ping.get(mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID())))
               .append("ms")
         ),
         var2.getX(),
         var2.getY(),
         EsdeathClient.getInstance().rainbow(500)
      );
   }

   public Ping() {
      super("Ping", "Ping", Category.VISUAL, true);
   }

   @Override
   public void onEnable() {
      EventManager.register(this);
   }

   @Override
   public void onSettingsDrawScreen(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      SettingsUtil.drawVisual(var1, var2, var3, var4, var5, var6, var7, var8);
      super.onSettingsDrawScreen(var1, var2, var3, var4, var5, var6, var7, var8);
   }

   @Override
   public void onDisable() {
      EventManager.unregister(this);
   }

}
