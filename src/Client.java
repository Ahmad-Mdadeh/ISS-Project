import java.io.*;
import java.net.*;
import java.util.*;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

// Client class 
class Client {
	private ObjectOutputStream objectOut = null;
	private Socket socket = null;
	private Scanner sc = null;
	String EncryptType = "";
	byte[] nonce = new byte[32];
	SecretKey symmetricKey;

	// driver code
	public Client(String address, int port) {
		boolean check = false;

		try {

			// creating an object of socket
			socket = new Socket(address, port);
			System.out.println("Connection Established!! ");
			// taking input from user
			sc = new Scanner(System.in);
			// opening output stream on the socket
			objectOut = new ObjectOutputStream(socket.getOutputStream());
			PrintStream printStream = new PrintStream(socket.getOutputStream());

			System.out
					.println("Enter   0 for no Encryption\n\t1 for symmetric Encryption\n\t2 for Asymmetric Encryption");
			this.EncryptType = sc.nextLine();
			printStream.println(this.EncryptType);

			// ArrayList<String> request = new ArrayList<String>();

			// writing to server
			// PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			// reading from server

			// object of scanner class

			while (!check) {
				ArrayList<String> request = new ArrayList<String>();

				System.out.println(
						"Enter: 'E' or 'e' to exit.\nEnter: 'L' or 'l' to login.\nEnter: 'S' or 's' to signup.");
				System.out.print(
						"Your Option: ");
				String tmp = sc.nextLine();
				String input;

				switch (tmp) {
					case "E":
					case "e":
						input = "exit";
						break;
					case "L":
					case "l":
						input = "login";
						break;
					case "S":
					case "s":
						input = "signup";
						break;
					default:
						input = "";
				}
				if (input.equals("exit")) {
					break;
				} else if (input.equals("login")) {
					System.out.println("enter username and password");
					request.add(sc.nextLine());
					request.add(sc.nextLine());
					request.add("login");
				} else if (input.equals("signup")) {
					System.out.println("enter username and password");
					request.add("signup");
					request.add(sc.nextLine());
					String p = sc.nextLine();
					while (p.length() < 8) {
						System.out.println("retype  password of 8 char at least");
						p = sc.nextLine();
					}
					request.add(p);
				}

				try {
					String response = "";

					ArrayList<String> EncryptedRequest = new ArrayList<String>();
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					if (this.EncryptType.equals("1")) {

						symmetricKey = Symmetric.createAESKey("03150040010");
						System.out.println("The Symmetric Key is :"
								+ DatatypeConverter.printHexBinary(
										symmetricKey.getEncoded()));
						EncryptedRequest = Symmetric.encryptAES(request, symmetricKey);
						System.out.println("_______________________ :" + EncryptedRequest.get(0));
						objectOut.writeObject(EncryptedRequest);
						// get plain response
						response = in.readLine();
						System.out.println("Server replied ===> " + response);
					}

				} catch (Exception exception) {

				}
				// sending the user input to server
				// objectOut.writeObject(request);
				// objectOut.flush();

				// displaying server reply

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String argvs[]) {
		// creating object of class Client
		new Client("localhost", 1234);

	}
}
