package me.txb1.player.modulesystem.modules.visuals;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.awt.Color;
import me.txb1.EsdeathClient;
import me.txb1.extras.settings.SettingsUtil;
import me.txb1.extras.settings.anzeige.AnzeigeSettings;
import me.txb1.extras.settings.anzeige.Cordinates;
import me.txb1.player.events.EventRender;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

public class KeyStrokes extends Module {
   int countA;
   int countW;
   int countS;
   int countD;

   @Override
   public void onSettingsDrawScreen(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      SettingsUtil.drawVisual(var1, var2, var3, var4, var5, var6, var7, var8);
      super.onSettingsDrawScreen(var1, var2, var3, var4, var5, var6, var7, var8);
   }

   @Override
   public void onDisable() {
      EventManager.unregister(this);
   }

   public KeyStrokes() {
      super("KeyStrokes", "KeyStrokes", Category.VISUAL, true);
      this.countA = 0;
      this.countS = 0;
      this.countD = 0;
      this.countW = 0;
   }

   @EventTarget
   public void onRender(EventRender var1) {
      Cordinates var2 = AnzeigeSettings.getCords(this.getName().toLowerCase());
      int var3 = var2.getX();
      int var4 = var2.getY();
      Color var5 = new Color(33, 31, 31, 180 - this.countA * 2);
      Color var6 = new Color(33, 31, 31, 180 - this.countS * 2);
      Color var7 = new Color(33, 31, 31, 180 - this.countD * 2);
      Color var8 = new Color(33, 31, 31, 180 - this.countW * 2);
      if ((Keyboard.isKeyDown(30))) {
         if (((this.countA) < (50))) {
            this.countA = this.countA + 3;
            
         }
      } else if (((this.countA) > (1))) {
         this.countA = this.countA - 2;
      }

      if ((Keyboard.isKeyDown(17))) {
         if (((this.countW) < (50))) {
            this.countW = this.countW + 3;
            
         }
      } else if (((this.countW) > (1))) {
         this.countW = this.countW - 2;
      }

      if ((Keyboard.isKeyDown(31))) {
         if (((this.countS) < (50))) {
            this.countS = this.countS + 3;
            
         }
      } else if (((this.countS) > (1))) {
         this.countS = this.countS - 2;
      }

      if ((Keyboard.isKeyDown(32))) {
         if (((this.countD) < (50))) {
            this.countD = this.countD + 3;
            
         }
      } else if (((this.countD) > (1))) {
         this.countD = this.countD - 2;
      }

      Gui.drawRect(var3, var4, var3 + 25, var4 + 25, var5.getRGB());
      mc.fontRendererObj.drawStringWithShadow("A", var3 + 10, var4 + 10, EsdeathClient.getInstance().rainbow(100));
      Gui.drawRect(var3 + 2 + 25, var4, var3 + 25 + 2 + 25, var4 + 25, var6.getRGB());
      mc.fontRendererObj.drawStringWithShadow("S", var3 + 10 + 2 + 25, var4 + 10, EsdeathClient.getInstance().rainbow(300));
      Gui.drawRect(
         var3 + 2 + 25,
         var4 - 2 - 25,
         var3 + 25 + 2 + 25,
         var4 + 25 - 2 - 25,
         var8.getRGB()
      );
      mc.fontRendererObj
         .drawStringWithShadow(
            "W", var3 + 10 + 2 + 25, var4 + 10 - 2 - 25, EsdeathClient.getInstance().rainbow(450)
         );
      Gui.drawRect(
         var3 + 2 + 25 + 2 + 25,
         var4,
         var3 + 25 + 2 + 25 + 2 + 25,
         var4 + 25,
         var7.getRGB()
      );
      mc.fontRendererObj
         .drawStringWithShadow(
            "D", var3 + 10 + 2 + 25 + 2 + 25, var4 + 10, EsdeathClient.getInstance().rainbow(600)
         );
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

}
