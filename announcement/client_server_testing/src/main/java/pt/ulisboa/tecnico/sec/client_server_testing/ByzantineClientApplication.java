package pt.ulisboa.tecnico.sec.client_server_testing;

import pt.ulisboa.tecnico.sec.client.ByzantineClient;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

public class ByzantineClientApplication {

    public final static String CLIENT_KEYSTORE_PASSWD = "password";
    public final static String CLIENT_ENTRY_PASSWD = "password";
    public final static String ALIAS = "alias";

    public final static String SERVER_PUBLIC_KEY_PATH = "../server/src/main/resources/crypto/public";
    public final static String SERVER_KEYSTORE_PATH = "../server/src/main/resources/crypto/server_keystore";

    public final static String PUBLICKEY_PATH1 = "../client/src/main/resources/crypto/public1.key";
    public final static String KEYSTORE_PATH1 = "../client/src/main/resources/crypto/client1_keystore.jks";

    public final static String PUBLICKEY_PATH2 = "../client/src/main/resources/crypto/public2.key";
    public final static String KEYSTORE_PATH2 = "../client/src/main/resources/crypto/client2_keystore.jks";

    public final static String PUBLICKEY_PATH3 = "../client/src/main/resources/crypto/public3.key";
    public final static String KEYSTORE_PATH3 = "../client/src/main/resources/crypto/client3_keystore.jks";

    public final static int N_SERVERS = 4;
    public final static int N_FAULTS = 1;
    public final static int SERVER_BYZANTINE_PORT = 8004;

    public static ByzantineClient _byzantineClient;

    public static void main(String[] args) {

        _byzantineClient = new ByzantineClient(PUBLICKEY_PATH2, KEYSTORE_PATH2,
                CLIENT_KEYSTORE_PASSWD, CLIENT_ENTRY_PASSWD, ALIAS, N_SERVERS, N_FAULTS);

        //sendDifferentMessagesWithNullFields();

        sendDifferentMessages();
    }

    static void sendDifferentMessagesWithNullFields() {
        System.out.println("------------ Send Different Messages With Null Fields TEST ------------");
        StatusCode sc = _byzantineClient.sendDifferentMessages("NULL");
        System.out.println("Quorum status code: " + sc);
    }

    static void sendDifferentMessages() {
        System.out.println("------------ Send Different Messages TEST ------------");
        StatusCode sc = _byzantineClient.sendDifferentMessages("OK");
        System.out.println("Quorum status code: " + sc);
    }

    static void colludeWithServer() {
        System.out.println("------------ Collude With Server TEST ------------");
        StatusCode sc = _byzantineClient.colludeWithServer(SERVER_BYZANTINE_PORT).getProtocolMessage().getStatusCode();
        System.out.println("Colluding server status code: " + sc);
    }
}