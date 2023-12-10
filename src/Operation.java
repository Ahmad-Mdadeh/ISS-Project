import java.util.ArrayList;

import javax.xml.bind.DatatypeConverter;

public class Operation {
    ArrayList<String> decrypted = new ArrayList<>();
    String res = "";

    Operation() throws Exception {

    }

    static ArrayList<String> decrypt(ArrayList<String> EncryptedRequest) throws Exception {
        System.out.println("The Symmetric Key is :"
                + DatatypeConverter.printHexBinary(
                        Symmetric.createAESKey("03150040010").getEncoded()));
        return Symmetric.decryptAES(EncryptedRequest, Symmetric.createAESKey("03150040010"));

    }

    void getRequest(ArrayList<String> received) {
        for (int i = 0; i < received.size(); i++) {
            decrypted.add(received.get(i).toString());

            System.out.println("received : " + decrypted.get(i).toString());
        }

    }

    String Auth() {
        System.out.println("==================");
        if (decrypted.get(2).toString().equals("login")) {
            ConnectToDatabase connectToDatabase = new ConnectToDatabase();
            res = connectToDatabase.Login(decrypted.get(0).toString(),
                    decrypted.get(1).toString());

        } else if (decrypted.get(2).toString().equals("signup")) {
            ConnectToDatabase connectToDatabase = new ConnectToDatabase();
            res = connectToDatabase.Signup(decrypted.get(0).toString(),
                    decrypted.get(1).toString());
        }

        return res;
    }

}
