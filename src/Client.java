import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

// Client class
class Client {
	private ObjectOutputStream objectOut = null;
	private ObjectInputStream objectIn = null;
	private Socket socket = null;
	private Scanner sc = null;
	String encryptType = "";
	SecretKey sessionKey;
	PublicKey publicKeyFromServer = null;
	byte[] encryptedSessionKey = null;
	String permissions = "";
	BufferedReader in;

	// driver code
	public Client(String address, int port) {
		KeyPair keyPair;
		PublicKey publicKey = null;
		PrivateKey privateKey = null;
		PrintWriter printWriterOut = null;
		try {

			// create Public and Private keys
			keyPair = Hyper.generateKeyPair();
			publicKey = keyPair.getPublic();
			privateKey = keyPair.getPrivate();

			// System.out.println(
			// "The Public Key is: "
			// + DatatypeConverter.printHexBinary(
			// publicKey.getEncoded()));
			// System.out.println(
			// "----------------------------------------------------------------------------------------------------------------------------");
			// System.out.println(
			// "The Private Key is: "
			// + DatatypeConverter.printHexBinary(
			// privateKey.getEncoded()));

			// creating an object of socket
			socket = new Socket(address, port);

			System.out.println("Connection Established!! ");

			// taking input from user
			sc = new Scanner(System.in);

			// opening output stream on the socket
			objectOut = new ObjectOutputStream(socket.getOutputStream());
			objectIn = new ObjectInputStream(socket.getInputStream());
			printWriterOut = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintStream printStream = new PrintStream(socket.getOutputStream());

			System.out
					.println("Enter:  0 for no Encryption\n\t1 for symmetric Encryption\n\t2 forAsymmetric Encryption");
			System.out.print(
					"Your Option : ");
			this.encryptType = sc.nextLine();

			while (!(this.encryptType.equals("0") || this.encryptType.equals("1") || this.encryptType.equals("2"))) {
				System.out.print(
						"Please Try again : ");
				this.encryptType = sc.nextLine();
			}

			printStream.println(this.encryptType);

			if (this.encryptType.equals("2")) {
				// received Public Key From Server
				publicKeyFromServer = (PublicKey) objectIn.readObject();

				// generateSessionKey
				sessionKey = Symmetric.GenerateSessionKey();

				System.out.println(
						"----------------------------------------------------------------------------------------------------------------------------");
				System.out
						.println("The Session Key is :" + DatatypeConverter.printHexBinary(sessionKey.getEncoded()));
				encryptedSessionKey = Hyper.encrept(DatatypeConverter.printHexBinary(sessionKey.getEncoded()),
						publicKeyFromServer);
				System.out.println(
						"----------------------------------------------------------------------------------------------------------------------------");
				System.out.println(
						"The Encrypted Session Key is :" + DatatypeConverter.printHexBinary(encryptedSessionKey));
				System.out.println(
						"----------------------------------------------------------------------------------------------------------------------------");

				// Send the Encrypted Session Key to Server
				printWriterOut.println(DatatypeConverter.printHexBinary(encryptedSessionKey));

				System.out.println(in.readLine());
			}

			// UserInteraction
			UserInteraction userInteraction = new UserInteraction(socket, objectOut, encryptType, sessionKey);
			userInteraction.startInteraction();

			System.out.println(
					"----------------------------------------------------------------------------------------------------------------------------\n");

			if (userInteraction.getIsExit()) {
				return;
			}

			// InformationUpdater
			InformationUpdater informationUpdater = new InformationUpdater(socket, objectOut, encryptType, sessionKey);
			informationUpdater.setNationalNumber(userInteraction.getNationalNumber());
			informationUpdater.setInformation();

			System.out.println(
					"----------------------------------------------------------------------------------------------------------------------------\n");

			if (userInteraction.getIsExit()) {
				return;
			}

			PracticalProjects practicalProjects = new PracticalProjects(socket, objectOut, sessionKey);
			practicalProjects.setDescriptionOfPracticalProjects();

			System.out.println(
					"----------------------------------------------------------------------------------------------------------------------------\n");

			if (userInteraction.getIsExit()) {
				return;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			objectOut.close();
			socket.close();
		} catch (IOException io) {
			System.out.println(io);
		}
	}

	public static void main(String argvs[]) {
		// creating object of class Client
		new Client("localhost", 1234);

	}
}
