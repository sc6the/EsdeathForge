package net.labymod.voice.protocol.udp;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import net.labymod.voice.protocol.Encryption;
import net.labymod.voice.protocol.PacketRegistry;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.type.DisconnectType;
import net.labymod.voice.protocol.type.EncryptType;

public abstract class AbstractUdpNetworking {
   protected final DatagramSocket socket;
   private final AtomicBoolean running = new AtomicBoolean(false);
   private final List<Thread> packetReceivers = new ArrayList<>();
   protected Encryption asymEncryption;

   protected AbstractUdpNetworking(DatagramSocket socket, Encryption asymEncryption) throws SocketException {
      this.socket = socket;
      this.asymEncryption = asymEncryption;
      this.running.set(true);
   }

   protected void createPacketReceiver() {
      this.createPacketReceiver(null);
   }

   protected void createPacketReceiver(Cipher cipher) {
      Thread thread = new Thread(() -> {
         byte[] buffer = new byte[1024];

         while (this.running.get()) {
            try {
               DatagramPacket datagramPacket = new DatagramPacket(buffer, 0, buffer.length);
               this.socket.receive(datagramPacket);
               this.handlePacket(datagramPacket, cipher);
            } catch (Exception var4) {
               this.handleSocketException(var4);
            }
         }
      }, "UDP-IN" + this.packetReceivers.size() + 1);
      thread.start();
      this.packetReceivers.add(thread);
   }

   protected abstract void handlePacket(DatagramPacket var1, Cipher var2) throws Exception;

   protected abstract void handleSocketException(Exception var1);

   public <T extends VoicePacket<?>> int sendPacket(T packet, SocketAddress socketAddress, Encryption symEncryption) throws Exception {
      if (!this.isRunning()) {
         return 0;
      } else {
         int packetId = PacketRegistry.getPacketId(packet);
         ByteArrayOutputStream packetBuffer = new ByteArrayOutputStream();
         packetBuffer.write(packetId);
         byte[] firewallIdentifier = packet.getFirewallIdentifier();
         if (firewallIdentifier != null) {
            packetBuffer.write(firewallIdentifier);
         }

         ByteArrayOutputStream encryptBuffer = new ByteArrayOutputStream();
         packet.write(encryptBuffer);
         byte[] encrypted = this.encrypt(packet.getEncryptType(), symEncryption, encryptBuffer.toByteArray());
         packetBuffer.write(encrypted);
         byte[] data = packetBuffer.toByteArray();
         if (data.length > 1024) {
            throw new RuntimeException("Packet is larger than expected: " + data.length + "");
         } else {
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, socketAddress);
            this.socket.send(datagramPacket);
            return datagramPacket.getLength();
         }
      }
   }

   protected byte[] decrypt(EncryptType type, Cipher cipher, Encryption asymEncryption, Encryption symEncryption, byte[] data, int offset, int length) throws BadPaddingException, IllegalBlockSizeException {
      switch (type) {
         case ASYM:
            return asymEncryption.decrypt(data, offset, length, cipher);
         case SYM:
            return symEncryption.decrypt(data, offset, length);
         default:
            return Arrays.copyOfRange(data, offset, length);
      }
   }

   protected byte[] encrypt(EncryptType type, Encryption symEncryption, byte[] data) throws BadPaddingException, IllegalBlockSizeException {
      switch (type) {
         case ASYM:
            return this.asymEncryption.encrypt(data);
         case SYM:
            return symEncryption.encrypt(data);
         default:
            return data;
      }
   }

   public boolean isRunning() {
      return this.running.get();
   }

   public void stop(DisconnectType type) {
      for (Thread thread : this.packetReceivers) {
         thread.interrupt();
      }

      this.running.set(false);
      this.socket.close();
   }

   public Encryption getAsymEncryption() {
      return this.asymEncryption;
   }
}
