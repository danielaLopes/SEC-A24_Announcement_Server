package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.MessageComparator;
import pt.ulisboa.tecnico.sec.communication_lib.ProtocolMessage;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableProtocolMessage;
import pt.ulisboa.tecnico.sec.crypto_lib.SignatureUtil;
import pt.ulisboa.tecnico.sec.database_lib.Database;

import java.security.*;
import java.util.ArrayList;
import java.util.List;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class Application {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        //CTRL+C signal handler. Allows to clean databases.
        Signal.handle(new Signal("INT"), new SignalHandler() {
            public void handle(Signal sig) {
                System.out.println("(INFO) Server going down.");
                //Database.dropDatabase();
                System.exit(-1);
            }
        });

        if(args.length < 7) {
            System.out.println("\"Usage: <nServers> <nFaults> <port> <keyStorePassword> <entryPassword> <alias> <pubKeyPath> <keyStorePath>");
            return;
        }

        int nServers = Integer.parseInt(args[0]);
        System.out.println("(INFO) " + nServers + " servers running.");

        int nFaults = Integer.parseInt(args[1]);
        System.out.println("(INFO) Number of tolerated faults:  " + nFaults);

        int port = Integer.parseInt(args[2]);
        System.out.println("(INFO) Server running at port " + port);

        Server server = new Server(false, nServers, nFaults, port, args[3].toCharArray(), args[4].toCharArray(), args[5],
                args[6], args[7]);
        server.start();
    }
}