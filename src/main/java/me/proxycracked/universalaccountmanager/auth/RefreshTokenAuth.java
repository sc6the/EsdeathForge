package me.proxycracked.universalaccountmanager.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import me.proxycracked.universalaccountmanager.UniversalAccountManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

// Converts a long-lived Microsoft refresh token into a Minecraft access token.
//
// Refresh tokens exported by token tools are minted for the *legacy Minecraft
// launcher* client (id 00000000402b5328) with the MBI_SSL scope, so the refresh
// and Xbox-auth steps must use those exact parameters — note the "t=" RpsTicket
// prefix, which differs from the modern OAuth flow in MicrosoftAuth ("d=").
// The XSTS / login_with_xbox / profile steps are identical to the modern flow.
public final class RefreshTokenAuth {
  private RefreshTokenAuth() {}

  private static final String CLIENT_ID = "00000000402b5328";
  private static final String REDIRECT_URI = "https://login.live.com/oauth20_desktop.srf";
  private static final String SCOPE = "service::user.auth.xboxlive.com::MBI_SSL";

  private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
    .setConnectionRequestTimeout(30_000)
    .setConnectTimeout(30_000)
    .setSocketTimeout(30_000)
    .build();

  private static final ExecutorService EXEC = Executors.newFixedThreadPool(3, new ThreadFactory() {
    private final AtomicInteger n = new AtomicInteger(0);
    @Override public Thread newThread(Runnable r) {
      Thread t = new Thread(r, "UniversalAccountManager-Refresh-" + n.incrementAndGet());
      t.setDaemon(true);
      return t;
    }
  });

  // Outcome of a successful refresh-token exchange.
  public static final class Result {
    public final String mcAccessToken;
    public final String refreshToken; // rotated — MS issues a new one each time
    public final String username;
    public final String uuid;

    Result(String mcAccessToken, String refreshToken, String username, String uuid) {
      this.mcAccessToken = mcAccessToken;
      this.refreshToken = refreshToken;
      this.username = username;
      this.uuid = uuid;
    }
  }

  // Heuristic: Microsoft refresh tokens are minted with an "M." prefix (e.g. "M.C504_BAY.0.U.…",
  // "M.R3_BAY.…"), whereas a Minecraft access token is a JWT ("eyJ…"). Lets the token-login screen
  // auto-detect a pasted refresh token and route it through convert() instead of treating it as an
  // MC access token (which would just fail validation).
  public static boolean looksLikeRefreshToken(String token) {
    return token != null && token.trim().startsWith("M.");
  }

  // Blocking full chain: refresh token -> MS access token (+ rotated refresh
  // token) -> XBL -> XSTS -> MC access token -> profile.
  public static Result convert(String refreshToken) throws Exception {
    if (StringUtils.isBlank(refreshToken)) throw new Exception("Refresh token is empty");
    String[] msPair = refreshMSTokens(refreshToken);          // [access, refresh]
    String xblToken = acquireXboxToken(msPair[0]);
    String[] xsts = acquireXstsToken(xblToken);               // [token, uhs]
    String mcToken = acquireMCToken(xsts[0], xsts[1]);
    String[] profile = TokenAuth.getProfileInfo(mcToken);     // [name, uuid]
    return new Result(mcToken, msPair[1], profile[0], profile[1]);
  }

  // Re-derives an access token for every stored refresh account, in the
  // background, and converts the account into a usable token account. Called on
  // each account-list refresh. onUpdate (nullable) fires after each success so
  // the GUI can repaint.
  public static void refreshAll(Runnable onUpdate) {
    for (final Account acc : new ArrayList<>(UniversalAccountManager.accounts)) {
      if (!acc.isRefresh()) continue;
      final String rt = acc.getRefreshToken();
      if (StringUtils.isBlank(rt)) {
        acc.setAvailable(Boolean.FALSE);
        continue;
      }
      EXEC.submit(() -> {
        try {
          Result r = convert(rt);
          acc.setAccessToken(r.mcAccessToken);
          if (!StringUtils.isBlank(r.refreshToken)) acc.setRefreshToken(r.refreshToken);
          acc.setUsername(r.username);
          acc.setUuid(r.uuid);
          acc.setAvailable(Boolean.TRUE);
          UniversalAccountManager.save();
          if (onUpdate != null) onUpdate.run();
        } catch (Exception e) {
          acc.setAvailable(Boolean.FALSE);
          System.err.println("[UniversalAccountManager] Refresh-token conversion failed: " + e.getMessage());
        }
      });
    }
  }

  private static CloseableHttpClient httpClient() {
    return HttpClients.createDefault();
  }

  private static String[] refreshMSTokens(String refreshToken) throws Exception {
    try (CloseableHttpClient client = httpClient()) {
      HttpPost req = new HttpPost(URI.create("https://login.live.com/oauth20_token.srf"));
      req.setConfig(REQUEST_CONFIG);
      req.setHeader("Content-Type", "application/x-www-form-urlencoded");
      req.setEntity(new UrlEncodedFormEntity(Arrays.asList(
        new BasicNameValuePair("client_id", CLIENT_ID),
        new BasicNameValuePair("grant_type", "refresh_token"),
        new BasicNameValuePair("redirect_uri", REDIRECT_URI),
        new BasicNameValuePair("refresh_token", refreshToken),
        new BasicNameValuePair("scope", SCOPE)
      ), "UTF-8"));
      JsonObject json = new JsonParser().parse(EntityUtils.toString(client.execute(req).getEntity())).getAsJsonObject();
      String access = mustGet(json, "access_token");
      String refresh = mustGet(json, "refresh_token");
      return new String[]{ access, refresh };
    }
  }

  private static String acquireXboxToken(String accessToken) throws Exception {
    try (CloseableHttpClient client = httpClient()) {
      HttpPost req = new HttpPost(URI.create("https://user.auth.xboxlive.com/user/authenticate"));
      JsonObject entity = new JsonObject();
      JsonObject props = new JsonObject();
      props.addProperty("AuthMethod", "RPS");
      props.addProperty("SiteName", "user.auth.xboxlive.com");
      // Legacy launcher tokens use the "t=" prefix (not "d=").
      props.addProperty("RpsTicket", "t=" + accessToken);
      entity.add("Properties", props);
      entity.addProperty("RelyingParty", "http://auth.xboxlive.com");
      entity.addProperty("TokenType", "JWT");
      req.setConfig(REQUEST_CONFIG);
      req.setHeader("Content-Type", "application/json");
      req.setEntity(new StringEntity(entity.toString()));

      HttpResponse res = client.execute(req);
      JsonObject json = res.getStatusLine().getStatusCode() == 200
        ? new JsonParser().parse(EntityUtils.toString(res.getEntity())).getAsJsonObject()
        : new JsonObject();
      return Optional.ofNullable(json.get("Token"))
        .map(JsonElement::getAsString)
        .filter(s -> !StringUtils.isBlank(s))
        .orElseThrow(() -> new Exception(json.has("XErr")
          ? String.format("%s: %s", json.get("XErr").getAsString(), json.get("Message").getAsString())
          : "OAuth access token is invalid"));
    }
  }

  private static String[] acquireXstsToken(String xblToken) throws Exception {
    try (CloseableHttpClient client = httpClient()) {
      HttpPost req = new HttpPost("https://xsts.auth.xboxlive.com/xsts/authorize");
      JsonObject entity = new JsonObject();
      JsonObject props = new JsonObject();
      JsonArray userTokens = new JsonArray();
      userTokens.add(new JsonPrimitive(xblToken));
      props.addProperty("SandboxId", "RETAIL");
      props.add("UserTokens", userTokens);
      entity.add("Properties", props);
      entity.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
      entity.addProperty("TokenType", "JWT");
      req.setConfig(REQUEST_CONFIG);
      req.setHeader("Content-Type", "application/json");
      req.setEntity(new StringEntity(entity.toString()));

      HttpResponse res = client.execute(req);
      JsonObject json = res.getStatusLine().getStatusCode() == 200
        ? new JsonParser().parse(EntityUtils.toString(res.getEntity())).getAsJsonObject()
        : new JsonObject();
      String token = Optional.ofNullable(json.get("Token"))
        .map(JsonElement::getAsString)
        .filter(s -> !StringUtils.isBlank(s))
        .orElseThrow(() -> new Exception(json.has("XErr")
          ? String.format("%s: %s", json.get("XErr").getAsString(), json.get("Message").getAsString())
          : "There was no XSTS token or error description present."));
      String uhs = json.get("DisplayClaims").getAsJsonObject()
        .get("xui").getAsJsonArray().get(0).getAsJsonObject()
        .get("uhs").getAsString();
      return new String[]{ token, uhs };
    }
  }

  private static String acquireMCToken(String xstsToken, String userHash) throws Exception {
    try (CloseableHttpClient client = httpClient()) {
      HttpPost req = new HttpPost(URI.create("https://api.minecraftservices.com/authentication/login_with_xbox"));
      req.setConfig(REQUEST_CONFIG);
      req.setHeader("Content-Type", "application/json");
      req.setEntity(new StringEntity(String.format("{\"identityToken\": \"XBL3.0 x=%s;%s\"}", userHash, xstsToken)));
      JsonObject json = new JsonParser().parse(EntityUtils.toString(client.execute(req).getEntity())).getAsJsonObject();
      return Optional.ofNullable(json.get("access_token"))
        .map(JsonElement::getAsString)
        .filter(s -> !StringUtils.isBlank(s))
        .orElseThrow(() -> new Exception(json.has("error")
          ? String.format("%s: %s", json.get("error").getAsString(),
              json.has("errorMessage") ? json.get("errorMessage").getAsString() : "")
          : "There was no MC access token or error description present."));
    }
  }

  private static String mustGet(JsonObject json, String key) throws Exception {
    return Optional.ofNullable(json.get(key))
      .map(JsonElement::getAsString)
      .filter(s -> !StringUtils.isBlank(s))
      .orElseThrow(() -> new Exception(json.has("error")
        ? String.format("%s: %s", json.get("error").getAsString(),
            json.has("error_description") ? json.get("error_description").getAsString() : "")
        : "Missing field: " + key));
  }
}
