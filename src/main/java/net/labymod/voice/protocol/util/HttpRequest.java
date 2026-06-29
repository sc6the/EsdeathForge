package net.labymod.voice.protocol.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest {
   public static String getRequest(String url) {
      try {
         HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
         con.setConnectTimeout(5000);
         con.setReadTimeout(5000);
         con.setRequestProperty(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36"
         );
         int response = con.getResponseCode();
         if (response != 200) {
            System.out.println("Got response " + response + " " + url);
         }

         BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
         StringBuilder content = new StringBuilder();

         String input;
         while ((input = reader.readLine()) != null) {
            content.append(input);
         }

         reader.close();
         return content.toString();
      } catch (IOException var6) {
         var6.printStackTrace();
         return null;
      }
   }
}
