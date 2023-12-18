import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

public class UserInteraction {

    private Socket socket;
    private ObjectOutputStream objectOut;
    private String EncryptType;
    private SecretKey symmetricKey;
    private Scanner sc;
    private String permissions;
    private boolean isExit;

   

    public UserInteraction(Socket socket, ObjectOutputStream objectOut, String EncryptType) {
        this.socket = socket;
        this.objectOut = objectOut;
        this.EncryptType = EncryptType;
        this.sc = new Scanner(System.in);
        this.permissions = "";
        this.isExit = false;

    }

    public boolean getIsExit() {
        return isExit;
    }
    public void startInteraction() throws Exception {
        ArrayList<String> request = getUserLogin();
        if (!(request.get(0).equals("exit"))) {
            processRequest(request);
            while (true) {
                try {
                    if (permissions.equals("0")) {
                        request.clear();
                        request = getUserRegistration();
                        if (!(request.get(0).equals("exit"))) {
                            processRequest(request);
                        } else {
                            request.clear();
                            request = getUserLogin();
                            if (!(request.get(0).equals("exit"))) {
                                processRequest(request);
                            } else {
                                isExit = true;
                                System.out.println("Exit");
                                break;
                            }

                        }
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            isExit = true;
            System.out.println("Exit");
        }
    }

    private ArrayList<String> getUserLogin() {

        ArrayList<String> request = new ArrayList<>();
        System.out.println(
                "----------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Enter:  E or e to exit.\n\tL or l to login.");
        System.out.print("Your Option : ");
        String tmp = sc.nextLine();

        while (!(tmp.equals("E") || tmp.equals("e") || tmp.equals("L") || tmp.equals("l"))) {
            System.out.print("Please Try again : ");
            tmp = sc.nextLine();
        }

        System.out.println(
                "----------------------------------------------------------------------------------------------------------------------------");

        switch (tmp) {
            case "E":
            case "e":
                request.add("exit");
                break;
            case "L":
            case "l":
                request.add("login");
                getUserInformation(request);
                break;
            default:
                break;
        }

        return request;
    }

    private ArrayList<String> getUserRegistration() {

        ArrayList<String> request = new ArrayList<>();

        System.out.println(
                "----------------------------------------------------------------------------------------------------------------------------");

        System.out.println("Enter:  E or e to exit.\n\tS or s to User Registration.");
        System.out.print("Your Option : ");

        String tmp = sc.nextLine();

        while (!(tmp.equals("E") || tmp.equals("e") || tmp.equals("S") || tmp.equals("s"))) {
            System.out.print("Please Try again : ");
            tmp = sc.nextLine();
        }

        System.out.println(
                "----------------------------------------------------------------------------------------------------------------------------");

        switch (tmp) {
            case "E":
            case "e":
                request.add("exit");
                break;
            case "S":
            case "s":
                request.add("signup");
                getUserInformation(request);
                break;
            default:
                break;
        }

        return request;
    }

    private void getUserInformation(ArrayList<String> request) {
        System.out.println("Please Enter User Information :");
        System.out.print("User Name : ");
        request.add(sc.nextLine());
        System.out.print("Password : ");
        String p = sc.nextLine();

        while (p.length() < 8) {
            System.out.println(" Please Retype Password Of 8 Char At Least.");
            System.out.print("Password : ");
            p = sc.nextLine();
        }

        request.add(p);
        System.out.print("NationalNumber : ");
        request.add(sc.nextLine());
    }

    private void processRequest(ArrayList<String> request) throws Exception {
        String response;
        ArrayList<String> encryptedRequest = new ArrayList<>();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        if (EncryptType.equals("0")) {
            objectOut.writeObject(request);
            response = in.readLine();
            System.out.println("Server replied ===> " + response);
        } else if (EncryptType.equals("1")) {
            symmetricKey = Symmetric.createAESKey("03150040010");
            System.out.println(
                    "----------------------------------------------------------------------------------------------------------------------------");
            System.out.println("The Symmetric Key is : " + DatatypeConverter.printHexBinary(symmetricKey.getEncoded()));
            encryptedRequest = Symmetric.encryptAES(request, symmetricKey);
            System.out.println(
                    "----------------------------------------------------------------------------------------------------------------------------");
            System.out.println("request sent !!");
            System.out.println(
                    "----------------------------------------------------------------------------------------------------------------------------");
            objectOut.writeObject(encryptedRequest);
            response = in.readLine();
            permissions = in.readLine();
            System.out.println("Server replied ===> " + response);
        } else if (EncryptType.equals("2")) {
            // Handle case when EncryptType is 2
        }

    }

}
