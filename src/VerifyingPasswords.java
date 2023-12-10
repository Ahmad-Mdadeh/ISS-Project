import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class VerifyingPasswords {
    // iterationCount : salt : hash
    public static void main(String[] args)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        String originalPassword = "password";

        String generatedSecuredPasswordHash = PBKDF2.generatePBKDF2Hash(originalPassword);
        System.out.println(generatedSecuredPasswordHash);

        boolean matched = validatePassword("password", generatedSecuredPasswordHash);
        System.out.println(matched);

        matched = validatePassword("password", generatedSecuredPasswordHash);
        System.out.println(matched);
    }

    public static String hashedPassword(String pass) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String originalPassword = pass;

        String generatedSecuredPasswordHash = PBKDF2.generatePBKDF2Hash(originalPassword);
        System.out.println(generatedSecuredPasswordHash);

        return generatedSecuredPasswordHash;
    }

    public static boolean validatePassword(String originalPassword, String storedPassword)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Split the stored password into its components
        String[] parts = storedPassword.split(":");
        // extracts the iteration count, salt, and hash from the stored password
        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = fromHex(parts[1]);
        byte[] hash = fromHex(parts[2]);

        PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(),
                salt, iterations, hash.length * 8);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hashBytes = factory.generateSecret(spec).getEncoded();

        int diff = hash.length ^ hashBytes.length;
        for (int i = 0; i < hash.length && i < hashBytes.length; i++) {
            diff |= hash[i] ^ hashBytes[i];
        }
        return diff == 0;
    }

    public static byte[] fromHex(String hex) throws NoSuchAlgorithmException {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}
    