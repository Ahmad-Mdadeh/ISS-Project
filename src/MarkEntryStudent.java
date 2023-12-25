import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Scanner;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

public class MarkEntryStudent {

    private Socket socket;
    private ObjectOutputStream objectOut;
    private SecretKey sessionKey;
    private Scanner sc;
    private boolean isExit;
    private PrivateKey privateKey;
    static byte[] digitalSignature;

    public MarkEntryStudent(Socket socket, ObjectOutputStream objectOut, SecretKey sessionKey,
            PrivateKey privateKey) {
        this.socket = socket;
        this.objectOut = objectOut;
        this.sessionKey = sessionKey;
        this.sc = new Scanner(System.in);
        this.isExit = false;
        this.privateKey = privateKey;
        ;

    }

    public boolean getIsExit() {
        return isExit;
    }

    public byte[] getDigitalSignature() {
        return digitalSignature;
    }

    public void setMark() throws Exception {
        ArrayList<String> request = getUserInput();
        if (!(request.get(0).equals("exit"))) {
            processRequest(request);
            while (!isExit) {
                try {
                    request.clear();
                    request = getUserInput();
                    if (!(request.get(0).equals("exit"))) {
                        processRequest(request);
                    } else {
                        System.out.println("-------------------------------------------------------------------------");
                        isExit = true;
                        System.out.println("Exit");
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private ArrayList<String> getUserInput() {
        ArrayList<String> request = new ArrayList<>();
        System.out.print("Do You Want To Enter Mark (yes/no) : ");
        String input = sc.nextLine();
        if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no")) {
            request.add("exit");
            return request;
        } else {
            request.add("mark");
            System.out.println("-------------------------------------------------------------------------");
            System.out.println("Please Enter The Student's Name : ");
            System.out.print("Student's Name : ");
            request.add(sc.nextLine());
            System.out.print("Please Enter The Student's Mark : ");
            request.add(sc.nextLine());
            return request;
        }
    }

    private void processRequest(ArrayList<String> request) throws Exception {
        String response = "";
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ArrayList<String> encryptedRequest = new ArrayList<>();

        PrintStream printStream = new PrintStream(socket.getOutputStream());
        printStream.println("signature");
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("The Session Key is: " + DatatypeConverter.printHexBinary(sessionKey.getEncoded()));
        encryptedRequest = SymmetricCryptography.encryptAES(request, sessionKey);
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Request sent!!");
        System.out.println("-------------------------------------------------------------------------");
        digitalSignature = DigitalSignature.createDigitalSignature(encryptedRequest, privateKey);

        // send the message
        objectOut.writeObject(encryptedRequest);

        // send digitalSignature
        objectOut.writeObject(digitalSignature);
        // get plain response
        response = in.readLine();
        System.out.println("Server replied ===> " + response);
        if (!response.contains("Successful")) {
            System.out.println("-------------------------------------------------------------------------");
            System.out.println("Please Try Again");
            System.out.println("-------------------------------------------------------------------------");
            setMark();
        } else {
            isExit = true;
        }

    }
}