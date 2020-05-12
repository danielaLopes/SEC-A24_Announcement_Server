import org.junit.jupiter.api.Test;
import pt.ulisboa.tecnico.sec.client.ByzantineClient;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByzantineClientTest extends BaseTest {

    ByzantineClient _byzantineClient = new ByzantineClient(PUBLICKEY_PATH2, KEYSTORE_PATH2,
            CLIENT_KEYSTORE_PASSWD, CLIENT_ENTRY_PASSWD, ALIAS, 3, 0);

    @Test
    void sendDifferentMessages() {
        StatusCode sc = _byzantineClient.sendDifferentMessages();
        System.out.println("Quorum status code: " + sc);
        assertEquals(StatusCode.NO_CONSENSUS, sc);
    }

    @Test
    void colludeWithServer() {
        StatusCode sc = _byzantineClient.colludeWithServer(8003).getProtocolMessage().getStatusCode();
        System.out.println("Colluding server status code: " + sc);
        assertEquals(StatusCode.OK, sc);
    }
}
