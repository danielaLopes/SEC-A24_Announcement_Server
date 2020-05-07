package pt.ulisboa.tecnico.sec.server;

import java.security.PublicKey;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;

public class RegisterValue {
    
    private int _timeStamp;
    private Announcement _value;
    private PublicKey _clientPublicKey;

    public RegisterValue() {
        _timeStamp = 0;
    }

    public RegisterValue(int timeStamp, Announcement value, PublicKey clientyPublicKey) {
        _timeStamp = timeStamp;
        _value = value;
        _clientPublicKey = clientyPublicKey;
    }

    public int getTimeStamp() { return _timeStamp; }
    public Announcement getValues() { return _value; }
    public PublicKey getPublicKey() { return _clientPublicKey; }

    public void setTimeStamp(int timeStamp) { _timeStamp = timeStamp; }
    public void setValues(Announcement value) { _value = value; }
}