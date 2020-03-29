import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PostTest extends BaseTest {

    private Client _client;
    private List<String> _otherUsersPubKeyPaths;

    public PostTest() {
        _otherUsersPubKeyPaths = new ArrayList<String>();
        _otherUsersPubKeyPaths.add(PUBLICKEY_PATH2);
        _otherUsersPubKeyPaths.add(PUBLICKEY_PATH3);

        _client = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH, _otherUsersPubKeyPaths);
    }

    @Test
    void success() {
        int statusCode = _client.post(MESSAGE, REFERENCES);
        
        assertEquals(statusCode, StatusCode.OK.getCode());
    }

    @Test
    void messageLengthIsInvalid() {
        String invalidMessage = "";
        for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
            invalidMessage += "A";
        }

        int statusCode = _client.post(invalidMessage, REFERENCES);
        
        assertEquals(statusCode, StatusCode.INVALID_MESSAGE_LENGTH.getCode());
    }

    @Test
    void messageIsNull() {
        int statusCode = _client.post(null, REFERENCES);
        
        assertEquals(statusCode, -1);
    }

    @Test
    void referencesIsNull() {
        int statusCode = _client.post(MESSAGE, null);
        
        assertEquals(statusCode, -1);
    }

}