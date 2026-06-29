package net.labymod.opus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.NoSuchElementException;

public class OpusCodec {
   private final OpusCodecOptions opusOptions;
   private boolean encoderInitialized = false;
   private boolean decoderInitialized = false;
   private long encoderState;
   private long decoderState;

   private OpusCodec(OpusCodecOptions opusOptions) {
      this.opusOptions = opusOptions;
   }

   public int getFrameSize() {
      return this.opusOptions.getFrameSize();
   }

   public int getSampleRate() {
      return this.opusOptions.getSampleRate();
   }

   public int getChannels() {
      return this.opusOptions.getChannels();
   }

   public int getBitrate() {
      return this.opusOptions.getBitrate();
   }

   public int getMaxFrameSize() {
      return this.opusOptions.getMaxFrameSize();
   }

   public int getMaxPacketSize() {
      return this.opusOptions.getMaxPacketSize();
   }

   public static OpusCodec createDefault() {
      return newBuilder().build();
   }

   public static OpusCodec createByOptions(OpusCodecOptions opusCodecOptions) {
      return new OpusCodec(opusCodecOptions);
   }

   public static OpusCodec.Builder newBuilder() {
      return new OpusCodec.Builder();
   }

   public byte[] encodeFrame(byte[] bytes) {
      return this.encodeFrame(bytes, 0, bytes.length);
   }

   public byte[] encodeFrame(byte[] bytes, int offset, int length) {
      if (length != this.getChannels() * this.getFrameSize() * 2) {
         throw new IllegalArgumentException(
            String.format(
               "data length must be == CHANNELS * FRAMESIZE * 2 (%d bytes) but is %d bytes", this.getChannels() * this.getFrameSize() * 2, bytes.length
            )
         );
      } else {
         this.ensureEncoderExistence();
         return this.encodeFrame(this.encoderState, bytes, offset, length);
      }
   }

   private native byte[] encodeFrame(long var1, byte[] var3, int var4, int var5);

   public byte[] decodeFrame(byte[] bytes) {
      this.ensureDecoderExistence();
      return this.decodeFrame(this.decoderState, bytes);
   }

   private native byte[] decodeFrame(long var1, byte[] var3);

   private void ensureEncoderExistence() {
      if (!this.encoderInitialized) {
         this.encoderState = this.createEncoder(this.opusOptions);
         this.encoderInitialized = true;
      }
   }

   private native long createEncoder(OpusCodecOptions var1);

   private void ensureDecoderExistence() {
      if (!this.decoderInitialized) {
         this.decoderState = this.createDecoder(this.opusOptions);
         this.decoderInitialized = true;
      }
   }

   private native long createDecoder(OpusCodecOptions var1);

   public void destroy() {
      if (this.encoderInitialized) {
         this.destroyEncoder(this.encoderState);
      }

      if (this.decoderInitialized) {
         this.destroyDecoder(this.decoderState);
      }

      this.encoderInitialized = false;
      this.decoderInitialized = false;
   }

   private native void destroyEncoder(long var1);

   private native void destroyDecoder(long var1);

   private static String getNativeLibraryName(boolean allowArm) {
      String bitnessArch = System.getProperty("os.arch").toLowerCase();
      String bitnessDataModel = System.getProperty("sun.arch.data.model", null);
      boolean is64bit = bitnessArch.contains("64") || bitnessDataModel != null && bitnessDataModel.contains("64");
      String arch = bitnessArch.startsWith("aarch") && allowArm ? "arm" : "";
      if (is64bit) {
         String library64 = processLibraryName("opus-jni-native-" + arch + "64");
         if (hasResource("/native-binaries/" + library64)) {
            return library64;
         }
      } else {
         String library32 = processLibraryName("opus-jni-native-" + arch + "32");
         if (hasResource("/native-binaries/" + library32)) {
            return library32;
         }
      }

      String library = processLibraryName("opus-jni-native");
      if (!hasResource("/native-binaries/" + library)) {
         throw new NoSuchElementException("No binary for the current system found, even after trying bit neutral names");
      } else {
         return library;
      }
   }

   private static String processLibraryName(String library) {
      String systemName = System.getProperty("os.name", "bare-metal?").toLowerCase();
      if (systemName.contains("nux") || systemName.contains("nix")) {
         return "lib" + library + ".so";
      } else if (systemName.contains("mac")) {
         return "lib" + library + ".dylib";
      } else if (systemName.contains("windows")) {
         return library + ".dll";
      } else {
         throw new NoSuchElementException("No native library for system " + systemName);
      }
   }

   private static boolean hasResource(String resource) {
      return OpusCodec.class.getResource(resource) != null;
   }

   public static void loadNative(File directory) throws IOException {
      loadNative(directory, true);
   }

   public static void loadNative(File directory, boolean allowArm) throws IOException {
      String nativeLibraryName = getNativeLibraryName(allowArm);
      InputStream source = OpusCodec.class.getResourceAsStream("/native-binaries/" + nativeLibraryName);
      if (source == null) {
         throw new IOException("Could not find native library " + nativeLibraryName);
      } else {
         Path destination = directory.toPath().resolve(nativeLibraryName);
         Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
         System.load(new File(directory, nativeLibraryName).getAbsolutePath());
      }
   }

   public static void setupWithTemporaryFolder() throws IOException {
      File temporaryDir = Files.createTempDirectory("opus-jni").toFile();
      temporaryDir.deleteOnExit();

      try {
         loadNative(temporaryDir);
      } catch (UnsatisfiedLinkError var2) {
         var2.printStackTrace();
         loadNative(temporaryDir, false);
      }
   }

   public static class Builder {
      private int frameSize = 960;
      private int sampleRate = 48000;
      private int channels = 1;
      private int bitrate = 64000;
      private int maxFrameSize = 5760;
      private int maxPacketSize = 3828;

      private Builder() {
      }

      public int getFrameSize() {
         return this.frameSize;
      }

      public OpusCodec.Builder withFrameSize(int frameSize) {
         this.frameSize = frameSize;
         return this;
      }

      public int getSampleRate() {
         return this.sampleRate;
      }

      public OpusCodec.Builder withSampleRate(int sampleRate) {
         this.sampleRate = sampleRate;
         return this;
      }

      public int getChannels() {
         return this.channels;
      }

      public OpusCodec.Builder withChannels(int channels) {
         this.channels = channels;
         return this;
      }

      public int getBitrate() {
         return this.bitrate;
      }

      public OpusCodec.Builder withBitrate(int bitrate) {
         this.bitrate = bitrate;
         return this;
      }

      public int getMaxFrameSize() {
         return this.maxFrameSize;
      }

      public OpusCodec.Builder withMaxFrameSize(int maxFrameSize) {
         this.maxFrameSize = maxFrameSize;
         return this;
      }

      public int getMaxPacketSize() {
         return this.maxPacketSize;
      }

      public OpusCodec.Builder withMaxPacketSize(int maxPacketSize) {
         this.maxPacketSize = maxPacketSize;
         return this;
      }

      public OpusCodec build() {
         return new OpusCodec(OpusCodecOptions.of(this.frameSize, this.sampleRate, this.channels, this.bitrate, this.maxFrameSize, this.maxPacketSize));
      }
   }
}
