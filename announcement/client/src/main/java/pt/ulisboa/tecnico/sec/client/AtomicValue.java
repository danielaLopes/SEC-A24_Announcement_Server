package pt.ulisboa.tecnico.sec.client;

import java.util.List;

import pt.ulisboa.tecnico.sec.communication_lib.VerifiableAnnouncement;

public class AtomicValue {
    private int _timeStamp;
    private List<VerifiableAnnouncement> _values;

    public AtomicValue(int timeStamp, List<VerifiableAnnouncement> values) {
        _timeStamp = timeStamp;
        _values = values;
    }

    public int getTimeStamp() { return _timeStamp; }
    public List<VerifiableAnnouncement> getValues() { return _values; }

    public void setTimeStamp(int timeStamp) { _timeStamp = timeStamp; }
    public void setValues(List<VerifiableAnnouncement> values) { _values = values; }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        if (!(o instanceof AtomicValue)) {
            return false;
        }

        AtomicValue av = (AtomicValue) o;

        return this.getTimeStamp() == av.getTimeStamp();
    }
}