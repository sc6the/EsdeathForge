package eu.firedata.firedb.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public class Serializer {

   public Object getObjectFromString(String var1) {
      byte[] var2 = this.stringToByteArray(var1);
      return this.byteArrayToObject(var2);
   }

   private Object byteArrayToObject(byte[] var1) {
      try {
         ByteArrayInputStream var2 = new ByteArrayInputStream(var1);
         ObjectInputStream var3 = new ObjectInputStream(var2);
         return var3.readObject();
      } catch (Exception var4) {
         return null;
      }
   }

   private String byteArrayToString(byte[] var1) {
      return Arrays.toString(var1);
   }

   private byte[] objToByteArray(Object var1) {
      try {
         ByteArrayOutputStream var2 = new ByteArrayOutputStream();
         ObjectOutputStream var3 = new ObjectOutputStream(var2);
         var3.writeObject(var1);
         return var2.toByteArray();
      } catch (Exception var4) {
         return null;
      }
   }

   public String getStringFromObject(Object var1) {
      byte[] var2 = this.objToByteArray(var1);
      return this.byteArrayToString(var2);
   }

   private byte[] stringToByteArray(String var1) {
      var1 = var1.replace("]", "").replace("[", "");
      String[] var2 = var1.split(",");
      byte[] var3 = new byte[var2.length];
      int var4 = 0;

      while (((var4) < (var3.length))) {
         var3[var4] = Byte.parseByte(var2[var4].trim());
         var4++;
         
      }

      return var3;
   }
}
