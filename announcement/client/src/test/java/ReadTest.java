import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class ReadTest extends BaseTest {

    private Client _client1, _client2;
    private List<String> _otherUsersPubKeyPaths;

    public ReadTest() {
        _otherUsersPubKeyPaths = new ArrayList<String>();
        _otherUsersPubKeyPaths.add(PUBLICKEY_PATH2);
        _client1 = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH, _otherUsersPubKeyPaths);
        
        _otherUsersPubKeyPaths = new ArrayList<String>();
        _otherUsersPubKeyPaths.add(PUBLICKEY_PATH1);
        _client2 = new Client(PUBLICKEY_PATH2, KEYSTORE_PATH2, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH, _otherUsersPubKeyPaths);    }

    @Test
    void success() {
        int statusCodePost = _client1.post(MESSAGE, REFERENCES);
        int statusCodeRead = _client2.read(0, 1);
        
        assertEquals(statusCodePost, StatusCode.OK.getCode());
        assertEquals(statusCodeRead, StatusCode.OK.getCode());
    }

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

    @Test
    void userIsNegative() {
        int statusCode = _client2.read(-1, 1);
        
        assertEquals(statusCode, StatusCode.USER_NOT_REGISTERED.getCode());
    }

    @Test
    void userDoesNotExist() {
        int statusCode = _client2.read(1, 1);
        
        assertEquals(statusCode, StatusCode.USER_NOT_REGISTERED.getCode());
    }

}