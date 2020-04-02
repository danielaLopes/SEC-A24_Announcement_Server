import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PostTest extends BaseTest {

    private Client _client1, _client2, _client3;

    public PostTest() {
        _client1 = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);
        _client2 = new Client(PUBLICKEY_PATH2, KEYSTORE_PATH2, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);
        _client3 = new Client(PUBLICKEY_PATH3, KEYSTORE_PATH3, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);
    }

    @Test
    void success() {
        StatusCode statusCode = _client1.post(MESSAGE, REFERENCES);
        
        assertEquals(statusCode, StatusCode.OK);
    }

    @Test
    void tooManyUsers() {
        StatusCode statusCode1 = _client1.post(MESSAGE, REFERENCES);
        StatusCode statusCode2 = _client2.post(MESSAGE, REFERENCES);
        StatusCode statusCode3 = _client3.post(MESSAGE, REFERENCES);
        
        assertEquals(statusCode1, StatusCode.OK);
        assertEquals(statusCode2, StatusCode.OK);
        assertEquals(statusCode3, StatusCode.OK);
    }

    @Test
    void messageLengthIsInvalid() {
        String invalidMessage = "";
        for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
            invalidMessage += "A";
        }

        StatusCode statusCode = _client1.post(invalidMessage, REFERENCES);
        
        assertEquals(statusCode, StatusCode.INVALID_MESSAGE_LENGTH);
    }

    @Test
    void messageIsNull() {
        StatusCode statusCode = _client1.post(null, REFERENCES);
        
        assertEquals(statusCode, StatusCode.NULL_FIELD);
    }

    @Test
    void referencesIsNull() {
        StatusCode statusCode = _client1.post(MESSAGE, null);
        
        assertEquals(statusCode, StatusCode.NULL_FIELD);
    }

}