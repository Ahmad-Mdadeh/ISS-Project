import java.security.KeyPair;


public class test {
    
    
    public static void main(String[] args) throws Exception {
    KeyPair keyPair = KeyGenerator.generateKeyPair();
    System.out.println(DigitalCertificate.convertPrivateKeyToString(keyPair.getPrivate()));
    System.out.println("##########################");
    System.out.println(DigitalCertificate.convertPublicKeyToString(keyPair.getPublic()));
    }
    
}
