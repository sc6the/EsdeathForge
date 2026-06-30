package me.txb1.extras.labyconnect.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Downloads image URLs found in chat messages and exposes them as bound textures for inline
 * embeds. One background download per URL; the GL upload (DynamicTexture) happens on the main
 * thread via addScheduledTask. Cached + negative-cached.
 */
public final class ImageEmbedCache {

    public static final class Embed {
        public ResourceLocation location;
        public int width;
        public int height;
        public boolean failed;
    }

    private static final Pattern IMAGE_URL = Pattern.compile(
        "(?i)\\bhttps?://[^\\s]+?\\.(?:png|jpe?g|gif)\\b|\\bhttps?://i\\.imgur\\.com/[A-Za-z0-9]+");

    private static final ConcurrentHashMap<String, Embed> CACHE = new ConcurrentHashMap<String, Embed>();
    private static int counter = 0;

    private ImageEmbedCache() {}

    /** First image URL in the message, or null. */
    public static String findImageUrl(String message) {
        if (message == null) return null;
        java.util.regex.Matcher m = IMAGE_URL.matcher(message);
        return m.find() ? m.group() : null;
    }

    public static boolean isImageUrl(String message) {
        return findImageUrl(message) != null;
    }

    /** Returns the embed for a URL, kicking off a download on first request. Never null. */
    public static Embed get(final String url) {
        Embed existing = CACHE.get(url);
        if (existing != null) return existing;

        final Embed embed = new Embed();
        CACHE.put(url, embed);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                    con.setRequestProperty("User-Agent", "Mozilla/5.0 EsdeathClient");
                    con.setConnectTimeout(10000);
                    con.setReadTimeout(20000);
                    final BufferedImage img = ImageIO.read(con.getInputStream());
                    con.disconnect();
                    if (img == null) {
                        embed.failed = true;
                        return;
                    }
                    Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ResourceLocation loc = new ResourceLocation("esdeath_embed", "img_" + (counter++));
                                Minecraft.getMinecraft().getTextureManager().loadTexture(loc, new DynamicTexture(img));
                                embed.width = img.getWidth();
                                embed.height = img.getHeight();
                                embed.location = loc;
                            } catch (Throwable t) {
                                embed.failed = true;
                            }
                        }
                    });
                } catch (Throwable t) {
                    embed.failed = true;
                }
            }
        }, "EmbedDownload");
        t.setDaemon(true);
        t.start();
        return embed;
    }
}
