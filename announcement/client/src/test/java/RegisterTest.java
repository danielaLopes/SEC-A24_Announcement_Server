import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegisterTest extends BaseTest {

    static private Client _client;

    @Test
    void success() {
        _client = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, 4, 1);
        _client.closeGroupCommunication();
    }

    @Test
    void registerTwice() {
        _client = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, 4, 1);
        sleep();

        List<StatusCode> statusCodes = _client.registerServersGroup();
        
        for (StatusCode sc: statusCodes) {
            assertEquals(StatusCode.USER_ALREADY_REGISTERED, sc); 
        }

        _client.closeGroupCommunication();
    }

    @BeforeAll
    static void setup() {
    }

    @AfterAll
    static void end() {
    }

}