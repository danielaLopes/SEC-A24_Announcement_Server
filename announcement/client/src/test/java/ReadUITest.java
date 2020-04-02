import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.AbstractMap;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class ReadUITest extends BaseTest {

    static private Client _client1, _client2, _client3;

    public ReadUITest() {
        List<String> otherUsersPubKeyPaths = new ArrayList<String>();
        otherUsersPubKeyPaths.add(PUBLICKEY_PATH2);
        otherUsersPubKeyPaths.add(PUBLICKEY_PATH3);
        _client1 = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH, otherUsersPubKeyPaths);

        otherUsersPubKeyPaths = new ArrayList<String>();
        otherUsersPubKeyPaths.add(PUBLICKEY_PATH1);
        otherUsersPubKeyPaths.add(PUBLICKEY_PATH3);
        _client2 = new Client(PUBLICKEY_PATH2, KEYSTORE_PATH2, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH, otherUsersPubKeyPaths);

        otherUsersPubKeyPaths = new ArrayList<String>();
        otherUsersPubKeyPaths.add(PUBLICKEY_PATH1);
        otherUsersPubKeyPaths.add(PUBLICKEY_PATH2);
        _client3 = new Client(PUBLICKEY_PATH3, KEYSTORE_PATH3, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH, otherUsersPubKeyPaths);
    }

    @Test
    void success() {
        _client1.post(MESSAGE, REFERENCES);
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response = _client2.read(1, 1);
        
        assertEquals(response.getKey(), StatusCode.OK);
        assertEquals(response.getValue().size(), 1);
    }

    @Test
    void tooManyUsers() {
        _client1.postGeneral(MESSAGE, REFERENCES);
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response2 = _client2.read(1, 1);
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response3 = _client3.read(1, 1);

        assertEquals(response2.getKey(), StatusCode.OK);
        assertEquals(response2.getValue().size(), 1);
        
        assertEquals(response3.getKey(), StatusCode.OK);
        assertEquals(response3.getValue().size(), 1);
    }

    @Test
    void negativeNumberOfAnnouncements() {
        _client1.post(MESSAGE, REFERENCES);
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response = _client2.read(1, -1);
        
        assertEquals(response.getKey(), StatusCode.OK);
        assertTrue(response.getValue().size() > 0);
    }

    @Test
    void zeroNumberOfAnnouncements() {
        _client1.post(MESSAGE, REFERENCES);
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response = _client2.read(1, 0);
        
        assertEquals(response.getKey(), StatusCode.OK);
        assertTrue(response.getValue().size() > 0);
    }

    @Test
    void tooManyAnnouncements() {
        for (int i = 0; i < 15; i++) {
            _client1.post(MESSAGE, REFERENCES);
        }
        
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response = _client2.read(1, 0);
        
        assertEquals(response.getKey(), StatusCode.OK);
        assertTrue(response.getValue().size() >= 15);
    }

    @Test
    void userIsNegative() {
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response = _client2.read(-1, 1);
        
        assertEquals(response.getKey(), StatusCode.USER_NOT_REGISTERED);
        assertEquals(response.getValue().size(), 0);
    }

    @Test
    void userDoesNotExist() {
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response = _client2.read(3, 1);
        
        assertEquals(response.getKey(), StatusCode.USER_NOT_REGISTERED);
        assertEquals(response.getValue().size(), 0);
    }

    @AfterAll
    static void closeCommunications() {
        _client1.closeCommunication();
        _client2.closeCommunication();
        _client3.closeCommunication();
    }

}