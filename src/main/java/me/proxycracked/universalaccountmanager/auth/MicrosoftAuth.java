package me.proxycracked.universalaccountmanager.auth;

import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.util.Session;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

// Based on AccountManager (ksyzov) which is based on AuthMe (axieum).
public final class MicrosoftAuth {
  private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
    .setConnectionRequestTimeout(30_000)
    .setConnectTimeout(30_000)
    .setSocketTimeout(30_000)
    .build();

  // UniversalAccountManager — public Microsoft Live client id (shared across community account managers).
  private static final String CLIENT_ID = "42a60a84-599d-44b2-a7c6-b00cdef1d6a2";
  private static final int PORT = 25575;

  private static CloseableHttpClient httpClient() {
    return HttpClients.createDefault();
  }

  public static URI getMSAuthLink(String state) {
    try {
      return new URIBuilder("https://login.live.com/oauth20_authorize.srf")
        .addParameter("client_id", CLIENT_ID)
        .addParameter("response_type", "code")
        .addParameter("redirect_uri", String.format("http://localhost:%d/callback", PORT))
        .addParameter("scope", "XboxLive.signin XboxLive.offline_access")
        .addParameter("state", state)
        .addParameter("prompt", "select_account")
        .build();
    } catch (Exception e) {
      return null;
    }
  }

  public static CompletableFuture<String> acquireMSAuthCode(String state, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> authCode = new AtomicReference<>(null);
        AtomicReference<String> errorMsg = new AtomicReference<>(null);

        server.createContext("/callback", exchange -> {
          Map<String, String> query = URLEncodedUtils
            .parse(exchange.getRequestURI().toString().replaceAll("/callback\\?", ""), StandardCharsets.UTF_8)
            .stream()
            .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));

          if (!state.equals(query.get("state"))) {
            errorMsg.set(String.format("State mismatch! Expected '%s' but got '%s'.", state, query.get("state")));
          } else if (query.containsKey("code")) {
            authCode.set(query.get("code"));
          } else if (query.containsKey("error")) {
            errorMsg.set(String.format("%s: %s", query.get("error"), query.get("error_description")));
          }

          InputStream stream = MicrosoftAuth.class.getResourceAsStream("/callback.html");
          byte[] response = stream != null ? IOUtils.toByteArray(stream)
            : "<html><body><h1>You may close this window.</h1></body></html>".getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().add("Content-Type", "text/html");
          exchange.sendResponseHeaders(200, response.length);
          exchange.getResponseBody().write(response);
          exchange.getResponseBody().close();

          latch.countDown();
        });

        try {
          server.start();
          latch.await();
          return Optional.ofNullable(authCode.get())
            .filter(c -> !StringUtils.isBlank(c))
            .orElseThrow(() -> new Exception(
              Optional.ofNullable(errorMsg.get()).orElse("There was no auth code or error description present.")
            ));
        } finally {
          server.stop(2);
        }
      } catch (InterruptedException e) {
        throw new CancellationException("Microsoft auth code acquisition was cancelled!");
      } catch (Exception e) {
        throw new CompletionException("Unable to acquire Microsoft auth code!", e);
      }
    }, executor);
  }

  public static CompletableFuture<Map<String, String>> acquireMSAccessTokens(String authCode, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {
      try (CloseableHttpClient client = httpClient()) {
        HttpPost req = new HttpPost(URI.create("https://login.live.com/oauth20_token.srf"));
        req.setConfig(REQUEST_CONFIG);
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.setEntity(new UrlEncodedFormEntity(Arrays.asList(
          new BasicNameValuePair("client_id", CLIENT_ID),
          new BasicNameValuePair("grant_type", "authorization_code"),
          new BasicNameValuePair("code", authCode),
          new BasicNameValuePair("redirect_uri", String.format("http://localhost:%d/callback", PORT))
        ), "UTF-8"));
        return parseTokenPair(client.execute(req));
      } catch (InterruptedException e) {
        throw new CancellationException("MS token acquisition cancelled!");
      } catch (Exception e) {
        throw new CompletionException("Unable to acquire MS access tokens!", e);
      }
    }, executor);
  }

  public static CompletableFuture<Map<String, String>> refreshMSAccessTokens(String refreshToken, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {
      try (CloseableHttpClient client = httpClient()) {
        HttpPost req = new HttpPost(URI.create("https://login.live.com/oauth20_token.srf"));
        req.setConfig(REQUEST_CONFIG);
        req.setHeader("Content-Type", "application/x-www-form-urlencoded");
        req.setEntity(new UrlEncodedFormEntity(Arrays.asList(
          new BasicNameValuePair("client_id", CLIENT_ID),
          new BasicNameValuePair("grant_type", "refresh_token"),
          new BasicNameValuePair("refresh_token", refreshToken),
          new BasicNameValuePair("redirect_uri", String.format("http://localhost:%d/callback", PORT))
        ), "UTF-8"));
        return parseTokenPair(client.execute(req));
      } catch (InterruptedException e) {
        throw new CancellationException("MS token refresh cancelled!");
      } catch (Exception e) {
        throw new CompletionException("Unable to refresh MS access tokens!", e);
      }
    }, executor);
  }

  private static Map<String, String> parseTokenPair(HttpResponse res) throws Exception {
    JsonObject json = new JsonParser().parse(EntityUtils.toString(res.getEntity())).getAsJsonObject();
    String accessToken = mustGet(json, "access_token");
    String refreshToken = mustGet(json, "refresh_token");
    Map<String, String> result = new HashMap<>();
    result.put("access_token", accessToken);
    result.put("refresh_token", refreshToken);
    return result;
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

  public static CompletableFuture<String> acquireXboxAccessToken(String accessToken, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {
      try (CloseableHttpClient client = httpClient()) {
        HttpPost req = new HttpPost(URI.create("https://user.auth.xboxlive.com/user/authenticate"));
        JsonObject entity = new JsonObject();
        JsonObject props = new JsonObject();
        props.addProperty("AuthMethod", "RPS");
        props.addProperty("SiteName", "user.auth.xboxlive.com");
        props.addProperty("RpsTicket", String.format("d=%s", accessToken));
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
            : "There was no Xbox access token or error description present."));
      } catch (InterruptedException e) {
        throw new CancellationException("Xbox token acquisition cancelled!");
      } catch (Exception e) {
        throw new CompletionException("Unable to acquire Xbox access token!", e);
      }
    }, executor);
  }

  public static CompletableFuture<Map<String, String>> acquireXboxXstsToken(String accessToken, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {
      try (CloseableHttpClient client = httpClient()) {
        HttpPost req = new HttpPost("https://xsts.auth.xboxlive.com/xsts/authorize");
        JsonObject entity = new JsonObject();
        JsonObject props = new JsonObject();
        JsonArray userTokens = new JsonArray();
        userTokens.add(new JsonPrimitive(accessToken));
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
        return Optional.ofNullable(json.get("Token"))
          .map(JsonElement::getAsString)
          .filter(s -> !StringUtils.isBlank(s))
          .map(token -> {
            String uhs = json.get("DisplayClaims").getAsJsonObject()
              .get("xui").getAsJsonArray().get(0).getAsJsonObject()
              .get("uhs").getAsString();
            Map<String, String> result = new HashMap<>();
            result.put("Token", token);
            result.put("uhs", uhs);
            return result;
          })
          .orElseThrow(() -> new Exception(json.has("XErr")
            ? String.format("%s: %s", json.get("XErr").getAsString(), json.get("Message").getAsString())
            : "There was no XSTS token or error description present."));
      } catch (InterruptedException e) {
        throw new CancellationException("XSTS token acquisition cancelled!");
      } catch (Exception e) {
        throw new CompletionException("Unable to acquire Xbox XSTS token!", e);
      }
    }, executor);
  }

  public static CompletableFuture<String> acquireMCAccessToken(String xstsToken, String userHash, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {
      try (CloseableHttpClient client = httpClient()) {
        HttpPost req = new HttpPost(URI.create("https://api.minecraftservices.com/authentication/login_with_xbox"));
        req.setConfig(REQUEST_CONFIG);
        req.setHeader("Content-Type", "application/json");
        req.setEntity(new StringEntity(String.format("{\"identityToken\": \"XBL3.0 x=%s;%s\"}", userHash, xstsToken)));
        HttpResponse res = client.execute(req);
        JsonObject json = new JsonParser().parse(EntityUtils.toString(res.getEntity())).getAsJsonObject();
        return Optional.ofNullable(json.get("access_token"))
          .map(JsonElement::getAsString)
          .filter(s -> !StringUtils.isBlank(s))
          .orElseThrow(() -> new Exception(json.has("error")
            ? String.format("%s: %s", json.get("error").getAsString(), json.get("errorMessage").getAsString())
            : "There was no MC access token or error description present."));
      } catch (InterruptedException e) {
        throw new CancellationException("MC token acquisition cancelled!");
      } catch (Exception e) {
        throw new CompletionException("Unable to acquire Minecraft access token!", e);
      }
    }, executor);
  }

  public static CompletableFuture<Session> login(String mcToken, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {
      try (CloseableHttpClient client = httpClient()) {
        HttpGet req = new HttpGet(URI.create("https://api.minecraftservices.com/minecraft/profile"));
        req.setConfig(REQUEST_CONFIG);
        req.setHeader("Authorization", "Bearer " + mcToken);
        HttpResponse res = client.execute(req);
        JsonObject json = new JsonParser().parse(EntityUtils.toString(res.getEntity())).getAsJsonObject();
        return Optional.ofNullable(json.get("id"))
          .map(JsonElement::getAsString)
          .filter(uuid -> !StringUtils.isBlank(uuid))
          .map(uuid -> new Session(json.get("name").getAsString(), uuid, mcToken, Session.Type.MOJANG.toString()))
          .orElseThrow(() -> new Exception(json.has("error")
            ? String.format("%s: %s", json.get("error").getAsString(), json.get("errorMessage").getAsString())
            : "There was no profile or error description present."));
      } catch (InterruptedException e) {
        throw new CancellationException("Profile fetch cancelled!");
      } catch (Exception e) {
        throw new CompletionException("Unable to fetch Minecraft profile!", e);
      }
    }, executor);
  }
}
