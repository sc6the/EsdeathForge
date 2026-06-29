package net.labymod.voice.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Callable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
   private Cipher encryptCipher;
   private Cipher decryptCipher;
   private Key pubKey;
   private int chunkSize = -1;
   private Callable<Cipher> createEncryptionCipher;
   private Callable<Cipher> createDecryptionCipher;
   private Key shareKey;

   public Encryption(String pubKeyContent) throws NoSuchAlgorithmException {
      this.chunkSize = 200;
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");

      try {
         EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(pubKeyContent));
         this.pubKey = keyFactory.generatePublic(publicKeySpec);
         this.encryptCipher = Cipher.getInstance("RSA");
         this.encryptCipher.init(1, this.pubKey);
         this.createEncryptionCipher = () -> {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(1, this.pubKey);
            return cipher;
         };
      } catch (InvalidKeySpecException var4) {
         var4.printStackTrace();
         throw new IllegalArgumentException("Invalid KeyFile");
      } catch (InvalidKeyException | NoSuchPaddingException var5) {
         var5.printStackTrace();
      }
   }

   public Encryption(File pubKeyFile) throws NoSuchAlgorithmException {
      this.chunkSize = 200;
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      if (pubKeyFile.exists()) {
         try {
            byte[] publicKeyBytes = Files.readAllBytes(pubKeyFile.toPath());
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBytes));
            this.pubKey = keyFactory.generatePublic(publicKeySpec);
            this.encryptCipher = Cipher.getInstance("RSA");
            this.encryptCipher.init(1, this.pubKey);
            this.createEncryptionCipher = () -> {
               Cipher cipher = Cipher.getInstance("RSA");
               cipher.init(1, this.pubKey);
               return cipher;
            };
         } catch (InvalidKeySpecException | IOException var5) {
            var5.printStackTrace();
            throw new IllegalArgumentException("Invalid KeyFile");
         } catch (InvalidKeyException | NoSuchPaddingException var6) {
            var6.printStackTrace();
         }
      } else {
         throw new IllegalArgumentException("Keygeneration is not supported here");
      }
   }

   public Encryption(File privKeyFile, File pubKeyFile) throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
      this.chunkSize = 200;
      KeyPair keyPair = null;
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      if (privKeyFile.exists() && pubKeyFile.exists()) {
         try {
            byte[] publicKeyBytes = Files.readAllBytes(pubKeyFile.toPath());
            byte[] privateKeyBytes = Files.readAllBytes(privKeyFile.toPath());
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBytes));
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBytes));
            keyPair = new KeyPair(keyFactory.generatePublic(publicKeySpec), keyFactory.generatePrivate(privateKeySpec));
         } catch (InvalidKeySpecException | IOException var36) {
            var36.printStackTrace();
         }
      }

      if (keyPair == null) {
         System.out.println("Keypair could not be loaded, generating new Pair");
         KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
         generator.initialize(2048);
         keyPair = generator.generateKeyPair();

         try {
            System.out.println("Savin new keypair");

            try (FileOutputStream fos = new FileOutputStream("public.key")) {
               fos.write(Base64.getEncoder().encode(keyPair.getPublic().getEncoded()));
            }

            try (FileOutputStream fos = new FileOutputStream("private.key")) {
               fos.write(Base64.getEncoder().encode(keyPair.getPrivate().getEncoded()));
            }
         } catch (IOException var39) {
            System.err.println("Could not save generated Keys, restarting the Server will generate new keys");
            throw var39;
         }
      }

      this.encryptCipher = Cipher.getInstance("RSA");
      this.encryptCipher.init(1, keyPair.getPublic());
      this.pubKey = keyPair.getPublic();
      this.createEncryptionCipher = () -> {
         Cipher cipher = Cipher.getInstance("RSA");
         cipher.init(1, this.pubKey);
         return cipher;
      };
      this.decryptCipher = Cipher.getInstance("RSA");
      this.decryptCipher.init(2, keyPair.getPrivate());
      KeyPair finalKeyPair = keyPair;
      this.createDecryptionCipher = () -> {
         Cipher cipher = Cipher.getInstance("RSA");
         cipher.init(2, finalKeyPair.getPrivate());
         return cipher;
      };
      if (!Arrays.equals("42".getBytes(StandardCharsets.UTF_8), this.decrypt(this.encrypt("42".getBytes(StandardCharsets.UTF_8))))) {
         throw new IllegalArgumentException("Keyfiles dont match");
      }
   }

   public Encryption(byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
      this.shareKey = new SecretKeySpec(key, "AES");
      this.encryptCipher = Cipher.getInstance("AES");
      this.encryptCipher.init(1, this.shareKey);
      this.createEncryptionCipher = () -> {
         Cipher cipher = Cipher.getInstance("AES");
         cipher.init(1, this.shareKey);
         return cipher;
      };
      this.decryptCipher = Cipher.getInstance("AES");
      this.decryptCipher.init(2, this.shareKey);
      this.createDecryptionCipher = () -> {
         Cipher cipher = Cipher.getInstance("AES");
         cipher.init(2, this.shareKey);
         return cipher;
      };
      if (!Arrays.equals("42".getBytes(StandardCharsets.UTF_8), this.decrypt(this.encrypt("42".getBytes(StandardCharsets.UTF_8))))) {
         throw new IllegalArgumentException("Keyfiles dont match");
      }
   }

   public Encryption() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
      KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
      keyGenerator.init(128);
      this.shareKey = keyGenerator.generateKey();
      this.encryptCipher = Cipher.getInstance("AES");
      this.encryptCipher.init(1, this.shareKey);
      this.createEncryptionCipher = () -> {
         Cipher cipher = Cipher.getInstance("AES");
         cipher.init(1, this.shareKey);
         return cipher;
      };
      this.decryptCipher = Cipher.getInstance("AES");
      this.decryptCipher.init(2, this.shareKey);
      this.createDecryptionCipher = () -> {
         Cipher cipher = Cipher.getInstance("AES");
         cipher.init(2, this.shareKey);
         return cipher;
      };
      if (!Arrays.equals("42".getBytes(StandardCharsets.UTF_8), this.decrypt(this.encrypt("42".getBytes(StandardCharsets.UTF_8))))) {
         throw new IllegalArgumentException("Keyfiles dont match");
      }
   }

   public byte[] encrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException {
      return this.encrypt(data, this.encryptCipher);
   }

   public byte[] encrypt(byte[] data, Cipher cipher) throws IllegalBlockSizeException, BadPaddingException {
      if (this.chunkSize <= 0) {
         return cipher.doFinal(data);
      } else {
         int maxChunkSize = this.chunkSize;
         int chunks = this.ceilDivide(data.length, maxChunkSize);
         ByteArrayOutputStream buffer = new ByteArrayOutputStream();

         for (int i = 0; i < chunks; i++) {
            int chunkStart = maxChunkSize * i;
            int currentChunkSize = Math.min(maxChunkSize, data.length - chunkStart);
            byte[] encrypted = cipher.doFinal(data, chunkStart, currentChunkSize);
            buffer.write(encrypted.length + -128);
            buffer.write(encrypted, 0, encrypted.length);
         }

         return buffer.toByteArray();
      }
   }

   public byte[] decrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException {
      return this.decrypt(data, 0, data.length, this.decryptCipher);
   }

   public byte[] decrypt(byte[] data, int offset, int length) throws IllegalBlockSizeException, BadPaddingException {
      return this.decrypt(data, offset, length, this.decryptCipher);
   }

   public byte[] decrypt(byte[] data, int offset, int length, Cipher cipher) throws IllegalBlockSizeException, BadPaddingException {
      if (this.chunkSize <= 0) {
         return cipher.doFinal(data, offset, length);
      } else {
         ByteArrayInputStream input = new ByteArrayInputStream(data, offset, length);
         ByteArrayOutputStream output = new ByteArrayOutputStream();

         while (input.available() > 0) {
            int encryptedSize = input.read() - -128;
            byte[] encrypted = new byte[encryptedSize];
            input.read(encrypted, 0, encryptedSize);
            byte[] decrypted = cipher.doFinal(encrypted);
            output.write(decrypted, 0, decrypted.length);
         }

         return output.toByteArray();
      }
   }

   private int ceilDivide(int a, int b) {
      int result = a / b;
      if (a % b != 0) {
         result++;
      }

      return result;
   }

   public Cipher getNewEncryptCipher() {
      if (this.createEncryptionCipher == null) {
         return null;
      } else {
         try {
            return this.createEncryptionCipher.call();
         } catch (Exception var2) {
            var2.printStackTrace();
            return null;
         }
      }
   }

   public Cipher getNewDecryptCipher() {
      if (this.createDecryptionCipher == null) {
         return null;
      } else {
         try {
            return this.createDecryptionCipher.call();
         } catch (Exception var2) {
            var2.printStackTrace();
            return null;
         }
      }
   }

   public Key getPubKey() {
      return this.pubKey;
   }

   public void setChunkSize(int chunkSize) {
      this.chunkSize = chunkSize;
   }

   public Key getShareKey() {
      return this.shareKey;
   }
}
