package me.txb1.extras.cosmetics.oam;

import me.txb1.EsdeathClient;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;

// Minimal shim of OAM's CosmeticManager — only the cross-cosmetic check a few cosmetics use to
// stay mutually exclusive (e.g. Sharingan hides if Rinnegan is equipped). Resolves the target id's
// displayname via its annotation and tests the player's equipped set.
public class CosmeticManager {
   public static boolean hasEntityCosmetic(Entity entity, int id) {
      try {
         Class<?> c = Class.forName(String.format("me.txb1.extras.cosmetics.oam.cosmetics.Cosmetic%03d", id));
         CosmeticInfo info = c.getAnnotation(CosmeticInfo.class);
         if (info == null) {
            return false;
         }
         if (entity instanceof AbstractClientPlayer) {
            return EsdeathClient.getInstance()
               .getPlayer(entity.getUniqueID().toString())
               .getCosmetics()
               .contains(info.displayname().toLowerCase());
         }
      } catch (Throwable ignored) {
      }
      return false;
   }
}
