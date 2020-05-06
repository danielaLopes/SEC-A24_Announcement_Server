package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AtomicRegisterMessages implements Serializable {
    private int _rid;
    private int _wts;
    private List<Announcement> _values;

    public AtomicRegisterMessages(int rid, int wts, List<Announcement> values) {
        _rid = rid;
        _wts = wts;
        _values = values;
    }

    public AtomicRegisterMessages(int rid) {
        _rid = rid;
        _wts = 0;
        _values = new ArrayList<Announcement>();
    }

    public int getRid() { return _rid; }
    public int getWts() { return _wts; }
    public List<Announcement> getValues() { return _values; }
}