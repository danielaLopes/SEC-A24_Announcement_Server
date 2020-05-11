package pt.ulisboa.tecnico.sec.client;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.RegisterMessage;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;
import pt.ulisboa.tecnico.sec.communication_lib.ProtocolMessage;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;

public class RegularRegisterNN {
    private AtomicValue _atomicValue;
    private int _rid;
    private int _wts;
    private AtomicInteger _acks;
    private HashMap<PublicKey, AtomicValue> _readList = new HashMap<>();

    private Client _client;

    public RegularRegisterNN(Client client) {
        _atomicValue = new AtomicValue(0, new ArrayList<Announcement>());
        _acks = new AtomicInteger(0);
        _rid = 0;
        _wts = 0;
        _client = client;
        _readList.clear();
    }

    public RegisterMessage read() {
        //System.out.println("read");
        _rid += 1;
        _readList.clear();
        return new RegisterMessage(_rid);
    }

    public void readReturn(ProtocolMessage pm) {
        //System.out.println("readreturn");
        RegisterMessage registerMessage = new RegisterMessage(pm.getAtomicRegisterMessages());
        if(_rid == registerMessage.getRid()) {
            AtomicValue av = new AtomicValue(registerMessage.getWts(), registerMessage.getValues());

            synchronized(_readList) {
                // print _readList before
                System.out.println("Print _readList before");
                for (AtomicValue val : _readList.values()) {
                    System.out.println("val before " + val);
                }
                System.out.flush();

                _readList.put(pm.getPublicKey(), av);

                // print _readList after
                System.out.println("Print _readList after");
                for (AtomicValue val : _readList.values()) {
                    System.out.println("val after " + val);
                }
                System.out.flush();

                if (_readList.size() > _client._nServers / 2) {
                    AtomicValue highest = highest();
                    _readList.clear();
                    _client.deliverReadGeneral(StatusCode.OK, highest.getValues());
                }
            }
        }
    }

    public AtomicValue highest() {
        int ts = -1;
        AtomicValue highestValue = new AtomicValue(-1, new ArrayList<>());
        for(Map.Entry<PublicKey, AtomicValue> r : _readList.entrySet()) {
            if (r.getValue().getTimeStamp() > ts) {
                highestValue = r.getValue();
                ts = highestValue.getTimeStamp();
            }
        }
        return highestValue;
    }

    public void write() {
        //System.out.println("write");
        _wts += 1;
        _acks.set(0);
    }

    public void writeReturn(RegisterMessage arm) {
        if (arm.getWts() == _wts) {
            _acks.incrementAndGet();
            synchronized(_acks) {
                if (_acks.get() > _client._nServers / 2) {
                    _acks.set(0);
                    _client.deliverPostGeneral(StatusCode.OK);
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

    public int getWts() {
        return _wts;
    }

}