import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RegisterTest extends BaseTest {

    static private Client _client1, _client2, _client3;

    @Test
    void success() {
        _client1 = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);

        StatusCode statusCode = _client1.register();

        assertEquals(StatusCode.USER_ALREADY_REGISTERED, statusCode);

        _client1.closeCommunication(0);
    }

    @Test
    void registerTwoUsers() {
        _client1 = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);
        _client2 = new Client(PUBLICKEY_PATH2, KEYSTORE_PATH2, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);

        StatusCode statusCode1 = _client1.register();
        StatusCode statusCode2 = _client1.register();

        assertEquals(StatusCode.USER_ALREADY_REGISTERED, statusCode1);
        assertEquals(StatusCode.USER_ALREADY_REGISTERED, statusCode2);

        _client1.closeCommunication(0);
        _client2.closeCommunication(0);
    }

    @Test
    void registerThreeUsers() {
        _client1 = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);
        _client2 = new Client(PUBLICKEY_PATH2, KEYSTORE_PATH2, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);
        _client3 = new Client(PUBLICKEY_PATH3, KEYSTORE_PATH3, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH);

        StatusCode statusCode1 = _client1.register();
        StatusCode statusCode2 = _client2.register();
        StatusCode statusCode3 = _client3.register();

        assertEquals(StatusCode.USER_ALREADY_REGISTERED, statusCode1);
        assertEquals(StatusCode.USER_ALREADY_REGISTERED, statusCode2);
        assertEquals(StatusCode.USER_ALREADY_REGISTERED, statusCode3);

        _client1.closeCommunication(0);
        _client2.closeCommunication(0);
        _client3.closeCommunication(0);
    }

}