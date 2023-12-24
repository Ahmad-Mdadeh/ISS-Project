
import java.security.*;

import javax.xml.bind.DatatypeConverter;

public class DigitalSignature {
        static byte[] digitalSignature;
        static boolean verified;

        public static byte[] CreatingDigitalSignature(byte[] data, PrivateKey privateKey) throws Exception {

                Signature signature = Signature.getInstance("SHA256withRSA");
                SecureRandom secureRandom = new SecureRandom();

                signature.initSign(privateKey, secureRandom);
                signature.update(data);
                digitalSignature = signature.sign();
                return digitalSignature;
        }

        public static boolean VerifyingDigitalSignature(byte[] data, byte[] signatureToVerify, PublicKey publicKey)
                        throws Exception {
                Signature signature = Signature.getInstance("SHA256withRSA");
                signature.initVerify(publicKey);
                signature.update(data);
                verified = signature.verify(signatureToVerify);
                return verified;
        }

        // Driver Code
        public static void main(String args[])
                        throws Exception {

                String input = "GEEKSFORGEEKS IS A"
                                + " COMPUTER SCIENCE PORTAL";
                KeyPair keyPair = KeyGenerator.generateKeyPair();

                // Function Call
                byte[] signature = CreatingDigitalSignature(
                                input.getBytes(),
                                keyPair.getPrivate());

                System.out.println(
                                "Signature Value:\n"
                                                + DatatypeConverter
                                                                .printHexBinary(signature));

                System.out.println(
                                "Verification: "
                                                + VerifyingDigitalSignature(
                                                                input.getBytes(),
                                                                signature, keyPair.getPublic()));
        }
}
