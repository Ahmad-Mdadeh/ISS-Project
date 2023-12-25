
import java.security.*;
import java.util.ArrayList;

import javax.xml.bind.DatatypeConverter;

public class DigitalSignature {
        static byte[] digitalSignature;
        static boolean verified;

        public static byte[] createDigitalSignature(ArrayList<String> dataList, PrivateKey privateKey)
                        throws Exception {
                Signature signature = Signature.getInstance("SHA256withRSA");
                SecureRandom secureRandom = new SecureRandom();

                signature.initSign(privateKey, secureRandom);

                for (String str : dataList) {
                        signature.update(str.getBytes());
                }

                byte[] digitalSignature = signature.sign();
              
                return digitalSignature;
        }

        public static boolean verifyingDigitalSignature(ArrayList<String> dataList, byte[] signatureToVerify,
                        PublicKey publicKey)
                        throws Exception {
                Signature signature = Signature.getInstance("SHA256withRSA");
                signature.initVerify(publicKey);

                for (String str : dataList) {
                        signature.update(str.getBytes());
                }

                verified = signature.verify(signatureToVerify);
                return verified;
        }

        // Driver Code
        public static void main(String args[])
                        throws Exception {

                ArrayList<String> input = new ArrayList<String>();
                input.add("Hello World");
                KeyPair keyPair = KeyGenerator.generateKeyPair();

                // Function Call
                byte[] signature = createDigitalSignature(input, keyPair.getPrivate());

                System.out.println(
                                "Signature Value:\n"
                                                + DatatypeConverter
                                                                .printHexBinary(signature));
                System.out.println(
                                "Verification: "
                                                + verifyingDigitalSignature(
                                                                input,
                                                                signature, keyPair.getPublic()));
        }
}
