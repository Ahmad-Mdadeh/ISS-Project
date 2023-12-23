import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.security.*;
import java.security.KeyPair;

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


}
