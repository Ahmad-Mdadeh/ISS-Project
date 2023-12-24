import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

// Client class
class Client {
	private ObjectOutputStream objectOut = null;
	private Socket socket = null;
	SecretKey sessionKey = null;
	PublicKey publicKeyFromServer = null;
	byte[] encryptedSessionKey = null;
	String permissions = "";
	BufferedReader in;
	KeyPair keyPair;
	PrintWriter printWriterOut = null;

	// driver code
	public Client(String address, int port) {

		try {

			// creating an object of socket
			socket = new Socket(address, port);

			System.out.println("Connection Established!! ");

			// opening output stream on the socket
			objectOut = new ObjectOutputStream(socket.getOutputStream());
			printWriterOut = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			getDecryptSessionKey();

			// UserInteraction
			UserInteraction userInteraction = new UserInteraction(socket, objectOut);
			userInteraction.startInteraction();

			System.out.println("-------------------------------------------------------------------------");

			if (userInteraction.getIsExit()) {
				return;
			}

			// InformationUpdater
			InformationUpdater informationUpdater = new InformationUpdater(socket,
					objectOut);
			informationUpdater.setNationalNumber(userInteraction.getNationalNumber());
			informationUpdater.setInformation();

			System.out.println("-------------------------------------------------------------------------");

			if (informationUpdater.getIsExit()) {
				return;
			}

			PracticalProjects practicalProjects = new PracticalProjects(socket, objectOut, sessionKey);
			practicalProjects.setDescriptionOfPracticalProjects();

			System.out.println("-------------------------------------------------------------------------");

			if (practicalProjects.getIsExit()) {
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

	private void getDecryptSessionKey() throws Exception {

		// create Public and Private keys
		keyPair = KeyGenerator.generateKeyPair();

		// Send the public Key to client
		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();

		objectOut.writeObject(publicKey);

		System.out.println("-------------------------------------------------------------------------");
		System.out.println("The client's public key is : " + DatatypeConverter.printHexBinary(publicKey.getEncoded()));
		System.out.println("-------------------------------------------------------------------------");
		System.out
				.println("The client's Private Key is : " + DatatypeConverter.printHexBinary(privateKey.getEncoded()));
		System.out.println("-------------------------------------------------------------------------");

		String encryptedSessionKey = in.readLine();
		String decryptSessionKey = KeyGenerator.decrypt(encryptedSessionKey, keyPair.getPrivate());
		byte[] decryptSessionKeyByte = DatatypeConverter.parseHexBinary(decryptSessionKey);

		sessionKey = new SecretKeySpec(decryptSessionKeyByte, 0, decryptSessionKeyByte.length, "AES");

		System.out.println("The Server's Session Key is : " + decryptSessionKey);
		System.out.println("-------------------------------------------------------------------------");
		System.out.println("Ther Public and Privet Key Hava Been Sent !!");

	}

	public static void main(String argvs[]) {
		// creating object of class Client
		new Client("localhost", 1234);

	}
}
