import org.junit.jupiter.api.BeforeAll;
import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.ProtocolMessage;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableProtocolMessage;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyPairUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;
import pt.ulisboa.tecnico.sec.crypto_lib.SignatureUtil;
import pt.ulisboa.tecnico.sec.server.Server;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseTest {

    public final static String MESSAGE1 = "message1";
    public final static String MESSAGE2 = "message2";
    public final static String MESSAGE3 = "message3";

    public final static String INVALID_LENGTH_MESSAGE = new String(new char[256]).replace('\0', 'A');

    public final static int MAX_MESSAGE_LENGTH = 255;

    public final static char[] KEYSTORE_PASSWD = {'p','a','s','s','w','o','r','d'};
    public final static char[] ENTRY_PASSWD = {'p','a','s','s','w','o','r','d'};
    public final static String ALIAS = "alias";

    public final static String SERVER_PUBLIC_KEY_PATH = "src/main/resources/crypto/public.key";
    public final static String SERVER_KEYSTORE_PATH = "src/main/resources/crypto/server_keystore.jks";

    public static PublicKey CLIENT1_PUBLIC_KEY;
    public static PrivateKey CLIENT1_PRIVATE_KEY;
    public static PublicKey CLIENT2_PUBLIC_KEY;
    public static PrivateKey CLIENT2_PRIVATE_KEY;
    public static PublicKey CLIENT3_PUBLIC_KEY;
    public static PrivateKey CLIENT3_PRIVATE_KEY;

    @BeforeAll
    public static void oneTimeSetup() {

        KeyPair client1KeyPair = KeyPairUtil.generateKeyPair("RSA", 2048);
        CLIENT1_PUBLIC_KEY = client1KeyPair.getPublic();
        CLIENT1_PRIVATE_KEY = client1KeyPair.getPrivate();

        KeyPair client2KeyPair = KeyPairUtil.generateKeyPair("RSA", 2048);
        CLIENT2_PUBLIC_KEY = client2KeyPair.getPublic();
        CLIENT2_PRIVATE_KEY = client2KeyPair.getPrivate();

        KeyPair client3KeyPair = KeyPairUtil.generateKeyPair("RSA", 2048);
        CLIENT3_PUBLIC_KEY = client3KeyPair.getPublic();
        CLIENT3_PRIVATE_KEY = client3KeyPair.getPrivate();
    }

    static VerifiableProtocolMessage forgeRegisterRequest(
            Server server, int opUuid, PublicKey clientPubKey, PrivateKey clientPrivKey) throws Exception {

        ProtocolMessage pmRegister1 = new ProtocolMessage(
                "REGISTER", clientPubKey, opUuid);
        byte[] bpmRegister1 = ProtocolMessageConverter.objToByteArray(pmRegister1);
        byte[] signedPmRegister1 = SignatureUtil.sign(bpmRegister1, clientPrivKey);
        VerifiableProtocolMessage vpmRegister1 = new VerifiableProtocolMessage(
                pmRegister1, signedPmRegister1);

        return server.registerUser(vpmRegister1);
    }

    /*VerifiableProtocolMessage forgePostRequest(
            Server server, int opUuid, Announcement announcement, PublicKey clientPubKey, PrivateKey clientPrivKey)
            throws Exception {

        ProtocolMessage pm1 = new ProtocolMessage(
                "POST", clientPubKey, opUuid, announcement);
        byte[] bpm1 = ProtocolMessageConverter.objToByteArray(pm1);
        byte[] signedpm1 = SignatureUtil.sign(bpm1, clientPrivKey);
        VerifiableProtocolMessage vpm1 = new VerifiableProtocolMessage(pm1, signedpm1);

        return server.post(vpm1);
    }*/

    void assertEqualAnnouncement(Announcement expected, Announcement obtained) {
        assertEquals(expected.getAnnouncement(), obtained.getAnnouncement());
        assertEquals(expected.getAnnouncementID(), obtained.getAnnouncementID());
        assertEquals(expected.getReferences(), obtained.getReferences());
        assertEquals(expected.getClientPublicKey(), obtained.getClientPublicKey());
        // TODO: check if signatures are the same
    }
}
