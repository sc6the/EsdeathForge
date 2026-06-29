package net.labymod.addons.voicechat.client;

import java.beans.ConstructorProperties;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import net.labymod.opus.OpusCodecOptions;

public class AudioConfig {
   private AudioConfig.Format opusMicrophoneFormat = AudioConfig.Format.fallback();
   private OpusCodecOptions opusCodec;
   private AudioFormat audioFormat = null;
   private long screamProtectionDuration;

   public AudioFormat getAudioFormat() {
      return this.audioFormat != null ? this.audioFormat : (this.audioFormat = this.opusMicrophoneFormat.build());
   }

   public AudioConfig.Format getOpusMicrophoneFormat() {
      return this.opusMicrophoneFormat;
   }

   public OpusCodecOptions getOpusCodec() {
      return this.opusCodec;
   }

   public long getScreamProtectionDuration() {
      return this.screamProtectionDuration;
   }

   public AudioConfig() {
   }

   @ConstructorProperties({"opusMicrophoneFormat", "opusCodec", "audioFormat", "screamProtectionDuration"})
   public AudioConfig(AudioConfig.Format opusMicrophoneFormat, OpusCodecOptions opusCodec, AudioFormat audioFormat, long screamProtectionDuration) {
      this.opusMicrophoneFormat = opusMicrophoneFormat;
      this.opusCodec = opusCodec;
      this.audioFormat = audioFormat;
      this.screamProtectionDuration = screamProtectionDuration;
   }

   public void setOpusMicrophoneFormat(AudioConfig.Format opusMicrophoneFormat) {
      this.opusMicrophoneFormat = opusMicrophoneFormat;
   }

   public static class Format {
      private String encoding;
      private float sampleRate;
      private int sampleSizeInBits;
      private int channels;
      private int frameSize;
      private int frameRate;
      private boolean bigEndian;

      public static AudioConfig.Format fallback() {
         return new AudioConfig.Format("PCM_SIGNED", 48000.0F, 16, 1, 2, 48000, false);
      }

      public AudioFormat build() {
         return new AudioFormat(
            new Encoding(this.encoding), this.sampleRate, this.sampleSizeInBits, this.channels, this.frameSize, (float)this.frameRate, this.bigEndian
         );
      }

      public String getEncoding() {
         return this.encoding;
      }

      public float getSampleRate() {
         return this.sampleRate;
      }

      public int getSampleSizeInBits() {
         return this.sampleSizeInBits;
      }

      public int getChannels() {
         return this.channels;
      }

      public int getFrameSize() {
         return this.frameSize;
      }

      public int getFrameRate() {
         return this.frameRate;
      }

      public boolean isBigEndian() {
         return this.bigEndian;
      }

      public Format() {
      }

      @ConstructorProperties({"encoding", "sampleRate", "sampleSizeInBits", "channels", "frameSize", "frameRate", "bigEndian"})
      public Format(String encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, int frameRate, boolean bigEndian) {
         this.encoding = encoding;
         this.sampleRate = sampleRate;
         this.sampleSizeInBits = sampleSizeInBits;
         this.channels = channels;
         this.frameSize = frameSize;
         this.frameRate = frameRate;
         this.bigEndian = bigEndian;
      }
   }
}
