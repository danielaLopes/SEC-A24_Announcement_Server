package pt.ulisboa.tecnico.sec.communication_lib;

import java.util.ArrayList;
import java.util.List;

public class RegisterMessage extends SerializableObject {
    private int _rid;
    private int _wts;
    private List<VerifiableAnnouncement> _values;


    public RegisterMessage(int rid, int wts, List<VerifiableAnnouncement> values) {
        _rid = rid;
        _wts = wts;
        _values = values;
    }

    public RegisterMessage(int rid) {
        _rid = rid;
        _wts = 0;
        _values = new ArrayList<VerifiableAnnouncement>();
    }

    public RegisterMessage() {
        _values = new ArrayList<VerifiableAnnouncement>();
    }

    public RegisterMessage(int wts, List<VerifiableAnnouncement> values) {
        _wts = wts;
        _values = values;
    }

    public RegisterMessage(byte[] bytes) {
        RegisterMessage registerMessage = (RegisterMessage) super.byteArrayToObj(bytes);
        _rid = registerMessage.getRid();
        _wts = registerMessage.getWts();
        _values = registerMessage.getValues();
    }

    public int getRid() { return _rid; }
    public void setRid(int rid) { _rid = rid; }
    public int getWts() { return _wts; }
    public void setWts(int wts) { _wts = wts; }
    public List<VerifiableAnnouncement> getValues() { return _values; }

    public byte[] getBytes() {
        return super.objToByteArray(this);
    }

    @Override
    public Object byteArrayToObj(byte[] b) {
        return super.byteArrayToObj(b);
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        if (!(o instanceof RegisterMessage)) {
            return false;
        }

        RegisterMessage rm = (RegisterMessage) o;

        return this.getRid() == rm.getRid() && this.getWts() == rm.getWts()
            && this.getValues().equals(rm.getValues());
    }
}