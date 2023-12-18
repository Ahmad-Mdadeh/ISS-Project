import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

public class InformationUpdater {

    private Socket socket;
    private ObjectOutputStream objectOut;
    private String EncryptType;
    private SecretKey symmetricKey;
    private Scanner sc;
    private boolean isExit;

    public InformationUpdater(Socket socket, ObjectOutputStream objectOut, String EncryptType) {
        this.socket = socket;
        this.objectOut = objectOut;
        this.EncryptType = EncryptType;
        this.sc = new Scanner(System.in);
        this.isExit = false;

    }

    public boolean getIsExit() {
        return isExit;
    }

    public void updateInformation() {
        ArrayList<String> request = getUserInput();
        try {
            if (!request.get(0).equals("exit"))
                processRequest(request);
            else {
                System.out.println(
                        "----------------------------------------------------------------------------------------------------------------------------");
                System.out.println("Exit");
                isExit = true;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private ArrayList<String> getUserInput() {
        ArrayList<String> request = new ArrayList<>();
        System.out.print("Do want complete information (yes/no) : ");
        String input = sc.nextLine();
        if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no")) {
            request.add("exit");
            return request;
        } else {
            System.out.println(
                    "----------------------------------------------------------------------------------------------------------------------------");
            System.out.println("Please Enter: 1-Phone Number, 2-Address, 3-Age, 4-NationalNumber");
            request.add("completeInformation");
            System.out.print("Phone Number: ");
            request.add(sc.nextLine());
            System.out.print("Address: ");
            request.add(sc.nextLine());
            System.out.print("Age: ");
            request.add(sc.nextLine());
            System.out.print("NationalNumber: ");
            request.add(sc.nextLine());
            return request;
        }

    }

    private void processRequest(ArrayList<String> request) throws Exception {
        String response = "";
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ArrayList<String> encryptedRequest = new ArrayList<>();

        if (EncryptType.equals("0")) {
            objectOut.writeObject(request);
            response = in.readLine();
            System.out.println("Server replied ===> " + response);
        } else if (EncryptType.equals("1")) {
            symmetricKey = Symmetric.createAESKey("03150040010");
            System.out.println(
                    "----------------------------------------------------------------------------------------------------------------------------");
            System.out.println("The Symmetric Key is: " + DatatypeConverter.printHexBinary(symmetricKey.getEncoded()));
            encryptedRequest = Symmetric.encryptAES(request, symmetricKey);
            System.out.println(encryptedRequest.get(1));
            System.out.println(
                    "----------------------------------------------------------------------------------------------------------------------------");
            System.out.println("Request sent!!");
            System.out.println(
                    "----------------------------------------------------------------------------------------------------------------------------");
            objectOut.writeObject(encryptedRequest);
            // get plain response
            response = in.readLine();
            System.out.println("Server replied ===> " + response);
            System.out.println(
                    "----------------------------------------------------------------------------------------------------------------------------");
        }
    }

}
