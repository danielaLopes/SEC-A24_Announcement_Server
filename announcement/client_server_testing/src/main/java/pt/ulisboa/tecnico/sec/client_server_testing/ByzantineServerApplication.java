package pt.ulisboa.tecnico.sec.client_server_testing;

import pt.ulisboa.tecnico.sec.server.ByzantineServer;

public class ByzantineServerApplication {

    public final static String SERVER_PUBLIC_KEY_PATH = "src/main/resources/crypto/public4.key";
    public final static String SERVER_KEYSTORE_PATH = "src/main/resources/crypto/server_keystore4.jks";

    public final static char[] KEYSTORE_PASSWD = {'p','a','s','s','w','o','r','d'};
    public final static char[] ENTRY_PASSWD = {'p','a','s','s','w','o','r','d'};
    public final static String ALIAS = "alias";

    public final static int N_SERVERS = 4;
    public final static int N_FAULTS = 1;
    public final static int SERVER_BYZANTINE_PORT = 8004;

    public static void main(String[] args) {

        ByzantineServer server = new ByzantineServer(false, N_SERVERS, N_FAULTS, SERVER_BYZANTINE_PORT,
                KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);
        server.start();
    }
}
