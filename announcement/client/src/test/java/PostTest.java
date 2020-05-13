import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PostTest extends BaseTest {

    static private Client _client1, _client2;

    public PostTest() {
    }

    @Test
    void success() {
        _client1.post("Hello Quorum!", new ArrayList<>());
        
        while (_client1.postDelivered == false) { sleep(); }

        assertEquals(StatusCode.OK, _client1.postDeliveredSC);
    }

    @Test
    void successTwoClients() {
        _client1.post("Hello Quorum!", new ArrayList<>());
        // sleep();
        _client2.post("Hello Quorum!", new ArrayList<>());
        
        while (_client1.postDelivered == false ||_client2.postDelivered == false) { sleep(); }

        assertEquals(StatusCode.OK, _client1.postDeliveredSC);
        assertEquals(StatusCode.OK, _client2.postDeliveredSC);
    }

    @Test
    void invalidMessage() {
        List<StatusCode> sc = _client1.post(null, new ArrayList<>());
        assertEquals(StatusCode.NULL_FIELD, sc.get(0));
    }

    @Test
    void invalidReferences() {
        List<StatusCode> sc = _client1.post("Hello Quorum!", null);
        assertEquals(StatusCode.NULL_FIELD, sc.get(0));
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