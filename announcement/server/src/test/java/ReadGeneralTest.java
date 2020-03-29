import org.junit.jupiter.api.Test;
import pt.ulisboa.tecnico.sec.server.Server;

import java.util.List;

public class ReadGeneralTest extends BaseTest {

    private Server _server;
    private List<String> _otherUsersPubKeyPaths;

    public ReadGeneralTest() {
        /*_otherUsersPubKeyPaths = new ArrayList<String>();
        _otherUsersPubKeyPaths.add(PUBLICKEY_PATH2);
        _otherUsersPubKeyPaths.add(PUBLICKEY_PATH3);

        _server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH, _otherUsersPubKeyPaths);*/
    }

    @Test
    void success() {
        /*boolean success = _client.post(MESSAGE, REFERENCES);

        assertEquals(success, true);*/
    }

    // Unregistered User
    @Test
    void userNotRegistered() {

    }

    // Number of announcements to retrieve related tests
    @Test
    void negativeNumberOfAnnouncements() {
        /*boolean success = _client.post(MESSAGE, REFERENCES);

        assertEquals(success, true);*/
    }

    @Test
    void zeroNumberOfAnnouncements() {
        /*boolean success = _client.post(MESSAGE, REFERENCES);

        assertEquals(success, true);*/
    }

    @Test
    void tooManyAnnouncements() {
        /*boolean success = _client.post(MESSAGE, REFERENCES);

        assertEquals(success, true);*/
    }

    // null Parameters
    @Test
    void publicKeyIsNull() {
        /*int statusCode = _client.post(null, REFERENCES);

        assertEquals(statusCode, -1);*/
    }

    @Test
    void numberIsNull() {
        /*int statusCode = _client.post(null, REFERENCES);

        assertEquals(statusCode, -1);*/
    }

    @Test
    void opUuidIsNull() {
        /*int statusCode = _client.post(null, REFERENCES);

        assertEquals(statusCode, -1);*/
    }

    @Test
    void signatureIsNull() {
        /*int statusCode = _client.post(null, REFERENCES);

        assertEquals(statusCode, -1);*/
    }

    // Replay attacks
    @Test
    void duplicatedOperation() {
        /*String invalidMessage = "";
        for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
            invalidMessage += "A";
        }

        boolean success = _client.post(invalidMessage, REFERENCES);

        assertEquals(success, false);*/
    }

    // Message Integrity attacks
    @Test
    void tamperedMessage() {
        /*String invalidMessage = "";
        for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
            invalidMessage += "A";
        }

        boolean success = _client.post(invalidMessage, REFERENCES);

        assertEquals(success, false);*/
    }

    @Test
    void invalidSignature() {
        /*String invalidMessage = "";
        for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
            invalidMessage += "A";
        }

        boolean success = _client.post(invalidMessage, REFERENCES);

        assertEquals(success, false);*/
    }
}
