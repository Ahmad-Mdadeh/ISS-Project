import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

public class PracticalProjects {

    private Socket socket;
    private ObjectOutputStream objectOut;
    private SecretKey sessionKey;
    private Scanner sc;
    private boolean isExit;

    public PracticalProjects(Socket socket, ObjectOutputStream objectOut,
            SecretKey sessionKey) {
        this.socket = socket;
        this.objectOut = objectOut;
        this.sessionKey = sessionKey;
        this.sc = new Scanner(System.in);
        this.isExit = false;

    }

    public boolean getIsExit() {
        return isExit;
    }

    public void setDescriptionOfPracticalProjects() throws Exception {
        ArrayList<String> request = getUserInput();
        if (!(request.get(0).equals("exit"))) {
            processRequest(request);
            while (true) {
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
        System.out.print("Do want set Description Of Practical Projects (yes/no) : ");
        String input = sc.nextLine();
        if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no")) {
            request.add("exit");
            return request;
        } else {
            System.out.println("-------------------------------------------------------------------------");
            request.add("projects");
            System.out.print("Enter Name Of the Projects : ");
            String projects = sc.nextLine();
            System.out.print("Enter Description Of the Projects : ");
            String description = sc.nextLine();
            request.add(projects + ":" + description);
            return request;
        }
    }

    private void processRequest(ArrayList<String> request) throws Exception {
        String response = "";
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ArrayList<String> encryptedRequest = new ArrayList<>();
        PrintStream printStream = new PrintStream(socket.getOutputStream());
        printStream.println("pgp");
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("The Session Key is: " + DatatypeConverter.printHexBinary(sessionKey.getEncoded()));
        encryptedRequest = SymmetricCryptography.encryptAES(request, sessionKey);
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Request sent!!");
        System.out.println("-------------------------------------------------------------------------");
        objectOut.writeObject(encryptedRequest);
        // get plain response
        response = in.readLine();
        System.out.println("Server replied ===> " + response);
        System.out.println("-------------------------------------------------------------------------");

    }
}