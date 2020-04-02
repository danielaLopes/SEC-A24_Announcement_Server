import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.security.PublicKey;
import java.util.AbstractMap;

import org.junit.jupiter.api.Test;

class ReadTest extends BaseTest {

    private Client _client1, _client2, _client3;
    private PublicKey _pubKey1, _pubKey2, _pubKey3;

    public ReadTest() throws Exception {
        _client1 = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);
        _client2 = new Client(PUBLICKEY_PATH2, KEYSTORE_PATH2, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);
        _client3 = new Client(PUBLICKEY_PATH3, KEYSTORE_PATH3, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);

        _pubKey1 = loadPublicKey(PUBLICKEY_PATH1);
        _pubKey2 = loadPublicKey(PUBLICKEY_PATH2);
        _pubKey3 = loadPublicKey(PUBLICKEY_PATH3);
    }

    @Test
    void success() throws Exception {
        _client1.post(MESSAGE, REFERENCES);

        AbstractMap.SimpleEntry<Integer, List<Announcement>> response = _client2.read(_pubKey1, 1);
        
        assertEquals(response.getKey(), StatusCode.OK.getCode());
        assertEquals(response.getValue().size(), 1);
    }

    @Test
    void tooManyUsers() throws Exception {
        _client1.post(MESSAGE, REFERENCES);

        AbstractMap.SimpleEntry<Integer, List<Announcement>> response2 = _client2.read(_pubKey1, 1);
        AbstractMap.SimpleEntry<Integer, List<Announcement>> response3 = _client3.read(_pubKey1, 1);

        assertEquals(response2.getKey(), StatusCode.OK.getCode());
        assertEquals(response2.getValue().size(), 1);
        
        assertEquals(response3.getKey(), StatusCode.OK.getCode());
        assertEquals(response3.getValue().size(), 1);
    }

    @Test
    void negativeNumberOfAnnouncements() {
        _client1.post(MESSAGE, REFERENCES);
        AbstractMap.SimpleEntry<Integer, List<Announcement>> response = _client2.read(_pubKey1, -1);
        
        assertEquals(response.getKey(), StatusCode.OK.getCode());
        assertTrue(response.getValue().size() > 0);
    }

    @Test
    void zeroNumberOfAnnouncements() {
        _client1.post(MESSAGE, REFERENCES);
        AbstractMap.SimpleEntry<Integer, List<Announcement>> response = _client2.read(_pubKey1, 0);
        
        assertEquals(response.getKey(), StatusCode.OK.getCode());
        assertTrue(response.getValue().size() > 0);
    }

    @Test
    void tooManyAnnouncements() {
        for (int i = 0; i < 5; i++) {
            _client1.post(MESSAGE, REFERENCES);
        }
        
        AbstractMap.SimpleEntry<Integer, List<Announcement>> response = _client2.read(_pubKey1, 0);
        
        assertEquals(response.getKey(), StatusCode.OK.getCode());
        assertTrue(response.getValue().size() >= 5);
    }

    @Test
    void userIsNull() {
        AbstractMap.SimpleEntry<Integer, List<Announcement>> response = _client2.read(null, 1);
        
        assertEquals(response.getKey(), StatusCode.NULL_FIELD.getCode());
        assertEquals(response.getValue().size(), 0);
    }

}