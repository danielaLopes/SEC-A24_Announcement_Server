package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.crypto_lib.SignatureUtil;

import java.security.*;
import java.util.ArrayList;
import java.util.List;

public class Application {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        if(args.length < 5) {
            System.out.println("\"Usage: <keyStorePassword> <entryPassword> <alias> <pubKeyPath> <keyStorePath>");
            return;
        }

        System.out.println("Hello world server");

        Server server = new Server(false, args[0].toCharArray(), args[1].toCharArray(), args[2],
                args[3], args[4]);
        server.start();

        // testing
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        PublicKey userPubKey = kp.getPublic();
        PrivateKey userPrivateKey = kp.getPrivate();

        //server.registerUser(userPubKey);

        // generate signature
        // message + references
        String toSign = "1" + "|" + "message1" + "|" + "1";
        byte[] signature = SignatureUtil.sign(toSign.getBytes(), userPrivateKey);

        List<Integer> references = new ArrayList<>();
        references.add(1);
        //server.postGeneral(userPubKey, "message1", 1, references, signature);
        List<PostOperation> announcements1 = server.readGeneral(1);
        for (PostOperation announcement: announcements1) {
            System.out.println(announcement.getMessage());
        }
    }
}