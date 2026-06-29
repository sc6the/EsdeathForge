package me.proxycracked.universalaccountmanager.auth;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;

// Mojang username availability + rename. Endpoints mirror yx-zero/tokenlogin.
public final class NameChanger {
  private NameChanger() {}

  // Returns "AVAILABLE", "DUPLICATE", "NOT_ALLOWED", or "UNKNOWN".
  public static String checkAvailability(String name, String mcAccessToken) throws Exception {
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      HttpGet req = new HttpGet(
          "https://api.minecraftservices.com/minecraft/profile/name/" + name + "/available");
      req.setHeader("Authorization", "Bearer " + mcAccessToken);
      String body = EntityUtils.toString(client.execute(req).getEntity(), StandardCharsets.UTF_8);
      JsonElement el = new JsonParser().parse(body);
      if (!el.isJsonObject()) return "UNKNOWN";
      JsonObject json = el.getAsJsonObject();
      if (json.has("status")) return json.get("status").getAsString();
      return "UNKNOWN";
    }
  }

  // PUT to the Mojang rename endpoint. Returns the HTTP status code.
  public static int changeName(String newName, String mcAccessToken) throws Exception {
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      HttpPut req = new HttpPut(
          "https://api.minecraftservices.com/minecraft/profile/name/" + newName);
      req.setHeader("Authorization", "Bearer " + mcAccessToken);
      return client.execute(req).getStatusLine().getStatusCode();
    }
  }
}
