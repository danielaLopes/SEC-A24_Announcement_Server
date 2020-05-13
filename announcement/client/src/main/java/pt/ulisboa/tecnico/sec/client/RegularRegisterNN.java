package pt.ulisboa.tecnico.sec.client;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import pt.ulisboa.tecnico.sec.communication_lib.*;


public class RegularRegisterNN {
    private AtomicValue _atomicValue;
    private int _rid;
    private int _wts;
    private AtomicInteger _acks;
    private ConcurrentHashMap<PublicKey, AtomicValue> _readList = new ConcurrentHashMap<>();
    private final int _quorum;

    private Client _client;

    public RegularRegisterNN(Client client) {
        _atomicValue = new AtomicValue(0, new ArrayList<VerifiableAnnouncement>());
        _acks = new AtomicInteger(0);
        _rid = 0;
        _wts = 0;
        _client = client;
        _readList.clear();
        _quorum = (_client._nServers + _client._nFaults) / 2;
    }

    public RegisterMessage read() {

        _rid += 1;
        _readList.clear();
        return new RegisterMessage(_rid);
    }

    public void readReturn(ProtocolMessage pm, List<VerifiableProtocolMessage> responses) {

        RegisterMessage registerMessage = new RegisterMessage(pm.getAtomicRegisterMessages());
        if(_rid == registerMessage.getRid()) {
            AtomicValue av = new AtomicValue(registerMessage.getWts(), registerMessage.getValues());

            synchronized(_readList) {

                _readList.put(pm.getPublicKey(), av);

                if (_readList.size() > _quorum) {
                    Map.Entry<StatusCode, List<VerifiableAnnouncement>> quorumMessages =
                            MessageComparator.compareServerResponses(responses, responses.size() / 2);

                    if (quorumMessages != null) {
                        AtomicValue highest = highest();
                        _readList.clear();

                        _client.deliverReadGeneral(quorumMessages.getKey(), quorumMessages.getValue());
                    }
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

    public ConcurrentHashMap<PublicKey, AtomicValue> getReadList() {
        return _readList;
    }

    public int getWts() {
        return _wts;
    }

}