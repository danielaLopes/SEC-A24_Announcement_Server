package pt.ulisboa.tecnico.sec.communication_lib;

public class VerifiableProtocolMessage {
    private ProtocolMessage _pm;
    private byte[] _signedpm;

    public VerifiableProtocolMessage(ProtocolMessage pm, byte[] signedpm) {
        _pm = pm;
        _signedpm = signedpm;
    }

    public ProtocolMessage getProtocolMessage() { return _pm; }
    public byte[] getSignedProtocolMessage() { return _signedpm; }
}