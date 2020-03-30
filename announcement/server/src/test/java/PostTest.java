import org.bouncycastle.jcajce.provider.symmetric.ARC4;
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

    public PostTest() {}

    @Test
    void success() throws Exception {

        Server server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        // registering client1
        int opUuidRegister1 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT1_PUBLIC_KEY, opUuidRegister1);
        byte[] bpmRegister1 = ProtocolMessageConverter.objToByteArray(pmRegister1);
        byte[] signedPmRegister1 = SignatureUtil.sign(bpmRegister1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                pmRegister1, signedPmRegister1);

        VerifiableProtocolMessage vpm_responseRegister1 = server.registerUser(vpmRegister1);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(scRegister1, StatusCode.OK);

        // registering client2
        int opUuidRegister2 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister2 = new ProtocolMessage(
                "REGISTER", CLIENT2_PUBLIC_KEY, opUuidRegister2);
        byte[] bpmRegister2 = ProtocolMessageConverter.objToByteArray(pmRegister2);
        byte[] signedPmRegister2 = SignatureUtil.sign(bpmRegister2, CLIENT2_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister2 = new VerifiableProtocolMessage(
                pmRegister2, signedPmRegister2);

        VerifiableProtocolMessage vpm_responseRegister2 =server.registerUser(vpmRegister2);
        StatusCode scRegister2 = vpm_responseRegister2.getProtocolMessage().getStatusCode();
        assertEquals(scRegister2, StatusCode.OK);

        // registering client3
        int opUuidRegister3 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister3 = new ProtocolMessage(
                "REGISTER", CLIENT2_PUBLIC_KEY, opUuidRegister3);
        byte[] bpmRegister3 = ProtocolMessageConverter.objToByteArray(pmRegister3);
        byte[] signedPmRegister3 = SignatureUtil.sign(bpmRegister2, CLIENT3_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister3 = new VerifiableProtocolMessage(
                pmRegister3, signedPmRegister3);

        VerifiableProtocolMessage vpm_responseRegister3 = server.registerUser(vpmRegister3);
        StatusCode scRegister3 = vpm_responseRegister3.getProtocolMessage().getStatusCode();
        assertEquals(scRegister3, StatusCode.OK);


        // posting first announcement
        List<Integer> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(sc1, StatusCode.OK);

        // posting second announcement
        Announcement a1 = vpm_response1.getProtocolMessage().getPostAnnouncement();
        int ref1Uuid = vpm_response1.getProtocolMessage().getPostAnnouncement().getAnnouncementID();
        List<Integer> references2 = new ArrayList<>(Arrays.asList(ref1Uuid));
        Announcement announcement2 = new Announcement(MESSAGE2, references2);
        int opUuid2 = UUIDGenerator.generateUUID();
        ProtocolMessage pm2 = new ProtocolMessage(
                "POST", CLIENT2_PUBLIC_KEY, opUuid2, announcement2);
        byte[] bpm2 = ProtocolMessageConverter.objToByteArray(pm2);
        byte[] signedpm2 = SignatureUtil.sign(bpm2, CLIENT2_PRIVATE_KEY);
        VerifiableProtocolMessage vpm2 = new VerifiableProtocolMessage(pm2, signedpm2);

        VerifiableProtocolMessage vpm_response2 = server.post(vpm2);
        StatusCode sc2 = vpm_response2.getProtocolMessage().getStatusCode();
        assertEquals(sc2, StatusCode.OK);

        // posting third announcement
        int ref2Uuid = vpm_response2.getProtocolMessage().getPostAnnouncement().getAnnouncementID();
        List<Integer> references3 = new ArrayList<>(Arrays.asList(ref1Uuid, ref2Uuid));
        Announcement announcement3 = new Announcement(MESSAGE3, references3);
        int opUuid3 = UUIDGenerator.generateUUID();
        ProtocolMessage pm3 = new ProtocolMessage(
                "POST", CLIENT3_PUBLIC_KEY, opUuid3, announcement3);
        byte[] bpm3 = ProtocolMessageConverter.objToByteArray(pm3);
        byte[] signedpm3 = SignatureUtil.sign(bpm3, CLIENT3_PRIVATE_KEY);
        VerifiableProtocolMessage vpm3 = new VerifiableProtocolMessage(pm3, signedpm3);

        VerifiableProtocolMessage vpm_response3 = server.post(vpm3);
        StatusCode sc3 = vpm_response3.getProtocolMessage().getStatusCode();
        assertEquals(sc3, StatusCode.OK);
    }

    // Unregistered User
    @Test
    void userNotRegistered() throws Exception {

        /*Server server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        List<Integer> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(sc1, StatusCode.USER_NOT_REGISTERED);*/
    }

    // Invalid Announcement fields tests
    @Test
    void messageLengthIsInvalid() throws Exception {

        /*Server server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        // registering client1
        int opUuidRegister1 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT1_PUBLIC_KEY, opUuidRegister1);
        byte[] bpmRegister1 = ProtocolMessageConverter.objToByteArray(pmRegister1);
        byte[] signedPmRegister1 = SignatureUtil.sign(bpmRegister1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                pmRegister1, signedPmRegister1);

        VerifiableProtocolMessage vpm_responseRegister1 =server.registerUser(vpmRegister1);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(scRegister1, StatusCode.OK);

        // posting announcement
        List<Integer> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(INVALID_LENGTH_MESSAGE, references1);
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(sc1, StatusCode.INVALID_MESSAGE_LENGTH);*/
    }

    @Test
    void InvalidReferences() throws Exception {

        /*Server server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        // registering client1
        int opUuidRegister1 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT1_PUBLIC_KEY, opUuidRegister1);
        byte[] bpmRegister1 = ProtocolMessageConverter.objToByteArray(pmRegister1);
        byte[] signedPmRegister1 = SignatureUtil.sign(bpmRegister1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                pmRegister1, signedPmRegister1);

        VerifiableProtocolMessage vpm_responseRegister1 =server.registerUser(vpmRegister1);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(scRegister1, StatusCode.OK);

        // posting announcement
        int invalid_opUuid = UUIDGenerator.generateUUID();
        List<Integer> references1 = new ArrayList<>(Arrays.asList(invalid_opUuid));
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(sc1, StatusCode.INVALID_REFERENCE);*/
    }

    // null Parameters
    @Test
    void publicKeyIsNull() throws Exception {

        /*Server server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        // registering client1
        int opUuidRegister1 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT1_PUBLIC_KEY, opUuidRegister1);
        byte[] bpmRegister1 = ProtocolMessageConverter.objToByteArray(pmRegister1);
        byte[] signedPmRegister1 = SignatureUtil.sign(bpmRegister1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                pmRegister1, signedPmRegister1);

        VerifiableProtocolMessage vpm_responseRegister1 =server.registerUser(vpmRegister1);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(scRegister1, StatusCode.OK);

        // posting announcement
        List<Integer> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", null, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(sc1, StatusCode.NULL_FIELD);*/
    }

    @Test
    void messageIsNull() throws Exception {

        /*Server server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        // registering client1
        int opUuidRegister1 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT1_PUBLIC_KEY, opUuidRegister1);
        byte[] bpmRegister1 = ProtocolMessageConverter.objToByteArray(pmRegister1);
        byte[] signedPmRegister1 = SignatureUtil.sign(bpmRegister1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                pmRegister1, signedPmRegister1);

        VerifiableProtocolMessage vpm_responseRegister1 =server.registerUser(vpmRegister1);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(scRegister1, StatusCode.OK);

        // posting announcement
        List<Integer> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(null, references1);
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(sc1, StatusCode.NULL_FIELD);*/
    }

    @Test
    void referencesIsNull() throws Exception {

        /*Server server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        // registering client1
        int opUuidRegister1 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT1_PUBLIC_KEY, opUuidRegister1);
        byte[] bpmRegister1 = ProtocolMessageConverter.objToByteArray(pmRegister1);
        byte[] signedPmRegister1 = SignatureUtil.sign(bpmRegister1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                pmRegister1, signedPmRegister1);

        VerifiableProtocolMessage vpm_responseRegister1 =server.registerUser(vpmRegister1);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(scRegister1, StatusCode.OK);

        // posting announcement
        Announcement announcement1 = new Announcement(MESSAGE1, null);
        int opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(sc1, StatusCode.NULL_FIELD);*/
    }

    @Test
    void opUuidIsNull() throws Exception {

        /*Server server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        // registering client1
        int opUuidRegister1 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT1_PUBLIC_KEY, opUuidRegister1);
        byte[] bpmRegister1 = ProtocolMessageConverter.objToByteArray(pmRegister1);
        byte[] signedPmRegister1 = SignatureUtil.sign(bpmRegister1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                pmRegister1, signedPmRegister1);

        VerifiableProtocolMessage vpm_responseRegister1 =server.registerUser(vpmRegister1);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(scRegister1, StatusCode.OK);

        // posting announcement
        List<Integer> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        Integer opUuid1 = null;
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(sc1, StatusCode.NULL_FIELD);*/
    }

    @Test
    void signatureIsNull() throws Exception {

        /*Server server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        // registering client1
        int opUuidRegister1 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT1_PUBLIC_KEY, opUuidRegister1);
        byte[] bpmRegister1 = ProtocolMessageConverter.objToByteArray(pmRegister1);
        byte[] signedPmRegister1 = SignatureUtil.sign(bpmRegister1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                pmRegister1, signedPmRegister1);

        VerifiableProtocolMessage vpm_responseRegister1 =server.registerUser(vpmRegister1);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(scRegister1, StatusCode.OK);

        // posting announcement
        List<Integer> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        int opUuid = UUIDGenerator.generateUUID();
        ProtocolMessage pm = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid, announcement1);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm, null);

        VerifiableProtocolMessage vpm_response1 = server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(sc1, StatusCode.NULL_FIELD);*/

    }

    // Replay attacks
    @Test
    void duplicatedOperation() throws Exception {

        /*Server server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        // registering client1
        int opUuidRepeated = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT1_PUBLIC_KEY, opUuidRepeated);
        byte[] bpmRegister1 = ProtocolMessageConverter.objToByteArray(pmRegister1);
        byte[] signedPmRegister1 = SignatureUtil.sign(bpmRegister1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                pmRegister1, signedPmRegister1);

        VerifiableProtocolMessage vpm_responseRegister1 =server.registerUser(vpmRegister1);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(scRegister1, StatusCode.OK);

        // posting announcement
        List<Integer> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuidRepeated, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(sc1, StatusCode.DUPLICATE_OPERATION);*/
    }

    // Message Integrity attacks
    @Test
    void tamperedMessage() throws Exception {

        /*Server server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        // registering client1
        int opUuidRegister1 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT1_PUBLIC_KEY, opUuidRegister1);
        byte[] bpmRegister1 = ProtocolMessageConverter.objToByteArray(pmRegister1);
        byte[] signedPmRegister1 = SignatureUtil.sign(bpmRegister1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                pmRegister1, signedPmRegister1);

        VerifiableProtocolMessage vpm_responseRegister1 =server.registerUser(vpmRegister1);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(scRegister1, StatusCode.OK);

        // posting announcement
        List<Integer> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        int opUuid = UUIDGenerator.generateUUID();
        ProtocolMessage pm = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid, announcement1);
        Announcement tampAnnouncement = new Announcement(MESSAGE2, references1);
        ProtocolMessage tampPm = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid, tampAnnouncement);
        byte[] bpm = ProtocolMessageConverter.objToByteArray(pm);
        byte[] signedpm = SignatureUtil.sign(bpm, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(tampPm, signedpm);

        VerifiableProtocolMessage vpm_response1 = server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(sc1, StatusCode.INVALID_SIGNATURE);*/
    }

    @Test
    void invalidSignature() throws Exception {

        /*Server server = new Server(false, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS,
                SERVER_PUBLIC_KEY_PATH, SERVER_KEYSTORE_PATH);

        // registering client1
        int opUuidRegister1 = UUIDGenerator.generateUUID();
        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", CLIENT1_PUBLIC_KEY, opUuidRegister1);
        byte[] bpmRegister1 = ProtocolMessageConverter.objToByteArray(pmRegister1);
        byte[] signedPmRegister1 = SignatureUtil.sign(bpmRegister1, CLIENT1_PRIVATE_KEY);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                pmRegister1, signedPmRegister1);

        VerifiableProtocolMessage vpm_responseRegister1 =server.registerUser(vpmRegister1);
        StatusCode scRegister1 = vpm_responseRegister1.getProtocolMessage().getStatusCode();
        assertEquals(scRegister1, StatusCode.OK);

        // posting announcement
        List<Integer> references1 = new ArrayList<>();
        Announcement announcement1 = new Announcement(MESSAGE1, references1);
        Integer opUuid1 = UUIDGenerator.generateUUID();
        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", CLIENT1_PUBLIC_KEY, opUuid1, announcement1);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, CLIENT2_PRIVATE_KEY);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        VerifiableProtocolMessage vpm_response1 = server.post(vpm1);
        StatusCode sc1 = vpm_response1.getProtocolMessage().getStatusCode();
        assertEquals(sc1, StatusCode.INVALID_SIGNATURE);*/
    }

}
