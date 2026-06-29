package me.txb1.extras.settings.anzeige;

import java.util.HashMap;
import me.txb1.EsdeathClient;

public class AnzeigeSettings {
   private static HashMap<String, Cordinates> cords = new HashMap<>();

   public static HashMap<String, Cordinates> getCords() {
      return cords;
   }

   public static void save() {
      EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("cords", cords);
   }

   public static void load() {
      EsdeathClient.getInstance().getFireDB().getDataBase().pull();
      if (((EsdeathClient.getInstance().getFireDB().getDataBase().getObject("cords")) == null)) {
         cords.put("fps", new Cordinates(2, 12));
         cords.put("cps", new Cordinates(2, 24));
         cords.put("plains", new Cordinates(2, 38));
         cords.put("xyz", new Cordinates(2, 50));
         cords.put("hud", new Cordinates(2, 2));
         cords.put("reachdisplay", new Cordinates(270, 2));
         cords.put("potionhud", new Cordinates(2, 70));
         if (((68 + 111 - 52 + 84 ^ 49 + 125 - 103 + 121) & (218 ^ 184 ^ 1 ^ 112 ^ -1))
            != ((209 ^ 168 ^ 81 ^ 16) & (66 + 142 - 173 + 113 ^ 22 + 72 - 16 + 94 ^ -1))) {
            return;
         }
      } else {
         EsdeathClient.getInstance().getFireDB().getDataBase().getObjects().forEach(var0 -> {
            var0.getObject();
         });
         cords = (HashMap<String, Cordinates>)EsdeathClient.getInstance().getFireDB().getDataBase().getObject("cords");
      }
   }

   public static Cordinates getCords(String var0) {
      if (!(cords.containsKey(var0))) {
         cords.put(var0, new Cordinates());
      }

      return cords.get(var0);
   }

}
