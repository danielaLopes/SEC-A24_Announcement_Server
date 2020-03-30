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

public class ReadGeneralTest extends BaseTest {

    private Server _server;
    private Announcement _announcement1;
    private Announcement _announcement2;
    private Announcement _announcement3;

    public ReadGeneralTest() throws Exception {

        _server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        // registering client1
        int opUuidRegister1 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT1_PUBLIC_KEY, opUuidRegister1);
        byte[] bpmRegister1 = ProtocolMessageConverter.objToByteArray(pmRegister1);
        byte[] signedPmRegister1 = SignatureUtil.sign(bpmRegister1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                pmRegister1, signedPmRegister1);

        VerifiableProtocolMessage vpm_responseRegister1 = _server.registerUser(vpmRegister1);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, scRegister1);

        // registering client2
        int opUuidRegister2 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister2 = new ProtocolMessage(
                "REGISTER", CLIENT2_PUBLIC_KEY, opUuidRegister2);
        byte[] bpmRegister2 = ProtocolMessageConverter.objToByteArray(pmRegister2);
        byte[] signedPmRegister2 = SignatureUtil.sign(bpmRegister2, CLIENT2_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister2 = new VerifiableProtocolMessage(
                pmRegister2, signedPmRegister2);

        VerifiableProtocolMessage vpm_responseRegister2 = _server.registerUser(vpmRegister2);
        StatusCode scRegister2 = vpm_responseRegister2.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, scRegister2);

        // posting first announcement
        List<Integer> references1 = new ArrayList<>();
        _announcement1 = new Announcement(MESSAGE1, references1);
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, _announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.postGeneral(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc1);

        // posting second announcement
        int ref1Uuid = vpm_response1.getProtocolMessage().getPostAnnouncement().getAnnouncementID();
        List<Integer> references2 = new ArrayList<>(Arrays.asList(ref1Uuid));
        _announcement2 = new Announcement(MESSAGE2, references2);
        int opUuid2 = UUIDGenerator.generateUUID();
        ProtocolMessage pm2 = new ProtocolMessage(
                "POST", CLIENT2_PUBLIC_KEY, opUuid2, _announcement2);
        byte[] bpm2 = ProtocolMessageConverter.objToByteArray(pm2);
        byte[] signedpm2 = SignatureUtil.sign(bpm2, CLIENT2_PRIVATE_KEY);
        VerifiableProtocolMessage vpm2 = new VerifiableProtocolMessage(pm2, signedpm2);

        VerifiableProtocolMessage vpm_response2 = _server.postGeneral(vpm2);
        StatusCode sc2 = vpm_response2.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc2);

        // posting third announcement
        int ref2Uuid = vpm_response2.getProtocolMessage().getPostAnnouncement().getAnnouncementID();
        List<Integer> references3 = new ArrayList<>(Arrays.asList(ref1Uuid, ref2Uuid));
        _announcement3 = new Announcement(MESSAGE3, references3);
        int opUuid3 = UUIDGenerator.generateUUID();
        ProtocolMessage pm3 = new ProtocolMessage(
                "POST", CLIENT2_PUBLIC_KEY, opUuid3, _announcement3);
        byte[] bpm3 = ProtocolMessageConverter.objToByteArray(pm3);
        byte[] signedpm3 = SignatureUtil.sign(bpm3, CLIENT2_PRIVATE_KEY);
        VerifiableProtocolMessage vpm3 = new VerifiableProtocolMessage(pm3, signedpm3);

        VerifiableProtocolMessage vpm_response3 = _server.postGeneral(vpm3);
        StatusCode sc3 = vpm_response3.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc3);
    }

    @Test
    void success() throws Exception {

        // read all announcements
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "READGENERAL", CLIENT1_PUBLIC_KEY, opUuid1, 0);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.readGeneral(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc1);
        List<Announcement> announcements1 = vpm_response1.getProtocolMessage().getAnnouncements();
        assertEquals(3, announcements1.size());

        assertEquals(_announcement1.getAnnouncement(), announcements1.get(0).getAnnouncement());
        assertEquals(_announcement1.getAnnouncementID(), announcements1.get(0).getAnnouncementID());
        assertEquals(_announcement1.getReferences(), announcements1.get(0).getReferences());
        assertEquals(_announcement1.getClientPublicKey(), announcements1.get(0).getClientPublicKey());
        // TODO: check if signatures are the same

        assertEquals(_announcement2.getAnnouncement(), announcements1.get(1).getAnnouncement());
        assertEquals(_announcement2.getAnnouncementID(), announcements1.get(1).getAnnouncementID());
        assertEquals(_announcement2.getReferences(), announcements1.get(1).getReferences());
        assertEquals(_announcement2.getClientPublicKey(), announcements1.get(1).getClientPublicKey());
        // TODO: check if signatures are the same

        assertEquals(_announcement3.getAnnouncement(), announcements1.get(2).getAnnouncement());
        assertEquals(_announcement3.getAnnouncementID(), announcements1.get(2).getAnnouncementID());
        assertEquals(_announcement3.getReferences(), announcements1.get(2).getReferences());
        assertEquals(_announcement3.getClientPublicKey(), announcements1.get(2).getClientPublicKey());
        // TODO: check if signatures are the same

        // read 1 announcement
        int opUuid2 = UUIDGenerator.generateUUID();
        ProtocolMessage pm2 = new ProtocolMessage(
                "READGENERAL", CLIENT1_PUBLIC_KEY, opUuid2, 1);
        byte[] bpm2 = ProtocolMessageConverter.objToByteArray(pm2);
        byte[] signedpm2 = SignatureUtil.sign(bpm2, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm2 = new VerifiableProtocolMessage(pm2, signedpm2);

        VerifiableProtocolMessage vpm_response2 = _server.readGeneral(vpm2);
        StatusCode sc2 = vpm_response2.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc2);
        List<Announcement> announcements2 = vpm_response2.getProtocolMessage().getAnnouncements();
        assertEquals(1, announcements2.size());

        assertEquals(_announcement3.getAnnouncement(), announcements2.get(0).getAnnouncement());
        assertEquals(_announcement3.getAnnouncementID(), announcements2.get(0).getAnnouncementID());
        assertEquals(_announcement3.getReferences(), announcements2.get(0).getReferences());
        assertEquals(_announcement3.getClientPublicKey(), announcements2.get(0).getClientPublicKey());
        // TODO: check if signatures are the same

        // read 3 announcements
        int opUuid3 = UUIDGenerator.generateUUID();
        ProtocolMessage pm3 = new ProtocolMessage(
                "READGENERAL", CLIENT1_PUBLIC_KEY, opUuid3, 3);
        byte[] bpm3 = ProtocolMessageConverter.objToByteArray(pm3);
        byte[] signedpm3 = SignatureUtil.sign(bpm3, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm3 = new VerifiableProtocolMessage(pm3, signedpm3);

        VerifiableProtocolMessage vpm_response3 = _server.readGeneral(vpm3);
        StatusCode sc3 = vpm_response3.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc3);
        List<Announcement> announcements3 = vpm_response3.getProtocolMessage().getAnnouncements();
        assertEquals(3, announcements3.size());

        assertEquals(_announcement1.getAnnouncement(), announcements3.get(0).getAnnouncement());
        assertEquals(_announcement1.getAnnouncementID(), announcements3.get(0).getAnnouncementID());
        assertEquals(_announcement1.getReferences(), announcements3.get(0).getReferences());
        assertEquals(_announcement1.getClientPublicKey(), announcements3.get(0).getClientPublicKey());
        // TODO: check if signatures are the same

        assertEquals(_announcement2.getAnnouncement(), announcements3.get(1).getAnnouncement());
        assertEquals(_announcement2.getAnnouncementID(), announcements3.get(1).getAnnouncementID());
        assertEquals(_announcement2.getReferences(), announcements3.get(1).getReferences());
        assertEquals(_announcement2.getClientPublicKey(), announcements3.get(1).getClientPublicKey());
        // TODO: check if signatures are the same

        assertEquals(_announcement3.getAnnouncement(), announcements3.get(2).getAnnouncement());
        assertEquals(_announcement3.getAnnouncementID(), announcements3.get(2).getAnnouncementID());
        assertEquals(_announcement3.getReferences(), announcements3.get(2).getReferences());
        assertEquals(_announcement3.getClientPublicKey(), announcements3.get(2).getClientPublicKey());
        // TODO: check if signatures are the same
    }

    // negative numbers, numbers higher than the number of announcements should return all announcements in the board
    @Test
    void successWithInvalidNumberOfAnnouncements() throws Exception {

        // negative number
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "READGENERAL", CLIENT1_PUBLIC_KEY, opUuid1, -1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = _server.readGeneral(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc1);
        List<Announcement> announcements1 = vpm_response1.getProtocolMessage().getAnnouncements();
        assertEquals(3, announcements1.size());

        assertEquals(_announcement1.getAnnouncement(), announcements1.get(0).getAnnouncement());
        assertEquals(_announcement1.getAnnouncementID(), announcements1.get(0).getAnnouncementID());
        assertEquals(_announcement1.getReferences(), announcements1.get(0).getReferences());
        assertEquals(_announcement1.getClientPublicKey(), announcements1.get(0).getClientPublicKey());
        // TODO: check if signatures are the same

        assertEquals(_announcement2.getAnnouncement(), announcements1.get(1).getAnnouncement());
        assertEquals(_announcement2.getAnnouncementID(), announcements1.get(1).getAnnouncementID());
        assertEquals(_announcement2.getReferences(), announcements1.get(1).getReferences());
        assertEquals(_announcement2.getClientPublicKey(), announcements1.get(1).getClientPublicKey());
        // TODO: check if signatures are the same

        assertEquals(_announcement3.getAnnouncement(), announcements1.get(2).getAnnouncement());
        assertEquals(_announcement3.getAnnouncementID(), announcements1.get(2).getAnnouncementID());
        assertEquals(_announcement3.getReferences(), announcements1.get(2).getReferences());
        assertEquals(_announcement3.getClientPublicKey(), announcements1.get(2).getClientPublicKey());
        // TODO: check if signatures are the same

        // number higher than the number of announcements
        int opUuid2 = UUIDGenerator.generateUUID();
        ProtocolMessage pm2 = new ProtocolMessage(
                "READGENERAL", CLIENT1_PUBLIC_KEY, opUuid2, 4);
        byte[] bpm2 = ProtocolMessageConverter.objToByteArray(pm2);
        byte[] signedpm2 = SignatureUtil.sign(bpm2, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm2 = new VerifiableProtocolMessage(pm2, signedpm2);

        VerifiableProtocolMessage vpm_response2 = _server.readGeneral(vpm2);
        StatusCode sc2 = vpm_response2.getProtocolMessage().getStatusCode();
        assertEquals(StatusCode.OK, sc2);
        List<Announcement> announcements2 = vpm_response2.getProtocolMessage().getAnnouncements();
        assertEquals(3, announcements2.size());

        assertEquals(_announcement1.getAnnouncement(), announcements2.get(0).getAnnouncement());
        assertEquals(_announcement1.getAnnouncementID(), announcements2.get(0).getAnnouncementID());
        assertEquals(_announcement1.getReferences(), announcements2.get(0).getReferences());
        assertEquals(_announcement1.getClientPublicKey(), announcements2.get(0).getClientPublicKey());
        // TODO: check if signatures are the same

        assertEquals(_announcement2.getAnnouncement(), announcements2.get(1).getAnnouncement());
        assertEquals(_announcement2.getAnnouncementID(), announcements2.get(1).getAnnouncementID());
        assertEquals(_announcement2.getReferences(), announcements2.get(1).getReferences());
        assertEquals(_announcement2.getClientPublicKey(), announcements2.get(1).getClientPublicKey());
        // TODO: check if signatures are the same

        assertEquals(_announcement3.getAnnouncement(), announcements2.get(2).getAnnouncement());
        assertEquals(_announcement3.getAnnouncementID(), announcements2.get(2).getAnnouncementID());
        assertEquals(_announcement3.getReferences(), announcements2.get(2).getReferences());
        assertEquals(_announcement3.getClientPublicKey(), announcements2.get(2).getClientPublicKey());
        // TODO: check if signatures are the same
    }

    // Unregistered User
    @Test
    void userNotRegistered() {

    }

    // Number of announcements to retrieve related tests
    @Test
    void negativeNumberOfAnnouncements() {
        /*boolean success = _client.post(MESSAGE, REFERENCES);

        assertEquals(success, true);*/
    }

    @Test
    void zeroNumberOfAnnouncements() {
        /*boolean success = _client.post(MESSAGE, REFERENCES);

        assertEquals(success, true);*/
    }

    @Test
    void tooManyAnnouncements() {
        /*boolean success = _client.post(MESSAGE, REFERENCES);

        assertEquals(success, true);*/
    }

    // null Parameters
    @Test
    void publicKeyIsNull() {
        /*int statusCode = _client.post(null, REFERENCES);

        assertEquals(statusCode, -1);*/
    }

    @Test
    void numberIsNull() {
        /*int statusCode = _client.post(null, REFERENCES);

        assertEquals(statusCode, -1);*/
    }

    @Test
    void opUuidIsNull() {
        /*int statusCode = _client.post(null, REFERENCES);

        assertEquals(statusCode, -1);*/
    }

    @Test
    void signatureIsNull() {
        /*int statusCode = _client.post(null, REFERENCES);

        assertEquals(statusCode, -1);*/
    }

    // Replay attacks
    @Test
    void duplicatedOperation() {
        /*String invalidMessage = "";
        for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
            invalidMessage += "A";
        }

        boolean success = _client.post(invalidMessage, REFERENCES);

        assertEquals(success, false);*/
    }

    // Message Integrity attacks
    @Test
    void tamperedMessage() {
        /*String invalidMessage = "";
        for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
            invalidMessage += "A";
        }

        boolean success = _client.post(invalidMessage, REFERENCES);

        assertEquals(success, false);*/
    }

    @Test
    void invalidSignature() {
        /*String invalidMessage = "";
        for (int i = 0; i < MAX_MESSAGE_LENGTH; i++) {
            invalidMessage += "A";
        }

        boolean success = _client.post(invalidMessage, REFERENCES);

        assertEquals(success, false);*/
    }
}
