import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;

public class CreateFile {
    static String publicKeyString;
    static String privetKeyString;
    static String fileName;

    static public void createFile(String id) {
        try {
            // Generate a unique filename based on the current timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
            fileName = "Client_" + timestamp + ".txt";

            File myObj = new File(fileName);
            myObj.createNewFile();

            FileWriter myWriter = new FileWriter(fileName);
            myWriter.write(id + System.lineSeparator());
            myWriter.write(publicKeyString + System.lineSeparator());
            myWriter.write(privetKeyString + System.lineSeparator());

            myWriter.close();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void publicKeyToString(PublicKey publicKey) {
        byte[] publicKeyBytes = publicKey.getEncoded();
        publicKeyString = base64Encode(publicKeyBytes);

    }

    public static void privateKeyToString(PrivateKey privateKey) {
        byte[] privateKeyBytes = privateKey.getEncoded();
        privetKeyString = base64Encode(privateKeyBytes);
    }

    private static String base64Encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static void main(String[] args) {
        try {
            File myObj = new File("Client_2023-12-25-16-24-10.txt");
            Scanner myReader = new Scanner(myObj);

            // Read and print each line in the file
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                System.out.println("================================");
                System.out.println(data);
            }

            myReader.close(); // Close the scanner

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

}
