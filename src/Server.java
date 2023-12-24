import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

// Server class 
class Server {
	static KeyPair keyPair;

	public static void main(String[] args) {
		ServerSocket server = null;

		try {

			// server is listening on port 1234
			server = new ServerSocket(1234);
			server.setReuseAddress(true);


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
		PrintWriter printWriterOut;
		SecretKey sessionKey;

		// Constructor
		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {
			OutputStream outObj = null;
			ObjectInputStream inObj = null;
			BufferedReader encryptTypeIn = null;
			PrintStream printStream = null;
			try {
				String response = "";
				ArrayList<String> received, decrypt = new ArrayList<>();

				// get the outputstream of client
				outObj = clientSocket.getOutputStream();
				inObj = new ObjectInputStream(clientSocket.getInputStream());
				encryptTypeIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				printStream = new PrintStream(outObj);
				printWriterOut = new PrintWriter(clientSocket.getOutputStream(), true);

				getEncryptedSessionKey(inObj);

				while (true) {
					this.encryptType = encryptTypeIn.readLine();

					// writing the received message from client
					System.out.println("new request");

					received = (ArrayList<String>) inObj.readObject();

					Operation operation = new Operation();

					switch (this.encryptType) {
						case "symmetric":
							decrypt = operation.decrypt(received, SymmetricCryptography.createAESKey(symmetricKey));
							break;
						case "pgp":

							decrypt = operation.decrypt(received, sessionKey);
							break;
						case "no":
							decrypt = received;
							break;
						default:
							break;
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
						System.out.print("Permission : ");
						System.out.println(resParts[1]);

					} else {
						printStream.println(response);
						printStream.println("-1");
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

		private void getEncryptedSessionKey(ObjectInputStream inObj) throws Exception {

			PublicKey publicKeyFromClient = (PublicKey) inObj.readObject();

			// generateSessionKey
			sessionKey = SymmetricCryptography.GenerateSessionKey();

			System.out.println("-------------------------------------------------------------------------");

			System.out.println(
					"The Server's Session Key is : " + DatatypeConverter.printHexBinary(sessionKey.getEncoded()));

			byte[] encryptedSessionKey = KeyGenerator.encrept(DatatypeConverter.printHexBinary(sessionKey.getEncoded()),
					publicKeyFromClient);

			System.out.println("-------------------------------------------------------------------------");

			System.out.println(
					"The Server's Encrypted Session Key is : " + DatatypeConverter.printHexBinary(encryptedSessionKey));

			System.out.println("-------------------------------------------------------------------------");

			// Send the Encrypted Session Key to Server
			printWriterOut.println(DatatypeConverter.printHexBinary(encryptedSessionKey));
		}

	}
}
