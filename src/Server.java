import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import javax.xml.bind.DatatypeConverter;

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
					"The Public Key is: "
							+ DatatypeConverter.printHexBinary(
									publicKey.getEncoded()));
			System.out.println(
					"----------------------------------------------------------------------------------------------------------------------------");
			System.out.println(
					"The Private Key is: "
							+ DatatypeConverter.printHexBinary(
									privateKey.getEncoded()));

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
		String EncryptType = "";

		// Constructor
		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {
			OutputStream outObj = null;
			ObjectInputStream inObj = null;
			BufferedReader EncryptTypeIn = null;
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
				EncryptTypeIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				printWriterOut = new PrintWriter(outObj, true);
				printStream = new PrintStream(outObj);
				this.EncryptType = EncryptTypeIn.readLine();

				if (this.EncryptType.equals("2")) {
					// Send Serve public Key
					PublicKey publicKey = keyPair.getPublic();
					ObjectdataOut.writeObject(publicKey);
					String strCSK = EncryptTypeIn.readLine();
					System.out.println(
							"The Session Key is :" + Hyper.decrypt(strCSK, keyPair.getPrivate()));
				}
				while (true) {
					// writing the received message from
					// client
					System.out.println("new request");

					received = (ArrayList<String>) inObj.readObject();
					Operation operation = new Operation();

					// Decrypt the Received
					if (this.EncryptType.equals("1") || this.EncryptType.equals("2")) {
						decrypt = Operation.decrypt(received);
					}

					operation.getRequest(decrypt);
					response = operation.auth();
					if (response.contains("Successful")) {
						String[] resParts = response.split("! ");
						printStream.println(resParts[0]);
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
