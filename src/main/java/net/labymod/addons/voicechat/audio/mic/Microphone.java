package net.labymod.addons.voicechat.audio.mic;

import java.io.IOException;
import java.net.SocketException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.DataLine.Info;
import net.labymod.addons.voicechat.VoiceChat;
import net.labymod.addons.voicechat.audio.AudioModifier;
import net.labymod.addons.voicechat.audio.opus.EnumOpusCodecDirection;
import net.labymod.addons.voicechat.audio.opus.EnumOpusError;
import net.labymod.addons.voicechat.audio.opus.OpusCodecManager;
import net.labymod.main.LabyMod;
import net.labymod.utils.Consumer;

public class Microphone {
   private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
   private VoiceChat voiceChat;
   private Info dataLineInfo;
   private String microphoneName;
   private Future<?> futureLocal;
   private Future<?> futureRemote;
   private InputDevice localInputDevice;
   private RemoteInputDeviceImpl remoteInputDevice;
   private long lastRemoteReceived = -1L;
   private byte[] remoteBuffer = new byte[0];
   private int remoteBufferPosition = 0;

   public Microphone(VoiceChat voiceChat) {
      this.voiceChat = voiceChat;
      this.dataLineInfo = new Info(TargetDataLine.class, voiceChat.getVoiceClientListener().getAudioConfig().getAudioFormat());
   }

   public static List<String> getMicrophones() {
      List<String> microphones = new ArrayList<>();
      javax.sound.sampled.Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

      for (javax.sound.sampled.Mixer.Info info : mixerInfos) {
         Mixer mixer = AudioSystem.getMixer(info);
         javax.sound.sampled.Line.Info[] lineInfos = mixer.getTargetLineInfo();
         if (lineInfos.length != 0 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
            microphones.add(info.getName());
         }
      }

      return microphones;
   }

   public void open(TargetDataLine targetDataLineInfo, String microphoneName) throws LineUnavailableException {
      this.remoteInputDevice = new RemoteInputDeviceImpl(3520);
      this.microphoneName = microphoneName == null ? "Unknown microphone" : microphoneName;
      this.localInputDevice = new JavaInputDeviceImpl(targetDataLineInfo);
      this.localInputDevice.open(this.voiceChat.getVoiceClientListener().getAudioConfig().getAudioFormat());
      this.localInputDevice.start();
      this.voiceChat.log("Open " + this.microphoneName);
   }

   public void stopNow() {
      try {
         if (this.localInputDevice != null && this.localInputDevice.isOpen()) {
            if (this.futureLocal != null) {
               this.futureLocal.cancel(true);
            }

            this.localInputDevice.close();
            this.localInputDevice.stop();
         }

         if (this.remoteInputDevice != null && this.remoteInputDevice.isOpen()) {
            if (this.futureRemote != null) {
               this.futureRemote.cancel(true);
            }

            this.remoteInputDevice.close();
            this.remoteInputDevice.stop();
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }

   public void start() {
      OpusCodecManager opusCodecManager = this.voiceChat.getOpusCodecManager();
      if (opusCodecManager.getStatus() == EnumOpusError.OK) {
         int bufferInSize = opusCodecManager.getBufferInSize();
         this.futureLocal = executorService.scheduleAtFixedRate(() -> {
            try {
               if (this.localInputDevice == null || !this.localInputDevice.isOpen()) {
                  this.futureLocal.cancel(true);
                  this.futureLocal = null;
                  return;
               }

               if (!this.isAllowInput() || this.lastRemoteReceived + 1000L > System.currentTimeMillis()) {
                  return;
               }

               if (this.voiceChat.cleanup) {
                  byte[] data = new byte[this.localInputDevice.available()];
                  this.localInputDevice.read(data);
                  this.voiceChat.cleanup = false;
               }

               if (this.localInputDevice.available() >= bufferInSize) {
                  byte[] data = new byte[bufferInSize];
                  this.localInputDevice.read(data);
                  this.feed(data);
               }
            } catch (Throwable var3) {
               var3.printStackTrace();
            }
         }, 0L, 5L, TimeUnit.MILLISECONDS);
         this.futureRemote = executorService.scheduleAtFixedRate(() -> {
            try {
               if (this.remoteInputDevice == null || !this.remoteInputDevice.isOpen()) {
                  this.futureRemote.cancel(true);
                  this.futureRemote = null;
                  return;
               }

               if (!this.isAllowInput()) {
                  return;
               }

               if (this.remoteBufferPosition + 2048 > this.remoteBuffer.length) {
                  byte[] newBuffer = new byte[this.remoteBuffer.length + 2048];
                  System.arraycopy(this.remoteBuffer, 0, newBuffer, 0, this.remoteBuffer.length);
                  this.remoteBuffer = newBuffer;
               }

               byte[] packet = new byte[2048];
               this.remoteInputDevice.read(packet);
               System.arraycopy(packet, 0, this.remoteBuffer, this.remoteBufferPosition, packet.length);
               this.remoteBufferPosition += packet.length;

               while (this.remoteBufferPosition >= bufferInSize * 2) {
                  byte[] chunk = new byte[bufferInSize * 2];
                  System.arraycopy(this.remoteBuffer, 0, chunk, 0, bufferInSize * 2);
                  byte[] second = new byte[this.remoteBuffer.length - bufferInSize * 2];
                  System.arraycopy(this.remoteBuffer, bufferInSize * 2, second, 0, this.remoteBuffer.length - bufferInSize * 2);
                  this.remoteBuffer = second;
                  this.remoteBufferPosition -= bufferInSize * 2;
                  byte[] raw = new byte[bufferInSize];

                  for (int i = 0; i < chunk.length; i += 4) {
                     short value = (short)((int)(ByteBuffer.wrap(chunk, i, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat() * 32768.0F));
                     raw[i / 2] = (byte)value;
                     raw[i / 2 + 1] = (byte)(value >> 8);
                  }

                  this.feed(raw);
               }

               this.lastRemoteReceived = System.currentTimeMillis();
            } catch (Throwable var8) {
               if (!(var8 instanceof SocketException)) {
                  var8.printStackTrace();
               }
            }
         }, 0L, 5L, TimeUnit.MILLISECONDS);
      }
   }

   private boolean isAllowInput() {
      UUID uuid = LabyMod.getInstance().getPlayerUUID();
      boolean wantsToTalk = this.voiceChat.isPushToTalkPressed() || this.voiceChat.isVoiceActivity() && !this.voiceChat.isVoiceActivitySuppressed();
      boolean active = this.voiceChat.isConnected()
         && LabyMod.getInstance().isInGame()
         && wantsToTalk
         && this.voiceChat.isEnabled()
         && this.voiceChat.isAllowed()
         && this.voiceChat.getVoiceClientListener().getGlobalMuted().get(uuid) == null;
      boolean testingMicrophone = this.voiceChat.isTestingMicrophone();
      return this.voiceChat.isEnabled() && (testingMicrophone || active);
   }

   private void feed(byte[] data) {
      UUID uuid = LabyMod.getInstance().getPlayerUUID();
      OpusCodecManager opusCodecManager = this.voiceChat.getOpusCodecManager();
      final boolean testingMicrophone = this.voiceChat.isTestingMicrophone();
      this.voiceChat.lastRMSLevel = AudioModifier.calculateRMSLevel(data);
      boolean voiceActivityKeepAlive = this.voiceChat.lastVoiceActivityActivation + 1000L > System.currentTimeMillis();
      if (!this.voiceChat.isPushToTalkPressed()
         && this.voiceChat.isVoiceActivity()
         && this.voiceChat.lastRMSLevel < this.voiceChat.voiceActivityActivationLevel) {
         if (!voiceActivityKeepAlive) {
            return;
         }
      } else {
         this.voiceChat.lastVoiceActivityActivation = System.currentTimeMillis();
      }

      float volume = 0.1F * (float)this.voiceChat.getMicrophoneVolume();
      data = AudioModifier.adjustVolume(data, volume);
      opusCodecManager.convert(uuid, EnumOpusCodecDirection.ENCODE, data, new Consumer<byte[]>() {
         public void accept(byte[] bytes) {
            ByteBuffer encodedBuffer = ByteBuffer.allocate(bytes.length + 8);
            encodedBuffer.putLong(System.nanoTime());
            encodedBuffer.put(bytes);
            ((Buffer)encodedBuffer).flip();
            bytes = encodedBuffer.array();
            Microphone.this.voiceChat.getSurroundManager().updateSelfTalking();
            if (testingMicrophone) {
               Microphone.this.voiceChat.getVoiceClientListener().onAudioReceived(LabyMod.getInstance().getPlayerUUID(), bytes);
            } else if (Microphone.this.voiceChat.getServerIncomingPacketListener() != null) {
               Microphone.this.voiceChat.getVoiceClient().sendAudioChunk(bytes);
            }
         }
      });
   }

   public void openDefault() throws LineUnavailableException {
      TargetDataLine defaultLine = this.getDefaultTargetDataLine();
      this.open(defaultLine, "Default microphone");
   }

   public boolean openMicrophoneByName(String name) {
      if (name != null && !name.isEmpty()) {
         javax.sound.sampled.Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

         for (javax.sound.sampled.Mixer.Info info : mixerInfos) {
            if (info.getName().equals(name)) {
               Mixer mixer = AudioSystem.getMixer(info);
               javax.sound.sampled.Line.Info[] lineInfos = mixer.getTargetLineInfo();
               if (lineInfos.length != 0 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
                  try {
                     this.open((TargetDataLine)mixer.getLine(this.dataLineInfo), info.getName());
                     return true;
                  } catch (LineUnavailableException var11) {
                     var11.printStackTrace();
                  }
               }
            }
         }
      }

      try {
         this.openDefault();
         return true;
      } catch (LineUnavailableException var10) {
         var10.printStackTrace();
         return false;
      }
   }

   public TargetDataLine getDefaultTargetDataLine() throws LineUnavailableException {
      return (TargetDataLine)AudioSystem.getLine(this.dataLineInfo);
   }

   public boolean isRunning() {
      try {
         return this.lastRemoteReceived + 1000L > System.currentTimeMillis()
            ? true
            : this.futureLocal != null && !this.futureLocal.isCancelled() && !this.futureLocal.isDone() && this.localInputDevice.isOpen();
      } catch (IOException var2) {
         var2.printStackTrace();
         return false;
      }
   }

   public VoiceChat getVoiceChat() {
      return this.voiceChat;
   }

   public Info getDataLineInfo() {
      return this.dataLineInfo;
   }

   public String getMicrophoneName() {
      return this.microphoneName;
   }

   public Future<?> getFutureLocal() {
      return this.futureLocal;
   }

   public Future<?> getFutureRemote() {
      return this.futureRemote;
   }

   public InputDevice getLocalInputDevice() {
      return this.localInputDevice;
   }

   public RemoteInputDeviceImpl getRemoteInputDevice() {
      return this.remoteInputDevice;
   }

   public long getLastRemoteReceived() {
      return this.lastRemoteReceived;
   }

   public byte[] getRemoteBuffer() {
      return this.remoteBuffer;
   }

   public int getRemoteBufferPosition() {
      return this.remoteBufferPosition;
   }

   public void setVoiceChat(VoiceChat voiceChat) {
      this.voiceChat = voiceChat;
   }

   public void setDataLineInfo(Info dataLineInfo) {
      this.dataLineInfo = dataLineInfo;
   }

   public void setMicrophoneName(String microphoneName) {
      this.microphoneName = microphoneName;
   }

   public void setFutureLocal(Future<?> futureLocal) {
      this.futureLocal = futureLocal;
   }

   public void setFutureRemote(Future<?> futureRemote) {
      this.futureRemote = futureRemote;
   }

   public void setLocalInputDevice(InputDevice localInputDevice) {
      this.localInputDevice = localInputDevice;
   }

   public void setRemoteInputDevice(RemoteInputDeviceImpl remoteInputDevice) {
      this.remoteInputDevice = remoteInputDevice;
   }

   public void setLastRemoteReceived(long lastRemoteReceived) {
      this.lastRemoteReceived = lastRemoteReceived;
   }

   public void setRemoteBuffer(byte[] remoteBuffer) {
      this.remoteBuffer = remoteBuffer;
   }

   public void setRemoteBufferPosition(int remoteBufferPosition) {
      this.remoteBufferPosition = remoteBufferPosition;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Microphone)) {
         return false;
      } else {
         Microphone other = (Microphone)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$voiceChat = this.getVoiceChat();
            Object other$voiceChat = other.getVoiceChat();
            if (this$voiceChat == null ? other$voiceChat == null : this$voiceChat.equals(other$voiceChat)) {
               Object this$dataLineInfo = this.getDataLineInfo();
               Object other$dataLineInfo = other.getDataLineInfo();
               if (this$dataLineInfo == null ? other$dataLineInfo == null : this$dataLineInfo.equals(other$dataLineInfo)) {
                  Object this$microphoneName = this.getMicrophoneName();
                  Object other$microphoneName = other.getMicrophoneName();
                  if (this$microphoneName == null ? other$microphoneName == null : this$microphoneName.equals(other$microphoneName)) {
                     Object this$futureLocal = this.getFutureLocal();
                     Object other$futureLocal = other.getFutureLocal();
                     if (this$futureLocal == null ? other$futureLocal == null : this$futureLocal.equals(other$futureLocal)) {
                        Object this$futureRemote = this.getFutureRemote();
                        Object other$futureRemote = other.getFutureRemote();
                        if (this$futureRemote == null ? other$futureRemote == null : this$futureRemote.equals(other$futureRemote)) {
                           Object this$localInputDevice = this.getLocalInputDevice();
                           Object other$localInputDevice = other.getLocalInputDevice();
                           if (this$localInputDevice == null ? other$localInputDevice == null : this$localInputDevice.equals(other$localInputDevice)) {
                              Object this$remoteInputDevice = this.getRemoteInputDevice();
                              Object other$remoteInputDevice = other.getRemoteInputDevice();
                              if (this$remoteInputDevice == null ? other$remoteInputDevice == null : this$remoteInputDevice.equals(other$remoteInputDevice)) {
                                 if (this.getLastRemoteReceived() != other.getLastRemoteReceived()) {
                                    return false;
                                 } else {
                                    return !Arrays.equals(this.getRemoteBuffer(), other.getRemoteBuffer())
                                       ? false
                                       : this.getRemoteBufferPosition() == other.getRemoteBufferPosition();
                                 }
                              } else {
                                 return false;
                              }
                           } else {
                              return false;
                           }
                        } else {
                           return false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof Microphone;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $voiceChat = this.getVoiceChat();
      result = result * 59 + ($voiceChat == null ? 43 : $voiceChat.hashCode());
      Object $dataLineInfo = this.getDataLineInfo();
      result = result * 59 + ($dataLineInfo == null ? 43 : $dataLineInfo.hashCode());
      Object $microphoneName = this.getMicrophoneName();
      result = result * 59 + ($microphoneName == null ? 43 : $microphoneName.hashCode());
      Object $futureLocal = this.getFutureLocal();
      result = result * 59 + ($futureLocal == null ? 43 : $futureLocal.hashCode());
      Object $futureRemote = this.getFutureRemote();
      result = result * 59 + ($futureRemote == null ? 43 : $futureRemote.hashCode());
      Object $localInputDevice = this.getLocalInputDevice();
      result = result * 59 + ($localInputDevice == null ? 43 : $localInputDevice.hashCode());
      Object $remoteInputDevice = this.getRemoteInputDevice();
      result = result * 59 + ($remoteInputDevice == null ? 43 : $remoteInputDevice.hashCode());
      long $lastRemoteReceived = this.getLastRemoteReceived();
      result = result * 59 + (int)($lastRemoteReceived >>> 32 ^ $lastRemoteReceived);
      result = result * 59 + Arrays.hashCode(this.getRemoteBuffer());
      return result * 59 + this.getRemoteBufferPosition();
   }

   @Override
   public String toString() {
      return "Microphone(voiceChat="
         + this.getVoiceChat()
         + ", dataLineInfo="
         + this.getDataLineInfo()
         + ", microphoneName="
         + this.getMicrophoneName()
         + ", futureLocal="
         + this.getFutureLocal()
         + ", futureRemote="
         + this.getFutureRemote()
         + ", localInputDevice="
         + this.getLocalInputDevice()
         + ", remoteInputDevice="
         + this.getRemoteInputDevice()
         + ", lastRemoteReceived="
         + this.getLastRemoteReceived()
         + ", remoteBuffer="
         + Arrays.toString(this.getRemoteBuffer())
         + ", remoteBufferPosition="
         + this.getRemoteBufferPosition()
         + ")";
   }
}
