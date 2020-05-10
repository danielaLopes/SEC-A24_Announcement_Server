package pt.ulisboa.tecnico.sec.communication_lib;

public class VerifiableServerMessage extends SerializableObject {
    private byte[] _sm;
    private byte[] _signedsm;

    public VerifiableServerMessage(ServerMessage sm, byte[] signedsm) {
        _sm = sm.getBytes();
        _signedsm = signedsm;
    }

    public VerifiableServerMessage(byte[] bytes) {
        VerifiableServerMessage vsm = (VerifiableServerMessage) super.byteArrayToObj(bytes);
        _sm = vsm.getServerMessage().getBytes();
        _signedsm = vsm.getSignedServerMessage();
    }

    public ServerMessage getServerMessage() { return (ServerMessage) super.byteArrayToObj(_sm); }
    public byte[] getSignedServerMessage() { return _signedsm; }

    public byte[] getBytes() {
        return super.objToByteArray(this);
    }

    @Override
    public Object byteArrayToObj(byte[] b) {
        return super.byteArrayToObj(b);
    }
}