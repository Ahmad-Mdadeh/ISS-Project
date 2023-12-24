import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

public class UserInteraction {

    private Socket socket;
    private ObjectOutputStream objectOut;
    private Scanner sc;
    private String permissions;
    private boolean isExit;
    String nationalNumber;
    SecretKey symmetricKey;

    public UserInteraction(Socket socket, ObjectOutputStream objectOut) {
        this.socket = socket;
        this.objectOut = objectOut;
        this.sc = new Scanner(System.in);
        this.nationalNumber = "";
        this.isExit = false;

    }

    public boolean getIsExit() {
        return isExit;
    }

    public String getNationalNumber() {
        return nationalNumber;
    }

    public void startInteraction() throws Exception {
        ArrayList<String> request = getUserLogin();
        this.nationalNumber = request.get(3);
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
                                System.out.println(
                                        "-------------------------------------------------------------------------");
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
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Enter:  E or e to exit.\n\tL or l to login.");
        System.out.print("Your Option : ");
        String tmp = sc.nextLine();

        while (!(tmp.equals("E") || tmp.equals("e") || tmp.equals("L") || tmp.equals("l"))) {
            System.out.print("Please Try again : ");
            tmp = sc.nextLine();
        }

        System.out.println("-------------------------------------------------------------------------");

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

        System.out.println("-------------------------------------------------------------------------");

        System.out.println("Enter:  E or e to exit.\n\tS or s to User Registration.");
        System.out.print("Your Option : ");

        String tmp = sc.nextLine();

        while (!(tmp.equals("E") || tmp.equals("e") || tmp.equals("S") || tmp.equals("s"))) {
            System.out.print("Please Try again : ");
            tmp = sc.nextLine();
        }

        System.out.println("-------------------------------------------------------------------------");

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
        PrintStream printStream = new PrintStream(socket.getOutputStream());

        if (request.get(0).equals("signup")) {
            symmetricKey = SymmetricCryptography.createAESKey(nationalNumber);
            System.out.println(
                    "The Symmetric Key is : " + DatatypeConverter.printHexBinary(symmetricKey.getEncoded()));
            encryptedRequest = SymmetricCryptography.encryptAES(request, symmetricKey);
            System.out.println("-------------------------------------------------------------------------");
        } else {
            encryptedRequest = request;
            System.out.println("-------------------------------------------------------------------------");
        }
        System.out.println("request sent !!");
        System.out.println("-------------------------------------------------------------------------");
        objectOut.writeObject(encryptedRequest);

        response = in.readLine();
        this.permissions = in.readLine();
        System.out.println("Server replied ===> " + response);


    }

}
