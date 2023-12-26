import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Scanner;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class UserInteraction {

    private Socket socket;
    private ObjectOutputStream objectOut;
    private Scanner sc;
    private String permissions;
    private boolean isExit;
    private String nationalNumber;
    private SecretKey symmetricKey;
    private SecretKeySpec sessionKey;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private KeyPair keyPair;
    private String encryptedSessionKey;
    private String response;
    private String id;

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

    public String getPermission() {
        return permissions;
    }

    public void startInteraction() throws Exception {
        ArrayList<String> request = getUserInput();
        this.nationalNumber = request.get(3);
        if (!(request.get(0).equals("exit"))) {
            processRequest(request);
            while (true) {
                try {
                    if (permissions.equals("0")) {
                        request.clear();
                        request = getUserInputRegistration();
                        if (!(request.get(0).equals("exit"))) {
                            processRequest(request);
                        } else {
                            request.clear();
                            request = getUserInput();
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

    private ArrayList<String> getUserInput() {

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

    private ArrayList<String> getUserInputRegistration() {

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

    private void getDecryptSessionKey() throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // create Public and Private keys
        keyPair = KeyGenerator.generateKeyPair();

        // Send the public Key to client
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();

        objectOut.writeObject(publicKey);

        System.out.println("-------------------------------------------------------------------------");

        System.out.println("The Public Key Hava Been Sent !!");

        System.out.println("-------------------------------------------------------------------------");
        System.out.println("The client's public key is:\n" + DatatypeConverter.printHexBinary(publicKey.getEncoded()));
        System.out.println("-------------------------------------------------------------------------");
        System.out
                .println("The client's Private Key is:\n" + DatatypeConverter.printHexBinary(privateKey.getEncoded()));
        System.out.println("-------------------------------------------------------------------------");

        encryptedSessionKey = in.readLine();

        System.out.println("==============");
        this.response = in.readLine();
        this.permissions = in.readLine();
        System.out.println(response);
        System.out.println("==============");
        System.out.println(permissions);
        System.out.println("==============");
        this.id = in.readLine();
        System.out.println(id);

        System.out.println("The Server's Encrypted Session Key is:\n" +
                encryptedSessionKey);
        System.out.println("-------------------------------------------------------------------------");

        String decryptSessionKey = KeyGenerator.decrypt(encryptedSessionKey, keyPair.getPrivate());
        byte[] decryptSessionKeyByte = DatatypeConverter.parseHexBinary(decryptSessionKey);

        sessionKey = new SecretKeySpec(decryptSessionKeyByte, 0, decryptSessionKeyByte.length, "AES");
        System.out.println("The Server's Session Key is:\n" + decryptSessionKey);
        System.out.println("-------------------------------------------------------------------------");

    }

    private void processRequest(ArrayList<String> request) throws Exception {
        ArrayList<String> encryptedRequest = new ArrayList<>();
        PrintStream printStream = new PrintStream(socket.getOutputStream());
        if (request.get(0).equals("signup")) {
            printStream.println("symmetric");
            symmetricKey = SymmetricCryptography.createAESKey(nationalNumber);
            System.out.println(
                    "The Symmetric Key is : " + DatatypeConverter.printHexBinary(symmetricKey.getEncoded()));
            encryptedRequest = SymmetricCryptography.encryptAES(request, symmetricKey);
            System.out.println("-------------------------------------------------------------------------");

        } else {
            printStream.println("no");
            encryptedRequest = request;
        }

        objectOut.writeObject(encryptedRequest);
        getDecryptSessionKey();

        System.out.println("request sent !!");
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Server replied ===> " + response);
        
        if (!response.contains("Successful")) {
            System.out.println("-------------------------------------------------------------------------");
            System.out.println("Please Try Again");
            startInteraction();
        } else {
            CreateFile.publicKeyToString(publicKey);
            CreateFile.privateKeyToString(privateKey);
            CreateFile.createFile(id);
        }

    }

}
