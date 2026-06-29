package me.proxycracked.universalaccountmanager.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Session;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

// Login by raw Mojang session token (yx-zero/tokenlogin).
public final class TokenAuth {
  private TokenAuth() {}

  public static String[] getProfileInfo(String token) throws Exception {
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      HttpGet req = new HttpGet("https://api.minecraftservices.com/minecraft/profile");
      req.setHeader("Authorization", "Bearer " + token);
      String body = EntityUtils.toString(client.execute(req).getEntity(), StandardCharsets.UTF_8);
      JsonObject json = new JsonParser().parse(body).getAsJsonObject();
      if (!json.has("name") || !json.has("id")) {
        throw new Exception(json.has("error")
          ? json.get("error").getAsString() + ": " + (json.has("errorMessage") ? json.get("errorMessage").getAsString() : "")
          : "Token rejected");
      }
      return new String[]{ json.get("name").getAsString(), json.get("id").getAsString() };
    }
  }

  public static boolean validate(String token) {
    try {
      getProfileInfo(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static CompletableFuture<Session> login(String token, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        String[] info = getProfileInfo(token);
        return new Session(info[0], info[1], token, Session.Type.MOJANG.toString());
      } catch (InterruptedException e) {
        throw new CancellationException("Token login cancelled!");
      } catch (Exception e) {
        throw new CompletionException("Token login failed!", e);
      }
    }, executor);
  }
}
