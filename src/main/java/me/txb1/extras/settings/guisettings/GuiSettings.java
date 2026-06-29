package me.txb1.extras.settings.guisettings;

import java.awt.Color;
import me.txb1.EsdeathClient;

public class GuiSettings {
   public static Color umrandung;
   public static String button;

   public static void save() {
      EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("gui_umrandung", umrandung);
      EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("gui_farbe", button);
   }

   public static void load() {
      Object var0 = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("gui_umrandung");
      Object var1 = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("gui_farbe");
      if (((var0) != null)) {
         umrandung = (Color)var0;
         
      } else {
         umrandung = new Color(101, 36, 59, 191);
      }

      if (((var1) != null)) {
         button = (String)var1;
         
      } else {
         button = "Schwarz";
      }
   }

}
