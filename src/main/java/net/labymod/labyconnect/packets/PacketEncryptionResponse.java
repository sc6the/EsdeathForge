package net.labymod.labyconnect.packets;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import net.labymod.labyconnect.handling.PacketHandler;

public class PacketEncryptionResponse extends Packet {
   private byte[] sharedSecret;
   private byte[] verifyToken;
   private byte[] pin = new byte[0];

   public PacketEncryptionResponse(SecretKey key, PublicKey publicKey, byte[] hash) {
      this.sharedSecret = CryptManager.encryptData(publicKey, key.getEncoded());
      this.verifyToken = CryptManager.encryptData(publicKey, hash);
   }

   public PacketEncryptionResponse(SecretKey key, PublicKey publicKey, byte[] hash, String pin) {
      this(key, publicKey, hash);
      this.pin = CryptManager.encryptData(publicKey, pin.getBytes(StandardCharsets.UTF_8));
   }

   public PacketEncryptionResponse() {
   }

   public byte[] getSharedSecret() {
      return this.sharedSecret;
   }

   public byte[] getVerifyToken() {
      return this.verifyToken;
   }

   @Override
   public void read(PacketBuf buf) {
      this.sharedSecret = buf.readByteArray();
      this.verifyToken = buf.readByteArray();
   }

   @Override
   public void write(PacketBuf buf) {
      buf.writeByteArray(new byte[]{42});
      buf.writeByteArray(this.sharedSecret);
      buf.writeByteArray(this.verifyToken);
      buf.writeByteArray(this.pin);
   }

   @Override
   public void handle(PacketHandler packetHandler) {
      packetHandler.handle(this);
   }
}
