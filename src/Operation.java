import java.util.ArrayList;

public class Operation {
    ArrayList<String> decrypted = new ArrayList<>();
    String res = "";

    Operation(ArrayList<String> received) {
        for (int i = 0; i < received.size(); i++) {
            decrypted.add(received.get(i).toString());
            if (i != 0)
                System.out.println("received : " + decrypted.get(i).toString());
        }
    }

    String Auth() {
        if (decrypted.get(0).toString().equals("login")) {
            ConnectToDatabase connectToDatabase = new ConnectToDatabase();
            res = connectToDatabase.Login(decrypted.get(1).toString(),
                    decrypted.get(2).toString());
        } else if (decrypted.get(0).toString().equals("signup")) {
            ConnectToDatabase connectToDatabase = new ConnectToDatabase();
            res = connectToDatabase.Signup(decrypted.get(1).toString(),
                    decrypted.get(2).toString());
        }
        return res;
    }

}
