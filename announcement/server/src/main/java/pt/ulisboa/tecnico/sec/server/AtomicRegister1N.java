package pt.ulisboa.tecnico.sec.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.mysql.cj.protocol.Protocol;

import java.io.IOException;
import java.security.*;

import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;

public class AtomicRegister1N {
    private Server _server;
    private int _nServers;
    private int _ts = 0;
    private List<Announcement> _values = new ArrayList<>();

    public int getTimeStamp() { return _ts; }
    public List<Announcement> getValues() { return _values; }

    public AtomicRegisterMessages acknowledge(AtomicRegisterMessages arm) {
        //System.out.println("acknowledge");
        if (arm.getWts() > _ts) {
            _ts = arm.getWts();
            _values.addAll(arm.getValues());
        }
        return new AtomicRegisterMessages(arm.getRid());
    }

    public AtomicRegisterMessages value(AtomicRegisterMessages arm, int n) {
        //System.out.println("value");
        return new AtomicRegisterMessages(arm.getRid(), _ts, getUserAnnouncements(n));
    }

    public List<Announcement> getUserAnnouncements(int number) {
        int nAnnouncements = _values.size();
        if ((0 < number) && (number <= nAnnouncements)) {
            return new ArrayList<>(_values.subList(nAnnouncements - number, nAnnouncements));
        }
        else {
            return _values;
        }
    }


}