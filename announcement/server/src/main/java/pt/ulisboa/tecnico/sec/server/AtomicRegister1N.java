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
    private Communication _communication = new Communication();
    //Pair that contains (ts, value)
    private AbstractMap.SimpleEntry<Integer, List<Announcement>> _values = new AbstractMap.SimpleEntry<>(0, null);
    private Server _server;
    private int _nServers;
    private int _wts = 0;
    private AtomicInteger _acks = new AtomicInteger(0);
    private AtomicInteger _rid = new AtomicInteger(0);
    private VerifiableProtocolMessage _vpm;
    private ClientMessageHandler _cmh; // if it's null, then no request from the client has been received
    private String _token;
    private String _newToken;
    //List that contains (ts, value) for each response from server with PublicKey
    private ConcurrentHashMap<PublicKey, AbstractMap.SimpleEntry<Integer, List<Announcement>>> _readList = new ConcurrentHashMap<>();

    // atomic register only
    AtomicBoolean _reading = new AtomicBoolean(false);
    List<Announcement> _readVal = new ArrayList<>();

    // when triggered by a server message on a serverThread
    public AtomicRegister1N(Server server, int nServers, VerifiableProtocolMessage vpm) {
        _server = server;
        _nServers = nServers;
        _vpm = vpm;
    }

    // whenn triggered by a client message on the server
    public AtomicRegister1N(Server server, int nServers, VerifiableProtocolMessage vpm, ClientMessageHandler cmh, String token, String newToken) {
        _server = server;
        _nServers = nServers;
        _vpm = vpm;
        _cmh = cmh;
        _token = token;
        _newToken = newToken;
    }

    public void setClientMessageHandler(ClientMessageHandler cmh) {_cmh = cmh;}

    public void readLocal() {
        System.out.println("readLocal");
        _rid.incrementAndGet();
        _readList.clear();
        _reading.set(true);
        List<Announcement> a = _server.getUserAnnouncements(_vpm.getProtocolMessage().getPublicKey());
        _readList.put(_server.getPublicKey(), new AbstractMap.SimpleEntry<>(_values.getKey(), a));
    }

    public ServerMessage read() {
        System.out.println("read");
        return new ServerMessage(_server.getPublicKey(), _vpm, "READ", _rid);
    }

    public ServerMessage value(ServerMessage sm) {
        System.out.println("value");
        List<Announcement> a = _server.getUserAnnouncements(_vpm.getProtocolMessage().getPublicKey());
        AbstractMap.SimpleEntry<Integer, List<Announcement>> readAnnouncements = new AbstractMap.SimpleEntry<>(_values.getKey(), a);
        //return new ServerMessage(_server.getPublicKey(), _vpm.getProtocolMessage().getPublicKey(), "VALUE", sm.getRid(), readAnnouncements);
        return new ServerMessage(_server.getPublicKey(), _vpm, "VALUE", sm.getRid(), ProtocolMessageConverter.objToByteArray(readAnnouncements));
    }

    public ServerMessage readReturn(ServerMessage sm) {
        //System.out.println("readreturn" + " sm.getRid() " + sm.getRid() + " _rid " + _rid);
        System.out.println("readreturn");
        if (sm.getRid().get() == _rid.get()) {
            _readList.put(sm.getPublicKey(), (AbstractMap.SimpleEntry<Integer, List<Announcement>>)
                    ProtocolMessageConverter.byteArrayToObj(sm.getReadAnnouncements()));
            if(_readList.size() > _nServers / 2) {
                AbstractMap.SimpleEntry<Integer, List<Announcement>> value = highestValue();
                _readList.clear();
                // server has to increment its own ack
                _acks.set(1);
                return new ServerMessage(_server.getPublicKey(), _vpm, "WRITE", sm.getRid(), ProtocolMessageConverter.objToByteArray(value));
                //_server.deliverRead(_vpm, _cmh, _token, _newToken, value.getValue());
            }
        }
        return null;
    }

    public AbstractMap.SimpleEntry<Integer, List<Announcement>> highestValue() {
        int ts = -1;
        List<Announcement> highestValue = null;
        for(Map.Entry<PublicKey, AbstractMap.SimpleEntry<Integer, List<Announcement>>> r : _readList.entrySet()) {
            if (r.getValue().getKey() > ts) {
                highestValue = r.getValue().getValue();
                ts = r.getValue().getKey();
            }
        }
        return new AbstractMap.SimpleEntry<>(ts, highestValue);
    }

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
        ServerMessage sm = new ServerMessage(_server.getPublicKey(), _vpm, "WRITE", ProtocolMessageConverter.objToByteArray(values));
        return sm;
    }

    public ServerMessage acknowledge(ServerMessage sm) {
        System.out.println("acknowledge");
        AbstractMap.SimpleEntry<Integer, List<Announcement>> values = (AbstractMap.SimpleEntry<Integer, List<Announcement>>)ProtocolMessageConverter.byteArrayToObj(sm.getReadAnnouncements());
        if(values.getKey() > _values.getKey()) {
            _values = new AbstractMap.SimpleEntry<>(values.getKey(), values.getValue());
        }
        return new ServerMessage(_server.getPublicKey(), _vpm, "ACK", ProtocolMessageConverter.objToByteArray(_values));
        //return new ServerMessage(_server.getPublicKey(), null, "ACK", ProtocolMessageConverter.objToByteArray(_values));

    }

    public void deliver() {
        System.out.println("deliver reading: " + _reading.get());
        _acks.incrementAndGet();
        //System.out.println("_acks " + _acks.get());
        if (_acks.get() > _nServers/2) {
            System.out.println("enough acks to deliver " + _acks.get());
            _acks.set(0);
            if (_reading.get() == true) {
                _reading.set(false);
                //System.out.println("entrou");
                _server.deliverRead(_vpm, _cmh, _token, _newToken, _readVal);
            }
            else {
                _server.deliverPost(_vpm, _cmh, _token, _newToken);
            }
        }
    }
}