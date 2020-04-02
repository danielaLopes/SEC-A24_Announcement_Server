import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import static org.junit.jupiter.api.Assertions.assertNull;

public class ReadTest extends BaseTest {

    private static Server _server;
    private static Announcement _ann1Client1;
    private static Announcement _ann2Client2;
    private static Announcement _ann3Client2;

    public ReadTest() {}

    @BeforeAll
    static void setup() throws Exception {

        _server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
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

        // posting first announcement
        List<Integer> references1 = new ArrayList<>();
        _ann1Client1 = new Announcement(MESSAGE1, references1);
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, _ann1Client1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc1);

        // posting second announcement
        int ref1Uuid = vpm_response1.getProtocolMessage().getPostAnnouncement().getAnnouncementID();
        List<Integer> references2 = new ArrayList<>(Arrays.asList(ref1Uuid));
        _ann2Client2 = new Announcement(MESSAGE2, references2);
        int opUuid2 = UUIDGenerator.generateUUID();
        ProtocolMessage pm2 = new ProtocolMessage(
                "POST", CLIENT2_PUBLIC_KEY, opUuid2, _ann2Client2);
        byte[] bpm2 = ProtocolMessageConverter.objToByteArray(pm2);
        byte[] signedpm2 = SignatureUtil.sign(bpm2, CLIENT2_PRIVATE_KEY);
        VerifiableProtocolMessage vpm2 = new VerifiableProtocolMessage(pm2, signedpm2);

        VerifiableProtocolMessage vpm_response2 = _server.post(vpm2);
        StatusCode sc2 = vpm_response2.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc2);

        // posting third announcement
        int ref2Uuid = vpm_response2.getProtocolMessage().getPostAnnouncement().getAnnouncementID();
        List<Integer> references3 = new ArrayList<>(Arrays.asList(ref1Uuid, ref2Uuid));
        _ann3Client2 = new Announcement(MESSAGE3, references3);
        int opUuid3 = UUIDGenerator.generateUUID();
        ProtocolMessage pm3 = new ProtocolMessage(
                "POST", CLIENT2_PUBLIC_KEY, opUuid3, _ann3Client2);
        byte[] bpm3 = ProtocolMessageConverter.objToByteArray(pm3);
        byte[] signedpm3 = SignatureUtil.sign(bpm3, CLIENT2_PRIVATE_KEY);
        VerifiableProtocolMessage vpm3 = new VerifiableProtocolMessage(pm3, signedpm3);

        VerifiableProtocolMessage vpm_response3 = _server.post(vpm3);
        StatusCode sc3 = vpm_response3.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc3);
    }


    @AfterAll
    public static void cleanDatabase() {

        _server.resetDatabase();
    }

    @Test
    void successReadZero() throws Exception {

        // read all announcements from user1 board
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "READ", CLIENT1_PUBLIC_KEY, opUuid1, 0, CLIENT1_PUBLIC_KEY);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.read(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc1);
        List<Announcement> annsClient1 = vpm_response1.getProtocolMessage().getAnnouncements();
        assertEquals(1, annsClient1.size());

        assertEqualAnnouncement(_ann1Client1, annsClient1.get(0));

        // read all announcements from user2 board
        int opUuid2 = UUIDGenerator.generateUUID();
        ProtocolMessage pm2 = new ProtocolMessage(
                "READ", CLIENT1_PUBLIC_KEY, opUuid2, 0, CLIENT2_PUBLIC_KEY);
        byte[] bpm2 = ProtocolMessageConverter.objToByteArray(pm2);
        byte[] signedpm2 = SignatureUtil.sign(bpm2, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm2 = new VerifiableProtocolMessage(pm2, signedpm2);

        VerifiableProtocolMessage vpm_response2 = _server.read(vpm2);
        StatusCode sc2 = vpm_response2.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc2);
        List<Announcement> annsClient2 = vpm_response2.getProtocolMessage().getAnnouncements();
        assertEquals(2, annsClient2.size());

        assertEqualAnnouncement(_ann2Client2, annsClient2.get(0));
        assertEqualAnnouncement(_ann3Client2, annsClient2.get(1));
    }

    @Test
    void successReadOne() throws Exception {

        // read one announcement from user1 board
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "READ", CLIENT1_PUBLIC_KEY, opUuid1, 1, CLIENT1_PUBLIC_KEY);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.read(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc1);
        List<Announcement> annsClient1 = vpm_response1.getProtocolMessage().getAnnouncements();
        assertEquals(1, annsClient1.size());

        assertEqualAnnouncement(_ann1Client1, annsClient1.get(0));

        // read one announcement from user2 board
        int opUuid2 = UUIDGenerator.generateUUID();
        ProtocolMessage pm2 = new ProtocolMessage(
                "READ", CLIENT1_PUBLIC_KEY, opUuid2, 1, CLIENT2_PUBLIC_KEY);
        byte[] bpm2 = ProtocolMessageConverter.objToByteArray(pm2);
        byte[] signedpm2 = SignatureUtil.sign(bpm2, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm2 = new VerifiableProtocolMessage(pm2, signedpm2);

        VerifiableProtocolMessage vpm_response2 = _server.read(vpm2);
        StatusCode sc2 = vpm_response2.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc2);
        List<Announcement> annsClient2 = vpm_response2.getProtocolMessage().getAnnouncements();
        assertEquals(1, annsClient2.size());

        assertEqualAnnouncement(_ann3Client2, annsClient2.get(0));
    }

    @Test
    void successReadTwo() throws Exception {

        // read one announcement from user2 board
        int opUuid2 = UUIDGenerator.generateUUID();
        ProtocolMessage pm2 = new ProtocolMessage(
                "READ", CLIENT1_PUBLIC_KEY, opUuid2, 2, CLIENT2_PUBLIC_KEY);
        byte[] bpm2 = ProtocolMessageConverter.objToByteArray(pm2);
        byte[] signedpm2 = SignatureUtil.sign(bpm2, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm2 = new VerifiableProtocolMessage(pm2, signedpm2);

        VerifiableProtocolMessage vpm_response2 = _server.read(vpm2);
        StatusCode sc2 = vpm_response2.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc2);
        List<Announcement> annsClient2 = vpm_response2.getProtocolMessage().getAnnouncements();
        assertEquals(2, annsClient2.size());

        assertEqualAnnouncement(_ann2Client2, annsClient2.get(0));
        assertEqualAnnouncement(_ann3Client2, annsClient2.get(1));
    }

    // negative numbers, numbers higher than the number of announcements should return all announcements in the board
    @Test
    void successReadNegative() throws Exception {

        // read one announcement from user2 board
        int opUuid2 = UUIDGenerator.generateUUID();
        ProtocolMessage pm2 = new ProtocolMessage(
                "READ", CLIENT1_PUBLIC_KEY, opUuid2, -999999999 - 999999999, CLIENT2_PUBLIC_KEY);
        byte[] bpm2 = ProtocolMessageConverter.objToByteArray(pm2);
        byte[] signedpm2 = SignatureUtil.sign(bpm2, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm2 = new VerifiableProtocolMessage(pm2, signedpm2);

        VerifiableProtocolMessage vpm_response2 = _server.read(vpm2);
        StatusCode sc2 = vpm_response2.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc2);
        List<Announcement> annsClient2 = vpm_response2.getProtocolMessage().getAnnouncements();
        assertEquals(2, annsClient2.size());

        assertEqualAnnouncement(_ann2Client2, annsClient2.get(0));
        assertEqualAnnouncement(_ann3Client2, annsClient2.get(1));
    }

    @Test
    void successReadTooMany() throws Exception {

        // read one announcement from user2 board
        int opUuid2 = UUIDGenerator.generateUUID();
        ProtocolMessage pm2 = new ProtocolMessage(
                "READ", CLIENT1_PUBLIC_KEY, opUuid2, +999999999 + 999999999, CLIENT2_PUBLIC_KEY);
        byte[] bpm2 = ProtocolMessageConverter.objToByteArray(pm2);
        byte[] signedpm2 = SignatureUtil.sign(bpm2, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm2 = new VerifiableProtocolMessage(pm2, signedpm2);

        VerifiableProtocolMessage vpm_response2 = _server.read(vpm2);
        StatusCode sc2 = vpm_response2.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc2);
        List<Announcement> annsClient2 = vpm_response2.getProtocolMessage().getAnnouncements();
        assertEquals(2, annsClient2.size());

        assertEqualAnnouncement(_ann2Client2, annsClient2.get(0));
        assertEqualAnnouncement(_ann3Client2, annsClient2.get(1));
    }

    // Unregistered User
    @Test
    void userNotRegistered() throws Exception {

        // read one announcement from user3 board
        int opUuid2 = UUIDGenerator.generateUUID();
        ProtocolMessage pm2 = new ProtocolMessage(
                "READ", CLIENT3_PUBLIC_KEY, opUuid2, 0, CLIENT2_PUBLIC_KEY);
        byte[] bpm2 = ProtocolMessageConverter.objToByteArray(pm2);
        byte[] signedpm2 = SignatureUtil.sign(bpm2, CLIENT3_PRIVATE_KEY);
        VerifiableProtocolMessage vpm2 = new VerifiableProtocolMessage(pm2, signedpm2);

        VerifiableProtocolMessage vpm_response2 = _server.read(vpm2);
        StatusCode sc2 = vpm_response2.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.USER_NOT_REGISTERED, sc2);
        List<Announcement> annsClient2 = vpm_response2.getProtocolMessage().getAnnouncements();
        assertNull(annsClient2);
    }

    // null Parameters
    @Test
    void toReadPublicKeyIsNull() throws Exception {

        // read one announcement from user1 board
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "READ", CLIENT1_PUBLIC_KEY, opUuid1, 1, null);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.read(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.NULL_FIELD, sc1);
        List<Announcement> annsClient1 = vpm_response1.getProtocolMessage().getAnnouncements();
        assertNull(annsClient1);
    }

    @Test
    void publicKeyIsNull() throws Exception {

        // read one announcement from user1 board
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "READ", null, opUuid1, 1, CLIENT1_PUBLIC_KEY);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.read(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.NULL_FIELD, sc1);
        List<Announcement> annsClient1 = vpm_response1.getProtocolMessage().getAnnouncements();
        assertNull(annsClient1);
    }

    @Test
    void opUuidIsNull() throws Exception {

        // read 1 announcement
        ProtocolMessage pm1 = new ProtocolMessage(
                "READ", CLIENT1_PUBLIC_KEY, 0, 1, CLIENT1_PUBLIC_KEY);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.read(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.NULL_FIELD, sc1);
        List<Announcement> announcements1 = vpm_response1.getProtocolMessage().getAnnouncements();
        assertNull(announcements1);
    }

    @Test
    void signatureIsNull() {

        // read 1 announcement
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "READ", CLIENT1_PUBLIC_KEY, opUuid1, 1, CLIENT1_PUBLIC_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, null);

        VerifiableProtocolMessage vpm_response1 = _server.read(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.NULL_FIELD, sc1);
        List<Announcement> announcements1 = vpm_response1.getProtocolMessage().getAnnouncements();
        assertNull(announcements1);
    }

    // Replay attacks
    @Test
    void duplicatedOperation() throws Exception {

        // read 1 announcement
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "READ", CLIENT1_PUBLIC_KEY, opUuid1, 1, CLIENT1_PUBLIC_KEY);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.read(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc1);
        List<Announcement> announcements1 = vpm_response1.getProtocolMessage().getAnnouncements();
        assertEquals(1, announcements1.size());

        assertEqualAnnouncement(_ann1Client1, announcements1.get(0));

        VerifiableProtocolMessage vpm_responseDup = _server.read(vpm1);
        StatusCode sc2 = vpm_responseDup.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc2);
        assertEquals(sc1, sc2);
        List<Announcement> announcements2 = vpm_responseDup.getProtocolMessage().getAnnouncements();
        assertEquals(1, announcements2.size());

        assertEqualAnnouncement(_ann1Client1, announcements2.get(0));
    }

    // Message Integrity attacks
    @Test
    void tamperedMessage() throws Exception {

        // read 1 announcement
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "READ", CLIENT1_PUBLIC_KEY, opUuid1, 1, CLIENT1_PUBLIC_KEY);
        ProtocolMessage tampPm1 = new ProtocolMessage(
                "READ", CLIENT2_PUBLIC_KEY, opUuid1, 1, CLIENT1_PUBLIC_KEY);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(tampPm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.read(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.INVALID_SIGNATURE, sc1);
        List<Announcement> announcements1 = vpm_response1.getProtocolMessage().getAnnouncements();
        assertNull(announcements1);
    }

    @Test
    void tamperedCommand() throws Exception {

        // read 1 announcement
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "READ", CLIENT1_PUBLIC_KEY, opUuid1, 1, CLIENT1_PUBLIC_KEY);
        ProtocolMessage tampPm1 = new ProtocolMessage(
                "READGENER", CLIENT2_PUBLIC_KEY, opUuid1, 1, CLIENT1_PUBLIC_KEY);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(tampPm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.read(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.INVALID_SIGNATURE, sc1);
        List<Announcement> announcements1 = vpm_response1.getProtocolMessage().getAnnouncements();
        assertNull(announcements1);
    }

    @Test
    void invalidSignature() throws Exception {

        // read 1 announcement
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "READ", CLIENT1_PUBLIC_KEY, opUuid1, 1, CLIENT1_PUBLIC_KEY);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT3_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.read(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.INVALID_SIGNATURE, sc1);
        List<Announcement> announcements1 = vpm_response1.getProtocolMessage().getAnnouncements();
        assertNull(announcements1);
    }

    @Test
    void invalidToReadKey() throws Exception {

        // read 1 announcement
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "READ", CLIENT1_PUBLIC_KEY, opUuid1, 1, CLIENT3_PUBLIC_KEY);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.read(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.USER_NOT_REGISTERED, sc1);
        List<Announcement> announcements1 = vpm_response1.getProtocolMessage().getAnnouncements();
        assertNull(announcements1);

        // registering client 3
        VerifiableProtocolMessage vpm_responseRegister3 = forgeRegisterRequest(
                _server, UUIDGenerator.generateUUID(), CLIENT3_PUBLIC_KEY, CLIENT3_PRIVATE_KEY);
        StatusCode scRegister3 = vpm_responseRegister3.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, scRegister3);

        // repeat after client 3 being registered
        ProtocolMessage pm2 = new ProtocolMessage(
                "READ", CLIENT1_PUBLIC_KEY, opUuid1, 1, CLIENT3_PUBLIC_KEY);
        byte[] bpm2 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm2 = SignatureUtil.sign(bpm2, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm2 = new VerifiableProtocolMessage(pm2, signedpm2);

        VerifiableProtocolMessage vpm_response2 = _server.read(vpm2);
        StatusCode sc2 = vpm_response2.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc2);
        List<Announcement> announcements2 = vpm_response2.getProtocolMessage().getAnnouncements();
        assertEquals(0, announcements2.size());

        // delete client3 from server and database
        _server.resetDatabase();

        setup();
    }
}
