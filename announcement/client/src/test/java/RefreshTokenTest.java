import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class RefreshTokenTest extends BaseTest {
    static private Client _client;

    @Test
    void success() {
        _client = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);

        StatusCode statusCode = _client.refreshToken();

        assertEquals(StatusCode.OK, statusCode);

        _client.closeCommunication();
    }
}