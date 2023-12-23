import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

//  TLS handshake (Transport Layer Security)
// Server class 
class Server {
	static KeyPair keyPair;
	private static PublicKey publicKey = null;
	private static PrivateKey privateKey = null;

	public static void main(String[] args) {
		ServerSocket server = null;

		try {

			// server is listening on port 1234
			server = new ServerSocket(1234);
			server.setReuseAddress(true);

			// create Public and Private keys
			keyPair = Hyper.generateKeyPair();
			publicKey = keyPair.getPublic();
			privateKey = keyPair.getPrivate();
			System.out.println(
					"----------------------------------------------------------------------------------------------------------------------------");
			System.out.println("Ther Public and Privet Key Hava Been Sent !!");
			System.out.println(
					"----------------------------------------------------------------------------------------------------------------------------");
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

			// running infinite loop for getting client request
			while (true) {

				// socket object to receive incoming client requests
				Socket client = server.accept();

				// Displaying that new client is connected to server
				System.out.println("New client connected : "
						+ client.getInetAddress()
								.getHostAddress());

				// create a new thread object
				ClientHandler clientSock = new ClientHandler(client);

				// This thread will handle the client separately
				new Thread(clientSock).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (server != null) {
				try {
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// ClientHandler class
	private static class ClientHandler implements Runnable {
		private final Socket clientSocket;
		private String encryptType;
		private String symmetricKey;
		private String decryptSessionKey;

		// Constructor
		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {
			OutputStream outObj = null;
			ObjectInputStream inObj = null;
			BufferedReader encryptTypeIn = null;
			BufferedReader encryptedSessionKeyIn = null;

			PrintWriter printWriterOut = null;
			ObjectOutputStream ObjectdataOut = null;
			PrintStream printStream = null;
			try {
				String response = "";
				ArrayList<String> received, decrypt = new ArrayList<>();

				// get the outputstream of client
				outObj = clientSocket.getOutputStream();
				ObjectdataOut = new ObjectOutputStream(outObj);
				inObj = new ObjectInputStream(clientSocket.getInputStream());
				encryptTypeIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				encryptedSessionKeyIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				printWriterOut = new PrintWriter(outObj, true);
				printStream = new PrintStream(outObj);
				this.encryptType = encryptTypeIn.readLine();

				if (this.encryptType.equals("2")) {
					// Send Serve public Key
					PublicKey publicKey = keyPair.getPublic();
					ObjectdataOut.writeObject(publicKey);
					// receive the Encrypt Session Key
					String encryptedSessionKey = encryptedSessionKeyIn.readLine();
					decryptSessionKey = Hyper.decrypt(encryptedSessionKey, keyPair.getPrivate());
					System.out.println("The Session Key is :" + decryptSessionKey);
					System.out.println(
							"----------------------------------------------------------------------------------------------------------------------------");
					printStream.println("The Session Key Has Been Received By The Server !!");
				}

				while (true) {
					// writing the received message from
					// client
					System.out.println("new request");

					received = (ArrayList<String>) inObj.readObject();
					Operation operation = new Operation();

					// Decrypt the Received
					if (this.encryptType.equals("1")) {
						if (received.get(0).equals("login")) {
							decrypt = received;
						} else {
							decrypt = operation.decrypt(received, Symmetric.createAESKey(symmetricKey));
						}
					} else if (this.encryptType.equals("2")) {
						if (received.get(0).equals("login")) {
							decrypt = received;
						} else {
							byte[] decryptSessionKeyByte = DatatypeConverter.parseHexBinary(decryptSessionKey);
							SecretKey secretKey = new SecretKeySpec(decryptSessionKeyByte, 0,
									decryptSessionKeyByte.length, "AES");
							decrypt = operation.decrypt(received, secretKey);
						}
					}

					operation.getRequest(decrypt);
					response = operation.auth();
					if (response.contains("Successful")) {
						String[] resParts = response.split("! ");
						printStream.println(resParts[0]);
						printStream.println(resParts[1]);
						if (3 <= resParts.length) {
							this.symmetricKey = resParts[2];
						}
						System.out.print("Permission :");
						System.out.println(resParts[1]);

					} else {
						printStream.println(response);
						System.out.println(response);
					}
					received.clear();
					decrypt.clear();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {

			}
		}
	}
}
