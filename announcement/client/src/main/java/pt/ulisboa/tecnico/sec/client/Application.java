package pt.ulisboa.tecnico.sec.client;

import java.util.List;
import java.util.ArrayList;

public class Application {
    public static void main(String[] args) {
        if (args.length < 6) {
            System.out.println("Usage: <pubKeyPath> <keyStorePath> <keyStorePassword>" + 
            " <entryPassword> <alias> <numberOfServers> <numberOfFaults> " +
                    "<numberOfOtherClients> <otherClientsPubKeyPaths>*");
        }

        List<String> otherUsersPubKeys = new ArrayList<String>();
        for (int i = 1; i <= Integer.parseInt(args[7]); i++) {
            otherUsersPubKeys.add(args[7+i]);
        }

        ClientUI clientUi = new ClientUI(args[0], args[1], args[2], args[3], args[4],
                Integer.parseInt(args[5]), Integer.parseInt(args[6]), otherUsersPubKeys);
        clientUi.start();
    }
}