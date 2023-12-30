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
    private KeyPair keyPair;
    String nationalNumber;
    SecretKey symmetricKey;
    String SessionKey;
    BufferedReader in;

    public UserInteraction(Socket socket, ObjectOutputStream objectOut,KeyPair keyPair) {
        this.socket = socket;
        this.objectOut = objectOut;
        this.sc = new Scanner(System.in);
        this.nationalNumber = "";
        this.isExit = false;
        this.keyPair = keyPair;

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
        if(request.get(0).equals("login")){
            String certificatePassword;
            while(true){
                System.out.print("Enter certificate password (8 characters): ");
                certificatePassword = sc.nextLine();
                if (certificatePassword.length() == 8) {
                    request.add(certificatePassword);
                    break; 
                } else {
                    System.out.println("Invalid password length. Please enter a password with 8 characters.");
                }
            }
        }
    }

    private void processRequest(ArrayList<String> request) throws Exception {
        String response;
        ArrayList<String> encryptedRequest = new ArrayList<>();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintStream printStream = new PrintStream(socket.getOutputStream());

        if (request.get(0).equals("signup")) {
            printStream.println("symmetric");
            symmetricKey = SymmetricCryptography.createAESKey(nationalNumber);
            System.out.println(
                    "The Symmetric Key is : " + DatatypeConverter.printHexBinary(symmetricKey.getEncoded()));
            encryptedRequest = SymmetricCryptography.encryptAES(request, symmetricKey);
            System.out.println("-------------------------------------------------------------------------");
            objectOut.writeObject(encryptedRequest);
        } else {
            printStream.println("no");
            Boolean status;
            //############################################################
            String resultSearch = DigitalCertificate.searchValueInFile(request.get(1)+"_"+request.get(4),"SaveCertificate.txt");
            if(resultSearch != null){
                encryptedRequest.add(resultSearch); 
                System.out.println("your certificate:");
                System.out.println(resultSearch);
                objectOut.writeObject(encryptedRequest);
                encryptedRequest.clear();
                System.out.println("------------------------------------------------------");
                status =true;
            }else{
                    System.out.println("must create certificate");
                    String CertificateRequest = DigitalCertificate.InformationCSR(keyPair,request);
                    encryptedRequest.add("request");
                    encryptedRequest.add(CertificateRequest);
                    objectOut.writeObject(encryptedRequest);
                    encryptedRequest.clear();
                    System.out.println(in.readLine());
                    System.out.println(in.readLine());
                    System.out.println(in.readLine());
                    printStream.println(sc.nextLine());
                    String message = in.readLine();
                    if(!message.contains("warning::")){
                        String publicKeyString = DigitalCertificate.readFileContent("ServerPublicKey.txt");
                        PublicKey publicKey = DigitalCertificate.convertStringToPublicKey(publicKeyString);
                        System.out.println("response:"+DigitalCertificate.CertificateValidation(message, publicKey));
                        System.out.println("------------------------------------------------------");
                        String Search = DigitalCertificate.searchValueInFile(request.get(1)+"_"+request.get(4),"SaveCertificate.txt");
                        System.out.println("your certificate:");
                        printStream.println(Search);
                        System.out.println(Search);
                        System.out.println("------------------------------------------------------");
                        status =true;
                    }else{
                        System.out.println(message);
                        printStream.println(message);
                        status =false;
                    }  
            }
            if(status){
                String encryptedSessionKey = in.readLine();
                if(!encryptedSessionKey.contains("warning::")){
                    System.out.println("session key:");
                    String privateKeyString = DigitalCertificate.searchValueInFile(request.get(1)+"_"+request.get(4),"ClientPrivateKey.txt");
                    String[] InfoCsr = privateKeyString.split("\\|");
                    PrivateKey privateKey = DigitalCertificate.convertStringToPrivateKey(InfoCsr[1]);
                    String sessionKeyString = KeyGenerator.decrypt(encryptedSessionKey, privateKey);
                    System.out.println(sessionKeyString);
                    System.out.println("-------------------------------------------------------------------------");
                    encryptedRequest.clear();
                    byte[] decryptSessionKeyByte = DatatypeConverter.parseHexBinary(sessionKeyString);
                    SecretKey sessionKey = new SecretKeySpec(decryptSessionKeyByte, 0, decryptSessionKeyByte.length, "AES");
                    encryptedRequest = SymmetricCryptography.encryptAES(request,sessionKey);
                    System.out.println("request sent !!");
                    System.out.println("-------------------------------------------------------------------------");
                    objectOut.writeObject(encryptedRequest);
                }else{
                    System.out.println("warning:");
                    System.out.println(encryptedSessionKey);
                    System.out.println("-------------------------------------------------------------------------");
                }
            }else{
            objectOut.writeObject(request);
           }
            
            // ######################################################
        }
        

        response = in.readLine();
        this.permissions = in.readLine();
        System.out.println("Server replied ===> " + response);
        if (!response.contains("Successful")) {
            System.out.println("-------------------------------------------------------------------------");
            System.out.println("Please Try Again");
            startInteraction();

        }

    }

}
