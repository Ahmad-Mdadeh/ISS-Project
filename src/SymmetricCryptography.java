import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import javax.xml.bind.DatatypeConverter;

// Password Base Key  Derivation Function 2 Hash Massage Authentication Code With Secure Hash Algorithm 512
public class SymmetricCryptography {

    static SecretKey symmetricKey;
    private static final String key = "aesEncryptionKey";
    private static int keyBitSize = 256;

    // Function to create a secret key
    public static SecretKey createAESKey(String nationalNumber) throws Exception {

        // Step 1: Create a SecretKeyFactory using PBKDF2
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");

        // Step 2: Create a KeySpec using the provided password and key as salt
        KeySpec keySpec = new PBEKeySpec(nationalNumber.toCharArray(), key.getBytes(), 1, keyBitSize);

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

    public static SecretKey GenerateSessionKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = new SecureRandom();
        keyGenerator.init(keyBitSize, secureRandom);
        symmetricKey = keyGenerator.generateKey();
        return symmetricKey;

    }

}
