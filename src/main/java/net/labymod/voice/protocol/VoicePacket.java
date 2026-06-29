package net.labymod.voice.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.labymod.voice.protocol.handler.VoicePacketHandler;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.EncryptType;

public abstract class VoicePacket<T extends VoicePacketHandler> {
   private final EncryptType encryptType;
   private final ConnectionState allowedState;

   public abstract void write(ByteArrayOutputStream var1) throws IOException;

   public abstract void read(ByteArrayInputStream var1, int var2) throws IOException;

   public abstract void handle(T var1);

   public byte[] getFirewallIdentifier() throws IOException {
      return null;
   }

   public static long readVarLong(InputStream buf) throws IOException {
      long i = 0L;
      int j = 0;

      byte b0;
      do {
         b0 = (byte)buf.read();
         i |= (long)(b0 & 127) << j++ * 7;
         if (j > 10) {
            throw new RuntimeException("VarLong too big");
         }
      } while ((b0 & 128) == 128);

      return i;
   }

   public static void writeVarLong(long value, OutputStream buf) throws IOException {
      while ((value & -128L) != 0L) {
         buf.write((int)(value & 127L) | 128);
         value >>>= 7;
      }

      buf.write((int)value);
   }

   public static void writeUUID(UUID uuid, OutputStream buf) throws IOException {
      writeLong(uuid == null ? 0L : uuid.getMostSignificantBits(), buf);
      writeLong(uuid == null ? 0L : uuid.getLeastSignificantBits(), buf);
   }

   public static UUID readUUID(InputStream buf) throws IOException {
      return new UUID(readLong(buf), readLong(buf));
   }

   public static void writeEnum(Enum<?> e, OutputStream buf) throws IOException {
      int ord = e != null ? e.ordinal() : -1;
      buf.write(ord);
   }

   public static <T> T readEnum(InputStream buf, T[] values) throws IOException {
      int ord = buf.read();
      if (ord == -1) {
         return null;
      } else if (values.length <= ord) {
         throw new RuntimeException("No Enum found");
      } else {
         return values[ord];
      }
   }

   public static void writeString(String s, OutputStream buf) throws IOException {
      writeString(s, StandardCharsets.UTF_8, buf);
   }

   public static void writeString(String s, Charset charset, OutputStream buf) throws IOException {
      if (s == null) {
         s = "";
      }

      byte[] b = s.getBytes(charset);
      buf.write(intToBytes(b.length));
      buf.write(b);
   }

   public static String readString(InputStream buf) throws IOException {
      return readString(StandardCharsets.UTF_8, buf);
   }

   public static String readString(Charset charset, InputStream buf) throws IOException {
      byte[] lenData = new byte[4];
      buf.read(lenData);
      int len = byteArrayToInt(lenData);
      byte[] b = new byte[len];
      buf.read(b);
      String s = new String(b, charset);
      return s.equals("") ? null : s;
   }

   public static void writeInt(int i, OutputStream buf) throws IOException {
      buf.write(intToBytes(i));
   }

   public static int readInt(InputStream buf) throws IOException {
      byte[] data = new byte[4];
      buf.read(data);
      return byteArrayToInt(data);
   }

   public static void writeLong(long value, OutputStream buf) throws IOException {
      byte[] bytes = new byte[8];
      int length = bytes.length;

      for (int i = 0; i < length; i++) {
         bytes[length - i - 1] = (byte)((int)(value & 255L));
         value >>= 8;
      }

      buf.write(bytes);
   }

   public static long readLong(InputStream buf) throws IOException {
      byte[] data = new byte[8];
      buf.read(data);
      long value = 0L;

      for (byte b : data) {
         value = (value << 8) + (long)(b & 255);
      }

      return value;
   }

   public static byte[] intToBytes(int value) {
      byte[] bytes = new byte[4];
      int length = bytes.length;

      for (int i = 0; i < length; i++) {
         bytes[length - i - 1] = (byte)(value & 0xFF);
         value >>= 8;
      }

      return bytes;
   }

   public static int byteArrayToInt(byte[] data) {
      return data != null && data.length == 4 ? (0xFF & data[0]) << 24 | (0xFF & data[1]) << 16 | (0xFF & data[2]) << 8 | 0xFF & data[3] : 0;
   }

   public VoicePacket(EncryptType encryptType, ConnectionState allowedState) {
      this.encryptType = encryptType;
      this.allowedState = allowedState;
   }

   public EncryptType getEncryptType() {
      return this.encryptType;
   }

   public ConnectionState getAllowedState() {
      return this.allowedState;
   }
}
