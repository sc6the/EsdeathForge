package net.labymod.addons.voicechat.audio.surround;

import net.labymod.addons.voicechat.client.DefaultVoiceClientListener;
import net.labymod.core.LabyModCore;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.opengl.GL11;
import paulscode.sound.Vector3D;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class UserStream implements CompressionUpdater {
   public static final long TIME_OUT = 500L;
   private UUID uuid;
   private long lastUpdated;
   private float[] visualBuffer;
   private boolean visualOdd;
   private long lastVisualChanged;
   private boolean initialized = false;
   private int sourceId = -1;
   private int inQueue = 0;
   private boolean started = false;
   private HashSet<Integer> allocatedBuffers = new HashSet<>();
   private Queue<byte[]> queue = new LinkedList<>();
   private int highSwapRateCount = 0;
   private DefaultVoiceClientListener voiceAudioListener;

   public UserStream(UUID uuid) {
      this.uuid = uuid;
      this.visualBuffer = new float[0];
      this.keepAlive();
   }

   public void init(DefaultVoiceClientListener voiceAudioListener) {
      this.initialized = true;
      this.voiceAudioListener = voiceAudioListener;
   }

   public void writeVisualBuffer(byte[] data) {
      int len = data.length;
      if (this.visualBuffer.length != len) {
         this.visualBuffer = new float[len];
      }
      if (len > 0) {
         int offset = this.visualOdd ? len / 2 : 0;
         for (int i = 0; i < len; i += 2) {
            this.visualBuffer[i / 2 + offset] = (float)(data[i] & 255 | data[i + 1] << 8) / 32768.0F;
         }
         this.visualOdd = !this.visualOdd;
         if (this.visualOdd) {
            this.lastVisualChanged = System.currentTimeMillis();
         }
      }
   }

   public void renderVisual(double x, double y, double width) {
      float[] array = this.visualBuffer;
      int len = array.length;
      if (len > 0) {
         long passed = System.currentTimeMillis() - this.lastVisualChanged;
         int start = (int)(passed * (long)(len / 2 / 500) % (long)len);
         try {
            this.renderWave(array, len, x, y, width, start, len / 4);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   public void renderWave(float[] buffer, int bufferLength, double x, double y, double width, int offset, int length) {
      if (bufferLength > 0) {
         int steps = length / 100;
         for (int i = 0; i < length; i += steps) {
            int index = offset + i;
            if (index >= bufferLength - steps) {
               index -= bufferLength - steps;
            }
            float prevValue = buffer[index];
            float value = buffer[index + steps];
            double prevPosX = x + (double)i / (double)length * width;
            double posX = x + (double)(i + steps) / (double)length * width;
            double prevPosY = y + (double)(prevValue * 40.0F) + 5.0;
            double posY = y + (double)(value * 40.0F) + 5.0;
            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            GL11.glBegin(1);
            GL11.glVertex2d(prevPosX, prevPosY);
            GL11.glVertex2d(posX, posY);
            GL11.glEnd();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
         }
      }
   }

   public void cleanup() {
      if (this.sourceId != -1) {
         this.inQueue = 0;
         this.started = false;
         AL10.alSourceStop(this.sourceId);
         AL10.alDeleteSources(this.sourceId);
         this.allocatedBuffers.forEach(AL10::alDeleteBuffers);
      }
   }

   public void keepAlive() { this.lastUpdated = System.currentTimeMillis(); }
   public boolean isTimedOut() { return this.lastUpdated + 500L < System.currentTimeMillis(); }

   public void requestNewSourceId() { this.sourceId = AL10.alGenSources(); }

   public void play() {
      synchronized (this) {
         try {
            if (this.sourceId == -1) {
               this.requestNewSourceId();
            }
            int state = AL10.alGetSourcei(this.sourceId, 4112);
            if (this.started && state != 4114 && state != 4115) {
               this.cleanup();
               this.requestNewSourceId();
            }
            if (this.inQueue - AL10.alGetSourcei(this.sourceId, 4118) < 2 && this.started && state != 4115) {
               AL10.alSourcePause(this.sourceId);
               state = AL10.alGetSourcei(this.sourceId, 4118);
            }
            boolean crashFix = this.voiceAudioListener.getVoiceChat().crashFix;
            while (this.queue.size() > 0) {
               byte[] poll = new byte[0];
               if (crashFix) {
                  poll = this.queue.poll();
                  if (poll == null) continue;
               }
               int processed = AL10.alGetSourcei(this.sourceId, 4118);
               int buffer;
               if (processed > 0) {
                  buffer = AL10.alSourceUnqueueBuffers(this.sourceId);
                  this.inQueue--;
               } else {
                  buffer = AL10.alGenBuffers();
                  this.allocatedBuffers.add(buffer);
               }
               if (!crashFix) poll = this.queue.poll();
               ByteBuffer byteBuffer = BufferUtils.createByteBuffer(poll.length);
               byteBuffer.put(poll);
               ((Buffer)byteBuffer).flip();
               AL10.alBufferData(buffer, 4353, byteBuffer, 48000);
               EntityPlayerSP selfPlayer = LabyModCore.getMinecraft().getPlayer();
               if (selfPlayer != null) {
                  Vector3D selfPlayerLocation = new Vector3D((float)selfPlayer.posX, (float)selfPlayer.posY, (float)selfPlayer.posZ);
                  EntityPlayer otherPlayer = LabyModCore.getMinecraft().getWorld().b(this.uuid);
                  if (otherPlayer != null) {
                     Vector3D otherPlayerLocation = new Vector3D((float)otherPlayer.posX, (float)otherPlayer.posY, (float)otherPlayer.posZ);
                     Vector3D differenceLocation = otherPlayerLocation.clone().subtract(selfPlayerLocation);
                     differenceLocation = rotateVectorCC(differenceLocation, new Vector3D(0.0F, 1.0F, 0.0F), Math.toRadians((double)selfPlayer.rotationYaw));
                     differenceLocation.x = -differenceLocation.x;
                     differenceLocation.y = -differenceLocation.y;
                     if ((double)Math.abs(differenceLocation.x) < 0.1) differenceLocation.x = 0.0F;
                     if ((double)Math.abs(differenceLocation.z) < 0.1) differenceLocation.z = 0.0F;
                     AL10.alSource3f(this.sourceId, 4100, differenceLocation.x, differenceLocation.y, differenceLocation.z);
                     AL10.alDistanceModel(53253);
                     AL10.alSourcei(this.sourceId, 4128, this.voiceAudioListener.getVoiceChat().getSurroundRange());
                     AL10.alSourcei(this.sourceId, 4131, 100);
                     AL10.alSourcef(this.sourceId, 4129, 3.4F);
                  }
               }
               AL10.alSourcei(this.sourceId, 514, 1);
               AL10.alSourcef(this.sourceId, 4106, 1.0F);
               AL10.alSourceQueueBuffers(this.sourceId, buffer);
               this.inQueue++;
            }
            if (this.inQueue - AL10.alGetSourcei(this.sourceId, 4118) > 3) {
               if (!this.started) { AL10.alSourcePlay(this.sourceId); this.started = true; }
               else if (state == 4115) AL10.alSourcePlay(this.sourceId);
            }
         } catch (Throwable t) {
            t.printStackTrace();
            try {
               this.inQueue = 0;
               this.started = false;
               AL10.alDeleteSources(this.sourceId);
            } catch (Throwable t2) { t2.printStackTrace(); }
         }
      }
   }

   @Override
   public void currentState(boolean highSwapRate) {
      if (highSwapRate) {
         this.highSwapRateCount++;
         if (this.highSwapRateCount > 10) {
            this.voiceAudioListener.getHighSwapMutedUsers().put(this.uuid, System.currentTimeMillis());
         }
      } else this.highSwapRateCount = 0;
   }

   public static float wrapDegrees(float var0) {
      float var1 = var0 % 360.0F;
      if (var1 >= 180.0F) var1 -= 360.0F;
      if (var1 < -180.0F) var1 += 360.0F;
      return var1;
   }

   public static Vector3D rotateVectorCC(Vector3D vec, Vector3D axis, double theta) {
      double x = vec.x, y = vec.y, z = vec.z;
      double u = axis.x, v = axis.y, w = axis.z;
      double xPrime = u * (u * x + v * y + w * z) * (1.0 - Math.cos(theta)) + x * Math.cos(theta) + (-w * y + v * z) * Math.sin(theta);
      double yPrime = v * (u * x + v * y + w * z) * (1.0 - Math.cos(theta)) + y * Math.cos(theta) + (w * x - u * z) * Math.sin(theta);
      double zPrime = w * (u * x + v * y + w * z) * (1.0 - Math.cos(theta)) + z * Math.cos(theta) + (-v * x + u * y) * Math.sin(theta);
      return new Vector3D((float)xPrime, (float)yPrime, (float)zPrime);
   }

   public UUID getUuid() { return uuid; }
   public long getLastUpdated() { return lastUpdated; }
   public float[] getVisualBuffer() { return visualBuffer; }
   public boolean isVisualOdd() { return visualOdd; }
   public long getLastVisualChanged() { return lastVisualChanged; }
   public boolean isInitialized() { return initialized; }
   public int getSourceId() { return sourceId; }
   public int getInQueue() { return inQueue; }
   public boolean isStarted() { return started; }
   public HashSet<Integer> getAllocatedBuffers() { return allocatedBuffers; }
   public Queue<byte[]> getQueue() { return queue; }
   public int getHighSwapRateCount() { return highSwapRateCount; }
   public DefaultVoiceClientListener getVoiceAudioListener() { return voiceAudioListener; }
}
