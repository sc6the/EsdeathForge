package net.labymod.addons.voicechat.audio;

import net.labymod.addons.voicechat.audio.surround.CompressionUpdater;

public class AudioModifier {
   public static byte[] adjustVolume(byte[] audioSamples, float volume) {
      byte[] array = new byte[audioSamples.length];

      for (int i = 0; i < array.length; i += 2) {
         short buf1 = (short)audioSamples[i + 1];
         short buf2 = (short)audioSamples[i];
         buf1 = (short)((buf1 & 255) << 8);
         buf2 = (short)(buf2 & 255);
         short res = (short)(buf1 | buf2);
         res = (short)((int)((float)res * volume));
         array[i] = (byte)res;
         array[i + 1] = (byte)(res >> 8);
      }

      return array;
   }

   public static int calculateRMSLevel(byte[] audioData) {
      long lSum = 0L;

      for (int i = 0; i < audioData.length; i++) {
         lSum += (long)audioData[i];
      }

      double dAvg = (double)(lSum / (long)audioData.length);
      double sumMeanSquare = 0.0;

      for (int j = 0; j < audioData.length; j++) {
         sumMeanSquare += Math.pow((double)audioData[j] - dAvg, 2.0);
      }

      double averageMeanSquare = sumMeanSquare / (double)audioData.length;
      return (int)averageMeanSquare;
   }

   public static float[] analyseAudio(byte[] audioData) {
      float sum = 0.0F;
      int length = 0;
      float min = 1000000.0F;
      float max = -1000000.0F;

      for (int i = 0; i < audioData.length; i += 2) {
         float data = (float)(audioData[i] & 255 | audioData[i + 1] << 8) / 32768.0F;
         if (data > max) {
            max = data;
         }

         if (data < min) {
            min = data;
         }

         sum += data < 0.0F ? -data : data;
         length++;
      }

      float average = sum / (float)length;
      return new float[]{average, min, max};
   }

   public static byte[] compress(byte[] chunk, int targetLevel, int swapLimit, CompressionUpdater updater) {
      int max = 0;
      int min = 0;
      boolean lastPositive = true;
      int swaps = 0;

      for (int i = 0; i < chunk.length; i += 2) {
         int data = chunk[i] & 255 | chunk[i + 1] << 8;
         max = Math.max(data, max);
         min = Math.min(data, min);
         if (data > 0) {
            if (!lastPositive) {
               lastPositive = true;
               swaps++;
            }
         } else if (lastPositive) {
            lastPositive = false;
            swaps++;
         }
      }

      float fPositive = (float)targetLevel / (float)max;
      float fNegative = (float)targetLevel / (float)(-min);
      float factor = Math.max(fPositive, fNegative);
      boolean adjustVolume = factor < 1.0F;
      boolean highSwapRate = swaps > swapLimit;
      if (!adjustVolume) {
         return chunk;
      } else {
         if (updater != null) {
            updater.currentState(highSwapRate && adjustVolume);
         }

         byte[] modifiedChunk = new byte[chunk.length];

         for (int ix = 0; ix < chunk.length; ix += 2) {
            float data = (float)(chunk[ix] & 255 | chunk[ix + 1] << 8) / 32768.0F;
            data *= factor;
            int modifiedData = Math.round(data * 32768.0F);
            modifiedChunk[ix] = (byte)(modifiedData & 0xFF);
            modifiedChunk[ix + 1] = (byte)(modifiedData >> 8 & 0xFF);
         }

         return modifiedChunk;
      }
   }
}
