package me.proxycracked.universalaccountmanager.skin;

import me.proxycracked.universalaccountmanager.utils.HttpUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

// Transient, in-memory skin override for the local player. Unlike ForceSkin (which writes
// config/skinforce/skin.png to disk and persists across launches), this only swaps the rendered
// texture for the current session — it does NOT touch the user's saved Force Skin. The in-game
// Skinchanger uses it so previewing/applying a skin doesn't clobber a Force Skin the user set up.
// MixinAbstractClientPlayer prefers this over ForceSkinLoader when present.
public final class SessionSkin {
   private static final String KEY = "esd_session_skin";
   private static ResourceLocation loc;
   private static boolean slim;

   private SessionSkin() {
   }

   public static boolean hasSkin() {
      return loc != null;
   }

   public static ResourceLocation getSkinLocation() {
      return loc;
   }

   public static boolean isSlim() {
      return slim;
   }

   public static void clear() {
      if (loc != null) {
         final ResourceLocation old = loc;
         loc = null;
         Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
               try {
                  Minecraft.getMinecraft().getTextureManager().deleteTexture(old);
               } catch (Throwable ignored) {
               }
            }
         });
      }
   }

   // Register raw PNG bytes as the session skin (processed through Mojang's skin parser so overlay
   // transparency / legacy 64x32 sheets render correctly). Safe to call from any thread — the GL
   // upload is posted to the render thread.
   public static void setFromBytes(final byte[] png, final boolean slimVariant) {
      Minecraft.getMinecraft().addScheduledTask(new Runnable() {
         @Override
         public void run() {
            try {
               BufferedImage img = ImageIO.read(new ByteArrayInputStream(png));
               if (img == null) {
                  return;
               }
               BufferedImage processed = new ImageBufferDownload().parseUserSkin(img);
               if (processed != null) {
                  img = processed;
               }
               if (loc != null) {
                  try {
                     Minecraft.getMinecraft().getTextureManager().deleteTexture(loc);
                  } catch (Throwable ignored) {
                  }
               }
               loc = Minecraft.getMinecraft().getTextureManager()
                  .getDynamicTextureLocation(KEY, new DynamicTexture(img));
               slim = slimVariant;
            } catch (Throwable ignored) {
            }
         }
      });
   }

   // Download a skin URL and register it (blocking download — call off-thread).
   public static void setFromUrl(String url, boolean slimVariant) throws Exception {
      setFromBytes(HttpUtils.getBytes(url), slimVariant);
   }
}
