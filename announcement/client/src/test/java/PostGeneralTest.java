import pt.ulisboa.tecnico.sec.client.Client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PostGeneralTest extends BaseTest {

    private Client _client;
    private List<String> _otherUsersPubKeyPaths;

    public PostGeneralTest() {
        _otherUsersPubKeyPaths = new ArrayList<String>();
        _otherUsersPubKeyPaths.add(PUBLICKEY_PATH2);
        _otherUsersPubKeyPaths.add(PUBLICKEY_PATH3);

        _client = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, _otherUsersPubKeyPaths);
    }

    @Test
    void success() {
        boolean success = _client.postGeneral(MESSAGE, REFERENCES);
        
        assertEquals(success, true);
    }

    @Test
    void messageLengthIsInvalid() {
        String invalidMessage = "";
        for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
            invalidMessage += "A";
        }

        boolean success = _client.postGeneral(invalidMessage, REFERENCES);
        
        assertEquals(success, false);
    }

}