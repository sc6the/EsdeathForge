package net.labymod.addons.voicechat.audio.mic;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class JavaInputDeviceImpl implements InputDevice {
   private final TargetDataLine line;

   public JavaInputDeviceImpl(TargetDataLine line) {
      this.line = line;
   }

   @Override
   public void open(AudioFormat audioFormat) throws LineUnavailableException {
      this.line.open(audioFormat);
   }

   @Override
   public void start() {
      this.line.start();
   }

   @Override
   public boolean isOpen() {
      return this.line.isOpen();
   }

   @Override
   public void close() {
      this.line.close();
   }

   @Override
   public void stop() {
      this.line.stop();
   }

   @Override
   public void read(byte[] chunk) {
      this.line.read(chunk, 0, chunk.length);
   }

   @Override
   public int available() {
      return this.line.available();
   }
}
