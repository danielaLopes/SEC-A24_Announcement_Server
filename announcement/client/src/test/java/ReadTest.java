import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.security.PublicKey;
import java.util.AbstractMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ReadTest extends BaseTest {

    static private Client _client1, _client2;

    @Test
    void success() {
        _client1.read(_client1.getPubKey(), 1);
        
        while (_client1.readDelivered == false) { sleep(); }

        assertEquals(StatusCode.OK, _client1.readDeliveredSC);
    }

    @Test
    void successTwoClients() {
        _client1.read(_client1.getPubKey(), 1);
        // sleep();
        _client2.read(_client1.getPubKey(), 1);
        
        while (_client1.readDelivered == false ||_client2.readDelivered == false) { sleep(); }

        assertEquals(StatusCode.OK, _client1.readDeliveredSC);
        assertEquals(StatusCode.OK, _client2.readDeliveredSC);
    }

    @Test
    void invalidUser() {
        StatusCode sc = _client1.read(null, 1).get(0).getKey();
        assertEquals(StatusCode.NULL_FIELD, sc);
    }

    @BeforeAll
    static void setup() {
        _client1 = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, 4, 1);
        _client2 = new Client(PUBLICKEY_PATH2, KEYSTORE_PATH2, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, 4, 1);
    }

    @AfterAll
    static void end() {
        _client1.closeGroupCommunication();
        _client2.closeGroupCommunication();
    }

}