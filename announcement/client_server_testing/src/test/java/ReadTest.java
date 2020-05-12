import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.client.ClientTest;
import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableProtocolMessage;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;
import pt.ulisboa.tecnico.sec.server.Server;
import org.junit.jupiter.api.BeforeEach;


import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadTest extends BaseTest {

    static ClientTest _clientTest;

    ReadTest() {
    }

    

    @BeforeEach
    void resetClient() {
    }

}
