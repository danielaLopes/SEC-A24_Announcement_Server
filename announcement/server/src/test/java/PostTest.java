import org.bouncycastle.jcajce.provider.symmetric.ARC4;
import org.junit.jupiter.api.Test;
import pt.ulisboa.tecnico.sec.server.Server;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostTest extends BaseTest {
    private Server _server;

    public PostTest() {

        _server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);
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

    // Invalid Announcement fields tests
    @Test
    void messageLengthIsInvalid() {
        /*String invalidMessage = "";
        for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
            invalidMessage += "A";
        }

        boolean success = _client.post(invalidMessage, REFERENCES);

        assertEquals(success, false);*/
    }

    @Test
    void InvalidReferences() {
        /*String invalidMessage = "";
        for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
            invalidMessage += "A";
        }

        boolean success = _client.post(invalidMessage, REFERENCES);

        assertEquals(success, false);*/
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
