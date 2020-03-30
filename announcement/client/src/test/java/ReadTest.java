import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.AbstractMap;
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
        _client1.post(MESSAGE, REFERENCES);
        AbstractMap.SimpleEntry<Integer, List<Announcement>> response = _client2.read(1, 1);
        
        assertEquals(response.getKey(), StatusCode.OK.getCode());
        assertEquals(response.getValue().size(), 1);
    }

    @Test
    void negativeNumberOfAnnouncements() {
        _client1.post(MESSAGE, REFERENCES);
        AbstractMap.SimpleEntry<Integer, List<Announcement>> response = _client2.read(1, -1);
        
        assertEquals(response.getKey(), StatusCode.OK.getCode());
        assertTrue(response.getValue().size() > 0);
    }

    @Test
    void zeroNumberOfAnnouncements() {
        _client1.post(MESSAGE, REFERENCES);
        AbstractMap.SimpleEntry<Integer, List<Announcement>> response = _client2.read(1, 0);
        
        assertEquals(response.getKey(), StatusCode.OK.getCode());
        assertTrue(response.getValue().size() > 0);
    }

    @Test
    void tooManyAnnouncements() {
        for (int i = 0; i < 50; i++) {
            _client1.post(MESSAGE, REFERENCES);
        }
        
        AbstractMap.SimpleEntry<Integer, List<Announcement>> response = _client2.read(1, 0);
        
        assertEquals(response.getKey(), StatusCode.OK.getCode());
        assertTrue(response.getValue().size() >= 50);
    }

    @Test
    void userIsNegative() {
        AbstractMap.SimpleEntry<Integer, List<Announcement>> response = _client2.read(-1, 1);
        
        assertEquals(response.getKey(), StatusCode.USER_NOT_REGISTERED.getCode());
        assertEquals(response.getValue().size(), 0);
    }

    @Test
    void userDoesNotExist() {
        AbstractMap.SimpleEntry<Integer, List<Announcement>> response = _client2.read(2, 1);
        
        assertEquals(response.getKey(), StatusCode.USER_NOT_REGISTERED.getCode());
        assertEquals(response.getValue().size(), 0);
    }

}