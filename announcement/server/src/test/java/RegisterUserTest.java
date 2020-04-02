import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.ulisboa.tecnico.sec.communication_lib.ProtocolMessage;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableProtocolMessage;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;
import pt.ulisboa.tecnico.sec.crypto_lib.SignatureUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.UUIDGenerator;
import pt.ulisboa.tecnico.sec.server.Server;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegisterUserTest extends BaseTest {

    private static Server _server;

    public RegisterUserTest() {

        _server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);
    }

    @BeforeEach
    void testSetup() {

        _server.resetDatabase();

        _server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);
    }

        @Test
    void success() throws Exception {

        // registering client1
        VerifiableProtocolMessage vpm_responseRegister1 = forgeRegisterRequest(
                _server, UUIDGenerator.generateUUID(), CLIENT1_PUBLIC_KEY, CLIENT1_PRIVATE_KEY);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, scRegister1);
    }

    @AfterAll
    public static void cleanDatabase() {

        _server.resetDatabase();
    }

    // null Parameters
    @Test
    void publicKeyIsNull() throws Exception {

        // registering client1
        VerifiableProtocolMessage vpm_responseRegister1 = forgeRegisterRequest(
                _server, UUIDGenerator.generateUUID(), null, CLIENT1_PRIVATE_KEY);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.NULL_FIELD, scRegister1);
    }

    @Test
    void opUuidIsNull() throws Exception {

        // registering client1
        VerifiableProtocolMessage vpm_responseRegister1 = forgeRegisterRequest(
                _server, 0, CLIENT1_PUBLIC_KEY, CLIENT1_PRIVATE_KEY);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.NULL_FIELD, scRegister1);
    }

    @Test
    void signatureIsNull() {

        // registering client1
        int opUuidRegister1 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT1_PUBLIC_KEY, opUuidRegister1);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                pmRegister1, null);

        VerifiableProtocolMessage vpm_responseRegister1 = _server.registerUser(vpmRegister1);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.NULL_FIELD, scRegister1);
    }

    // Replay attacks
    @Test
    void duplicatedOperation() throws Exception {

        // registering client1
        int opUuidRepeated = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT1_PUBLIC_KEY, opUuidRepeated);
        byte[] bpmRegister1 = ProtocolMessageConverter.objToByteArray(pmRegister1);
        byte[] signedPmRegister1 = SignatureUtil.sign(bpmRegister1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                pmRegister1, signedPmRegister1);

        VerifiableProtocolMessage vpm_responseRegister1 = _server.registerUser(vpmRegister1);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, scRegister1);

        // repeated operation
        VerifiableProtocolMessage vpm_responseRegister2 = _server.registerUser(vpmRegister1);
        StatusCode scRegister2 = vpm_responseRegister2.getProtocolMessage().getStatusCode();
        assertEquals(scRegister1, scRegister2);
    }

    // Message Integrity attacks
    @Test
    void tamperedMessage() throws Exception {

        // registering client1
        int opUuidRegister1 = UUIDGenerator.generateUUID();
        int tampOpUuidRegister1 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT1_PUBLIC_KEY, opUuidRegister1);
        ProtocolMessage tampPmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT2_PUBLIC_KEY, tampOpUuidRegister1);
        byte[] bpmRegister1 = ProtocolMessageConverter.objToByteArray(pmRegister1);
        byte[] signedPmRegister1 = SignatureUtil.sign(bpmRegister1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                tampPmRegister1, signedPmRegister1);

        VerifiableProtocolMessage vpm_responseRegister1 = _server.registerUser(vpmRegister1);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.INVALID_SIGNATURE, scRegister1);
    }

    @Test
    void invalidSignature() throws Exception {

        // registering client1
        int opUuidRegister1 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT1_PUBLIC_KEY, opUuidRegister1);
        byte[] bpmRegister1 = ProtocolMessageConverter.objToByteArray(pmRegister1);
        byte[] signedPmRegister1 = SignatureUtil.sign(bpmRegister1, CLIENT2_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                pmRegister1, signedPmRegister1);

        VerifiableProtocolMessage vpm_responseRegister1 = _server.registerUser(vpmRegister1);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.INVALID_SIGNATURE, scRegister1);
    }

    @Test
    void userAlreadyRegistered() throws Exception {

        // registering client1
        VerifiableProtocolMessage vpm_responseRegister1 = forgeRegisterRequest(
                _server, UUIDGenerator.generateUUID(), CLIENT1_PUBLIC_KEY, CLIENT1_PRIVATE_KEY);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, scRegister1);

        // registering client1 twice
        VerifiableProtocolMessage vpm_responseRegister2 = forgeRegisterRequest(
                _server, UUIDGenerator.generateUUID(), CLIENT1_PUBLIC_KEY, CLIENT1_PRIVATE_KEY);
        StatusCode scRegister2 = vpm_responseRegister2.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.USER_ALREADY_REGISTERED, scRegister2);
    }
}
