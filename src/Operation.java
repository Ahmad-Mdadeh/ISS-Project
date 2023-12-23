import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

public class Operation {
    ArrayList<String> decrypted = new ArrayList<>();
    String res;

    ArrayList<String> decrypt(ArrayList<String> EncryptedRequest, SecretKey symmetricKey) throws Exception {
        System.out.println("The Symmetric Key is :"
                + DatatypeConverter.printHexBinary(
                        symmetricKey.getEncoded()));
        return Symmetric.decryptAES(EncryptedRequest, symmetricKey);

    }

    void getRequest(ArrayList<String> received) {

        for (int i = 0; i < received.size(); i++) {
            decrypted.add(received.get(i).toString());
            System.out.println("received : " + decrypted.get(i).toString());
        }

    }

    String auth() {
        if (decrypted.get(0).toString().equals("login")) {
            ConnectToDatabase connectToDatabase = new ConnectToDatabase();
            res = connectToDatabase.login(decrypted.get(1).toString(),
                    decrypted.get(2).toString(), decrypted.get(3).toString());
        } else if (decrypted.get(0).toString().equals("signup")) {
            ConnectToDatabase connectToDatabase = new ConnectToDatabase();
            res = connectToDatabase.signup(decrypted.get(1).toString(),
                    decrypted.get(2).toString(), decrypted.get(3).toString());
        } else if (decrypted.get(0).toString().equals("completeInformation")) {
            ConnectToDatabase connectToDatabase = new ConnectToDatabase();
            res = connectToDatabase.updateInformation(decrypted.get(1).toString(),
                    decrypted.get(2).toString(),
                    decrypted.get(3).toString());
        } else if (decrypted.get(0).toString().equals("projects")) {
            ConnectToDatabase connectToDatabase = new ConnectToDatabase();
            res = connectToDatabase.setPracticalProjects(decrypted.get(1).toString());
        }
        return res;
    }

}
