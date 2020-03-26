import pt.ulisboa.tecnico.sec.client.Client;

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
        boolean successPost = _client1.post(MESSAGE, REFERENCES);
        boolean successRead = _client2.read(0, 1);
        
        assertEquals(successPost, true);
        assertEquals(successRead, true);
    }

    @Test
    void numberOfAnnouncementsIsZero() {
        boolean success = _client2.read(0, 0);
        
        assertEquals(success, false);
    }

    @Test
    void numberOfAnnouncementsIsNegative() {
        boolean success = _client2.read(0, -1);
        
        assertEquals(success, false);
    }

    @Test
    void userIsNegative() {
        boolean success = _client2.read(-1, 1);
        
        assertEquals(success, false);
    }

    @Test
    void userDoesNotExist() {
        boolean success = _client2.read(1, 1);
        
        assertEquals(success, false);
    }

}