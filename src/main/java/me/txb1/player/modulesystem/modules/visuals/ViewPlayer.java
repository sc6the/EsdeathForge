package me.txb1.player.modulesystem.modules.visuals;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.awt.Color;
import me.txb1.extras.settings.anzeige.AnzeigeGui;
import me.txb1.extras.settings.anzeige.AnzeigeSettings;
import me.txb1.extras.settings.anzeige.Cordinates;
import me.txb1.player.events.EventRender;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import me.txb1.utils.EsdeathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

public class ViewPlayer extends Module {

   @Override
   public void onDisable() {
      EventManager.unregister(this);
   }

   @Override
   public void onSettingsDrawScreen(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      Gui.drawRect(var3 + 5, var4 + 30, var3 + 70, var4 + 45, Color.GREEN.getRGB());
      String var10001 = "Display";
      int var10002 = var3 + 18;
      Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(var10001, var10002, var4 + 30 + 4, Color.BLACK.getRGB());
      super.onSettingsDrawScreen(var1, var2, var3, var4, var5, var6, var7, var8);
   }

   @Override
   public void mouseClicked(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      if (((var1) > (var4 + 5)) && ((var1) < (var4 + 70)) && ((var2) > (var5 + 30)) && ((var2) < (var5 + 45))) {
         Minecraft.getMinecraft().displayGuiScreen(new AnzeigeGui(this, Minecraft.getMinecraft().currentScreen));
         Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
      }

      super.mouseClicked(var1, var2, var3, var4, var5, var6, var7);
   }

   @Override
   public void onEnable() {
      EventManager.register(this);
   }

   public ViewPlayer() {
      super("ViewPlayer", "ViewPlayer", Category.VISUAL, true);
   }

   @EventTarget
   public void OnRender(EventRender var1) {
      Cordinates var2 = AnzeigeSettings.getCords(this.getName().toLowerCase());
      if (!((mc.thePlayer) == null)) {
         EsdeathUtils.drawEntityOnScreen(var2.getX(), var2.getY(), 25, 0.0F, -3.0F, mc.thePlayer);
      }
   }
}
