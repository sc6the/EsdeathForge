package me.txb1.player.modulesystem.modules.visuals;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.text.DecimalFormat;
import me.txb1.EsdeathClient;
import me.txb1.extras.settings.SettingsUtil;
import me.txb1.extras.settings.anzeige.AnzeigeSettings;
import me.txb1.extras.settings.anzeige.Cordinates;
import me.txb1.player.events.EventRender;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class ReachDisplay extends Module {
   private static final DecimalFormat FMT = new DecimalFormat("0.00");
   // how long (ms) a measured reach stays on screen after the last registered hit
   private static final long HOLD_MS = 3000L;

   private static volatile double lastReach = 0.0;
   private static volatile long lastHitMs = 0L;

   // Called from MixinPlayerControllerMP#attackEntity — i.e. only on an actual registered melee hit.
   public static void recordHit(double reach) {
      if (reach < 0.0) {
         reach = 0.0;
      }
      lastReach = reach;
      lastHitMs = System.currentTimeMillis();
   }

   @EventTarget
   public void OnRender(EventRender var1) {
      Cordinates var2 = AnzeigeSettings.getCords(this.getName().toLowerCase());
      FontRenderer var3 = Minecraft.getMinecraft().fontRendererObj;
      String text;
      if (lastHitMs != 0L && System.currentTimeMillis() - lastHitMs <= HOLD_MS) {
         text = "        " + FMT.format(lastReach);
      } else {
         text = "HASN'T ATTACKED";
      }
      var3.drawStringWithShadow(text, var2.getX(), var2.getY(), EsdeathClient.getInstance().rainbow(500));
   }

   @Override
   public void onEnable() {
      EventManager.register(this);
   }

   @Override
   public void mouseClicked(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      SettingsUtil.mouseClickVisual(var1, var2, var3, var4, var5, var6, var7, this);
      super.mouseClicked(var1, var2, var3, var4, var5, var6, var7);
   }

   @Override
   public void onDisable() {
      EventManager.unregister(this);
   }

   @Override
   public void onSettingsDrawScreen(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      SettingsUtil.drawVisual(var1, var2, var3, var4, var5, var6, var7, var8);
      super.onSettingsDrawScreen(var1, var2, var3, var4, var5, var6, var7, var8);
   }

   public ReachDisplay() {
      super("ReachDisplay", "ReachDisplay", Category.VISUAL, true);
   }

}
