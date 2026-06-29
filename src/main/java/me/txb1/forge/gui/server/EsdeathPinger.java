package me.txb1.forge.gui.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.EnumChatFormatting;

// Self-contained Server List Ping (modern 1.7+ protocol) over a plain socket. Replaces the use of
// vanilla OldServerPinger in EsdeathServerListGui, which left every row stuck on
// "Can't connect to server." Each call runs on its own thread and writes the result straight onto
// the ServerData (MOTD, ping ms, online/max). No netty, no pingPendingNetworks pump needed.
public final class EsdeathPinger {
   private EsdeathPinger() {
   }

   public static void ping(ServerData server) {
      ServerAddress addr = ServerAddress.fromString(server.serverIP);
      String host = addr.getIP();
      int port = addr.getPort();

      Socket socket = new Socket();
      try {
         socket.setSoTimeout(4000);
         socket.connect(new InetSocketAddress(host, port), 4000);
         OutputStream rawOut = socket.getOutputStream();
         InputStream rawIn = socket.getInputStream();
         DataOutputStream out = new DataOutputStream(rawOut);
         DataInputStream in = new DataInputStream(rawIn);

         // ---- handshake (next state = 1 = status) ----
         java.io.ByteArrayOutputStream hs = new java.io.ByteArrayOutputStream();
         DataOutputStream hsd = new DataOutputStream(hs);
         writeVarInt(hsd, 0x00);          // packet id
         writeVarInt(hsd, 47);            // protocol version (1.8)
         writeString(hsd, host);          // server address
         hsd.writeShort(port);            // server port
         writeVarInt(hsd, 1);             // next state: status
         writePacket(out, hs.toByteArray());

         // ---- status request ----
         java.io.ByteArrayOutputStream req = new java.io.ByteArrayOutputStream();
         writeVarInt(new DataOutputStream(req), 0x00);
         writePacket(out, req.toByteArray());

         // ---- status response ----
         long start = System.currentTimeMillis();
         readVarInt(in);                  // total length (ignored)
         int id = readVarInt(in);
         if (id != 0x00) {
            throw new IOException("bad status packet id " + id);
         }
         int jsonLen = readVarInt(in);
         byte[] jsonBytes = new byte[jsonLen];
         in.readFully(jsonBytes);
         long ping = System.currentTimeMillis() - start;
         String json = new String(jsonBytes, "UTF-8");

         applyResponse(server, json, ping);
         server.pingToServer = ping;
      } catch (Throwable t) {
         server.pingToServer = -1L;
         server.serverMOTD = EnumChatFormatting.DARK_RED + "Can't connect to server.";
      } finally {
         try {
            socket.close();
         } catch (IOException ignored) {
         }
      }
   }

   private static void applyResponse(ServerData server, String json, long ping) {
      try {
         JsonObject root = new JsonParser().parse(json).getAsJsonObject();
         server.serverMOTD = extractMotd(root.get("description"));
         if (root.has("players")) {
            JsonObject players = root.getAsJsonObject("players");
            int online = players.has("online") ? players.get("online").getAsInt() : 0;
            int max = players.has("max") ? players.get("max").getAsInt() : 0;
            server.populationInfo = EnumChatFormatting.GRAY + "" + online + EnumChatFormatting.DARK_GRAY + "/" + EnumChatFormatting.GRAY + max;
         }
         if (root.has("version") && root.getAsJsonObject("version").has("name")) {
            server.gameVersion = root.getAsJsonObject("version").get("name").getAsString();
         }
      } catch (Throwable t) {
         server.serverMOTD = EnumChatFormatting.GRAY + "(unparseable response)";
      }
   }

   // description may be a plain string or a chat-component object ({text, extra:[...]}).
   private static String extractMotd(JsonElement desc) {
      if (desc == null) {
         return "";
      }
      if (desc.isJsonPrimitive()) {
         return desc.getAsString();
      }
      StringBuilder sb = new StringBuilder();
      collectComponent(desc, sb);
      String s = sb.toString().replace('\n', ' ');
      return s.length() > 0 ? s : "";
   }

   private static void collectComponent(JsonElement el, StringBuilder sb) {
      if (el == null) {
         return;
      }
      if (el.isJsonPrimitive()) {
         sb.append(el.getAsString());
         return;
      }
      if (el.isJsonArray()) {
         for (JsonElement child : el.getAsJsonArray()) {
            collectComponent(child, sb);
         }
         return;
      }
      JsonObject o = el.getAsJsonObject();
      if (o.has("text")) {
         sb.append(o.get("text").getAsString());
      }
      if (o.has("extra")) {
         collectComponent(o.get("extra"), sb);
      }
   }

   // ---- protocol helpers ----
   private static void writePacket(DataOutputStream out, byte[] data) throws IOException {
      java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
      writeVarInt(new DataOutputStream(buf), data.length);
      buf.write(data);
      out.write(buf.toByteArray());
      out.flush();
   }

   private static void writeString(DataOutputStream out, String s) throws IOException {
      byte[] b = s.getBytes("UTF-8");
      writeVarInt(out, b.length);
      out.write(b);
   }

   private static void writeVarInt(DataOutputStream out, int value) throws IOException {
      while ((value & ~0x7F) != 0) {
         out.writeByte((value & 0x7F) | 0x80);
         value >>>= 7;
      }
      out.writeByte(value);
   }

   private static int readVarInt(DataInputStream in) throws IOException {
      int result = 0;
      int shift = 0;
      while (true) {
         int b = in.readByte();
         result |= (b & 0x7F) << shift;
         if ((b & 0x80) == 0) {
            break;
         }
         shift += 7;
         if (shift >= 35) {
            throw new IOException("VarInt too big");
         }
      }
      return result;
   }
}
