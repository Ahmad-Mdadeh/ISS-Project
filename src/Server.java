import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.SQLException;
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
		static byte[] digitalSignature;
		PublicKey publicKeyFromClient;
		byte[] encryptedSessionKey;

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
				ArrayList<String> received1 = new ArrayList<>();
				// get the outputstream of client
				outObj = clientSocket.getOutputStream();
				inObj = new ObjectInputStream(clientSocket.getInputStream());
				encryptTypeIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				printStream = new PrintStream(outObj);
				printWriterOut = new PrintWriter(clientSocket.getOutputStream(), true);

				getEncryptedSessionKey(inObj);

				String csr = "-1";
				while (true) {
					this.encryptType = encryptTypeIn.readLine();

					// writing the received message from client
					System.out.println("new request");

					received = (ArrayList<String>) inObj.readObject();

					Operation operation = new Operation();
					boolean status;
					switch (this.encryptType) {
						case "symmetric":
							decrypt = operation.decrypt(received, SymmetricCryptography.createAESKey(symmetricKey));
							operation.getRequest(decrypt);
							response = operation.insertIntoDataBase();
							break;
						case "pgp":
							decrypt = operation.decrypt(received, sessionKey);
							operation.getRequest(decrypt);
							response = operation.insertIntoDataBase();
							break;
						case "signature":
							digitalSignature = (byte[]) inObj.readObject();
							decrypt = operation.decrypt(received, sessionKey);
							if (verifingSginature(received)) {
								operation.getRequest(decrypt);
								response = operation.insertIntoDataBase();
							} else {
								response = "Digital Signature is invaled !!";
							}
							break;
						case "no":
							// ######################################
							if (received.get(0).equals("request")) {
								new DigitalCertificate(printStream, encryptTypeIn);
								System.out.println("request certificate:");
								System.out.println(received.get(1));
								System.out.println("--------------------------------------------------");
								String privateKeyString = DigitalCertificate.readFileContent("ServerPrivateKey.txt");
								PrivateKey privateKey = DigitalCertificate.convertStringToPrivateKey(privateKeyString);
								String message = DigitalCertificate.VerificationFromCSR(received.get(1), privateKey);
								System.out.println("new certificat is:");
								System.out.println(message);
								printStream.println(message);
								System.out.println("--------------------------------------------------");
								csr = encryptTypeIn.readLine();
								if(message.contains("warning::")){
									decrypt = (ArrayList<String>) inObj.readObject();
									status =false;
								}else{
									// receiv certificate
									System.out.println("received certificate:");
									System.out.println(csr);
									System.out.println("--------------------------------------------------");
									decrypt = E(csr,inObj);
									status =true;
								}
							} else {
								// receiv certificate
								System.out.println("received certificate:");
								System.out.println(received.get(0));
								csr = received.get(0);
								System.out.println("--------------------------------------------------");
								decrypt = E(csr,inObj);
								status =true;
							}
							// ######################################
							if(status){
								System.out.println(decrypt);
								operation.getRequest(decrypt);
								response = operation.insertIntoDataBase();
							}else{
								response = "client dont have certificate";
							}
							
							break;
						default:
							break;
					}
					if (response.contains("Successful")) {
						String[] resParts = response.split("! ");
						printStream.println(resParts[0]);
						printStream.println(permissions(csr));
						if (3 <= resParts.length) {
							this.symmetricKey = resParts[2];
						}
						System.out.print("Permission : ");
						System.out.println(permissions(csr));

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

		private boolean verifingSginature(ArrayList<String> received) throws Exception {
			return DigitalSignature.verifyingDigitalSignature(received, digitalSignature,
					publicKeyFromClient);
		}

		private void getEncryptedSessionKey(ObjectInputStream inObj) throws Exception {

			publicKeyFromClient = (PublicKey) inObj.readObject();

			// generateSessionKey
			sessionKey = SymmetricCryptography.GenerateSessionKey();

			System.out.println("-------------------------------------------------------------------------");

			System.out.println(
					"The Server's Session Key is:\n" + DatatypeConverter.printHexBinary(sessionKey.getEncoded()));

			encryptedSessionKey = KeyGenerator.encrept(DatatypeConverter.printHexBinary(sessionKey.getEncoded()),
					publicKeyFromClient);

			System.out.println("-------------------------------------------------------------------------");

			System.out.println(
					"The Server's Encrypted Session Key is:\n" + DatatypeConverter.printHexBinary(encryptedSessionKey));

			System.out.println("-------------------------------------------------------------------------");

			// Send the Encrypted Session Key to Server
			printWriterOut.println(DatatypeConverter.printHexBinary(encryptedSessionKey));
		}

		private ArrayList<String> E(String csr,ObjectInputStream inObj)throws Exception{
			ArrayList<String> received1, decrypt;
			String publicKeyString = DigitalCertificate.readFileContent("ServerPublicKey.txt");
			PublicKey publicKey = DigitalCertificate.convertStringToPublicKey(publicKeyString);
			String[] InfoCsr = csr.split("\\|");
			Boolean certificateSignatureIs = DigitalCertificate.verifyDigitalSignature(
							InfoCsr[0] + "|" + InfoCsr[1] + "|" + InfoCsr[2] + "|", InfoCsr[3], publicKey);
			System.out.println("certificate signature is:" + certificateSignatureIs);
			if (certificateSignatureIs == true) 
			{
				String publicKeyClientString = InfoCsr[2];
				PublicKey publicKeyClient = DigitalCertificate.convertStringToPublicKey(publicKeyClientString);
				byte[] encrept = KeyGenerator.encrept(DatatypeConverter.printHexBinary(sessionKey.getEncoded()), publicKeyClient);
				System.out.println("sesion key:");
				System.out.println(DatatypeConverter.printHexBinary(sessionKey.getEncoded()));
				System.out.println("--------------------------------------------------");
				System.out.println("encrept sesion key:");
				System.out.println(DatatypeConverter.printHexBinary(encrept));
				printWriterOut.println(DatatypeConverter.printHexBinary(encrept));
				System.out.println("--------------------------------------------------");
				received1 = (ArrayList<String>) inObj.readObject();
				decrypt = SymmetricCryptography.decryptAES(received1, sessionKey);	
			} else
			{
				printWriterOut.println("warning:: the certificate is error");
				decrypt = null;
			}
			return decrypt;
		}
		
		private String permissions(String csr) throws SQLException{
			
			String permissions;
			if(!csr.equals("-1")){
				String[] InfoCsr = csr.split("\\|");
				if(InfoCsr[1].equals("d")){
					permissions = "1";
				}else if(InfoCsr[1].equals("a")){
					permissions = "0";
				}else{
					permissions = "2";
				}
			}else{
				permissions = "-1";
			}		
			return permissions;
		}
	}
}
