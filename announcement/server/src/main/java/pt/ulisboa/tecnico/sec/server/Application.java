package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.MessageComparator;
import pt.ulisboa.tecnico.sec.communication_lib.ProtocolMessage;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableProtocolMessage;
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
            System.out.println("\"Usage: <nServers> <port> <keyStorePassword> <entryPassword> <alias> <pubKeyPath> <keyStorePath>");
            return;
        }

        int nServers = Integer.parseInt(args[0]);
        System.out.println(nServers + " servers running.");

        int port = Integer.parseInt(args[1]);
        System.out.println("Server running at port " + port);

        Server server = new Server(false, nServers, port, args[2].toCharArray(), args[3].toCharArray(), args[4],
                args[5], args[6]);
        server.start();

        // test ComparePost
        /*List<VerifiableProtocolMessage> vpms = new ArrayList<>();

        Announcement a = new Announcement("ola", new ArrayList<>());
        a.setPublicKey(server.getPublicKey());
        ProtocolMessage pm = new ProtocolMessage("POST", server.getPublicKey(), a, null);
        vpms.add(new VerifiableProtocolMessage(pm, null));

        Announcement a1 = new Announcement("ola", new ArrayList<>());
        a1.setPublicKey(server.getPublicKey());
        ProtocolMessage pm1 = new ProtocolMessage("POST", server.getPublicKey(), a1, null);
        vpms.add(new VerifiableProtocolMessage(pm1, null));

        System.out.println(MessageComparator.compareClientMessages(vpms, 1));
*/
    }
}