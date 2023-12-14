import java.io.*;
import java.net.*;
import java.util.ArrayList;

// Server class 
class Server {
	public static void main(String[] args) {
		ServerSocket server = null;

		try {

			// server is listening on port 1234
			server = new ServerSocket(1234);
			server.setReuseAddress(true);

			// running infinite loop for getting
			// client request
			while (true) {

				// socket object to receive incoming client
				// requests
				Socket client = server.accept();

				// Displaying that new client is connected
				// to server
				System.out.println("New client connected : "
						+ client.getInetAddress()
								.getHostAddress());

				// create a new thread object
				ClientHandler clientSock = new ClientHandler(client);

				// This thread will handle the client
				// separately
				new Thread(clientSock).start();
			}
		} catch (IOException e) {
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

			try {
				// String res = "";
				// get the outputstream of client
				outObj = clientSocket.getOutputStream();
				PrintStream printStream = new PrintStream(clientSocket.getOutputStream());
				// get the inputstream of client
				inObj = new ObjectInputStream(clientSocket.getInputStream());
				BufferedReader EncryptTypeIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

				this.EncryptType = EncryptTypeIn.readLine();

				while (true) {
					// writing the received message from
					// client
					System.out.println("new request");

					ArrayList<String> received, decrypt = new ArrayList<>();
					ArrayList<String> encryptResponse = new ArrayList<>();

					received = (ArrayList<String>) inObj.readObject();

					Operation operation = new Operation();
					// Decrypt the Received
					if (this.EncryptType.equals("1")) {
						decrypt = Operation.decrypt(received);
					}

					operation.getRequest(decrypt);
					encryptResponse.add(operation.auth());
					if (encryptResponse.get(0).contains("!!!")) {
						String[] resParts = encryptResponse.get(0).split("! ");
						encryptResponse.add(resParts[0]);
						printStream.println(encryptResponse.get(1));
						System.out.println(resParts[1]);
					} else {
						printStream.println(encryptResponse.get(0));
						System.out.println(encryptResponse.get(0));
					}
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
