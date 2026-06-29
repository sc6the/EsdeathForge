package me.proxycracked.universalaccountmanager.auth;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.proxycracked.universalaccountmanager.utils.HttpUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

// Async cache for name-availability lookups via the public Mojang endpoint
// `api.mojang.com/users/profiles/minecraft/<name>`:
//   - HTTP 200 + JSON profile  → name is taken
//   - HTTP 204 / empty body    → name is available
//   - HTTP 400                 → name is malformed
//
// Only tells you whether someone *currently* owns the name, not whether your
// account is allowed to claim it (cooldowns, rename grace period, etc. are
// handled by the auth-required endpoint when the user actually tries to
// change). Good enough for live "Available / Taken" feedback as the user types.
public final class NameAvailabilityCache {
  public enum State { LOADING, AVAILABLE, TAKEN, INVALID, ERROR }

  private static final Map<String, State> cache = new HashMap<>();
  private static final Set<String> loading = new HashSet<>();

  private static final ExecutorService EXEC = Executors.newSingleThreadExecutor(new ThreadFactory() {
    private final AtomicInteger n = new AtomicInteger(0);
    @Override public Thread newThread(Runnable r) {
      Thread t = new Thread(r, "UniversalAccountManager-NameAvail-" + n.incrementAndGet());
      t.setDaemon(true);
      return t;
    }
  });

  private NameAvailabilityCache() {}

  // Returns LOADING until the fetch completes. Callers can poll this from a
  // draw method — repeat calls don't queue duplicate work.
  public static State get(String name) {
    if (name == null) return State.INVALID;
    String key = name.toLowerCase();
    if (key.isEmpty() || key.length() > 16) return State.INVALID;
    if (!key.matches("[a-z0-9_]+")) return State.INVALID;

    synchronized (cache) {
      State s = cache.get(key);
      if (s != null) return s;
      if (loading.contains(key)) return State.LOADING;
      loading.add(key);
    }
    EXEC.submit(() -> fetch(key));
    return State.LOADING;
  }

  private static void fetch(String name) {
    State result = State.ERROR;
    try {
      String body = HttpUtils.get("https://api.mojang.com/users/profiles/minecraft/" + name);
      if (body == null || body.isEmpty()) {
        result = State.AVAILABLE;
      } else {
        JsonElement el = new JsonParser().parse(body);
        if (el.isJsonObject()) {
          JsonObject obj = el.getAsJsonObject();
          if (obj.has("error")) {
            // Mojang puts "errorMessage" details inside; treat any error as
            // "name not taken" (typically NotFoundException).
            String err = obj.has("errorMessage") ? obj.get("errorMessage").getAsString() : "";
            if (err.toLowerCase().contains("invalid") || err.toLowerCase().contains("path")) {
              result = State.INVALID;
            } else {
              result = State.AVAILABLE;
            }
          } else if (obj.has("id")) {
            result = State.TAKEN;
          } else {
            result = State.AVAILABLE;
          }
        }
      }
    } catch (Throwable ignored) {
      result = State.ERROR;
    }
    synchronized (cache) {
      cache.put(name, result);
      loading.remove(name);
    }
  }
}
