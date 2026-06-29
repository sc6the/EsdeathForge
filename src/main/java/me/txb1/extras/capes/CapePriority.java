package me.txb1.extras.capes;

import me.proxycracked.capemod.cape.CapeManager;
import me.txb1.EsdeathClient;

// Which cape source wins when several are available: the gifcapes custom cape, the OptiFine cape, or
// the LabyMod cape. Drives capemod (custom cape) on/off and gates the LabyMod getLocationCape hook.
//   CUSTOM   -> capemod renders its custom cape (its default behaviour; it also overrides OptiFine)
//   OPTIFINE -> capemod custom cape disabled, so the OptiFine/vanilla cape shows
//   LABYMOD  -> capemod disabled + the LabyMod cape is returned from getLocationCape (if owned)
public final class CapePriority {

   public static final int CUSTOM = 0;
   public static final int OPTIFINE = 1;
   public static final int LABYMOD = 2;
   private static final String[] NAMES = {"Custom", "Optifine", "Labymod"};

   private static int priority = CUSTOM;
   private static boolean loaded;

   private CapePriority() {
   }

   public static int get() {
      ensureLoaded();
      return priority;
   }

   public static String name() {
      return NAMES[get()];
   }

   public static boolean isLabymod() {
      return get() == LABYMOD;
   }

   public static void cycle() {
      set((get() + 1) % NAMES.length);
   }

   public static void set(int p) {
      ensureLoaded();
      priority = ((p % NAMES.length) + NAMES.length) % NAMES.length;
      apply();
      save();
   }

   // Reflect the priority onto capemod: only the CUSTOM source keeps capemod's custom cape active.
   public static void apply() {
      try {
         CapeManager.setEnabled(priority == CUSTOM);
      } catch (Throwable ignored) {
      }
   }

   private static void ensureLoaded() {
      if (loaded) {
         return;
      }
      loaded = true;
      try {
         Object o = EsdeathClient.getInstance().getFireDB().getDataBase().getObject("cape_priority");
         if (o != null) {
            priority = Math.max(0, Math.min(NAMES.length - 1, Integer.parseInt(String.valueOf(o).trim())));
         }
      } catch (Throwable ignored) {
      }
      apply();
   }

   private static void save() {
      try {
         EsdeathClient.getInstance().getFireDB().getDataBase().saveObject("cape_priority", String.valueOf(priority));
         EsdeathClient.getInstance().getFireDB().getDataBase().push();
      } catch (Throwable ignored) {
      }
   }
}
