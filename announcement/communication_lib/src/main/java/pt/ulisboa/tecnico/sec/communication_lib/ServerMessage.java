package pt.ulisboa.tecnico.sec.communication_lib;

import java.security.PublicKey;
import java.util.List;

public class ServerMessage extends SerializableObject {
    private String _command;
    private String _bcb; // uuid to prevent replay attacks
    private PublicKey _publicKey;
    private VerifiableProtocolMessage _clientVPM;
    private byte[] _sigma; 

    public ServerMessage(PublicKey publicKey, String command, VerifiableProtocolMessage clientVPM) {
        _publicKey = publicKey;
        _command = command;
        _clientVPM = clientVPM;
    }

    public ServerMessage(PublicKey publicKey, String command, VerifiableProtocolMessage clientVPM, String bcb) {
        _publicKey = publicKey;
        _command = command;
        _clientVPM = clientVPM;
        _bcb = bcb;
    }

    public ServerMessage(byte[] bytes) {
        ServerMessage sm = (ServerMessage) super.byteArrayToObj(bytes);
        _publicKey = sm.getPublicKey();
        _command = sm.getCommand();
        _clientVPM = sm.getClientMessage();
    }

    public void setBcb(String bcb) { _bcb = bcb; }
    public String getBcb() { return _bcb; }

    public byte[] getSigma() { return _sigma; }
    public void setSigma(byte[] sigma) {
        _sigma = sigma;
    }

    public byte[] getBytes() {
        return super.objToByteArray(this);
    }

    @Override
    public Object byteArrayToObj(byte[] b) {
        return super.byteArrayToObj(b);
    }

    public PublicKey getPublicKey() {return _publicKey; }
    public VerifiableProtocolMessage getClientMessage() { return _clientVPM; }
    public String getCommand() {return _command; }
}