package net.labymod.addons.voicechat.audio.mic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;

public class RemoteInputDeviceImpl implements InputDevice {
   private DatagramSocket socket;

   public RemoteInputDeviceImpl(int port) {
      try {
         this.socket = new DatagramSocket(port);
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }

   @Override
   public void open(AudioFormat audioFormat) throws LineUnavailableException {
   }

   @Override
   public void start() {
   }

   @Override
   public boolean isOpen() {
      return this.socket != null && !this.socket.isClosed();
   }

   @Override
   public void close() throws IOException {
      this.socket.close();
   }

   @Override
   public void stop() throws IOException {
      this.socket.close();
   }

   @Override
   public void read(byte[] chunk) throws IOException {
      DatagramPacket packet = new DatagramPacket(chunk, chunk.length);
      this.socket.receive(packet);
   }

   @Override
   public int available() throws IOException {
      return -1;
   }
}
