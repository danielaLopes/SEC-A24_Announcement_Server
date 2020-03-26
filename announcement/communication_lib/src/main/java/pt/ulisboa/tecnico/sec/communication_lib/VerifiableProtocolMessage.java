package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.Serializable;

public class VerifiableProtocolMessage implements Serializable{
    private ProtocolMessage _pm;
    private byte[] _signedpm;

    public VerifiableProtocolMessage(ProtocolMessage pm, byte[] signedpm) {
        _pm = pm;
        _signedpm = signedpm;
    }

    public ProtocolMessage getProtocolMessage() { return _pm; }
    public byte[] getSignedProtocolMessage() { return _signedpm; }
}