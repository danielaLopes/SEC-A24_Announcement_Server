import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.communication_lib.ProtocolMessage;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableProtocolMessage;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;
import pt.ulisboa.tecnico.sec.crypto_lib.SignatureUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.UUIDGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class VerifySignatureTest extends BaseTest {

    private Client _client;

    public VerifySignatureTest() {
        List<String> otherUsersPubKeyPaths = new ArrayList<String>();
        otherUsersPubKeyPaths.add(PUBLICKEY_PATH2);
        otherUsersPubKeyPaths.add(PUBLICKEY_PATH3);
        _client = new Client(PUBLICKEY_PATH1, KEYSTORE_PATH1, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS, SERVER_PUBLICKEY_PATH, otherUsersPubKeyPaths);

    }

    @Test
    void success() throws Exception {
        int uuid = UUIDGenerator.generateUUID();
        ProtocolMessage requestPM = new ProtocolMessage("REGISTER", _client.getPublicKeyFromUser(0), uuid);

        ProtocolMessage responsePM = new ProtocolMessage(
                "REGISTER", StatusCode.OK, _client.getServerPubKey(), requestPM.getOpUuid());
        

        byte[] bpm = ProtocolMessageConverter.objToByteArray(responsePM);
        byte[] signedpm = SignatureUtil.sign(bpm, loadPrivateKey(SERVER_KEYSTORE_PATH1, SERVER_PASSWD, SERVER_PASSWD, SERVER_ALIAS));
        VerifiableProtocolMessage vpm = new VerifiableProtocolMessage(responsePM, signedpm);

        boolean success = _client.verifySignature(vpm);

        assertEquals(true, success);
    }

    @Test
    void invalidSignature() throws Exception {
        int uuid = UUIDGenerator.generateUUID();
        ProtocolMessage requestPM = new ProtocolMessage("REGISTER", _client.getPublicKeyFromUser(0), uuid);

        ProtocolMessage responsePM = new ProtocolMessage(
                "REGISTER", StatusCode.OK, _client.getServerPubKey(), requestPM.getOpUuid());
        

        byte[] bpm = ProtocolMessageConverter.objToByteArray(responsePM);
        byte[] signedpm = SignatureUtil.sign(bpm, loadPrivateKey(KEYSTORE_PATH2, KEYSTORE_PASSWD, ENTRY_PASSWD, ALIAS));
        VerifiableProtocolMessage vpm = new VerifiableProtocolMessage(responsePM, signedpm);

        boolean success = _client.verifySignature(vpm);

        assertEquals(false, success);
    }

}

