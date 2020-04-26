import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.ProtocolMessage;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableProtocolMessage;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;
import pt.ulisboa.tecnico.sec.crypto_lib.SignatureUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.UUIDGenerator;
import pt.ulisboa.tecnico.sec.server.Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostTest extends BaseTest {

    private static Server _server;

    public PostTest() {

        _server = new Server(false, 8000, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);
    }

    @BeforeEach
    void testSetup() throws Exception {

        _server.resetDatabase();

        _server = new Server(false, 8000, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        // registering client1
        VerifiableProtocolMessage vpm_responseRegister1 = forgeRegisterRequest(
                _server, UUIDGenerator.generateUUID(), CLIENT1_PUBLIC_KEY, CLIENT1_PRIVATE_KEY);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, scRegister1);

        // registering client2
        VerifiableProtocolMessage vpm_responseRegister2 = forgeRegisterRequest(
                _server, UUIDGenerator.generateUUID(), CLIENT2_PUBLIC_KEY, CLIENT2_PRIVATE_KEY);
        StatusCode scRegister2 = vpm_responseRegister2.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, scRegister2);
    }


    @AfterAll
    public static void cleanDatabase() {

        _server.resetDatabase();
    }

    @Test
    void success() throws Exception {

        // posting first announcement
        List<String> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        String opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc1);

        // posting second announcement
        String ref1Uuid = vpm_response1.getProtocolMessage().getPostAnnouncement().getAnnouncementID();
        List<String> references2 = new ArrayList<>(Arrays.asList(ref1Uuid));
        Announcement announcement2 = new Announcement(MESSAGE2, references2);
        String opUuid2 = UUIDGenerator.generateUUID();
        ProtocolMessage pm2 = new ProtocolMessage(
                "POST", CLIENT2_PUBLIC_KEY, opUuid2, announcement2);
        byte[] bpm2 = ProtocolMessageConverter.objToByteArray(pm2);
        byte[] signedpm2 = SignatureUtil.sign(bpm2, CLIENT2_PRIVATE_KEY);
        VerifiableProtocolMessage vpm2 = new VerifiableProtocolMessage(pm2, signedpm2);

        VerifiableProtocolMessage vpm_response2 = _server.post(vpm2);
        StatusCode sc2 = vpm_response2.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc2);

        // posting third announcement
        String ref2Uuid = vpm_response2.getProtocolMessage().getPostAnnouncement().getAnnouncementID();
        List<String> references3 = new ArrayList<>(Arrays.asList(ref1Uuid, ref2Uuid));
        Announcement announcement3 = new Announcement(MESSAGE3, references3);
        String opUuid3 = UUIDGenerator.generateUUID();
        ProtocolMessage pm3 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid3, announcement3);
        byte[] bpm3 = ProtocolMessageConverter.objToByteArray(pm3);
        byte[] signedpm3 = SignatureUtil.sign(bpm3, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm3 = new VerifiableProtocolMessage(pm3, signedpm3);

        VerifiableProtocolMessage vpm_response3 = _server.post(vpm3);
        StatusCode sc3 = vpm_response3.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc3);
    }

    // Unregistered User
    @Test
    void userNotRegistered() throws Exception {

        List<String> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        String opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT3_PUBLIC_KEY, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT3_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.USER_NOT_REGISTERED, sc1);
    }

    // Invalid Announcement fields tests
    @Test
    void messageLengthIsInvalid() throws Exception {

        // posting announcement
        List<String> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(INVALID_LENGTH_MESSAGE, references1);
        String opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.INVALID_MESSAGE_LENGTH, sc1);
    }

    @Test
    void InvalidReferences() throws Exception {

        // posting announcement
        String invalid_opUuid = UUIDGenerator.generateUUID();
        List<String> references1 = new ArrayList<>(Arrays.asList(invalid_opUuid));
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        String opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.INVALID_REFERENCE, sc1);
    }

    // null Parameters
    @Test
    void publicKeyIsNull() throws Exception {

        // posting announcement
        List<String> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        String opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", null, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.NULL_FIELD, sc1);
    }

    @Test
    void messageIsNull() throws Exception {

        // posting announcement
        List<String> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(null, references1);
        String opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.NULL_FIELD, sc1);
    }

    @Test
    void referencesIsNull() throws Exception {

        // posting announcement
        Announcement announcement1 = new Announcement(MESSAGE1, null);
        String opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.NULL_FIELD, sc1);
    }

    @Test
    void opUuidIsNull() throws Exception {

        // posting announcement
        List<String> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        //String opUuid1 = null;
        String opUuid1 = null;
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.NULL_FIELD, sc1);
    }

    @Test
    void signatureIsNull() throws Exception {

        // posting announcement
        List<String> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        String opUuid = UUIDGenerator.generateUUID();
        ProtocolMessage pm = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid, announcement1);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm, null);

        VerifiableProtocolMessage vpm_response1 = _server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.NULL_FIELD, sc1);
    }

    // Replay attacks
    @Test
    void duplicatedOperation() throws Exception {

        // posting announcement 1
        String opUuidRepeated = UUIDGenerator.generateUUID();
        List<String> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuidRepeated, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.postGeneral(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();

        // posting announcement with repeated opUuid
        List<String> references2 = new ArrayList<>();
        Announcement announcement2 = new Announcement(MESSAGE2, references2);
        ProtocolMessage pm2 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuidRepeated, announcement2);
        byte[] bpm2 = ProtocolMessageConverter.objToByteArray(pm2);
        byte[] signedpm2 = SignatureUtil.sign(bpm2, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm2 = new VerifiableProtocolMessage(pm2, signedpm2);

        VerifiableProtocolMessage vpm_response2 = _server.post(vpm2);
        StatusCode sc2 = vpm_response2.getProtocolMessage().getStatusCode();
        assertEquals(sc1, sc2);
    }

    // Message Integrity attacks
    @Test
    void tamperedMessage() throws Exception {

        // posting announcement
        List<String> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        String opUuid = UUIDGenerator.generateUUID();
        ProtocolMessage pm = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid, announcement1);
        Announcement tampAnnouncement = new Announcement(MESSAGE2, references1);
        ProtocolMessage tampPm = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid, tampAnnouncement);
        byte[] bpm = ProtocolMessageConverter.objToByteArray(pm);
        byte[] signedpm = SignatureUtil.sign(bpm, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(tampPm, signedpm);

        VerifiableProtocolMessage vpm_response1 = _server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.INVALID_SIGNATURE, sc1);
    }

    @Test
    void invalidSignature() throws Exception {

        // posting announcement
        List<String> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        String opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT2_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.INVALID_SIGNATURE, sc1);
    }
}
