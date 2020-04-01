import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PostGeneralTest extends BaseTest {

    private Client _client;
    
    public PostGeneralTest() {
        List<String> otherUsersPubKeyPaths = new ArrayList<String>();
        otherUsersPubKeyPaths.add(PUBLICKEY_PATH2);
        otherUsersPubKeyPaths.add(PUBLICKEY_PATH3);

        _client = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH, otherUsersPubKeyPaths);
    }

    @Test
    void success() {
        int statusCode = _client.postGeneral(MESSAGE, REFERENCES);
        
        assertEquals(statusCode, StatusCode.OK.getCode());
    }

    @Test
    void messageLengthIsInvalid() {
        String invalidMessage = "";
        for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
            invalidMessage += "A";
        }

        int statusCode = _client.postGeneral(invalidMessage, REFERENCES);
        
        assertEquals(statusCode, StatusCode.INVALID_MESSAGE_LENGTH.getCode());
    }

    @Test
    void messageIsNull() {
        int statusCode = _client.postGeneral(null, REFERENCES);
        
        assertEquals(statusCode, StatusCode.NULL_FIELD.getCode());
    }

    @Test
    void referencesIsNull() {
        int statusCode = _client.postGeneral(MESSAGE, null);
        
        assertEquals(statusCode, StatusCode.NULL_FIELD.getCode());
    }

}