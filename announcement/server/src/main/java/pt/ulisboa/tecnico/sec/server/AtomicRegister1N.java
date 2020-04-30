package pt.ulisboa.tecnico.sec.server;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.mysql.cj.protocol.Protocol;

import java.io.IOException;
import java.security.*;

import pt.ulisboa.tecnico.sec.communication_lib.*;

public class AtomicRegister1N {
    private Communication _communication = new Communication();
    //Pair that contains (ts, value)
    private AbstractMap.SimpleEntry<Integer, Announcement> _values = new AbstractMap.SimpleEntry<Integer, Announcement>(0, null);
    private Server _server;
    private int _nServers;
    private int _wts = 0;
    private AtomicInteger _acks = new AtomicInteger(0);
    private AtomicInteger _rid = new AtomicInteger(0);
    private VerifiableProtocolMessage _vpm;
    private ClientMessageHandler _cmh;
    private String _token;
    private String _newToken;
    //List that contains (ts, value) for each response from server with PublicKey
    private ConcurrentHashMap<PublicKey, AbstractMap.SimpleEntry<Integer, List<Announcement>>> _readList = new ConcurrentHashMap<PublicKey, AbstractMap.SimpleEntry<Integer, List<Announcement>>>();

    public AtomicRegister1N(Server server, int nServers, VerifiableProtocolMessage vpm, ClientMessageHandler cmh, String token, String newToken) {
        _server = server;
        _nServers = nServers;
        _vpm = vpm;
        _cmh = cmh;
        _token = token;
        _newToken = newToken;
    }

    public void readLocal() {
        System.out.println("readLocal");
        _rid.incrementAndGet();
        _readList.clear();
    }

    public ServerMessage read() {
        System.out.println("read");
        return new ServerMessage(_server.getPublicKey(), _vpm.getProtocolMessage().getPublicKey(), "READ", _rid);
    }

    public ServerMessage value(ServerMessage sm) {
        System.out.println("value");
        List<Announcement> a = _server.getUserAnnouncements(_vpm.getProtocolMessage().getPublicKey());
        return new ServerMessage(_server.getPublicKey(), _vpm.getProtocolMessage().getPublicKey(), "VALUE", sm.getRid(), a);
    }

    public void readReturn(ServerMessage sm) {
        if (sm.getRid().equals(_rid)) {
            _readList.put(sm.getPublicKey(), new AbstractMap.SimpleEntry<>(sm.getTimestamp(), sm.getAnnouncements()));
            if(_readList.size() > _nServers / 2) {
                List<Announcement> value = highestValue();
                _readList.clear();
                _server.deliverRead(_vpm, _cmh, _token, _newToken, value);
            }
        }
    }

    public List<Announcement> highestValue() {
        int ts = -1;
        List<Announcement> highestValue = null;
        for(Map.Entry<PublicKey, AbstractMap.SimpleEntry<Integer, List<Announcement>>> r : _readList.entrySet()) {
            if (r.getValue().getKey() > ts) {
                highestValue = r.getValue().getValue();
                ts = r.getValue().getKey();
            }
        }
        return highestValue;
    }

    public void writeLocal(Announcement a) {
        System.out.println("writeLocal");
        _wts += 1;
        _acks.set(0);
        if (_wts > _values.getKey())
            _values = new AbstractMap.SimpleEntry<Integer, Announcement>(_wts, a); 
        _acks.incrementAndGet();
        //only happens if number of servers = 1
        if (_acks.get() > _nServers/2) {
            _acks.set(0);
            _server.deliverPost(_vpm, _cmh, _token, _newToken);
        }
    }
    
    public ServerMessage write(Announcement a){
        System.out.println("write");
        ServerMessage sm = new ServerMessage(_server.getPublicKey(), _vpm.getProtocolMessage().getPublicKey(), "WRITE", new AbstractMap.SimpleEntry<Integer, Announcement>(_wts, a));
        return sm;
    }

    public ServerMessage acknowledge(ServerMessage sm) {
        System.out.println("ack");
        if(sm.getTimestamp() > _values.getKey()) {
            _values = new AbstractMap.SimpleEntry<Integer, Announcement>(sm.getTimestamp(), sm.getValue()); 
        }
        return new ServerMessage(_server.getPublicKey(), _vpm.getProtocolMessage().getPublicKey(), "ACK", _values);
    }

    public void writeReturn(ServerMessage sm) {
        System.out.println("writeReturn");
        _acks.incrementAndGet();
        if (_acks.get() > _nServers/2) {
            _acks.set(0);
            _server.deliverPost(_vpm, _cmh, _token, _newToken);
        }
    }
}