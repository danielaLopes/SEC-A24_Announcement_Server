package pt.ulisboa.tecnico.sec.client;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.AtomicRegisterMessages;
import pt.ulisboa.tecnico.sec.communication_lib.ProtocolMessage;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class AtomicRegister1N {
    private AtomicValue _atomicValue;
    private int _rid;
    private AtomicInteger _acks;
    private HashMap<PublicKey, AtomicValue> _readList = new HashMap<>();
    private boolean _reading;
    private int _wts;
    private List<Announcement> _readval;
    private Client _client;

    public AtomicRegister1N(Client client) {
        _atomicValue = new AtomicValue(0, new ArrayList<Announcement>());
        _wts = 0;
        _acks = new AtomicInteger(0);
        _rid = 0;
        _reading = false;
        _client = client;
        resetReadList();
        _readval = new ArrayList<Announcement>();
    }

    public void resetReadList() {
        _readList.clear();
    }

    public AtomicRegisterMessages read() {
        System.out.println("read");
        _rid += 1;
        _acks.set(0);
        resetReadList();
        _reading = true;
        return new AtomicRegisterMessages(_rid);
    }

    public AtomicRegisterMessages writeBack(ProtocolMessage pm) {
        if(_rid == pm.getAtomicRegisterMessages().getRid()) {
            AtomicValue av = new AtomicValue(pm.getAtomicRegisterMessages().getWts(), pm.getAtomicRegisterMessages().getValues());
            _readList.put(pm.getPublicKey(), av);
            if (_readList.size() > _client._nServers / 2) {
                AtomicValue highest = highest();
                resetReadList();
                _client.writeBack(new AtomicRegisterMessages(_rid, highest.getTimeStamp(), highest.getValues()));
            }
        }
        return null;
    }

    public AtomicValue highest() {
        int ts = -1;
        AtomicValue highestValue = new AtomicValue(-1, new ArrayList<>());
        for(Map.Entry<PublicKey, AtomicValue> r : _readList.entrySet()) {
            if (r.getValue().getTimeStamp() > ts) {
                highestValue = r.getValue();
                _readval = new ArrayList<Announcement>(highestValue.getValues());
                ts = highestValue.getTimeStamp();
            }
        }
        return highestValue;
    }

    public void write() {
        System.out.println("write");
        _rid += 1;
        _wts += 1;
        _acks.set(0);
    }

    public void writeReturn(int r) {
        System.out.println("writeReturn");
        if (r == _rid) {
            _acks.incrementAndGet();
            synchronized(_acks) {
                if (_acks.get() > _client._nServers / 2) {
                    _acks.set(0);
                    if(_reading) {
                        _reading = false;
                        System.out.println("" + _readval);
                        _client.deliverRead(_readval);
                    }
                    else
                        _client.deliverPost();
                }
            }
        }
    }


    public AtomicValue getAtomicValue() {
        return _atomicValue;
    }

    public int getRid() {
        return _rid;
    }

    public int getAcks() {
        return _acks.get();
    }

    public HashMap<PublicKey, AtomicValue> getReadList() {
        return _readList;
    }

    public boolean isReading() {
        return _reading;
    }

    public int getWts() {
        return _wts;
    }

    public List<Announcement> getReadVal() {
        return _readval;
    }

}