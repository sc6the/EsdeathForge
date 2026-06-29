package net.labymod.addons.voicechat.audio.opus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.labymod.opus.OpusCodec;
import net.labymod.opus.OpusCodecOptions;
import net.labymod.utils.Consumer;

public class OpusCodecManager {
   private OpusCodecOptions options;
   private ExternalServiceConnector externalServiceConnector;
   protected EnumOpusError status = EnumOpusError.NOT_INITIALIZED;
   private boolean minecraftNativesLoaded = false;
   private boolean useExternalApi = false;
   private Map<UUID, OpusCodec> opusCodecs = new HashMap<>();

   public void init(OpusCodecOptions config, boolean useExternalApi, final Consumer<EnumOpusError> consumer) {
      this.useExternalApi = useExternalApi;
      this.options = config;
      if (useExternalApi) {
         if (this.externalServiceConnector != null && this.externalServiceConnector.isServiceAvailable()) {
            this.externalServiceConnector.destroy();
         }

         this.externalServiceConnector = new ExternalServiceConnector(this, config, new Consumer<EnumOpusError>() {
            public void accept(EnumOpusError status) {
               OpusCodecManager.this.status = status;
               if (status == EnumOpusError.OK) {
                  OpusCodecManager.log("Opus initialized as external instance!");
               } else {
                  OpusCodecManager.log("Could not create opus as external instance: " + status.name());
               }

               consumer.accept(status);
            }
         });
      } else {
         try {
            if (this.status == EnumOpusError.NOT_INITIALIZED) {
               this.loadNatives();
               log("Opus initialized for minecraft instance!");
               consumer.accept(this.status = EnumOpusError.OK);
            }
         } catch (Throwable var5) {
            var5.printStackTrace();
            consumer.accept(this.status = EnumOpusError.LOAD_NATIVES_IN_MC_FAILED);
         }
      }
   }

   private void loadNatives() throws Throwable {
      if (this.minecraftNativesLoaded) {
         log("Minecraft natives already loaded, skip..");
      } else {
         log("Load opus natives for minecraft instance");
         OpusCodec.setupWithTemporaryFolder();
         this.minecraftNativesLoaded = true;
      }
   }

   public void convert(UUID uuid, EnumOpusCodecDirection direction, byte[] input, Consumer<byte[]> consumer) {
      if (this.status == EnumOpusError.OK) {
         if (this.useExternalApi && this.externalServiceConnector != null) {
            byte[] output = this.externalServiceConnector.sendConvertTask(uuid, direction.ordinal(), input);
            if (output.length != 0) {
               consumer.accept(output);
            }
         } else {
            OpusCodec opusCodec = this.getOpusCodec(uuid);
            switch (direction) {
               case ENCODE:
                  consumer.accept(opusCodec.encodeFrame(input));
                  break;
               case DECODE:
                  consumer.accept(opusCodec.decodeFrame(input));
            }
         }
      }
   }

   private OpusCodec getOpusCodec(UUID uuid) {
      OpusCodec opusCodec = this.opusCodecs.get(uuid);
      if (opusCodec == null) {
         log("Create opus instance for " + uuid.toString());
         opusCodec = OpusCodec.createByOptions(this.options);
         this.opusCodecs.put(uuid, opusCodec);
      }

      return opusCodec;
   }

   public void destroyCodecs() {
      log("Destroy " + this.opusCodecs.size() + " codec instances");

      for (Entry<UUID, OpusCodec> entry : this.opusCodecs.entrySet()) {
         entry.getValue().destroy();
      }

      this.opusCodecs.clear();
   }

   public void reinitialize(boolean useExternalApi) {
      this.status = EnumOpusError.NOT_INITIALIZED;
      this.useExternalApi = useExternalApi;
      this.destroyCodecs();
      this.init(this.options, useExternalApi, new Consumer<EnumOpusError>() {
         public void accept(EnumOpusError status) {
            OpusCodecManager.this.status = status;
            OpusCodecManager.log("Reinitialize: " + status.name());
         }
      });
   }

   public int getBufferInSize() {
      return this.options.getFrameSize() * this.options.getChannels() * 2;
   }

   public static void log(String message) {
      System.out.println("[VoiceChat] [Opus] " + message);
   }

   public OpusCodecOptions getOptions() {
      return this.options;
   }

   public ExternalServiceConnector getExternalServiceConnector() {
      return this.externalServiceConnector;
   }

   public EnumOpusError getStatus() {
      return this.status;
   }

   public boolean isMinecraftNativesLoaded() {
      return this.minecraftNativesLoaded;
   }

   public boolean isUseExternalApi() {
      return this.useExternalApi;
   }

   public Map<UUID, OpusCodec> getOpusCodecs() {
      return this.opusCodecs;
   }
}
