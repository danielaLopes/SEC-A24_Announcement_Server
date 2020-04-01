import pt.ulisboa.tecnico.sec.client.Client;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.AbstractMap;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class ReadGeneralTest extends BaseTest {

    private Client _client;

    public ReadGeneralTest() {
        List<String> otherUsersPubKeyPaths = new ArrayList<String>();
        otherUsersPubKeyPaths.add(PUBLICKEY_PATH2);
        otherUsersPubKeyPaths.add(PUBLICKEY_PATH3);

        _client = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH, otherUsersPubKeyPaths);    }

    @Test
    void success() {
        _client.postGeneral(MESSAGE, REFERENCES);
        AbstractMap.SimpleEntry<Integer, List<Announcement>> response = _client.readGeneral(1);
        
        assertEquals(response.getKey(), StatusCode.OK.getCode());
        assertEquals(response.getValue().size(), 1);
    }

    @Test
    void negativeNumberOfAnnouncements() {
        _client.postGeneral(MESSAGE, REFERENCES);
        AbstractMap.SimpleEntry<Integer, List<Announcement>> response = _client.readGeneral(-1);
        
        assertEquals(response.getKey(), StatusCode.OK.getCode());
        assertTrue(response.getValue().size() > 0);
    }

    @Test
    void zeroNumberOfAnnouncements() {
        _client.postGeneral(MESSAGE, REFERENCES);
        AbstractMap.SimpleEntry<Integer, List<Announcement>> response = _client.readGeneral(0);
        
        assertEquals(response.getKey(), StatusCode.OK.getCode());
        assertTrue(response.getValue().size() > 0);
    }

    @Test
    void tooManyAnnouncements() {
        for (int i = 0; i < 50; i++) {
            _client.postGeneral(MESSAGE, REFERENCES);
        }
        
        AbstractMap.SimpleEntry<Integer, List<Announcement>> response = _client.readGeneral(0);
        
        assertEquals(response.getKey(), StatusCode.OK.getCode());
        assertTrue(response.getValue().size() >= 50);
    }

}