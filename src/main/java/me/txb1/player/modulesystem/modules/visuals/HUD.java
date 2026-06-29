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

public class HUD extends Module {

   // Editable HUD label, split into two parts: part1 is drawn in the rainbow theme colour, part2 in
   // black (§0) right after it — so the default reads "EsdeathClient" (Esdeath + Client). Editable via
   // the custom settings GUI; persisted in FireDB.
   public static String part1 = "Esdeath";
   public static String part2 = "Client";
   private boolean loaded;

   @Override
   public void onSettingsDrawScreen(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      SettingsUtil.drawVisual(var1, var2, var3, var4, var5, var6, var7, var8);
      super.onSettingsDrawScreen(var1, var2, var3, var4, var5, var6, var7, var8);
   }

   // Text editor (two lines) + a reposition button. Opened by the settings pencil.
   @Override
   public net.minecraft.client.gui.GuiScreen getCustomSettingsGui() {
      this.load();
      return new HudSettingsGui(this, net.minecraft.client.Minecraft.getMinecraft().currentScreen);
   }

   @EventTarget
   public void OnRender(EventRender var1) {
      this.load();
      Cordinates var2 = AnzeigeSettings.getCords(this.getName().toLowerCase());
      if (!(mc.gameSettings.showDebugInfo)) {
         int x = var2.getX();
         int y = var2.getY();
         // part1 in the hud (rainbow/theme) colour, part2 in black — drawn separately so neither
         // part's colour can leak into the other.
         mc.fontRendererObj.drawStringWithShadow(part1, x, y, EsdeathClient.getInstance().rainbow(500));
         x += mc.fontRendererObj.getStringWidth(part1);
         mc.fontRendererObj.drawStringWithShadow(part2, x, y, 0x000000);
         x += mc.fontRendererObj.getStringWidth(part2);
         int n = EsdeathClient.getInstance().getPlayers().size();
         if (n > 0) {
            mc.fontRendererObj.drawStringWithShadow(" §8(" + n + ")", x, y, 0xFFFFFF);
         }
      }
   }

   private void load() {
      if (this.loaded) {
         return;
      }
      try {
         Object a = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("hud_part1");
         if (a != null) {
            part1 = String.valueOf(a);
         }
         Object b = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("hud_part2");
         if (b != null) {
            part2 = String.valueOf(b);
         }
      } catch (Exception ignored) {
      }
      this.loaded = true;
   }

   public static void save() {
      try {
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("hud_part1", part1);
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("hud_part2", part2);
         EsdeathClient.getInstance().getFireDB().getDataBase().push();
      } catch (Exception ignored) {
      }
   }

   @Override
   public void onEnable() {
      this.load();
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

   public HUD() {
      super("HUD", "HUD", Category.VISUAL, true);
   }

}
