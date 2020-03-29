import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class ReadGeneralTest extends BaseTest {

    private Client _client;
    private List<String> _otherUsersPubKeyPaths;

    public ReadGeneralTest() {
        _otherUsersPubKeyPaths = new ArrayList<String>();
        _otherUsersPubKeyPaths.add(PUBLICKEY_PATH2);
        _otherUsersPubKeyPaths.add(PUBLICKEY_PATH3);

        _client = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH, _otherUsersPubKeyPaths);    }

    @Test
    void success() {
        _client.post(MESSAGE, REFERENCES);
        int statusCode = _client.readGeneral(1);
        
        assertEquals(statusCode, StatusCode.OK.getCode());
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

}