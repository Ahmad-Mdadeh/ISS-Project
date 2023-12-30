import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;



public class DigitalCertificate {

    final static Scanner scanner = new Scanner(System.in);
    
    static BufferedReader in;
    static PrintStream out;
    public DigitalCertificate(PrintStream out,BufferedReader in ){
       DigitalCertificate.out = out ; 
       DigitalCertificate.in =in;
    }
   
    //enter information csr from client
    public static String InformationCSR(KeyPair keyPair,ArrayList<String> request) throws Exception 
     {  
        String username,certificatePassword,classification;
        while(true){
            System.out.print("Enter username: ");
            username = scanner.nextLine();
            if(username.equals(request.get(1))){
                break;
            }else{
                System.out.println("The username in the certificate application must be identical to the username on the platform");
            }
        }
        
        while (true) {
            System.out.print("Enter certificate password (8 characters): ");
            certificatePassword = scanner.nextLine();

            if (certificatePassword.length() == 8) {
                break; 
            } else {
                System.out.println("Invalid password length. Please enter a password with 8 characters.");
            }
        }
         while(true){
             System.out.println("Scientific Classifications: (d) or (s) or (a)");
             System.out.print("Enter your Scientific classification: ");
             classification = scanner.nextLine();
             if(classification.equals("d")||classification.equals("s")||classification.equals("a")){
                 break;
             }
         }
         String publicKeyString = convertPublicKeyToString(keyPair.getPublic());
         String privateKey = DigitalCertificate.convertPrivateKeyToString(keyPair.getPrivate());
         DigitalCertificate.appendStringToFile(username+"_"+certificatePassword+"|"+privateKey, "ClientPrivateKey.txt");
         String signature = createDigitalSignature(
            username+"_"+certificatePassword+"|"+classification+"|"+publicKeyString+"|",keyPair.getPrivate()
             );  
         return  username+"_"+certificatePassword+"|"
                 +classification+"|"   
                 +publicKeyString+"|"
                 +signature; 
     }
        
    //Identity verification
    public static String VerificationFromCSR(String csr,PrivateKey privateKey) throws Exception
        {
            String message;
            String[] InfoCsr = csr.split("\\|");
    
            PublicKey publicKey = convertStringToPublicKey(InfoCsr[2]);
            String signature = InfoCsr[3];
            String data = InfoCsr[0]+"|"+InfoCsr[1]+"|"+InfoCsr[2]+"|";
 
     
 
         if(verifyDigitalSignature(data, signature, publicKey)){
             if(InfoCsr[1].equals("d")){
                 out.println("Please solve the following problem");
                 out.println("100+100 = ?");
                 out.println("your answer :");
                 String theSolution = in.readLine();
                 if(theSolution.equals("dt"))
                 {
                    message =CreateTheCertificate(data,privateKey);
                 }else
                 {
                     message = "warning::The speaker has not been doctor";
                 }
             }
             else if(InfoCsr[1].equals("s")){
                out.println("Please solve the following problem");
                out.println("100+100 = ?");
                out.println("your answer :");
                String theSolution = in.readLine();
                if(theSolution.equals("200"))
                {
                    message =CreateTheCertificate(data,privateKey);
                }else
                {
                    message = "warning::The speaker has not been student";
                } 
             }
             else if(InfoCsr[1].equals("a")){
                out.println("Please solve the following problem");
                out.println("100+100 = ?");
                out.println("your answer :");
                String theSolution = in.readLine();
                if(theSolution.equals("at"))
                {
                    message =CreateTheCertificate(data,privateKey);
                }else
                {
                    message = "warning::The speaker has not been admin";
                } 
             }else {message = null;}
         }else{
             message = "warning::The signature is invalid";
         }
         return message;
     }
     
    //create certificate
    public static String CreateTheCertificate(String csr,PrivateKey privateKey) throws Exception
     {
         String NotarizedCsr;
         // String Id = 
         String[] InfoCsr = csr.split("\\|");
         String stringCsr = InfoCsr[0]+"|"+InfoCsr[1]+"|"+InfoCsr[2]+"|";
     
         String signature =  createDigitalSignature(stringCsr,privateKey);
         NotarizedCsr = stringCsr+signature;
         return NotarizedCsr;
         
     }
 
    //certificate validation
    public static String CertificateValidation(String csr,PublicKey publicKey) throws Exception
     {
         String[] InfoCsr = csr.split("\\|");
         String signature = InfoCsr[3];
         String data = InfoCsr[0]+"|"+InfoCsr[1]+"|"+InfoCsr[2]+"|";
         String message;
 
         boolean result = verifyDigitalSignature(data,signature,publicKey);
         if(result){
             appendStringToFile(csr,"SaveCertificate.txt");
             message = "Certificate created successfully";   
         }else{
             message = "Certificate is incorrect ";
         }
         return message;
     }
     
    //save certificate in file
    public static void appendStringToFile(String newData,String nameFile) {
         try (BufferedWriter writer = new BufferedWriter(new FileWriter(nameFile, true))) {
             // Append the new data to the file
             writer.write(newData);
             writer.newLine(); 
             writer.write("----------------------------------------------------------");
             writer.newLine();
         } catch (IOException e) {
             e.printStackTrace(); 
         }
     }
     
    //search certificate in file
    public static String searchValueInFile(String targetValue,String pathFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(pathFile))) {
            String line;
    
            // Read the file line by line
            while ((line = reader.readLine()) != null) {
                // Split the line using the '|' delimiter
                String[] parts = line.split("\\|");
    
                // Check each part for the target value
                for (String part : parts) {
                    if (part.equals(targetValue)) {
                        return line; // Return the entire line if the target value is found
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception based on your needs
        }
    
        return null; // Target value not found in any line
    }
    
     
    //Digital Signature
    public static boolean verifyDigitalSignature(String data, String signatureToVerify, PublicKey publicKey)throws Exception 
     {
         Signature signature = Signature.getInstance("SHA256withRSA");
         signature.initVerify(publicKey);
         signature.update(data.getBytes()); 
         boolean verified = signature.verify(Base64.getDecoder().decode(signatureToVerify));
 
         return verified;
     }
 
    public static String createDigitalSignature(String data, PrivateKey privateKey) throws Exception 
     {
         Signature signature = Signature.getInstance("SHA256withRSA");
         SecureRandom secureRandom = new SecureRandom();
 
         signature.initSign(privateKey, secureRandom);
         signature.update(data.getBytes()); 
         byte[] digitalSignature = signature.sign();
 
         return Base64.getEncoder().encodeToString(digitalSignature);
     }
 
    //helper function
    public static String convertPublicKeyToString(PublicKey publicKey) 
     {
         byte[] publicKeyBytes = publicKey.getEncoded();
         return Base64.getEncoder().encodeToString(publicKeyBytes);
     }
    public static PublicKey convertStringToPublicKey(String publicKeyString) throws Exception 
     {
         byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
         X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
         KeyFactory keyFactory = KeyFactory.getInstance("RSA"); 
         return keyFactory.generatePublic(keySpec);
     }
    public static String convertPrivateKeyToString(PrivateKey privateKey) {
        byte[] privateKeyBytes = privateKey.getEncoded();
        return Base64.getEncoder().encodeToString(privateKeyBytes);
    }
    public static PrivateKey convertStringToPrivateKey(String privateKeyString) throws Exception {
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); 
        return keyFactory.generatePrivate(keySpec);
    }
    public static String readFileContent(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString().replaceAll("\\s", "");
    }
}
