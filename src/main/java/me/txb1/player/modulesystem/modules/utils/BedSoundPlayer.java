package me.txb1.player.modulesystem.modules.utils;

import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

// Plays a user-chosen WAV / OGG / MP3 file on a background thread, cutting it off after a set number
// of seconds. WAV is decoded by the JDK; OGG and MP3 use the bundled javazoom readers/converters
// (instantiated directly so we don't depend on the javax SPI service files being merged).
public final class BedSoundPlayer {
   private BedSoundPlayer() {
   }

   public static void play(String path, final int cutoffSeconds, final int fadeMs) {
      if (path == null || path.trim().isEmpty()) {
         return;
      }
      final File f = new File(path.trim());
      if (!f.isFile()) {
         return;
      }
      Thread t = new Thread(new Runnable() {
         @Override
         public void run() {
            SourceDataLine line = null;
            AudioInputStream pcmStream = null;
            try {
               AudioInputStream encoded = open(f);
               if (encoded == null) {
                  return;
               }
               AudioFormat base = encoded.getFormat();
               AudioFormat pcm = new AudioFormat(
                  AudioFormat.Encoding.PCM_SIGNED, base.getSampleRate(), 16,
                  base.getChannels(), base.getChannels() * 2, base.getSampleRate(), false);
               pcmStream = decode(f, pcm, encoded);
               line = AudioSystem.getSourceDataLine(pcm);
               line.open(pcm);
               line.start();

               // playback position -> time, so we can apply the fade envelope per sample
               float bytesPerSec = pcm.getSampleRate() * pcm.getFrameSize();
               long cutoffMs = Math.max(1, cutoffSeconds) * 1000L;
               int fade = Math.max(0, fadeMs);
               long totalBytes = 0;

               byte[] buf = new byte[4096];
               int n;
               while ((n = pcmStream.read(buf, 0, buf.length)) != -1) {
                  double tMs = totalBytes / bytesPerSec * 1000.0;
                  if (tMs >= cutoffMs) {
                     line.flush(); // cut off: discard whatever is still buffered
                     break;
                  }
                  applyFade(buf, n, totalBytes, bytesPerSec, cutoffMs, fade);
                  line.write(buf, 0, n);
                  totalBytes += n;
               }
               if (totalBytes / bytesPerSec * 1000.0 < cutoffMs) {
                  line.drain(); // finished naturally before the cutoff
               }
            } catch (Throwable ignored) {
            } finally {
               try {
                  if (line != null) {
                     line.stop();
                     line.close();
                  }
               } catch (Throwable ignored) {
               }
               try {
                  if (pcmStream != null) {
                     pcmStream.close();
                  }
               } catch (Throwable ignored) {
               }
            }
         }
      }, "BedBreakSound");
      t.setDaemon(true);
      t.start();
   }

   // scale 16-bit little-endian PCM samples by the fade envelope (fade in at the start, fade out
   // toward the cutoff). totalBytes = bytes already written before this buffer.
   private static void applyFade(byte[] buf, int len, long totalBytes, float bytesPerSec, long cutoffMs, int fade) {
      if (fade <= 0) {
         return;
      }
      for (int i = 0; i + 1 < len; i += 2) {
         double ms = (totalBytes + i) / bytesPerSec * 1000.0;
         float g = gain(ms, cutoffMs, fade);
         if (g < 1.0F) {
            short s = (short) ((buf[i + 1] << 8) | (buf[i] & 0xFF));
            int v = Math.max(-32768, Math.min(32767, (int) (s * g)));
            buf[i] = (byte) (v & 0xFF);
            buf[i + 1] = (byte) ((v >> 8) & 0xFF);
         }
      }
   }

   private static float gain(double ms, long cutoffMs, int fade) {
      if (ms < fade) {
         return (float) (ms / fade); // fade in
      }
      if (ms > cutoffMs - fade) {
         return (float) Math.max(0.0, (cutoffMs - ms) / fade); // fade out toward the cutoff
      }
      return 1.0F;
   }

   private static AudioInputStream open(File f) throws Exception {
      String n = f.getName().toLowerCase();
      if (n.endsWith(".ogg")) {
         return new javazoom.spi.vorbis.sampled.file.VorbisAudioFileReader().getAudioInputStream(f);
      }
      if (n.endsWith(".mp3")) {
         return new javazoom.spi.mpeg.sampled.file.MpegAudioFileReader().getAudioInputStream(f);
      }
      return AudioSystem.getAudioInputStream(f); // wav (and anything the JDK supports)
   }

   private static AudioInputStream decode(File f, AudioFormat pcm, AudioInputStream encoded) {
      String n = f.getName().toLowerCase();
      if (n.endsWith(".ogg")) {
         return new javazoom.spi.vorbis.sampled.convert.VorbisFormatConversionProvider()
            .getAudioInputStream(pcm, encoded);
      }
      if (n.endsWith(".mp3")) {
         return new javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider()
            .getAudioInputStream(pcm, encoded);
      }
      return AudioSystem.getAudioInputStream(pcm, encoded);
   }
}
