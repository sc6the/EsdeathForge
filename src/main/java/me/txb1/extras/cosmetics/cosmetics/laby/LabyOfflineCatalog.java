package me.txb1.extras.cosmetics.cosmetics.laby;

import java.util.LinkedHashMap;
import java.util.Map;
import me.txb1.extras.cosmetics.CosmeticController;
import me.txb1.extras.cosmetics.laby.geo.LabyCosmetics;

// Turns the LabyMod cosmetic index into OFFLINE, user-selectable cosmetics in EsdeathForge's cosmetic
// menu (under the "Labymod" category). Every COSMETIC-type entry (pets excluded) is registered by its
// display name and rendered on the local player only, using the cosmetic's own `default_data` from the
// index (no laby.net ownership lookup, no showing other players' real cosmetics).
public final class LabyOfflineCatalog {

   // display-name (lowercase) -> cosmetic id. LinkedHashMap keeps index order for the menu.
   private static final Map<String, Integer> NAME_TO_ID = new LinkedHashMap<String, Integer>();
   private static volatile boolean registered;

   private LabyOfflineCatalog() {
   }

   // Resolve an equipped cosmetic name back to its LabyMod id (or -1 if unknown).
   public static int idOf(String name) {
      Integer id = NAME_TO_ID.get(name.toLowerCase());
      return id == null ? -1 : id;
   }

   public static boolean isRegistered() {
      return registered;
   }

   // Make sure the index is being fetched; once it's ready, register every cosmetic name exactly once.
   // Safe to call every client tick — it no-ops after the one-time registration.
   public static void ensureRegistered() {
      if (registered) {
         return;
      }
      LabyCosmetics.loadIndex();
      if (!LabyCosmetics.isIndexLoaded()) {
         return; // index still downloading; try again next tick
      }
      synchronized (LabyOfflineCatalog.class) {
         if (registered) {
            return;
         }
         // Names already taken by Esdeath/OAM cosmetics (labymod isn't registered yet). LabyMod shares
         // many display names with Esdeath's own cosmetics (Bandana, Halo, Tail, Crown, Wings, Hat…);
         // without disambiguation markLabymod() would flip the Esdeath cosmetic into the Labymod
         // category and the two would share one equip/active key (toggling one toggled both). Suffix
         // every colliding LabyMod name with " (LabyMod)" so it's a distinct entry.
         java.util.Set<String> taken = new java.util.HashSet<String>();
         for (String existing : CosmeticController.getCosmetics()) {
            taken.add(existing.toLowerCase());
         }
         for (LabyCosmetics.Meta m : LabyCosmetics.allMetas()) {
            if (m == null || m.name == null || m.name.isEmpty()) {
               continue;
            }
            // Offline cosmetics only: skip pets (FLYING_PET/WALKING_PET/SHOULDER_PET) and anything that
            // isn't a plain COSMETIC, and anything without default appearance data to render with.
            if (!"COSMETIC".equalsIgnoreCase(m.type) || m.defaultData == null || m.defaultData.length == 0) {
               continue;
            }
            String name = m.name;
            String key = name.toLowerCase();
            // Disambiguate against existing (Esdeath/OAM) names AND already-registered LabyMod names.
            if (taken.contains(key) || NAME_TO_ID.containsKey(key)) {
               name = m.name + " (LabyMod)";
               key = name.toLowerCase();
               if (taken.contains(key) || NAME_TO_ID.containsKey(key)) {
                  name = m.name + " (LabyMod #" + m.id + ")";
                  key = name.toLowerCase();
               }
            }
            NAME_TO_ID.put(key, m.id);
            CosmeticController.addCos(name);
            CosmeticController.markLabymod(name);
         }
         registered = true;
      }
   }
}
