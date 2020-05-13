import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RefreshTokenTest extends BaseTest {
    static private Client _client;

    @Test
    void success() {
        List<StatusCode> statusCodes = _client.refreshTokenServersGroup();
        
        for (StatusCode sc: statusCodes) {
            assertEquals(StatusCode.OK, sc); 
        }
    }

    @BeforeAll
    static void setup() {
        _client = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, 4, 1);
    }

    @AfterAll
    static void end() {
        _client.closeGroupCommunication();
    }
}
