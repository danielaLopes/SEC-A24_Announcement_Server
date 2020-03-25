package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.crypto_lib.SignatureUtil;

import java.security.*;
import java.util.ArrayList;
import java.util.List;

public class Application {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        if(args.length != 3) {
            System.out.println("Args: <keyStorePassword> <entryPassword> <alias>");
            return;
        }

        System.out.println("Hello world server");
        Server server = new Server(false, 8000, args[0].toCharArray(), args[1].toCharArray(), args[2]);
        server.start();

        // testing
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        PublicKey userPubKey = kp.getPublic();
        PrivateKey userPrivateKey = kp.getPrivate();

        server.registerUser(userPubKey);

        // generate signature
        // message + references
        String toSign = "1" + "|" + "message1" + "|" + "1";
        byte[] signature = SignatureUtil.sign(toSign.getBytes(), userPrivateKey);

        List<Integer> references = new ArrayList<>();
        references.add(1);
        server.postGeneral(userPubKey, "message1", 1, references, signature);
        List<PostOperation> announcements1 = server.readGeneral(1);
        for (PostOperation announcement: announcements1) {
            System.out.println(announcement.getMessage());
        }
        /*List<Integer> references2 = new ArrayList<>();
        references2.add(2);
        server.postGeneral(userPubKey, "message2", references2, signature);
        List<PostOperation> announcements2 = server.readGeneral(2);
        for (PostOperation announcement: announcements2) {
            System.out.println(announcement.getMessage());
        }*/

        /*catch (FileNotFoundException e) {
            System.out.println("Error: File " + file.getAbsolutePath() + " was not found!");
        } catch (IOException e) {
            System.out.println("Error: Not possible to close FileInputStream");
        } catch (CertificateException e) {
            System.out.println("Error: Not possible to load Certificate");
            e.printStackTrace();
        }*/
    }
}