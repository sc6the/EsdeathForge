package net.labymod.addons.voicechat.audio.mic;

import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

public interface InputDevice {
   void open(AudioFormat var1) throws LineUnavailableException;

   void start();

   boolean isOpen() throws IOException;

   void close() throws IOException;

   void stop() throws IOException;

   void read(byte[] var1) throws IOException;

   int available() throws IOException;
}
