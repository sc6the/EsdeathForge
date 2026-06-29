package net.labymod.addons.voicechat.audio.opus;

import com.google.gson.Gson;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import net.labymod.addons.voicechat.VoiceChat;
import net.labymod.main.LabyMod;
import net.labymod.opus.OpusCodecOptions;
import net.labymod.support.util.Debug;
import net.labymod.utils.Consumer;
import net.labymod.utils.ModColor;

public class ExternalServiceConnector {
   private static final String RESOURCE_EXTERNAL_JAR = "/ExternalOpusService.jar";
   private static final File TEMP_EXTERNAL_JAR = new File(System.getProperty("java.io.tmpdir"), "external-opus-service");
   private static final int MAX_RETRY_AMOUNT = 5;
   private Executor executor = Executors.newFixedThreadPool(1);
   private OpusCodecManager manager;
   private OpusCodecOptions options;
   private ServiceClient service;
   private boolean serviceStartupState = false;
   private int retryAmount = 0;

   public ExternalServiceConnector(OpusCodecManager manager, OpusCodecOptions options, final Consumer<EnumOpusError> callback) {
      this.manager = manager;
      this.options = options;
      this.serviceStartupState = true;
      this.executor.execute(new Runnable() {
         @Override
         public void run() {
            try {
               EnumOpusError status = ExternalServiceConnector.this.extractService();
               if (status != EnumOpusError.OK) {
                  callback.accept(status);
                  return;
               }

               status = ExternalServiceConnector.this.restartService();
               if (status != EnumOpusError.OK) {
                  callback.accept(status);
                  return;
               }

               callback.accept(EnumOpusError.OK);
            } catch (Throwable var2) {
               var2.printStackTrace();
               callback.accept(EnumOpusError.UNKNOWN_EXCEPTION);
            }
         }
      });
   }

   public synchronized byte[] sendConvertTask(UUID uuid, int directionId, byte[] input) {
      if (this.provideAccessibility()) {
         try {
            DataOutputStream dos = this.service.getDataOutputStream();
            dos.writeShort(1337);
            dos.writeLong(uuid.getMostSignificantBits());
            dos.writeLong(uuid.getLeastSignificantBits());
            dos.writeByte(directionId);
            dos.writeInt(input.length);
            dos.write(input);
            dos.writeShort(420);
            dos.flush();
            DataInputStream dis = this.service.getDataInputStream();
            if (dis.readShort() != 1337) {
               OpusCodecManager.log("Parity 1 failed");
               this.manager.status = this.restartService();
               return new byte[0];
            }

            byte[] output = new byte[dis.readInt()];
            dis.read(output);
            if (dis.readShort() != 420) {
               OpusCodecManager.log("Parity 2 failed");
               this.manager.status = this.restartService();
               return new byte[0];
            }

            this.retryAmount = 0;
            return output;
         } catch (Throwable var8) {
            try {
               this.manager.status = this.restartService();
            } catch (Throwable var7) {
               var7.printStackTrace();
            }

            var8.printStackTrace();
         }
      }

      return new byte[0];
   }

   public boolean isServiceAvailable() {
      return this.service != null && this.service.isRunning();
   }

   private EnumOpusError restartService() throws Throwable {
      if (this.retryAmount > 5) {
         OpusCodecManager.log("Max amount of retries reached! I'm giving up.");
         return EnumOpusError.MAX_RETRY_REACHED;
      } else {
         OpusCodecManager.log("Restart service");
         this.serviceStartupState = true;
         this.retryAmount++;
         if (this.service != null && this.service.isRunning()) {
            OpusCodecManager.log("Destroy current running service because of opus service restart");
            this.service.destroy();
         }

         this.service = this.createNewService();
         this.serviceStartupState = false;
         boolean success = this.service.isRunning();
         if (success) {
            OpusCodecManager.log("External opus service restartet and connected!");
            if (Debug.isActive() && LabyMod.getInstance().isInGame()) {
               LabyMod.getInstance().displayMessageInChat(ModColor.cl("e") + "External voice chat service recreated.");
            }
         } else {
            OpusCodecManager.log("Could not restart external service. Process is still offline!");
         }

         return success ? EnumOpusError.OK : EnumOpusError.SERVICE_OFFLINE;
      }
   }

   private void restartServiceAsync() {
      this.executor.execute(new Runnable() {
         @Override
         public void run() {
            try {
               ExternalServiceConnector.this.manager.status = ExternalServiceConnector.this.restartService();
            } catch (Throwable var2) {
               var2.printStackTrace();
            }
         }
      });
   }

   private EnumOpusError extractService() throws Throwable {
      InputStream inputStream = VoiceChat.class.getResourceAsStream("/ExternalOpusService.jar");
      if (inputStream == null) {
         OpusCodecManager.log("External service jar not found in resources");
         return EnumOpusError.SERVICE_RESOURCE_NOT_FOUND;
      } else {
         try {
            Files.copy(inputStream, TEMP_EXTERNAL_JAR.toPath(), StandardCopyOption.REPLACE_EXISTING);
         } catch (FileSystemException var3) {
            OpusCodecManager.log("Can't replace external service jar: " + TEMP_EXTERNAL_JAR.getAbsolutePath() + " (" + var3.getMessage() + ")");
         }

         if (!TEMP_EXTERNAL_JAR.exists()) {
            OpusCodecManager.log("Can't copy external service jar to temp folder " + TEMP_EXTERNAL_JAR.getAbsolutePath());
            return EnumOpusError.EXTRACT_SERVICE_FAILED;
         } else {
            return EnumOpusError.OK;
         }
      }
   }

   private ServiceClient createNewService() throws Throwable {
      List<String> command = this.generateCommand(TEMP_EXTERNAL_JAR);
      return new ServiceClient(command);
   }

   private List<String> generateCommand(File temp) {
      String javaHome = System.getProperty("java.home");
      String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
      List<String> command = new LinkedList<>();
      command.add(javaBin);
      command.add("-jar");
      command.add(temp.getAbsolutePath());
      String json = new Gson().toJson(this.options);
      command.add(json);
      StringBuilder debug = new StringBuilder();

      for (String cmd : command) {
         debug.append(cmd + " ");
      }

      OpusCodecManager.log("Execute: " + debug.toString());
      return command;
   }

   private boolean provideAccessibility() {
      if (this.isServiceAvailable()) {
         return true;
      } else {
         if (!this.serviceStartupState && this.manager.status != EnumOpusError.MAX_RETRY_REACHED) {
            OpusCodecManager.log("Could not provide accessibility, restart service..");
            this.restartServiceAsync();
         }

         return false;
      }
   }

   public void destroy() {
      this.serviceStartupState = true;
      if (this.service != null && this.service.isRunning()) {
         try {
            this.service.destroy();
         } catch (Throwable var2) {
            var2.printStackTrace();
         }
      }
   }
}
