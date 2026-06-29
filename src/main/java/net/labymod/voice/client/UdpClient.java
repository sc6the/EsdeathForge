package net.labymod.voice.client;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import net.labymod.voice.protocol.Encryption;
import net.labymod.voice.protocol.PacketRegistry;
import net.labymod.voice.protocol.ProtocolVersion;
import net.labymod.voice.protocol.VoicePacket;
import net.labymod.voice.protocol.handler.ServerVoicePacketHandler;
import net.labymod.voice.protocol.packet.KeepAlivePacket;
import net.labymod.voice.protocol.type.ConnectionState;
import net.labymod.voice.protocol.type.DisconnectType;
import net.labymod.voice.protocol.udp.AbstractUdpNetworking;

public class UdpClient extends AbstractUdpNetworking {
   private static final long TIMEOUT_MS = TimeUnit.SECONDS.toMillis(60L);
   private static final long KEEP_ALIVE_RATE_IN_SECONDS = 1L;
   private final InetSocketAddress target;
   private final ScheduledFuture timeoutTask;
   private final ServerVoicePacketHandler handler;
   private ConnectionState state = ConnectionState.HANDSHAKE;
   private final AtomicLong lastReceived = new AtomicLong(System.currentTimeMillis());
   private Consumer<DisconnectType> disconnectListener = null;
   private final Encryption symEncryption;
   private int remoteProtocolVersion = ProtocolVersion.VERSION;

   public UdpClient(ServerVoicePacketHandler handler, InetSocketAddress address, String publicKey) throws Exception {
      super(new DatagramSocket(), new Encryption(publicKey));
      this.handler = handler;
      this.target = address;
      this.symEncryption = new Encryption();
      this.socket.setSoTimeout(20000);
      this.createPacketReceiver();
      this.timeoutTask = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
         long lastPacketDuration = System.currentTimeMillis() - this.lastReceived.get();
         if (lastPacketDuration > TIMEOUT_MS) {
            this.stop(DisconnectType.TIMEOUT);
         } else {
            if (this.state == ConnectionState.CONNECTED) {
               this.sendPacket(new KeepAlivePacket());
            }
         }
      }, 1L, 1L, TimeUnit.SECONDS);
   }

   @Override
   protected void handleSocketException(Exception exception) {
      exception.printStackTrace();
      if (exception instanceof SocketTimeoutException && this.isRunning()) {
         this.stop(DisconnectType.TIMEOUT);
      }
   }

   @Override
   protected void handlePacket(DatagramPacket datagramPacket, Cipher cipher) throws Exception {
      byte[] data = datagramPacket.getData();
      int length = datagramPacket.getLength();
      byte id = data[0];
      if (id != -1 && PacketRegistry.isValidPacket(id)) {
         VoicePacket voicePacket = PacketRegistry.createPacket(id);
         if (voicePacket == null) {
            System.out.println("Invalid packet");
         } else if (voicePacket.getAllowedState() == this.state) {
            this.lastReceived.set(System.currentTimeMillis());

            try {
               data = this.decrypt(voicePacket.getEncryptType(), cipher, this.asymEncryption, this.symEncryption, data, 1, length - 1);
            } catch (BadPaddingException | IllegalBlockSizeException var8) {
               System.out.println("Failed to decrypt " + voicePacket.getClass().getSimpleName());
               var8.printStackTrace();
               return;
            }

            ByteArrayInputStream buffer = new ByteArrayInputStream(data);
            voicePacket.read(buffer, this.remoteProtocolVersion);
            voicePacket.handle(this.handler);
         }
      } else {
         System.out.println("Invalid packet");
      }
   }

   public void sendPacket(VoicePacket<?> packet) {
      try {
         this.sendPacket(packet, this.target, this.symEncryption);
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }

   @Override
   public void stop(DisconnectType type) {
      super.stop(type);
      this.timeoutTask.cancel(true);
      if (this.disconnectListener != null && type != DisconnectType.KICK) {
         this.disconnectListener.accept(type);
      }
   }

   public void setDisconnectListener(Consumer<DisconnectType> disconnectListener) {
      this.disconnectListener = disconnectListener;
   }

   public void setState(ConnectionState state) {
      this.state = state;
   }

   public ConnectionState getState() {
      return this.state;
   }

   public Encryption getSymEncryption() {
      return this.symEncryption;
   }

   public void setRemoteProtocolVersion(int remoteProtocolVersion) {
      this.remoteProtocolVersion = remoteProtocolVersion;
   }
}
