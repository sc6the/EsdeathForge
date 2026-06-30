package net.labymod.labyconnect;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.labymod.support.util.Debug;

public class IssueCollector {
   private static final String os = System.getProperty("os.name").toLowerCase();
   private static final String url = "https://issue.labymod.net/";
   private static boolean handled = false;

   public static void handle(String ip, int port) {
      if (!handled) {
         handled = true;
         Executors.newCachedThreadPool().execute(() -> {
            Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "Collect data to report the issue...");

            try {
               Gson gson = new Gson();
               Map<String, Object> data = collectData(ip, port);
               String dataString = gson.toJson(data);

               try {
                  Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "Post data: " + postData("https://issue.labymod.net/labyConnectSubmit.php", gson.toJson(data)));
               } catch (IOException var6) {
                  Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "Could not paste data: " + dataString);
                  var6.printStackTrace();
               }
            } catch (Exception var7) {
               var7.printStackTrace();
            }
         });
      }
   }

   private static Map<String, Object> collectData(String ip, int port) {
      InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
      Map<String, Object> data = new HashMap<>();
      data.put("ip", socketAddress.getAddress().getHostAddress());
      data.put("port", socketAddress.getPort());
      String connect = canConnect(socketAddress);
      if ("true".equalsIgnoreCase(connect)) {
         data.put("canConnect", true);
      } else {
         data.put("canConnect", false);
         data.put("connectError", connect);
      }

      try {
         data.put("currentIp", getMyCurrentIP());
      } catch (Exception var6) {
         var6.printStackTrace();
         data.put("currentIp", getStackTrace(var6));
      }

      data.put("traceRoute", traceRoute(ip));
      return data;
   }

   private static Map<String, String> traceRoute(String ip) {
      Map<String, String> response = new HashMap<>();

      try {
         Process traceRt;
         if (os.contains("win")) {
            traceRt = Runtime.getRuntime().exec("tracert " + ip);
         } else {
            traceRt = Runtime.getRuntime().exec("traceroute -I " + ip);
         }

         BufferedReader reader = new BufferedReader(new InputStreamReader(traceRt.getInputStream(), StandardCharsets.UTF_8));
         StringBuilder string = new StringBuilder();

         String line;
         while ((line = reader.readLine()) != null) {
            string.append(line);
            Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, line);
         }

         reader.close();
         response.put("response", string.toString());
         reader = new BufferedReader(new InputStreamReader(traceRt.getErrorStream(), StandardCharsets.UTF_8));
         string = new StringBuilder();

         while ((line = reader.readLine()) != null) {
            string.append(line);
         }

         reader.close();
         if (!string.toString().isEmpty()) {
            Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, string.toString());
         }

         response.put("errors", string.toString());
      } catch (IOException var6) {
         Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "error while performing trace route command");
         var6.printStackTrace();
      }

      return response;
   }

   public static String getMyCurrentIP() throws Exception {
      StringBuilder result = new StringBuilder();
      URL apiUrl = new URL("https://issue.labymod.net/myIp.php");
      HttpURLConnection conn = (HttpURLConnection)apiUrl.openConnection();
      conn.setRequestProperty(
         "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36"
      );
      conn.setRequestMethod("GET");

      String line;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
         while ((line = reader.readLine()) != null) {
            result.append(line);
         }
      }

      return result.toString();
   }

   private static String canConnect(InetSocketAddress socketAddress) {
      Socket socket = new Socket();

      try {
         socket.connect(socketAddress, (int)TimeUnit.SECONDS.toMillis(10L));
         socket.close();
         return "true";
      } catch (IOException var3) {
         return getStackTrace(var3);
      }
   }

   private static String getStackTrace(Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      return sw.toString();
   }

   private static String postData(String url, String data) throws IOException {
      URLConnection connection = new URL(url).openConnection();
      connection.setDoOutput(true);
      connection.setRequestProperty("Accept-Charset", "UTF-8");
      connection.setRequestProperty(
         "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36"
      );
      connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

      try (OutputStream output = connection.getOutputStream()) {
         output.write(data.getBytes("UTF-8"));
      }

      String var7;
      try (BufferedReader response = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
         StringBuilder s = new StringBuilder();

         String l;
         while ((l = response.readLine()) != null) {
            s.append(l).append("\n");
         }

         var7 = s.toString();
      }

      return var7;
   }
}
