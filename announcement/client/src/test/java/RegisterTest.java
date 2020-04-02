import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RegisterTest extends BaseTest {

    private Client _client1, _client2, _client3;

    @Test
    void success() {
        _client1 = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);

        int statusCode = _client1.register();

        assertEquals(StatusCode.USER_ALREADY_REGISTERED.getCode(), statusCode);
    }

    @Test
    void registerTwoUsers() {
        _client1 = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);
        _client2 = new Client(PUBLICKEY_PATH2, KEYSTORE_PATH2, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);

        int statusCode1 = _client1.register();
        int statusCode2 = _client1.register();

        assertEquals(StatusCode.USER_ALREADY_REGISTERED.getCode(), statusCode1);
        assertEquals(StatusCode.USER_ALREADY_REGISTERED.getCode(), statusCode2);
    }

    @Test
    void registerThreeUsers() {
        _client1 = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);
        _client2 = new Client(PUBLICKEY_PATH2, KEYSTORE_PATH2, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);
        _client3 = new Client(PUBLICKEY_PATH3, KEYSTORE_PATH3, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);

        int statusCode1 = _client1.register();
        int statusCode2 = _client2.register();
        int statusCode3 = _client3.register();

        assertEquals(StatusCode.USER_ALREADY_REGISTERED.getCode(), statusCode1);
        assertEquals(StatusCode.USER_ALREADY_REGISTERED.getCode(), statusCode2);
        assertEquals(StatusCode.USER_ALREADY_REGISTERED.getCode(), statusCode3);
    }

}