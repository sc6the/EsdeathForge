package me.txb1.extras.cosmetics.oam;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderPlayer;

// Drives OAM cosmetic animation (the original ran a 2ms Timer ticking each model's
// performAnimation to advance flap/sway counters) and builds the per-player layer set.
public class OamCosmeticManager {
   private static final List<CosmeticModelRenderer> ANIMATED = new ArrayList<CosmeticModelRenderer>();
   private static boolean timerStarted = false;

   // rainbow text colour (replaces OAM's LSD.rainbow util), ~1s cycle
   public static int rainbow() {
      float hue = (System.currentTimeMillis() % 1000L) / 1000.0f;
      return java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
   }

   public static void register(CosmeticModelRenderer model) {
      synchronized (ANIMATED) {
         ANIMATED.add(model);
      }
      startTimer();
   }

   private static synchronized void startTimer() {
      if (timerStarted) {
         return;
      }
      timerStarted = true;
      Timer t = new Timer("OamCosmeticAnimation", true);
      t.schedule(new TimerTask() {
         @Override
         public void run() {
            try {
               synchronized (ANIMATED) {
                  for (CosmeticModelRenderer model : ANIMATED) {
                     if (model.entityIn == null) {
                        continue;
                     }
                     model.runAnimationProcess();
                  }
               }
            } catch (Exception ignored) {
            }
         }
      }, 0L, 2L);
   }

   // instantiate every ported OAM cosmetic (ids skipped during the port simply fail Class.forName
   // and are ignored) and wrap each in an equip-gated layer using its own @CosmeticInfo displayname.
   public static List<me.txb1.extras.cosmetics.CosmeticBase> build(RenderPlayer renderPlayer) {
      List<me.txb1.extras.cosmetics.CosmeticBase> layers = new ArrayList<me.txb1.extras.cosmetics.CosmeticBase>();
      // eager-load CosmeticManager here (early, during RenderPlayer construction) alongside the
      // cosmetics. Some cosmetics (Rinnegan/Sharingan/Mini Me) only reference it at render time; if it
      // first loads then, the co-loaded raven transformer NPEs on it -> NoClassDefFoundError.
      try {
         Class.forName("me.txb1.extras.cosmetics.oam.CosmeticManager");
      } catch (Throwable ignored) {
      }
      ModelBase base = renderPlayer.getMainModel();
      for (int i = 1; i <= 59; i++) {
         String cls = String.format("me.txb1.extras.cosmetics.oam.cosmetics.Cosmetic%03d", i);
         try {
            Class<?> c = Class.forName(cls);
            CosmeticModelRenderer model = (CosmeticModelRenderer) c.getConstructor(ModelBase.class).newInstance(base);
            String name = model.getInformation().displayname();
            register(model);
            layers.add(new OamCosmeticLayer(renderPlayer, name, model));
         } catch (ClassNotFoundException notPorted) {
            // intentionally skipped cosmetic id
         } catch (Throwable err) {
            System.out.println("[Esdeath] failed to init OAM cosmetic " + cls + ": " + err);
         }
      }
      return layers;
   }
}
