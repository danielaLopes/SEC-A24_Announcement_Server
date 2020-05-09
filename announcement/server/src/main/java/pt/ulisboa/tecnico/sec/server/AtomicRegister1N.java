package pt.ulisboa.tecnico.sec.server;

import java.util.*;

import pt.ulisboa.tecnico.sec.communication_lib.*;

public class AtomicRegister1N {
    private Server _server;
    private int _nServers;
    private int _ts = 0;
    private List<Announcement> _values = new ArrayList<>();

    public int getTimeStamp() { return _ts; }
    public List<Announcement> getValues() { return _values; }

    public RegisterMessage acknowledge(RegisterMessage arm) {
        //System.out.println("acknowledge");
        if (arm.getWts() > _ts) {
            _ts = arm.getWts();
            _values.addAll(arm.getValues());
        }
        return new RegisterMessage(arm.getRid());
    }

    public RegisterMessage value(RegisterMessage arm, int n) {
        //System.out.println("value");
        return new RegisterMessage(arm.getRid(), _ts, getUserAnnouncements(n));
    }

    public List<Announcement> getUserAnnouncements(int number) {
        int nAnnouncements = _values.size();
        if ((0 < number) && (number <= nAnnouncements)) {
            System.out.println("returning " + number + " announcements");
            return new ArrayList<>(_values.subList(nAnnouncements - number, nAnnouncements));
        }
        else {
            System.out.println("returning all " + _values.size() + " announcements");
            return _values;
        }
    }


}