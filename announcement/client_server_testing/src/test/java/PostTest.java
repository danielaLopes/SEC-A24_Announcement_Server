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

    PostTest() {

        _server = new Server(false, SERVER_KEYSTORE_PASSWD, SERVER_ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        List<String> otherUsersPubKeyPaths = new ArrayList<String>();
        otherUsersPubKeyPaths.add(PUBLICKEY_PATH2);
        otherUsersPubKeyPaths.add(PUBLICKEY_PATH3);

        _client1 = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, CLIENT_KEYSTORE_PASSWD,
                CLIENT_ENTRY_PASSWD, ALIAS, SERVER_PUBLIC_KEY_PATH, otherUsersPubKeyPaths);

        _client1.register();
    }

    @Test
    void success() {

        int sc = _client1.post("message1", new ArrayList<>());
        assertEquals(1, sc);
    }
}
