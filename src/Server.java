import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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

			// create Public and Private keys
			keyPair = KeyGenerator.generateKeyPair();

			System.out.println("-------------------------------------------------------------------------");
			System.out.println("Ther Public and Privet Key Hava Been Sent !!");
			System.out.println("-------------------------------------------------------------------------");

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
				printStream = new PrintStream(outObj);

				getEncryptedSessionKey(encryptedSessionKeyIn, ObjectdataOut, printStream);

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
							byte[] decryptSessionKeyByte = DatatypeConverter.parseHexBinary(decryptSessionKey);
							SecretKey secretKey = new SecretKeySpec(decryptSessionKeyByte, 0,
									decryptSessionKeyByte.length, "AES");
							decrypt = operation.decrypt(received, secretKey);
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

		private void getEncryptedSessionKey(BufferedReader encryptedSessionKeyIn, ObjectOutputStream ObjectdataOut, PrintStream printStream) throws Exception {
			// Send the public Key to client
			PublicKey publicKey = keyPair.getPublic();
			ObjectdataOut.writeObject(publicKey);
			// receive the Encrypt Session Key
			String encryptedSessionKey = encryptedSessionKeyIn.readLine();
			decryptSessionKey = KeyGenerator.decrypt(encryptedSessionKey, keyPair.getPrivate());
			System.out.println("The Session Key is :" + decryptSessionKey);
			System.out.println("-------------------------------------------------------------------------");
			printStream.println("The Session Key Has Been Received By The Server !!");

		}

	}
}
