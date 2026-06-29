package dev.mergedvoicechat.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Bakes a 1px black outline into a downsampled copy of an icon texture.
 * Outline rule: any transparent pixel with at least one opaque 8-neighbor becomes opaque black.
 * Result is padded by {@link #PAD} px so the outline can extend beyond the original silhouette.
 *
 * Rendered with NEAREST filtering at the resulting padded size for pixel-crisp edges.
 */
public final class OutlinedIconCache {

    public static final int PAD = 1;
    /** Bake the texture at SCALE× the screen resolution; LINEAR sampling at render time gives subpixel-smoothed edges. */
    public static final int SCALE = 2;

    public static final class Entry {
        public final ResourceLocation location;
        public final int paddedSize; // contentSize + 2*PAD; quad should be drawn at this size shifted by -PAD
        Entry(ResourceLocation l, int s) { this.location = l; this.paddedSize = s; }
    }

    private static final Map<String, Entry> CACHE = new HashMap<String, Entry>();
    private static final Map<String, Boolean> FAILED = new HashMap<String, Boolean>();

    private OutlinedIconCache() {}

    public static Entry get(ResourceLocation source, int contentSize) {
        String key = source.toString() + "@" + contentSize;
        Entry cached = CACHE.get(key);
        if (cached != null) return cached;
        if (FAILED.containsKey(key)) return null;
        try {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(source);
            BufferedImage src;
            try {
                src = ImageIO.read(res.getInputStream());
            } finally {
                try { res.getInputStream().close(); } catch (Exception ignored) {}
            }
            if (src == null) { FAILED.put(key, Boolean.TRUE); return null; }

            int bakedContent = contentSize * SCALE;
            int bakedPad = PAD * SCALE;
            BufferedImage downsampled = downsample(src, bakedContent);
            BufferedImage padded = pad(downsampled, bakedPad);
            BufferedImage outlined = applyOutline(padded, 1);

            DynamicTexture dt = new DynamicTexture(outlined);
            String name = "mvc_outline_" + sanitize(source.getResourcePath()) + "_" + contentSize;
            ResourceLocation rl = Minecraft.getMinecraft().getTextureManager()
                .getDynamicTextureLocation(name, dt);
            Entry e = new Entry(rl, contentSize + 2 * PAD);
            CACHE.put(key, e);
            return e;
        } catch (Exception ex) {
            FAILED.put(key, Boolean.TRUE);
            return null;
        }
    }

    private static String sanitize(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            sb.append(Character.isLetterOrDigit(c) ? Character.toLowerCase(c) : '_');
        }
        return sb.toString();
    }

    /**
     * Box-filtered downsample. A target pixel is opaque iff the average alpha over the
     * source region it covers is >= 128. Color is the mean of the opaque source pixels in the
     * region (preserves the icon's shading); transparent target pixels are set to fully clear.
     */
    private static BufferedImage downsample(BufferedImage src, int target) {
        int sw = src.getWidth();
        int sh = src.getHeight();
        int[] srcPx = src.getRGB(0, 0, sw, sh, null, 0, sw);
        int[] outPx = new int[target * target];
        for (int oy = 0; oy < target; oy++) {
            int y0 = oy * sh / target;
            int y1 = (oy + 1) * sh / target;
            if (y1 == y0) y1 = Math.min(sh, y0 + 1);
            for (int ox = 0; ox < target; ox++) {
                int x0 = ox * sw / target;
                int x1 = (ox + 1) * sw / target;
                if (x1 == x0) x1 = Math.min(sw, x0 + 1);

                long aSum = 0;
                long rSum = 0, gSum = 0, bSum = 0;
                int count = 0, opaqueCount = 0;
                for (int y = y0; y < y1; y++) {
                    int row = y * sw;
                    for (int x = x0; x < x1; x++) {
                        int p = srcPx[row + x];
                        int a = (p >>> 24) & 0xFF;
                        aSum += a;
                        count++;
                        if (a > 0) {
                            rSum += (p >> 16) & 0xFF;
                            gSum += (p >> 8) & 0xFF;
                            bSum += p & 0xFF;
                            opaqueCount++;
                        }
                    }
                }
                int avgA = count == 0 ? 0 : (int) (aSum / count);
                if (avgA >= 128) {
                    int r = opaqueCount == 0 ? 255 : (int) (rSum / opaqueCount);
                    int g = opaqueCount == 0 ? 255 : (int) (gSum / opaqueCount);
                    int b = opaqueCount == 0 ? 255 : (int) (bSum / opaqueCount);
                    outPx[oy * target + ox] = 0xFF000000 | (r << 16) | (g << 8) | b;
                } else {
                    outPx[oy * target + ox] = 0;
                }
            }
        }
        BufferedImage out = new BufferedImage(target, target, BufferedImage.TYPE_INT_ARGB);
        out.setRGB(0, 0, target, target, outPx, 0, target);
        return out;
    }

    private static BufferedImage pad(BufferedImage src, int pad) {
        int w = src.getWidth() + 2 * pad;
        int h = src.getHeight() + 2 * pad;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] inPx = src.getRGB(0, 0, src.getWidth(), src.getHeight(), null, 0, src.getWidth());
        out.setRGB(pad, pad, src.getWidth(), src.getHeight(), inPx, 0, src.getWidth());
        return out;
    }

    /**
     * Iteratively dilate by {@code thickness} pixels. Each pass turns every fully-transparent pixel
     * that has at least one opaque 8-neighbor into opaque black; opaque pixels are kept verbatim.
     */
    private static BufferedImage applyOutline(BufferedImage src, int thickness) {
        BufferedImage cur = src;
        for (int i = 0; i < thickness; i++) {
            cur = dilateOnce(cur);
        }
        return cur;
    }

    private static BufferedImage dilateOnce(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int[] srcPx = src.getRGB(0, 0, w, h, null, 0, w);
        int[] outPx = new int[w * h];
        for (int y = 0; y < h; y++) {
            int row = y * w;
            for (int x = 0; x < w; x++) {
                int p = srcPx[row + x];
                int a = (p >>> 24) & 0xFF;
                if (a > 0) {
                    outPx[row + x] = p;
                    continue;
                }
                boolean adj = false;
                for (int dy = -1; dy <= 1 && !adj; dy++) {
                    int ny = y + dy;
                    if (ny < 0 || ny >= h) continue;
                    int nrow = ny * w;
                    for (int dx = -1; dx <= 1; dx++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = x + dx;
                        if (nx < 0 || nx >= w) continue;
                        if (((srcPx[nrow + nx] >>> 24) & 0xFF) > 0) { adj = true; break; }
                    }
                }
                outPx[row + x] = adj ? 0xFF000000 : 0;
            }
        }
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        out.setRGB(0, 0, w, h, outPx, 0, w);
        return out;
    }
}
