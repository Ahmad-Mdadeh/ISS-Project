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
    private String encryptType;
    private SecretKey symmetricKey;
    private SecretKey sessionKey;
    private Scanner sc;
    private boolean isExit;
    String nationalNumber;

    public InformationUpdater(Socket socket, ObjectOutputStream objectOut, String encryptType, SecretKey sessionKey) {
        this.socket = socket;
        this.objectOut = objectOut;
        this.encryptType = encryptType;
        this.sessionKey = sessionKey;
        this.sc = new Scanner(System.in);
        this.isExit = false;

    }

    public boolean getIsExit() {
        return isExit;
    }

    public void setNationalNumber(String nationalNumber) {
        this.nationalNumber = nationalNumber;
    }

    public void setInformation() {
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
            System.out.println("Please Enter: 1-Phone Number, 2-Address, 3-Age.");
            request.add("completeInformation");
            System.out.print("Phone Number: ");
            request.add(sc.nextLine());
            System.out.print("Address: ");
            request.add(sc.nextLine());
            System.out.print("Age: ");
            request.add(sc.nextLine());

            return request;
        }

    }

    private void processRequest(ArrayList<String> request) throws Exception {
        String response = "";
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ArrayList<String> encryptedRequest = new ArrayList<>();

        if (encryptType.equals("0")) {
            objectOut.writeObject(request);
            response = in.readLine();
            System.out.println("Server replied ===> " + response);
        } else if (encryptType.equals("1")) {
            symmetricKey = Symmetric.createAESKey(nationalNumber);
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
        } else if (encryptType.equals("2")) {
            System.out.println(
                    "----------------------------------------------------------------------------------------------------------------------------");
            System.out.println("The Session Key is: " + DatatypeConverter.printHexBinary(sessionKey.getEncoded()));
            encryptedRequest = Symmetric.encryptAES(request, sessionKey);
            System.out.println(
                    "----------------------------------------------------------------------------------------------------------------------------");
            System.out.println("Request sent !!");
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
