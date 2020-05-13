import pt.ulisboa.tecnico.sec.client.Client;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.AbstractMap;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ReadGeneralGeneralTest extends BaseTest {

    static private Client _client1, _client2;

    @Test
    void success() {
        _client1.readGeneral(1);
        
        while (_client1.readGeneralDelivered == false) { sleep(); }

        assertEquals(StatusCode.OK, _client1.readGeneralDeliveredSC);
    }

    @Test
    void successTwoClients() {
        _client1.readGeneral(1);
        // sleep();
        _client2.readGeneral(1);
        
        while (_client1.readGeneralDelivered == false ||_client2.readGeneralDelivered == false) { sleep(); }

        assertEquals(StatusCode.OK, _client1.readGeneralDeliveredSC);
        assertEquals(StatusCode.OK, _client2.readGeneralDeliveredSC);
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