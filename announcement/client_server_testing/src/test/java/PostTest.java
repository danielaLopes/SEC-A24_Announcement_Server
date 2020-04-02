import org.junit.jupiter.api.Test;
import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableProtocolMessage;
import pt.ulisboa.tecnico.sec.server.Server;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostTest extends BaseTest {

    Server _server;
    Client _client1;
    Client _client2;
    Client _client3;

    PostTest() {

        _server = new Server(false, SERVER_KEYSTORE_PASSWD, SERVER_ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        List<String> otherUsersPubKeyPaths1 = new ArrayList<>();
        otherUsersPubKeyPaths1.add(PUBLICKEY_PATH2);
        otherUsersPubKeyPaths1.add(PUBLICKEY_PATH3);
        List<String> otherUsersPubKeyPaths2 = new ArrayList<>();
        otherUsersPubKeyPaths2.add(PUBLICKEY_PATH1);
        otherUsersPubKeyPaths2.add(PUBLICKEY_PATH3);
        List<String> otherUsersPubKeyPaths3 = new ArrayList<>();
        otherUsersPubKeyPaths3.add(PUBLICKEY_PATH1);
        otherUsersPubKeyPaths3.add(PUBLICKEY_PATH2);

        _client1 = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, CLIENT_KEYSTORE_PASSWD,
                CLIENT_ENTRY_PASSWD, ALIAS, SERVER_PUBLIC_KEY_PATH, otherUsersPubKeyPaths1);
        _client2 = new Client(PUBLICKEY_PATH2, KEYSTORE_PATH2, CLIENT_KEYSTORE_PASSWD,
                CLIENT_ENTRY_PASSWD, ALIAS, SERVER_PUBLIC_KEY_PATH, otherUsersPubKeyPaths2);
        _client3 = new Client(PUBLICKEY_PATH3, KEYSTORE_PATH3, CLIENT_KEYSTORE_PASSWD,
                CLIENT_ENTRY_PASSWD, ALIAS, SERVER_PUBLIC_KEY_PATH, otherUsersPubKeyPaths3);

        _client1.register();
        _client2.register();
        _client3.register();
    }

    @Test
    void success() {

        //int sc = _client1.post("message1", new ArrayList<>());
        //assertEquals(1, sc);
    }
}
