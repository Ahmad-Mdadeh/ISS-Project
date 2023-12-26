import java.io.*;
import java.net.*;
import javax.crypto.SecretKey;

// Client class
class Client {
	private ObjectOutputStream objectOut = null;
	private Socket socket = null;
	private SecretKey sessionKey = null;

	// driver code
	public Client(String address, int port) {

		try {

			// creating an object of socket
			socket = new Socket(address, port);

			System.out.println("Connection Established!! ");

			// opening output stream on the socket
			objectOut = new ObjectOutputStream(socket.getOutputStream());

			// UserInteraction
			UserInteraction userInteraction = new UserInteraction(socket, objectOut);
			userInteraction.startInteraction();

			System.out.println("-------------------------------------------------------------------------");

			if (userInteraction.getIsExit()) {
				return;
			}

			// if (userInteraction.getPermission().equals("1")) {
			// MarkEntryStudent markEntryStudent = new MarkEntryStudent(socket, objectOut,
			// sessionKey, privateKey);
			// markEntryStudent.setMark();
			// }

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

	public static void main(String argvs[]) {
		// creating object of class Client
		new Client("localhost", 1234);

	}
}
