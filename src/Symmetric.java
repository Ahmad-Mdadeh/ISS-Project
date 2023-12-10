import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import javax.crypto.*;


public class Symmetric {

    static SecretKey symmetricKey;
    private static final String key = "aesEncryptionKey";

    // Function to create a secret key
   // Function to create a secret key
   public static SecretKey createAESKey(String password) throws Exception {

       // Step 1: Create a SecretKeyFactory using PBKDF2
       SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");

       // Step 2: Create a KeySpec using the provided password and key as salt
       KeySpec keySpec = new PBEKeySpec(password.toCharArray(), key.getBytes(), 1, 128);

       // Step 3: Generate a secret key using the SecretKeyFactory and KeySpec
       SecretKey secretKeyFromPBKDF2 = secretKeyFactory.generateSecret(keySpec);

       // Step 4: Create a SecretKeySpec from the derived key bytes
       symmetricKey = new SecretKeySpec(secretKeyFromPBKDF2.getEncoded(), "AES");

       // Step 5: Return the generated symmetric key
       return symmetricKey;

   }


   public static String encrypt(String data, GCMParameterSpec GCM) {
        String s = "";
        Cipher cipher = null;
        try {

            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, symmetricKey); // or Cipher.DECRYPT_MODE

            byte[] encrypted = cipher.doFinal(data.getBytes());
            s = Base64.getEncoder().encodeToString(encrypted);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static String decrypt(String data, GCMParameterSpec GCM) {
        byte[] encrypted = Base64.getDecoder().decode(data);

        String s = "";
        Cipher cipher = null;
        try {

            cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.DECRYPT_MODE, symmetricKey);

            byte[] decrypted = cipher.doFinal(encrypted);
            s = new String(decrypted, StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return s;
    }

    
    public static void main(String[] args) {
        try {
            // Step 6: Generate the AES key using the createAESKey function
            SecretKey key = createAESKey("your_password_here");
         

            // Optional: Print the key in hexadecimal format
            System.out.println("Generated Key (Hex): " + javax.xml.bind.DatatypeConverter.printHexBinary(key.getEncoded()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
