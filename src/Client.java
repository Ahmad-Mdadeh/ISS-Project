
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
					.println("Enter:  0 for no Encryption\n\t1 for symmetric Encryption\n\t2 forAsymmetric Encryption");
			System.out.print(
					"Your Option : ");
			this.EncryptType = sc.nextLine();
			printStream.println(this.EncryptType);

			// Auth
			while (!check) {
				ArrayList<String> request = new ArrayList<String>();

				System.out.println(
						"Enter: 'E' or 'e' to exit.\nEnter: 'L' or 'l' to login.\nEnter: 'S' or 's'to signup.");
				System.out.print(
						"Your Option : ");
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
					System.out.println("Please Enter User Name And Password :");
					System.out.print("User Name : ");
					request.add(sc.nextLine());
					System.out.print("Password : ");
					request.add(sc.nextLine());
					request.add("login");
				} else if (input.equals("signup")) {
					System.out.println("Please Enter User Name And Password :");
					request.add("signup");
					System.out.print("User Name : ");
					request.add(sc.nextLine());
					System.out.print("Password : ");
					String p = sc.nextLine();
					while (p.length() < 8) {
						System.out.println(" Please Retype Password Of 8 Char At Least.");
						System.out.print("Password : ");
						p = sc.nextLine();
					}
					request.add(p);
				}

				try {
					String response = "";

					ArrayList<String> EncryptedRequest = new ArrayList<String>();
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					if (this.EncryptType.equals("0")) {

						objectOut.writeObject(request);
						response = in.readLine();
						System.out.println("Server replied ===> " + response);

					} else if (this.EncryptType.equals("1")) {

						symmetricKey = Symmetric.createAESKey("03150040010");
						System.out.println(
								"-------------------------------------------------------------------------------------------------------------------------------------------");
						System.out.println("The Symmetric Key is : "
								+ DatatypeConverter.printHexBinary(
										symmetricKey.getEncoded()));
						EncryptedRequest = Symmetric.encryptAES(request, symmetricKey);
						System.out.println(
								"-------------------------------------------------------------------------------------------------------------------------------------------");
						System.out.println("request sent !!");
						System.out.println(
								"-------------------------------------------------------------------------------------------------------------------------------------------");
						objectOut.writeObject(EncryptedRequest);
						// get plain response
						response = in.readLine();
						System.out.println("Server replied ===> " + response);
					}

					if (response.contains("Successful")) {
						check = true;
					}
				} catch (Exception exception) {

				}

			}

			System.out.println(
					"-------------------------------------------------------------------------------------------------------------------------------------------");
			System.out.println();

			// Information
			while (check) {
				ArrayList<String> request = new ArrayList<String>();
				System.out.println("Please Enter: 1-Phone Number , 2-Addrees , 3-Age.");
				request.add("completeInformation");
				System.out.print("Phone Number : ");
				request.add(sc.nextLine());
				System.out.print("Addrees : ");
				request.add(sc.nextLine());
				System.out.print("Age : ");
				request.add(sc.nextLine());

				try {
					String response = "";
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					ArrayList<String> EncryptedRequest = new ArrayList<String>();
					if (this.EncryptType.equals("0")) {

						objectOut.writeObject(request);
						response = in.readLine();
						System.out.println("Server replied ===> " + response);

					} else if (this.EncryptType.equals("1")) {

						symmetricKey = Symmetric.createAESKey("03150040010");
						System.out.println(
								"-------------------------------------------------------------------------------------------------------------------------------------------");
						System.out.println("The Symmetric Key is : "
								+ DatatypeConverter.printHexBinary(symmetricKey.getEncoded()));
						EncryptedRequest = Symmetric.encryptAES(request, symmetricKey);
						System.out.println(EncryptedRequest.get(1));
						System.out.println(
								"-------------------------------------------------------------------------------------------------------------------------------------------");
						System.out.println("request sent !! ");
						System.out.println(
								"-------------------------------------------------------------------------------------------------------------------------------------------");
						objectOut.writeObject(EncryptedRequest);
						// get plain response
						response = in.readLine();
						System.out.println("Server replied ===> " + response);
						System.out.println(
								"-------------------------------------------------------------------------------------------------------------------------------------------");

					}
				} catch (Exception exception) {
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(" Connection Terminated !! ");

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
