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

    private Client _client1, _client2, _client3;

    public ReadGeneralTest() {
        _client1 = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);
        _client2 = new Client(PUBLICKEY_PATH2, KEYSTORE_PATH2, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);
        _client3 = new Client(PUBLICKEY_PATH3, KEYSTORE_PATH3, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);
    }

    @Test
    void success() {
        _client1.postGeneral(MESSAGE, REFERENCES);
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response = _client1.readGeneral(1);
        
        assertEquals(response.getKey(), StatusCode.OK);
        assertEquals(response.getValue().size(), 1);
        assertEquals(response.getValue().get(0).getAnnouncement(), MESSAGE);
    }

    @Test
    void tooManyUsers() {
        _client1.postGeneral(MESSAGE, REFERENCES);
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response1 = _client1.readGeneral(1);
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response2 = _client2.readGeneral(1);
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response3 = _client3.readGeneral(1);
        
        assertEquals(response1.getKey(), StatusCode.OK);
        assertEquals(response1.getValue().size(), 1);
        assertEquals(response1.getValue().get(0).getAnnouncement(), MESSAGE);

        assertEquals(response2.getKey(), StatusCode.OK);
        assertEquals(response2.getValue().size(), 1);
        assertEquals(response2.getValue().get(0).getAnnouncement(), MESSAGE);
        
        assertEquals(response3.getKey(), StatusCode.OK);
        assertEquals(response3.getValue().size(), 1);
        assertEquals(response3.getValue().get(0).getAnnouncement(), MESSAGE);
    }

    @Test
    void negativeNumberOfAnnouncements() {
        _client1.postGeneral(MESSAGE, REFERENCES);
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response = _client1.readGeneral(-1);
        
        assertEquals(response.getKey(), StatusCode.OK);
        assertTrue(response.getValue().size() > 0);
    }

    @Test
    void zeroNumberOfAnnouncements() {
        _client1.postGeneral(MESSAGE, REFERENCES);
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response = _client1.readGeneral(0);
        
        assertEquals(response.getKey(), StatusCode.OK);
        assertTrue(response.getValue().size() > 0);
    }

    @Test
    void tooManyAnnouncements() {
        for (int i = 0; i < 5; i++) {
            _client1.postGeneral(MESSAGE, REFERENCES);
        }
        for (int i = 0; i < 5; i++) {
            _client2.postGeneral(MESSAGE, REFERENCES);
        }
        for (int i = 0; i < 5; i++) {
            _client3.postGeneral(MESSAGE, REFERENCES);
        }
        
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response1 = _client1.readGeneral(0);
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response2 = _client2.readGeneral(0);
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response3 = _client3.readGeneral(0);
        
        assertEquals(response1.getKey(), StatusCode.OK);
        assertTrue(response1.getValue().size() >= 15);

        assertEquals(response2.getKey(), StatusCode.OK);
        assertTrue(response2.getValue().size() >= 15);
        
        assertEquals(response3.getKey(), StatusCode.OK);
        assertTrue(response3.getValue().size() >= 15);

        for (Announcement a: response1.getValue()) {
            assertEquals(a.getAnnouncement(), MESSAGE);
        }
        for (Announcement a: response2.getValue()) {
            assertEquals(a.getAnnouncement(), MESSAGE);
        }
        for (Announcement a: response3.getValue()) {
            assertEquals(a.getAnnouncement(), MESSAGE);
        }
    }

}