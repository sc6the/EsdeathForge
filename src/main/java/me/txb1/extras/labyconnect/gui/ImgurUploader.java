package me.txb1.extras.labyconnect.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

/**
 * Anonymous Imgur image upload for LabyChat image embeds.
 *
 * Needs a free Imgur API Client-ID (register once at https://api.imgur.com/oauth2/addclient —
 * pick "OAuth 2 without callback URL"; no review, instant). Provide it either by:
 *   - editing {@link #FALLBACK_CLIENT_ID} below and rebuilding, OR
 *   - dropping the id (one line) into  .minecraft/EsdeathClient/imgur_clientid.txt  (no rebuild).
 */
public final class ImgurUploader {

    /** Edit this and rebuild, or use the imgur_clientid.txt file instead. */
    private static final String FALLBACK_CLIENT_ID = "YOUR_IMGUR_CLIENT_ID";

    private ImgurUploader() {}

    public static boolean isConfigured() {
        String id = clientId();
        return id != null && !id.isEmpty() && !id.equals("YOUR_IMGUR_CLIENT_ID");
    }

    private static String clientId() {
        try {
            File f = new File(Minecraft.getMinecraft().mcDataDir, "EsdeathClient/imgur_clientid.txt");
            if (f.exists()) {
                Scanner sc = new Scanner(f, "UTF-8");
                String line = sc.hasNextLine() ? sc.nextLine().trim() : "";
                sc.close();
                if (!line.isEmpty()) return line;
            }
        } catch (Throwable ignored) {
        }
        return FALLBACK_CLIENT_ID;
    }

    /** Uploads a PNG of the image to Imgur, returns the direct link. Blocking — call off the main thread. */
    public static String upload(BufferedImage image) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException("Imgur Client-ID not set "
                + "(edit ImgurUploader or .minecraft/EsdeathClient/imgur_clientid.txt)");
        }
        ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
        ImageIO.write(image, "png", pngOut);
        String base64 = javax.xml.bind.DatatypeConverter.printBase64Binary(pngOut.toByteArray());
        byte[] body = ("image=" + URLEncoder.encode(base64, "UTF-8") + "&type=base64").getBytes("UTF-8");

        HttpURLConnection con = (HttpURLConnection) new URL("https://api.imgur.com/3/image").openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Client-ID " + clientId());
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("User-Agent", "EsdeathClient");
        con.setConnectTimeout(15000);
        con.setReadTimeout(30000);
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(body);
        os.flush();
        os.close();

        int code = con.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? con.getInputStream() : con.getErrorStream();
        String response = readAll(is);
        con.disconnect();

        JsonObject json = new JsonParser().parse(response).getAsJsonObject();
        if (json.has("success") && json.get("success").getAsBoolean() && json.has("data")) {
            return json.getAsJsonObject("data").get("link").getAsString();
        }
        String err = json.has("data") && json.getAsJsonObject("data").has("error")
            ? json.getAsJsonObject("data").get("error").toString() : response;
        throw new RuntimeException("Imgur upload failed (HTTP " + code + "): " + err);
    }

    private static String readAll(InputStream is) {
        if (is == null) return "";
        Scanner sc = new Scanner(is, "UTF-8").useDelimiter("\\A");
        String s = sc.hasNext() ? sc.next() : "";
        sc.close();
        return s;
    }
}
