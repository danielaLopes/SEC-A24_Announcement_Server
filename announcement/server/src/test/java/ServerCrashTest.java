import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

public class ServerCrashTest extends BaseTest {

    private static Server _server;

    private static Announcement _userAnn1;
    private static Announcement _userAnn2;
    private static Announcement _userAnn3;
    private static Announcement _generalAnn1;
    private static Announcement _generalAnn2;

    public ServerCrashTest() {}

    @BeforeAll
    static void testSetup() throws Exception {

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

        // posting first announcement Client1
        List<Integer> references1 = new ArrayList<>();
        _userAnn1 = new Announcement(MESSAGE1, references1);
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, _userAnn1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc1);

        // posting second announcement Client1
        int ref1Uuid = vpm_response1.getProtocolMessage().getPostAnnouncement().getAnnouncementID();
        List<Integer> references2 = new ArrayList<>(Arrays.asList(ref1Uuid));
        _userAnn2 = new Announcement(MESSAGE2, references2);
        int opUuid2 = UUIDGenerator.generateUUID();
        ProtocolMessage pm2 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid2, _userAnn2);
        byte[] bpm2 = ProtocolMessageConverter.objToByteArray(pm2);
        byte[] signedpm2 = SignatureUtil.sign(bpm2, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm2 = new VerifiableProtocolMessage(pm2, signedpm2);

        VerifiableProtocolMessage vpm_response2 = _server.post(vpm2);
        StatusCode sc2 = vpm_response2.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc2);

        // posting firs announcement Client2
        int ref2Uuid = vpm_response2.getProtocolMessage().getPostAnnouncement().getAnnouncementID();
        List<Integer> references3 = new ArrayList<>(Arrays.asList(ref1Uuid, ref2Uuid));
        _userAnn3 = new Announcement(MESSAGE2, references2);
        int opUuid3 = UUIDGenerator.generateUUID();
        ProtocolMessage pm3 = new ProtocolMessage(
                "POST", CLIENT2_PUBLIC_KEY, opUuid3, _userAnn3);
        byte[] bpm3 = ProtocolMessageConverter.objToByteArray(pm3);
        byte[] signedpm3 = SignatureUtil.sign(bpm3, CLIENT2_PRIVATE_KEY);
        VerifiableProtocolMessage vpm3 = new VerifiableProtocolMessage(pm3, signedpm3);

        VerifiableProtocolMessage vpm_response3 = _server.post(vpm3);
        StatusCode sc3 = vpm_response3.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc3);

        // posting first announcement GeneralBoard
        int ref3Uuid = vpm_response3.getProtocolMessage().getPostAnnouncement().getAnnouncementID();
        List<Integer> references4 = new ArrayList<>(Arrays.asList(ref1Uuid, ref2Uuid, ref3Uuid));
        _generalAnn1 = new Announcement(MESSAGE3, references4);
        int opUuid4 = UUIDGenerator.generateUUID();
        ProtocolMessage pm4 = new ProtocolMessage(
                "POSTGENERAL", CLIENT1_PUBLIC_KEY, opUuid4, _generalAnn1);
        byte[] bpm4 = ProtocolMessageConverter.objToByteArray(pm4);
        byte[] signedpm4 = SignatureUtil.sign(bpm4, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm4 = new VerifiableProtocolMessage(pm4, signedpm4);

        VerifiableProtocolMessage vpm_response4 = _server.postGeneral(vpm4);
        StatusCode sc4 = vpm_response4.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc4);

        // posting second announcement GeneralBoard
        int ref4Uuid = vpm_response4.getProtocolMessage().getPostAnnouncement().getAnnouncementID();
        List<Integer> references5 = new ArrayList<>(Arrays.asList(ref1Uuid, ref2Uuid, ref3Uuid, ref4Uuid));
        _generalAnn2 = new Announcement(MESSAGE3, references5);
        int opUuid5 = UUIDGenerator.generateUUID();
        ProtocolMessage pm5 = new ProtocolMessage(
                "POSTGENERAL", CLIENT1_PUBLIC_KEY, opUuid5, _generalAnn2);
        byte[] bpm5 = ProtocolMessageConverter.objToByteArray(pm5);
        byte[] signedpm5 = SignatureUtil.sign(bpm5, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm5 = new VerifiableProtocolMessage(pm5, signedpm5);

        VerifiableProtocolMessage vpm_response5 = _server.postGeneral(vpm5);
        StatusCode sc5 = vpm_response5.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc5);
    }

    @Test
    void success() throws Exception {

        // read announcements before server crash
        readAllAnnouncements();

        // server crashed and recovered
        _server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        // read announcements after server crash
        readAllAnnouncements();
    }

    public void readAllAnnouncements() throws Exception {
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
        assertEquals(2, annsClient1.size());

        assertEqualAnnouncement(_userAnn1, annsClient1.get(0));
        assertEqualAnnouncement(_userAnn2, annsClient1.get(1));

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
        assertEquals(1, annsClient2.size());

        assertEqualAnnouncement(_userAnn3, annsClient2.get(0));

        // read all announcements from GeneralBoard
        int opUuid3 = UUIDGenerator.generateUUID();
        ProtocolMessage pm3 = new ProtocolMessage(
                "READGENERAL", CLIENT1_PUBLIC_KEY, opUuid3, 0);
        byte[] bpm3 = ProtocolMessageConverter.objToByteArray(pm3);
        byte[] signedpm3 = SignatureUtil.sign(bpm3, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm3 = new VerifiableProtocolMessage(pm3, signedpm3);

        VerifiableProtocolMessage vpm_response3 = _server.readGeneral(vpm3);
        StatusCode sc3 = vpm_response3.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc3);
        List<Announcement> annsGeneral = vpm_response3.getProtocolMessage().getAnnouncements();
        assertEquals(2, annsGeneral.size());

        assertEqualAnnouncement(_generalAnn1, annsGeneral.get(0));
        assertEqualAnnouncement(_generalAnn2, annsGeneral.get(1));
    }

    @AfterAll
    public static void cleanDatabase() {

        _server.resetDatabase();
    }
}
