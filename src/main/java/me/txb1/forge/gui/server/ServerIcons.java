package me.txb1.forge.gui.server;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;

// Decodes server favicons (base64 PNG in ServerData) to a cached texture, like vanilla's server list.
// Returns null on missing/invalid data so the caller can draw the default icon.
public final class ServerIcons {
   private static final Map<String, ResourceLocation> CACHE = new HashMap<String, ResourceLocation>();
   private static final Set<String> FAILED = new HashSet<String>();

   private ServerIcons() {
   }

   public static ResourceLocation get(ServerData sd) {
      String data = sd.getBase64EncodedIconData();
      if (data == null || data.isEmpty()) {
         return null;
      }
      String key = sd.serverIP + "|" + data.hashCode();
      ResourceLocation loc = CACHE.get(key);
      if (loc != null) {
         return loc;
      }
      if (FAILED.contains(key)) {
         return null;
      }
      try {
         ByteBuf encoded = Unpooled.copiedBuffer(data, Charsets.UTF_8);
         ByteBuf decoded = Base64.decode(encoded);
         BufferedImage img = TextureUtil.readBufferedImage(new ByteBufInputStream(decoded));
         if (img == null || img.getWidth() != 64 || img.getHeight() != 64) {
            FAILED.add(key);
            return null;
         }
         DynamicTexture tex = new DynamicTexture(img);
         loc = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("esd_server_" + Integer.toHexString(key.hashCode()), tex);
         CACHE.put(key, loc);
         return loc;
      } catch (Throwable t) {
         FAILED.add(key);
         return null;
      }
   }
}
