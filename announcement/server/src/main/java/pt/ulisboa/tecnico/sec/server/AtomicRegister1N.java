package pt.ulisboa.tecnico.sec.server;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import pt.ulisboa.tecnico.sec.communication_lib.*;

public class AtomicRegister1N {
    private Object _lock = new Object();

    private Server _server;
    private int _nServers;
    private AtomicInteger _ts = new AtomicInteger(0);
    private List<VerifiableAnnouncement> _values = new ArrayList<>();

    public int getTimeStamp() { return _ts.get(); }
    public List<VerifiableAnnouncement> getValues() { return _values; }

    public RegisterMessage acknowledge(RegisterMessage arm) {
        //System.out.println("acknowledge");
        synchronized (_lock) {
            if (arm.getWts() > _ts.get()) {
                _ts.set(arm.getWts());
                _values.addAll(arm.getValues());
            }
        }
        return new RegisterMessage(arm.getRid());
    }

    public synchronized RegisterMessage value(RegisterMessage arm, int n) {
        //System.out.println("value");
        return new RegisterMessage(arm.getRid(), _ts.get(), getUserAnnouncements(n));
    }

    public List<VerifiableAnnouncement> getUserAnnouncements(int number) {
        int nAnnouncements = _values.size();
        if ((0 < number) && (number <= nAnnouncements)) {
            System.out.println("returning " + number + " announcements");
            return new ArrayList<VerifiableAnnouncement>(_values.subList(nAnnouncements - number, nAnnouncements));
        }
        else {
            System.out.println("returning all " + _values.size() + " announcements");
            return _values;
        }
    }


}