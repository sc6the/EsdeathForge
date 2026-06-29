package me.txb1.extras.settings.anzeige;

import java.io.IOException;
import java.util.Iterator;
import me.txb1.EsdeathClient;
import me.txb1.player.modulesystem.Category;
import me.txb1.player.modulesystem.Module;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class CordinateGui extends GuiScreen {

   @Override
   protected void keyTyped(char var1, int var2) throws IOException {
      super.keyTyped(var1, var2);
   }

   @Override
   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      super.mouseClicked(var1, var2, var3);
   }

   @Override
   protected void mouseReleased(int var1, int var2, int var3) {
      super.mouseReleased(var1, var2, var3);
   }

   @Override
   public void mouseClickMove(int var1, int var2, int var3, long var4) {
   }

   @Override
   protected void actionPerformed(GuiButton var1) throws IOException {
      Module var2 = EsdeathClient.getInstance().getModuleManager().getModuleByName(var1.displayString.replaceAll("§e", ""));
      if (!(var2.isEnabled())) {
         var2.toggle();
      }

      this.mc.displayGuiScreen(new AnzeigeGui(var2, this));
      super.actionPerformed(var1);
   }

   @Override
   public void drawScreen(int var1, int var2, float var3) {
      this.drawDefaultBackground();
      super.drawScreen(var1, var2, var3);
   }

   @Override
   public void updateScreen() {
      super.updateScreen();
   }

   @Override
   public void initGui() {
      int var1 = 0;
      int var2 = 0;
      int var3 = 0;
      Iterator var4 = EsdeathClient.getInstance().getModuleManager().getModules().iterator();

      while ((var4.hasNext())) {
         Module var5 = (Module)var4.next();
         if ((var5.getCategory().equals(Category.VISUAL))) {
            if (((var2) > (4))) {
               var2 = 0;
               var3++;
            }

            this.buttonList
               .add(
                  new GuiButton(
                     var1,
                     83 * var2 + 10,
                     60 + var3 * 30,
                     80,
                     20,
                     String.valueOf(new StringBuilder().append("§e").append(var5.getName()))
                  )
               );
            var1++;
            var2++;
         }

      }

      super.initGui();
   }

}
