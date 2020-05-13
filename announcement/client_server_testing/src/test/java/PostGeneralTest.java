import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.client.ClientTest;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableProtocolMessage;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;
import pt.ulisboa.tecnico.sec.server.Server;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostGeneralTest extends BaseTest {

    Client _client;

    PostGeneralTest() {
        _client = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, CLIENT_KEYSTORE_PASSWD, CLIENT_ENTRY_PASSWD, ALIAS, 4, 1);
    }

    @Test
    void success() {
        // List<Server> servers = new ArrayList<Server>();
        // for (int i = 0; i < 3; i++) {
        //     char[] password = {'p','a','s','s','w','o','r','d'};
        //     Server server = new Server(false, 4, 1, 9001+i, password, password,
        //         "alias", "../server/src/main/resources/crypto/public"+(i+1)+".key",
        //         "../server/src/main/resources/crypto/server_keystore"+(i+1)+".jks");
        //     servers.add(server);
        // }
        // for (Server server: servers) {
        //     Thread thread = new Thread(){
        //         public void run() {
        //             server.start();
        //         }
        //     };
        //     thread.start();
        // }

        _client.postGeneral("Ok. So. We have a question in chat from João Martinho. Yes. That is rright.", new ArrayList<>());

        while (_client.postGeneralDelivered == false) {
            try {
                Thread.sleep(1000);
            } catch(Exception e) {

            }
        }

        assertEquals(StatusCode.OK, _client.postGeneralDeliveredSC);
        
    }

    @Test
    void success2() {
        // List<Server> servers = new ArrayList<Server>();
        // for (int i = 0; i < 3; i++) {
        //     char[] password = {'p','a','s','s','w','o','r','d'};
        //     Server server = new Server(false, 4, 1, 9001+i, password, password,
        //         "alias", "../server/src/main/resources/crypto/public"+(i+1)+".key",
        //         "../server/src/main/resources/crypto/server_keystore"+(i+1)+".jks");
        //     servers.add(server);
        // }
        // for (Server server: servers) {
        //     Thread thread = new Thread(){
        //         public void run() {
        //             server.start();
        //         }
        //     };
        //     thread.start();
        // }

        _client.postGeneral("Ok. So. We have a question in chat from João Martinho. Yes. That is rright.", new ArrayList<>());

        while (_client.postGeneralDelivered == false) {
            try {
                Thread.sleep(1000);
            } catch(Exception e) {

            }
        }

        assertEquals(StatusCode.OK, _client.postGeneralDeliveredSC);
        
    }

    @BeforeEach
    void resetClient() {
        try {
            Thread.sleep(2000);
        } catch(Exception e) {
        }
        _client.postGeneralDelivered = false;
    }

}
