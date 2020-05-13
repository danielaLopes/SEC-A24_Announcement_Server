import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PostGeneralTest extends BaseTest {

    static private Client _client1, _client2;

    @Test
    void success() {
        _client1.postGeneral("Hello Quorum!", new ArrayList<>());
        
        while (_client1.postGeneralDelivered == false) { sleep(); }

        assertEquals(StatusCode.OK, _client1.postGeneralDeliveredSC);
    }

    @Test
    void successTwoClients() {
        _client1.postGeneral("Hello Quorum!", new ArrayList<>());
        // sleep();
        _client2.postGeneral("Hello Quorum!", new ArrayList<>());
        
        while (_client1.postGeneralDelivered == false ||_client2.postGeneralDelivered == false) { sleep(); }

        assertEquals(StatusCode.OK, _client1.postGeneralDeliveredSC);
        assertEquals(StatusCode.OK, _client2.postGeneralDeliveredSC);
    }

    @Test
    void invalidMessage() {
        List<StatusCode> sc = _client1.postGeneral(null, new ArrayList<>());
        assertEquals(StatusCode.NULL_FIELD, sc.get(0));
    }

    @Test
    void invalidReferences() {
        List<StatusCode> sc = _client1.postGeneral("Hello Quorum!", null);
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