package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.Communication;
import pt.ulisboa.tecnico.sec.communication_lib.ServerMessage;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;

import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RegularRegisterNN {

    private Server _server;
    private int _nServers;
    private AtomicInteger _acks;
    private AtomicInteger _rid;

    //List that contains (ts, value) for each response from server with PublicKey
    private ConcurrentHashMap<PublicKey, RegularValue> _readList = new ConcurrentHashMap<>();

    // when triggered by a server message on a serverThread
    public RegularRegisterNN(Server server, int nServers) {
        _server = server;
        _nServers = nServers;
        this.init();
    }
    
    public void init() {
        _acks = new AtomicInteger(0);
        _rid = new AtomicInteger(0);
        _readList = new ConcurrentHashMap<>();
    }

    //Simulates sending message to myself
    public void readLocal(List<Announcement> myGeneralBoardAnnouncements) {
        System.out.println("readLocal");
        _rid.incrementAndGet();
        RegularValue myRegularValue = _readList.get(_server.getPublicKey());
        _readList.clear();
        _readList.put(_server.getPublicKey(), myRegularValue);
       }

    public ServerMessage read(PublicKey clientPublicKey) {
        System.out.println("read");
        return new ServerMessage(_server.getPublicKey(), clientPublicKey, "READGENERAL", _rid);
    }

    public ServerMessage value(ServerMessage sm) {
        System.out.println("value");
        ServerMessage ssm = new ServerMessage(_server.getPublicKey(), sm.getClientPubKey(), "VALUEGENERAL", sm.getRid());
        ssm.setRegularValue(ProtocolMessageConverter.objToByteArray(_readList.get(_server.getPublicKey())));
       return ssm;
    }

    public ServerMessage readReturn(ServerMessage sm) {
        //System.out.println("readreturn" + " sm.getRid() " + sm.getRid() + " _rid " + _rid);
        System.out.println("readreturn");
        if (sm.getRid().get() == _rid.get()) {
            _readList.put(sm.getPublicKey(), (RegularValue) ProtocolMessageConverter.byteArrayToObj(sm.getRegularValue()));
            if(_readList.size() > _nServers / 2) {
                List<Announcement> value = highestValue();
                _readList.clear();
                _server.deliverReadGeneral(value, sm.getClientPubKey());
            }
        }
        return null;
    }

    public List<Announcement> highestValue() {
        int ts = -1;
        List<Announcement> highestValue = null;
        for(Map.Entry<PublicKey, RegularValue> r : _readList.entrySet()) {
            if (r.getValue().getTimestamp() > ts) {
                highestValue = r.getValue().getAnnouncements();
                ts = r.getValue().getTimestamp();
            }
        }
        return highestValue;
    }
/*
    public void writeLocal(Announcement a) {
        System.out.println("writeLocal");
        _wts += 1;
        _acks.set(0);
        if (_wts > _values.getKey())
            _values = new AbstractMap.SimpleEntry<>(_wts, new ArrayList<>(Arrays.asList(a)));
        _acks.incrementAndGet();
        //only happens if number of servers = 1
        if (_acks.get() > _nServers/2) {
            _acks.set(0);
            _server.deliverPost(_vpm, _cmh, _token, _newToken);
        }
    }

    public ServerMessage write(Announcement a){
        System.out.println("write");
        AbstractMap.SimpleEntry<Integer, List<Announcement>> values = new AbstractMap.SimpleEntry<>(_wts, new ArrayList<>(Arrays.asList(a)));
        //ServerMessage sm = new ServerMessage(_server.getPublicKey(), _vpm, "WRITE", ProtocolMessageConverter.objToByteArray(values));
        ServerMessage sm = new ServerMessage(_server.getPublicKey(), _clientPubKey, "WRITE", ProtocolMessageConverter.objToByteArray(values));
        return sm;
    }

    public ServerMessage acknowledge(ServerMessage sm) {
        System.out.println("acknowledge");
        AbstractMap.SimpleEntry<Integer, List<Announcement>> values = (AbstractMap.SimpleEntry<Integer, List<Announcement>>)ProtocolMessageConverter.byteArrayToObj(sm.getReadAnnouncements());
        if(values.getKey() > _values.getKey()) {
            _values = new AbstractMap.SimpleEntry<>(values.getKey(), values.getValue());
        }
        //return new ServerMessage(_server.getPublicKey(), _vpm, "ACK", ProtocolMessageConverter.objToByteArray(_values));
        return new ServerMessage(_server.getPublicKey(), _clientPubKey, "ACK", ProtocolMessageConverter.objToByteArray(_values));
        //return new ServerMessage(_server.getPublicKey(), null, "ACK", ProtocolMessageConverter.objToByteArray(_values));

    }

    public void deliverReadGeneral() {
        _acks.incrementAndGet();
        System.out.println("deliverReadGeneral acks: " + _acks.get());
        //System.out.println("_acks " + _acks.get());
        if (_acks.getAndSet(0) > _nServers/2) {
            System.out.println("enough acks to deliver ");
            _server.deliverReadGeneral(_vpm, _cmh, _token, _newToken, highestValue().getValue());
        }
    }

    public void deliverPostGeneral() {
        _acks.incrementAndGet();
        System.out.println("deliverPostGeneral acks: " + _acks.get());
        //System.out.println("_acks " + _acks.get());
        if (_acks.getAndSet(0) > _nServers/2) {
            System.out.println("enough acks to deliver ");
            _server.deliverPostGeneral(_vpm, _cmh, _token, _newToken);
        }
    }*/
}
