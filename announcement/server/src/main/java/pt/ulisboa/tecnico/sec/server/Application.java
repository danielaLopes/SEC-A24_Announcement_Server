package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.crypto_lib.SignatureUtil;

import java.security.*;
import java.util.ArrayList;
import java.util.List;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Application {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        //CTRL+C signal handler. Allows to clean databases.
        Signal.handle(new Signal("INT"), new SignalHandler() {
            public void handle(Signal sig) {
                System.out.println("sigint");
                System.exit(-1);
            }
        });

        if(args.length < 6) {
            System.out.println("\"Usage: <port> <keyStorePassword> <entryPassword> <alias> <pubKeyPath> <keyStorePath>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        System.out.println("Server running at port " + port);

        Server server = new Server(false, port, args[1].toCharArray(), args[2].toCharArray(), args[3],
                args[4], args[5]);
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
        //List<PostOperation> announcements1 = server.readGeneral(1);
        //for (PostOperation announcement: announcements1) {
        //    System.out.println(announcement.getMessage());
        //}
    }
}