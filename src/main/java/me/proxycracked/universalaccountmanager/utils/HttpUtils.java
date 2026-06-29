package me.proxycracked.universalaccountmanager.utils;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class HttpUtils {
  private HttpUtils() {}

  public static String get(String url) throws Exception {
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      HttpGet req = new HttpGet(url);
      req.setHeader("User-Agent", "UniversalAccountManager/1.0");
      return EntityUtils.toString(client.execute(req).getEntity());
    }
  }

  public static byte[] getBytes(String url) throws Exception {
    URL u = new URL(url);
    HttpURLConnection conn = (HttpURLConnection) u.openConnection();
    conn.setRequestProperty("User-Agent", "UniversalAccountManager/1.0");
    conn.setConnectTimeout(15_000);
    conn.setReadTimeout(15_000);
    try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buf = new byte[8192];
      int n;
      while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
      return out.toByteArray();
    } finally {
      conn.disconnect();
    }
  }
}
