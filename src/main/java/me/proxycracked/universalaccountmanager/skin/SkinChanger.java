package me.proxycracked.universalaccountmanager.skin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.proxycracked.universalaccountmanager.utils.HttpUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

// Skin lookup + Mojang skin upload. Uses Mojang's session API directly so the
// resolved skin URL/variant always reflect the player's current skin (the
// previous ashcon.app proxy caches results for several hours).
public final class SkinChanger {
  private SkinChanger() {}

  public static final class SkinInfo {
    public final String url;
    public final String variant; // "classic" or "slim"
    public SkinInfo(String url, String variant) {
      this.url = url;
      this.variant = variant;
    }
  }

  // Resolve a username to its current Mojang skin URL + variant in a single
  // round trip against api.mojang.com + sessionserver.mojang.com. Returns null
  // if the user does not exist or has no skin texture.
  public static SkinInfo resolveSkin(String username) {
    if (username == null || username.trim().isEmpty()) return null;
    try {
      String profile = HttpUtils.get(
          "https://api.mojang.com/users/profiles/minecraft/" + username.trim());
      if (profile == null || profile.isEmpty()) return null;
      JsonElement profileEl = new JsonParser().parse(profile);
      if (!profileEl.isJsonObject()) return null;
      JsonObject profileJson = profileEl.getAsJsonObject();
      if (profileJson.has("error") || !profileJson.has("id")) return null;
      String uuid = profileJson.get("id").getAsString();

      String session = HttpUtils.get(
          "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
      JsonObject sessionJson = new JsonParser().parse(session).getAsJsonObject();
      if (!sessionJson.has("properties")) return null;

      String texturesB64 = null;
      for (JsonElement el : sessionJson.getAsJsonArray("properties")) {
        JsonObject prop = el.getAsJsonObject();
        if ("textures".equals(prop.get("name").getAsString())) {
          texturesB64 = prop.get("value").getAsString();
          break;
        }
      }
      if (texturesB64 == null) return null;

      String texturesJsonStr = new String(Base64.getDecoder().decode(texturesB64));
      JsonObject textures = new JsonParser().parse(texturesJsonStr)
          .getAsJsonObject().getAsJsonObject("textures");
      if (textures == null) return null;
      JsonObject skin = textures.getAsJsonObject("SKIN");
      if (skin == null || !skin.has("url")) return null;

      String url = skin.get("url").getAsString();
      String variant = "classic";
      if (skin.has("metadata")) {
        JsonObject meta = skin.getAsJsonObject("metadata");
        if (meta.has("model") && "slim".equals(meta.get("model").getAsString())) {
          variant = "slim";
        }
      }
      return new SkinInfo(url, variant);
    } catch (Exception e) {
      return null;
    }
  }

  // POST to the Mojang skin endpoint with the active session's MC access token.
  public static int applySkinUrl(String skinUrl, String variant, String mcAccessToken) throws Exception {
    if (skinUrl == null || skinUrl.isEmpty()) return -1;
    String v = "slim".equalsIgnoreCase(variant) ? "slim" : "classic";
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      HttpPost req = new HttpPost("https://api.minecraftservices.com/minecraft/profile/skins");
      req.setHeader("Authorization", "Bearer " + mcAccessToken);
      req.setHeader("Content-Type", "application/json");
      String body = String.format("{\"variant\":\"%s\",\"url\":\"%s\"}", v, skinUrl);
      req.setEntity(new StringEntity(body));
      return client.execute(req).getStatusLine().getStatusCode();
    }
  }

  // Multipart POST of a raw PNG to Mojang's skin endpoint. Hand-rolled so we
  // don't need httpmime on the classpath — it's only ~30 lines anyway.
  public static int applySkinFile(byte[] pngBytes, String variant, String mcAccessToken) throws Exception {
    if (pngBytes == null || pngBytes.length == 0) return -1;
    String v = "slim".equalsIgnoreCase(variant) ? "slim" : "classic";
    String boundary = "----UAM" + Long.toHexString(System.nanoTime());
    String CRLF = "\r\n";

    HttpURLConnection conn = (HttpURLConnection) new URL(
      "https://api.minecraftservices.com/minecraft/profile/skins").openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Authorization", "Bearer " + mcAccessToken);
    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    conn.setConnectTimeout(10000);
    conn.setReadTimeout(20000);

    try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
      // variant part
      out.writeBytes("--" + boundary + CRLF);
      out.writeBytes("Content-Disposition: form-data; name=\"variant\"" + CRLF + CRLF);
      out.write(v.getBytes(StandardCharsets.UTF_8));
      out.writeBytes(CRLF);

      // file part
      out.writeBytes("--" + boundary + CRLF);
      out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"skin.png\"" + CRLF);
      out.writeBytes("Content-Type: image/png" + CRLF + CRLF);
      out.write(pngBytes);
      out.writeBytes(CRLF);

      out.writeBytes("--" + boundary + "--" + CRLF);
    }
    return conn.getResponseCode();
  }
}
