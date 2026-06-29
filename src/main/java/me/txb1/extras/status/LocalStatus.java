package me.txb1.extras.status;

import me.txb1.EsdeathClient;

// The local player's status text (the backend that used to host it is dead). Shown above the player's
// own nametag in 3rd person. Size is a multiplier on the billboard scale (1.0 = same size as the
// vanilla nametag). Persisted in FireDB.
public final class LocalStatus {
   public static String text = "";
   public static float size = 1.0F;
   public static float y = 0.0F; // vertical offset (world units) above the default position
   private static boolean loaded;

   private LocalStatus() {
   }

   public static void ensure() {
      if (loaded) {
         return;
      }
      loaded = true;
      try {
         Object t = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("local_status");
         if (t != null) {
            text = String.valueOf(t);
         }
         Object s = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("local_status_size");
         if (s != null) {
            size = Float.parseFloat(String.valueOf(s));
         }
         Object yo = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("local_status_y");
         if (yo != null) {
            y = Float.parseFloat(String.valueOf(yo));
         }
      } catch (Exception ignored) {
      }
   }

   public static void save() {
      try {
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("local_status", text);
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("local_status_size", String.valueOf(size));
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("local_status_y", String.valueOf(y));
         EsdeathClient.getInstance().getFireDB().getDataBase().push();
      } catch (Exception ignored) {
      }
   }
}
