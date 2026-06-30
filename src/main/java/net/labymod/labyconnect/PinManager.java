package net.labymod.labyconnect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.labymod.main.Source;

public class PinManager {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private Map<UUID, PinManager.Pin> pins = new HashMap<>();

   private PinManager() {
   }

   public void invalidatePinOf(UUID uuid) {
      this.pins.remove(uuid);
   }

   public void update(UUID uuid, String pin, long expiresAt) {
      this.pins.put(uuid, new PinManager.Pin(pin, expiresAt));

      try {
         FileWriter writer = new FileWriter(Source.FILE_PINS);
         GSON.toJson(this, writer);
         writer.close();
      } catch (Exception var6) {
         var6.printStackTrace();
      }
   }

   public boolean hasValidPin(UUID uuid) {
      return this.getValidPinOf(uuid) != null;
   }

   public String getValidPinOf(UUID uuid) {
      PinManager.Pin pin = this.pins.get(uuid);
      return pin != null && !pin.isExpired() ? pin.getPin() : null;
   }

   public static PinManager load() {
      if (Source.FILE_PINS.exists()) {
         try {
            JsonReader reader = new JsonReader(new FileReader(Source.FILE_PINS));
            Throwable var1 = null;

            PinManager var2;
            try {
               var2 = (PinManager)GSON.fromJson(reader, PinManager.class);
            } catch (Throwable var12) {
               var1 = var12;
               throw var12;
            } finally {
               if (reader != null) {
                  if (var1 != null) {
                     try {
                        reader.close();
                     } catch (Throwable var11) {
                        var1.addSuppressed(var11);
                     }
                  } else {
                     reader.close();
                  }
               }
            }

            return var2;
         } catch (Exception var14) {
            var14.printStackTrace();
         }
      }

      return new PinManager();
   }

   public static class Pin {
      private final String pin;
      private final long expiresAt;

      public Pin(String pin, long expiresAt) {
         this.pin = pin;
         this.expiresAt = expiresAt;
      }

      public String getPin() {
         return this.pin;
      }

      public boolean isExpired() {
         return System.currentTimeMillis() > this.expiresAt;
      }
   }
}
