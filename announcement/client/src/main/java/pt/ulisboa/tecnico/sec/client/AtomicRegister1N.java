package pt.ulisboa.tecnico.sec.client;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.RegisterMessage;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableAnnouncement;
import pt.ulisboa.tecnico.sec.communication_lib.ProtocolMessage;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class AtomicRegister1N {
    private AtomicValue _atomicValue;
    private AtomicInteger _rid;
    private AtomicInteger _acks;
    private ConcurrentHashMap<PublicKey, AtomicValue> _readList = new ConcurrentHashMap<>();
    private AtomicBoolean _reading;
    private AtomicInteger _wts;
    private List<VerifiableAnnouncement> _readval;
    private Client _client;

    // in order to lock both _readList and _readval
    private Object _lock = new Object();

    public AtomicRegister1N(Client client) {
        _atomicValue = new AtomicValue(0, new ArrayList<VerifiableAnnouncement>());
        _wts = new AtomicInteger(0);
        _acks = new AtomicInteger(0);
        _rid = new AtomicInteger(0);
        _reading = new AtomicBoolean(false);
        _client = client;
        resetReadList();
        _readval = new ArrayList<VerifiableAnnouncement>();
    }

    public void resetReadList() {
        synchronized (_readList) {
            _readList.clear();
        }
    }

    public RegisterMessage read() {
        //System.out.println("read");
        int rid = _rid.incrementAndGet();
        _acks.set(0);
        resetReadList();
        _reading.set(true);
        return new RegisterMessage(rid);
    }

    public RegisterMessage writeBack(ProtocolMessage pm) {
        RegisterMessage registerMessage = new RegisterMessage(pm.getAtomicRegisterMessages());
        int rid = _rid.get();
        if (rid == registerMessage.getRid()) {
            AtomicValue av = new AtomicValue(registerMessage.getWts(), registerMessage.getValues());

            synchronized (_readList) {

                _readList.put(pm.getPublicKey(), av);
                System.out.flush();

                _readList.put(pm.getPublicKey(), av);
                AtomicValue highest = highest();
                if (_readList.size() > _client._nServers / 2 && highest != null) {
                    resetReadList();
                    _client.writeBack(new RegisterMessage(rid, highest.getTimeStamp(), highest.getValues()));
                }

            }
        }
        return null;
    }

    public AtomicValue highest() {
        Map<AtomicValue, Integer> atomicValues = new HashMap<>();

        for (Map.Entry<PublicKey, AtomicValue> r : _readList.entrySet()) {
            AtomicValue av = findAtomicValue(atomicValues, r.getValue());
            if (av == null) {
                atomicValues.put(r.getValue(), 1);
            }
            else {
                atomicValues.put(av, atomicValues.get(av) + 1);
            }
        }

        int maxOccurences = -1;
        AtomicValue av = null;
        for (Map.Entry<AtomicValue, Integer> e : atomicValues.entrySet()) {
            if (e.getValue() > _client._nServers / 2 && e.getValue() > maxOccurences) {
                maxOccurences = e.getValue();
                av = e.getKey();
            }
        }

        if (av == null) return null;
        _readval = av.getValues();
        return av;

    }

    public AtomicValue findAtomicValue(Map<AtomicValue, Integer> atomicValues, AtomicValue av) {
        for (Map.Entry<AtomicValue, Integer> otherAv : atomicValues.entrySet()) {
            if (av.equals(otherAv.getKey())) {
                return otherAv.getKey();
            }
        }
        return null;
    }

    public void write() {
        //System.out.println("write");
        _rid.incrementAndGet();
        _wts.incrementAndGet();
        _acks.set(0);
    }

    public void writeReturn(int r) {
        //System.out.println("writeReturn");
        if (r == _rid.get()) {
            _acks.incrementAndGet();
            synchronized(_lock) {
                //System.out.println(_acks.get());
                if (_acks.get() > _client._nServers / 2) {
                    _acks.set(0);
                    if(_reading.compareAndSet(true, false)) {
                        //_reading = false;
                        /*System.out.println("_readVal size before calling deliverRead() " + _readval.size());
                        System.out.flush();*/
                        _client.deliverRead(StatusCode.OK, _readval);
                    }
                    else
                        _client.deliverPost(StatusCode.OK);
                }
            }
        }
    }


    public AtomicValue getAtomicValue() {
        return _atomicValue;
    }

    public int getRid() {
        return _rid.get();
    }

    public int getAcks() {
        return _acks.get();
    }

    public ConcurrentHashMap<PublicKey, AtomicValue> getReadList() {
        return _readList;
    }

    public boolean isReading() {
        return _reading.get();
    }

    public int getWts() {
        return _wts.get();
    }

    public List<VerifiableAnnouncement> getReadVal() {
        return _readval;
    }

}