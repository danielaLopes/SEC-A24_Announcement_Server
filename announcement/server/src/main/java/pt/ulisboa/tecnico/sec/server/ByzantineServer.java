package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.Communication;
import pt.ulisboa.tecnico.sec.database_lib.Database;

import java.util.concurrent.ConcurrentHashMap;

public class ByzantineServer extends Server {

    public ByzantineServer(boolean activateCC, int nServers, int nFaults, int port, char[] keyStorePasswd, char[] entryPasswd, String alias, String pubKeyPath,
                  String keyStorePath) {

        super(activateCC, nServers, nFaults, port, keyStorePasswd, entryPasswd, alias, pubKeyPath, keyStorePath);
    }

    public void colludingWithClient() {

    }
}
