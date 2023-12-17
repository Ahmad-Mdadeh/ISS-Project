import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.Base64;

public class Hyper {
        // Rivest Shamir Adleman
        private static final String RSA = "RSA";

        public static KeyPair generateKeyPair() throws Exception {
                SecureRandom secureRandom = new SecureRandom();
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
                keyPairGenerator.initialize(2048, secureRandom);
                return keyPairGenerator.generateKeyPair();
        }

        // Encryption function which converts
        public static byte[] encrept(String plainText, PublicKey publicKey)
                        throws Exception {
                Cipher cipher = Cipher.getInstance(RSA);

                cipher.init(Cipher.ENCRYPT_MODE, publicKey);

                return cipher.doFinal(plainText.getBytes());
        }

        // Decryption function
        public static String decrypt(String cipherText, PrivateKey privateKey)
                        throws Exception {
                byte[] cipherTextByte = DatatypeConverter.parseHexBinary(cipherText);

                Cipher cipher = Cipher.getInstance(RSA);

                cipher.init(Cipher.DECRYPT_MODE,
                                privateKey);
                byte[] result = cipher.doFinal(cipherTextByte);

                return new String(result);
        }

        public static String decreptsecretkeyserver(byte[] cipherText, PrivateKey privateKey)
                        throws Exception {
                Cipher cipher = Cipher.getInstance(RSA);

                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                byte[] result = cipher.doFinal(cipherText);

                return new String(result);
        }

        public static void main(String[] args) {
                try {
                        System.out.println(
                                        DatatypeConverter.printHexBinary(generateKeyPair().getPublic().getEncoded()));
                        System.out.println(
                                        "-------------------------------------------------------------------------------------------------------------------------------------------");
                        System.out.println(
                                        DatatypeConverter.printHexBinary(generateKeyPair().getPrivate().getEncoded()));

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}
