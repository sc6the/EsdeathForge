package me.proxycracked.universalaccountmanager.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.proxycracked.universalaccountmanager.utils.HttpUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

// Async username-history fetcher.
//
// Mojang retired the canonical history endpoint
// (api.mojang.com/user/profiles/<uuid>/names) in September 2022, so most
// public APIs no longer expose real historical names. Two sources still do:
//
//   1. api.crafty.gg/api/v2/players/<id> — community-crawled history.
//      Returns `data.usernames[]` ordered newest-first, with `changed_at`
//      timestamps and `null` on the original. Cloudflare's WAF rejects the
//      default `curl/...` User-Agent with 403, so HttpUtils' UA header is
//      what makes this reachable from the mod.
//
//   2. api.ashcon.app/mojang/v2/user/<id> — fallback. Since the Mojang
//      cutoff this only returns the current name, but it's still useful
//      when crafty.gg is unreachable or rate-limited.
//
// laby.net and namemc.com both sit behind Cloudflare Turnstile (HTTP 428)
// and can't be solved without a JS engine, so they're not used here.
//
// Returned entries are ordered oldest-first, with `changedAt == null` for
// the original (pre-rename) name. The cache is process-lifetime — there's
// no invalidation since name history only ever grows.
public final class NameHistoryCache {

  public static final class Entry {
    public final String name;
    public final String changedAt; // null for the very first name
    public Entry(String name, String changedAt) {
      this.name = name;
      this.changedAt = changedAt;
    }
  }

  // Sentinel for "fetch attempted, nothing usable came back" so the UI can
  // tell the difference between "still loading" and "no history available".
  public static final List<Entry> UNAVAILABLE = Collections.emptyList();

  private static final Map<String, List<Entry>> cache = new HashMap<>();
  private static final Set<String> loading = new HashSet<>();

  private static final ExecutorService EXEC = Executors.newFixedThreadPool(2, new ThreadFactory() {
    private final AtomicInteger n = new AtomicInteger(0);
    @Override public Thread newThread(Runnable r) {
      Thread t = new Thread(r, "UniversalAccountManager-NameHistory-" + n.incrementAndGet());
      t.setDaemon(true);
      return t;
    }
  });

  private NameHistoryCache() {}

  // Returns:
  //   null               → not fetched yet (caller should display a spinner)
  //   empty list         → fetch finished, nothing to show
  //   non-empty list     → ordered oldest → newest
  public static List<Entry> get(String username, String uuid) {
    String key = key(username, uuid);
    if (key == null) return UNAVAILABLE;
    synchronized (cache) {
      List<Entry> v = cache.get(key);
      if (v != null) return v;
      if (loading.contains(key)) return null;
      loading.add(key);
    }
    EXEC.submit(() -> fetch(key, username, uuid));
    return null;
  }

  private static String key(String username, String uuid) {
    if (uuid != null && !uuid.isEmpty()) return uuid.replace("-", "").toLowerCase();
    if (username != null && !username.isEmpty()) return "name:" + username.toLowerCase();
    return null;
  }

  private static void fetch(String key, String username, String uuid) {
    List<Entry> result = null;
    try {
      String lookup = (uuid != null && !uuid.isEmpty()) ? uuid.replace("-", "") : username;
      result = tryCrafty(lookup);
      if (result == null) result = tryAshcon(lookup);
    } catch (Throwable ignored) {
      // Network or parser failure — fall through to UNAVAILABLE.
    }
    if (result == null) result = UNAVAILABLE;
    final List<Entry> finalResult = result;
    synchronized (cache) {
      cache.put(key, finalResult);
      loading.remove(key);
    }
  }

  // crafty.gg returns the rename log under data.usernames[], ordered newest
  // -first, with changed_at == null on the original. Reverse it so the
  // first Entry is the original and the last is the current name, matching
  // the contract documented at the top of this file.
  private static List<Entry> tryCrafty(String lookup) {
    try {
      String body = HttpUtils.get("https://api.crafty.gg/api/v2/players/" + lookup);
      if (body == null || body.isEmpty()) return null;
      JsonElement root = new JsonParser().parse(body);
      if (!root.isJsonObject()) return null;
      JsonObject obj = root.getAsJsonObject();
      if (!obj.has("success") || !obj.get("success").getAsBoolean()) return null;
      if (!obj.has("data") || !obj.get("data").isJsonObject()) return null;
      JsonObject data = obj.getAsJsonObject("data");
      if (!data.has("usernames") || !data.get("usernames").isJsonArray()) return null;
      JsonArray arr = data.getAsJsonArray("usernames");
      List<Entry> out = new ArrayList<>(arr.size());
      for (JsonElement el : arr) {
        if (!el.isJsonObject()) continue;
        JsonObject o = el.getAsJsonObject();
        String name = o.has("username") ? o.get("username").getAsString() : null;
        if (name == null || name.isEmpty()) continue;
        String changedAt = (o.has("changed_at") && !o.get("changed_at").isJsonNull())
          ? o.get("changed_at").getAsString() : null;
        out.add(new Entry(name, changedAt));
      }
      if (out.isEmpty()) return null;
      Collections.reverse(out);
      return out;
    } catch (Exception e) {
      return null;
    }
  }

  private static List<Entry> tryAshcon(String lookup) {
    try {
      String body = HttpUtils.get("https://api.ashcon.app/mojang/v2/user/" + lookup);
      if (body == null || body.isEmpty()) return null;
      JsonElement root = new JsonParser().parse(body);
      if (!root.isJsonObject()) return null;
      JsonObject obj = root.getAsJsonObject();
      if (obj.has("error")) return null;
      if (!obj.has("username_history")) return null;
      JsonArray arr = obj.getAsJsonArray("username_history");
      List<Entry> out = new ArrayList<>(arr.size());
      for (JsonElement el : arr) {
        if (!el.isJsonObject()) continue;
        JsonObject o = el.getAsJsonObject();
        String name = o.has("username") ? o.get("username").getAsString() : null;
        if (name == null || name.isEmpty()) continue;
        String changedAt = (o.has("changed_at") && !o.get("changed_at").isJsonNull())
          ? o.get("changed_at").getAsString() : null;
        out.add(new Entry(name, changedAt));
      }
      return out.isEmpty() ? null : out;
    } catch (Exception e) {
      return null;
    }
  }

  // Shorten an ISO-8601 timestamp to YYYY-MM-DD for display. Returns the
  // input unchanged if it doesn't look like an ISO date.
  public static String formatDate(String iso) {
    if (iso == null) return "";
    int t = iso.indexOf('T');
    if (t == 10) return iso.substring(0, 10);
    return iso.length() >= 10 ? iso.substring(0, 10) : iso;
  }
}
