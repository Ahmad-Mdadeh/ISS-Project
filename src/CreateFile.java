import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;

public class CreateFile {
    static String publicKeyString;
    static String privetKeyString;
    static String id;

    static String fileName;

    static public void createFile() {
        try {
            FileWriter myWriter = new FileWriter("Client_", true);
            myWriter.write(id + System.lineSeparator());
            myWriter.write(publicKeyString + System.lineSeparator());
            myWriter.write(privetKeyString + System.lineSeparator() + System.lineSeparator());

            myWriter.close();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    static public boolean parseFile(String searchStr) throws FileNotFoundException {
        Scanner scan = new Scanner(new File("Client_"));
        while (scan.hasNext()) {
            String line = scan.nextLine().toString();
            if (line.contains(searchStr)) {
                System.out.println("+++++++++++++");
                System.out.println(line);
                return true;
            }
        }
        return false;
    }

    static public PublicKey getPublicKey() throws Exception {
        int numberOfLine = 0;
        Scanner scan = new Scanner(new File("Client_"));
        while (numberOfLine < 3) {
            numberOfLine++;
            String line = scan.nextLine();
            if (numberOfLine == 2) {
             
                return stringToPublicKey(line);
            }
        }
        return null;
    }

    static public PrivateKey getPrivatKey() throws Exception {
        int numberOfLine = 0;
        Scanner scan = new Scanner(new File("Client_"));
        while (numberOfLine < 3) {
            numberOfLine++;
            String line = scan.nextLine();
            if (numberOfLine == 3) {
                return stringToPrivateKey(line);
            }
        }
        return null;
    }

    public static void publicKeyToString(PublicKey publicKey) {
        byte[] publicKeyBytes = publicKey.getEncoded();
        publicKeyString = base64Encode(publicKeyBytes);

    }

    public static void privateKeyToString(PrivateKey privateKey) {
        byte[] privateKeyBytes = privateKey.getEncoded();
        privetKeyString = base64Encode(privateKeyBytes);
    }

    static public PublicKey stringToPublicKey(String publicKeyStringX) throws Exception {
        byte[] decodedPublicKeyBytes = base64Decode(publicKeyStringX);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedPublicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // Change this to the algorithm used for your public key
        return keyFactory.generatePublic(keySpec);
    }

    static public PrivateKey stringToPrivateKey(String privateKeyStringX) throws Exception {
        byte[] decodedPrivateKeyBytes = base64Decode(privateKeyStringX);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPrivateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // Change this to the algorithm used for your private key
        return keyFactory.generatePrivate(keySpec);
    }

    private static String base64Encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private static byte[] base64Decode(String base64String) {
        return Base64.getDecoder().decode(base64String);
    }

    public static void main(String[] args) {
        try {
            File myObj = new File("Client_");
            Scanner myReader = new Scanner(myObj);

            // Read and print each line in the file
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                System.out.println("================================");
                System.out.println(data);
            }

            System.out.println(CreateFile.parseFile("77"));
            myReader.close(); // Close the scanner

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

}
