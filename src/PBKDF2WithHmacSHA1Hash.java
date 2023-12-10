import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

// Password Base Key Derivation Function 2 Hash Massage Authentication Code With Secure Hash Algorithm 1
public class PBKDF2WithHmacSHA1Hash {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        String password = "password";

        try {
            String hashedPassword = generatePBKDF2Hash(password);
            System.out.println("Generated PBKDF2 hash: " + hashedPassword);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public static String generatePBKDF2Hash(String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] salt = getSalt();
        int iterationCount = 1; // Adjust according to your security requirements
        int keyLength = 256;
        char[] passwordChars = password.toCharArray();

        KeySpec keySpec = new PBEKeySpec(passwordChars, salt, iterationCount, keyLength);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");

        byte[] hashBytes = secretKeyFactory.generateSecret(keySpec).getEncoded();

        // Convert the salt and hash to hexadecimal strings
        String hexSalt = toHex(salt);
        String hexHash = toHex(hashBytes);

        // Combine the iteration count, salt, and hash in a string
        return iterationCount + ":" + hexSalt + ":" + hexHash;
    }

    private static byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    private static String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);

        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }
}
