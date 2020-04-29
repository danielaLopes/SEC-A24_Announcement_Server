package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.Serializable;

public class VerifiableServerMessage implements Serializable{
    private ServerMessage _sm;
    private byte[] _signedsm;

    public VerifiableServerMessage(ServerMessage sm, byte[] signedsm) {
        _sm = sm;
        _signedsm = signedsm;
    }

    public ServerMessage getServerMessage() { return _sm; }
    public byte[] getSignedServerMessage() { return _signedsm; }

    public void setServerMessage(ServerMessage sm) { _sm = sm; }
}