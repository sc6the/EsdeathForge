package me.txb1.extras.skin;

import me.proxycracked.universalaccountmanager.skin.SkinChanger;
import net.minecraft.client.Minecraft;

// Holds a real Mojang skin change that has been previewed locally (via Force Skin) but not yet
// pushed to Mojang. The actual upload is deferred until the player quits the current server
// (SkinDisconnectHandler) — uploading mid-session doesn't propagate to other players until a
// reconnect anyway, and Mojang rate-limits the endpoint, so we batch it to the disconnect.
//
// When the player isn't on a server (in the main menu), EsdeathSkinGui flushes immediately.
public final class PendingSkin {
   // exactly one of {url, bytes} is non-null when a change is pending
   private static volatile String url;
   private static volatile byte[] bytes;
   private static volatile String variant = "classic";

   private PendingSkin() {
   }

   public static synchronized void queueUrl(String skinUrl, String variantIn) {
      url = skinUrl;
      bytes = null;
      variant = "slim".equalsIgnoreCase(variantIn) ? "slim" : "classic";
   }

   public static synchronized void queueBytes(byte[] pngBytes, String variantIn) {
      bytes = pngBytes;
      url = null;
      variant = "slim".equalsIgnoreCase(variantIn) ? "slim" : "classic";
   }

   public static synchronized boolean hasPending() {
      return url != null || bytes != null;
   }

   public static synchronized void clear() {
      url = null;
      bytes = null;
   }

   // Push the queued skin to Mojang on a background thread (HTTP + rate-limited). No-op if nothing
   // is pending. Clears the queue on a successful (200) upload so a later disconnect doesn't re-send.
   public static void flushAsync() {
      final String u;
      final byte[] b;
      final String v;
      synchronized (PendingSkin.class) {
         if (url == null && bytes == null) {
            return;
         }
         u = url;
         b = bytes;
         v = variant;
      }
      final String token = Minecraft.getMinecraft().getSession().getToken();
      new Thread(() -> {
         try {
            int code = (u != null)
               ? SkinChanger.applySkinUrl(u, v, token)
               : SkinChanger.applySkinFile(b, v, token);
            if (code == 200) {
               clear();
               System.out.println("[EsdeathSkin] Mojang skin applied on disconnect.");
            } else {
               System.err.println("[EsdeathSkin] Mojang skin upload returned " + code);
            }
         } catch (Exception e) {
            System.err.println("[EsdeathSkin] Mojang skin upload failed: " + e.getMessage());
         }
      }, "Esdeath-PendingSkin").start();
   }
}
