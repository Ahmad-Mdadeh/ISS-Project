import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.spec.KeySpec;
import java.util.ArrayList;

import javax.xml.bind.DatatypeConverter;

public class Symmetric {

    static SecretKey symmetricKey;
    private static final String key = "aesEncryptionKey";

    // Function to create a secret key
    // Function to create a secret key
    public static SecretKey createAESKey(String password) throws Exception {

        // Step 1: Create a SecretKeyFactory using PBKDF2
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");

        // Step 2: Create a KeySpec using the provided password and key as salt
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), key.getBytes(), 1, 256);

        // Step 3: Generate a secret key using the SecretKeyFactory and KeySpec
        SecretKey secretKeyFromPBKDF2 = secretKeyFactory.generateSecret(keySpec);

        // Step 4: Create a SecretKeySpec from the derived key bytes
        symmetricKey = new SecretKeySpec(secretKeyFromPBKDF2.getEncoded(), "AES");

        // Step 5: Return the generated symmetric key
        return symmetricKey;

    }

    // Function to encrypt a string using AES
    static ArrayList<String> encryptAES(ArrayList<String> plainText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        ArrayList<String> encryptedTextList = new ArrayList<>();

        for (String text : plainText) {
            byte[] encryptedBytes = cipher.doFinal(text.getBytes());
            encryptedTextList.add(DatatypeConverter.printHexBinary(encryptedBytes));
        }

        return encryptedTextList;
    }

    // Function to decrypt an AES-encrypted string
    static ArrayList<String> decryptAES(ArrayList<String> encryptedTextList, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        ArrayList<String> decryptedTextList = new ArrayList<>();

        for (String encryptedText : encryptedTextList) {
            byte[] encryptedBytes = DatatypeConverter.parseHexBinary(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            decryptedTextList.add(new String(decryptedBytes));
        }

        return decryptedTextList;
    }

    public static void main(String[] args) {
        try {
            // Step 6: Generate the AES key using the createAESKey function
            SecretKey key = createAESKey("your_password_here");

            // Optional: Print the key in hexadecimal format
            System.out.println(
                    "Generated Key (Hex): " + javax.xml.bind.DatatypeConverter.printHexBinary(key.getEncoded()));
            // System.out.println("=======================");
            // ArrayList<String> s = encryptAES("TEST", key);
            // System.out.println(s);
            // System.out.println("+++++++++++++++++++++++");
            // System.out.println(decryptAES(s, key));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
