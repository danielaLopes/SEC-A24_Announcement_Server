package pt.ulisboa.tecnico.sec.server;

import java.security.PublicKey;

import pt.ulisboa.tecnico.sec.communication_lib.VerifiableAnnouncement;

public class RegisterValue {
    
    private int _timeStamp;
    private VerifiableAnnouncement _value;
    private PublicKey _clientPublicKey;

    public RegisterValue() {
        _timeStamp = 0;
    }

    public RegisterValue(int timeStamp, VerifiableAnnouncement value, PublicKey clientyPublicKey) {
        _timeStamp = timeStamp;
        _value = value;
        _clientPublicKey = clientyPublicKey;
    }

    public int getTimeStamp() { return _timeStamp; }
    public VerifiableAnnouncement getValues() { return _value; }
    public PublicKey getPublicKey() { return _clientPublicKey; }

    public void setTimeStamp(int timeStamp) { _timeStamp = timeStamp; }
    public void setValues(VerifiableAnnouncement value) { _value = value; }
}