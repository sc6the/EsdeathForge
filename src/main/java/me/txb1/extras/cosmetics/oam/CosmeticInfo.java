package me.txb1.extras.cosmetics.oam;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// OAM cosmetic metadata annotation — ported verbatim.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CosmeticInfo {
   int id();

   double height();

   boolean activatergb();

   String displayname();

   CosmeticType type();
}
