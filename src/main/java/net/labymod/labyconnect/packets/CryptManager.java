package net.labymod.labyconnect.packets;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.labymod.support.util.Debug;

public class CryptManager {
   public static SecretKey createNewSharedKey() {
      try {
         KeyGenerator key = KeyGenerator.getInstance("AES");
         key.init(128);
         return key.generateKey();
      } catch (NoSuchAlgorithmException var11) {
         throw new Error(var11);
      }
   }

   public static KeyPair createNewKeyPair() {
      try {
         KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
         keyPair.initialize(1024);
         return keyPair.generateKeyPair();
      } catch (NoSuchAlgorithmException var11) {
         var11.printStackTrace();
         Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "Key pair generation failed!");
         return null;
      }
   }

   public static byte[] getServerIdHash(String input, PublicKey publicKey, SecretKey secretKey) {
      try {
         return digestOperation("SHA-1", input.getBytes("ISO_8859_1"), secretKey.getEncoded(), publicKey.getEncoded());
      } catch (UnsupportedEncodingException var4) {
         var4.printStackTrace();
         return null;
      }
   }

   private static byte[] digestOperation(String type, byte[]... bytes) {
      try {
         MessageDigest disgest = MessageDigest.getInstance(type);

         for (byte[] b : bytes) {
            disgest.update(b);
         }

         return disgest.digest();
      } catch (NoSuchAlgorithmException var7) {
         var7.printStackTrace();
         return null;
      }
   }

   public static PublicKey decodePublicKey(byte[] p_75896_0_) {
      try {
         X509EncodedKeySpec var1 = new X509EncodedKeySpec(p_75896_0_);
         KeyFactory var2 = KeyFactory.getInstance("RSA");
         return var2.generatePublic(var1);
      } catch (NoSuchAlgorithmException var3) {
      } catch (InvalidKeySpecException var4) {
      }

      Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "Public key reconstitute failed!");
      return null;
   }

   public static SecretKey decryptSharedKey(PrivateKey p_75887_0_, byte[] p_75887_1_) {
      return new SecretKeySpec(decryptData(p_75887_0_, p_75887_1_), "AES");
   }

   public static byte[] encryptData(Key p_75894_0_, byte[] p_75894_1_) {
      return cipherOperation(1, p_75894_0_, p_75894_1_);
   }

   public static byte[] decryptData(Key p_75889_0_, byte[] p_75889_1_) {
      return cipherOperation(2, p_75889_0_, p_75889_1_);
   }

   private static byte[] cipherOperation(int p_75885_0_, Key p_75885_1_, byte[] p_75885_2_) {
      try {
         return createTheCipherInstance(p_75885_0_, p_75885_1_.getAlgorithm(), p_75885_1_).doFinal(p_75885_2_);
      } catch (IllegalBlockSizeException var4) {
         var4.printStackTrace();
      } catch (BadPaddingException var5) {
         var5.printStackTrace();
      }

      Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "Cipher data failed!");
      return null;
   }

   private static Cipher createTheCipherInstance(int p_75886_0_, String p_75886_1_, Key p_75886_2_) {
      try {
         Cipher var3 = Cipher.getInstance(p_75886_1_);
         var3.init(p_75886_0_, p_75886_2_);
         return var3;
      } catch (InvalidKeyException var4) {
         var4.printStackTrace();
      } catch (NoSuchAlgorithmException var5) {
         var5.printStackTrace();
      } catch (NoSuchPaddingException var61) {
         var61.printStackTrace();
      }

      Debug.log(Debug.EnumDebugMode.LABYMOD_CHAT, "Cipher creation failed!");
      return null;
   }

   public static Cipher createNetCipherInstance(int opMode, Key key) {
      try {
         Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
         cipher.init(opMode, key, new IvParameterSpec(key.getEncoded()));
         return cipher;
      } catch (GeneralSecurityException var3) {
         throw new RuntimeException(var3);
      }
   }
}
