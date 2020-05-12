import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.client.ClientTest;
import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableProtocolMessage;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;
import pt.ulisboa.tecnico.sec.server.Server;
import org.junit.jupiter.api.BeforeEach;


import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadTest extends BaseTest {

    /*static ClientTest _clientTest;

    ReadTest() {
        _clientTest = new ClientTest(PUBLICKEY_PATH1, KEYSTORE_PATH1, CLIENT_KEYSTORE_PASSWD,
                CLIENT_ENTRY_PASSWD, ALIAS, SERVER_PUBLIC_KEY_PATH);
    }

    @Test
    void success() {
        StatusCode sc1 = _clientTest.post("message1", new ArrayList<>());
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response = _clientTest.read(_clientTest.getPubKey(), 1);
        StatusCode sc2 = response.getKey();
        List<Announcement> announcements = response.getValue();

        assertEquals(StatusCode.OK, sc1);
        assertEquals(StatusCode.OK, sc2);
        assertEquals(announcements.get(0).getAnnouncement(), "message1");
    }

    @Test
    void tooManyAnnouncements() {
        for (int i = 0; i < 10; i++) {
            StatusCode sc1 = _clientTest.post("message" + (i+1), new ArrayList<>());
            assertEquals(StatusCode.OK, sc1);
        }
        
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response = _clientTest.read(_clientTest.getPubKey(), 10);
        StatusCode sc2 = response.getKey();
        List<Announcement> announcements = response.getValue();
        assertEquals(StatusCode.OK, sc2);

        for (int i = 9; i >= 0; i--) {
            assertEquals(announcements.get(i).getAnnouncement(), "message" + (i+1));
        }
    }

    @Test
    void repeatMessage() {
        StatusCode sc1 = _clientTest.post("message1", new ArrayList<>());
        assertEquals(StatusCode.OK, sc1);

        List<StatusCode> rsc = _clientTest.readTwice(_clientTest.getPubKey(), 1).getKey();
        assertEquals(StatusCode.OK, rsc.get(0));
        assertEquals(StatusCode.INVALID_TOKEN, rsc.get(1));
    }

    @Test
    void tamperedMessage() {
        StatusCode sc1 = _clientTest.post("message1", new ArrayList<>());
        assertEquals(StatusCode.OK, sc1);

        StatusCode sc2 = _clientTest.readTampered(_clientTest.getPubKey(), 1).getKey();
        assertEquals(StatusCode.INVALID_SIGNATURE, sc2);
    }

    @Test
    void droppedMessage() {
        StatusCode sc1 = _clientTest.post("message1", new ArrayList<>());
        assertEquals(StatusCode.OK, sc1);

        StatusCode sc2 = _clientTest.readDropped(_clientTest.getPubKey(), 1).getKey();
        assertEquals(StatusCode.NO_RESPONSE, sc2);
    }

    @Test
    void receivedNullMessage() {
        StatusCode sc1 = _clientTest.post("message1", new ArrayList<>());
        assertEquals(StatusCode.OK, sc1);

        StatusCode sc2 = _clientTest.readNull(_clientTest.getPubKey(), 1).getKey();
        assertEquals(StatusCode.NO_RESPONSE, sc2);
    }

    @Test
    void invalidRequest() {
        StatusCode sc1 = _clientTest.post("message1", new ArrayList<>());
        assertEquals(StatusCode.OK, sc1);

        StatusCode sc2 = _clientTest.readInvalid(_clientTest.getPubKey(), 1).getKey();
        assertEquals(StatusCode.INVALID_COMMAND, sc2);
    }

    @BeforeEach
    void resetClient() {
        _clientTest = new ClientTest(PUBLICKEY_PATH1, KEYSTORE_PATH1, CLIENT_KEYSTORE_PASSWD,
                CLIENT_ENTRY_PASSWD, ALIAS, SERVER_PUBLIC_KEY_PATH);
    }*/

}
