package pt.ulisboa.tecnico.sec.client;

import java.util.List;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;

public class AtomicValue {
    private int _timeStamp;
    private List<Announcement> _values;

    public AtomicValue(int timeStamp, List<Announcement> values) {
        _timeStamp = timeStamp;
        _values = values;
    }

    public int getTimeStamp() { return _timeStamp; }
    public List<Announcement> getValues() { return _values; }

    public void setTimeStamp(int timeStamp) { _timeStamp = timeStamp; }
    public void setValues(List<Announcement> values) { _values = values; }
}