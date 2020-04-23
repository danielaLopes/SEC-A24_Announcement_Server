import org.junit.jupiter.api.AfterAll;
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

    static ClientTest _clientTest;

    PostGeneralTest() {
        _clientTest = new ClientTest(PUBLICKEY_PATH1, KEYSTORE_PATH1, CLIENT_KEYSTORE_PASSWD,
                CLIENT_ENTRY_PASSWD, ALIAS, SERVER_PUBLIC_KEY_PATH);
    }

    @Test
    void success() {
        StatusCode sc = _clientTest.postGeneral("message1", new ArrayList<>());
        assertEquals(StatusCode.OK, sc);
    }

    @Test
    void tooManyAnnouncements() {
        for (int i = 0; i < 10; i++) {
            StatusCode sc = _clientTest.postGeneral("message" + (i+1), new ArrayList<>());
            assertEquals(StatusCode.OK, sc);
        }
    }

    @Test
    void repeatMessage() {
        List<StatusCode> rsc = _clientTest.postGeneralTwice("message1", new ArrayList<>());
        for (StatusCode sc: rsc) {
            assertEquals(StatusCode.OK, sc);
        }
    }

    @Test
    void tamperedMessage() {
        StatusCode sc = _clientTest.postGeneralTampered("message1", new ArrayList<>());
        assertEquals(StatusCode.INVALID_SIGNATURE, sc);
    }

    @Test
    void droppedMessage() {
        StatusCode sc = _clientTest.postGeneralDropped("message1", new ArrayList<>());
        assertEquals(StatusCode.NO_RESPONSE, sc);
    }

    @Test
    void receivedNullMessage() {
        StatusCode sc = _clientTest.postGeneralNull("message1", new ArrayList<>());
        assertEquals(StatusCode.NO_RESPONSE, sc);
    }

    @Test
    void invalidRequest() {
        StatusCode sc = _clientTest.postGeneralInvalid("message1", new ArrayList<>());
        assertEquals(StatusCode.INVALID_COMMAND, sc);
    }

}
