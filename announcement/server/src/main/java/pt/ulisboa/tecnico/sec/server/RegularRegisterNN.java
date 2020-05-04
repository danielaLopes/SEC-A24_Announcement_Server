package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.Communication;
import pt.ulisboa.tecnico.sec.communication_lib.ServerMessage;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableProtocolMessage;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;

import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RegularRegisterNN {

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

    private PublicKey _clientPubKey;

    private String _token;
    private String _newToken;

    //List that contains (ts, value) for each response from server with PublicKey
    private ConcurrentHashMap<PublicKey, AbstractMap.SimpleEntry<Integer, List<Announcement>>> _readList = new ConcurrentHashMap<>();

    class RegularValue {
        int _ts;
        int _wr;
        List<Announcement> _val;

        RegularValue(int ts, int wr, List<Announcement> val) {
            _ts = ts;
            _wr = wr; // writer identifier
            _val = val;
        }

    }

    // when triggered by a server message on a serverThread
    //public AtomicRegister1N(Server server, int nServers, VerifiableProtocolMessage vpm) {
    public RegularRegisterNN(Server server, int nServers,  PublicKey clientPubKey) {
        _server = server;
        _nServers = nServers;
        //_vpm = vpm;
        _clientPubKey = clientPubKey;
    }

    // when triggered by a client message on the server
    public RegularRegisterNN(Server server, int nServers, VerifiableProtocolMessage vpm, ClientMessageHandler cmh, String token, String newToken) {
        _server = server;
        _nServers = nServers;
        _vpm = vpm;
        _cmh = cmh;
        _token = token;
        _newToken = newToken;
        _clientPubKey = vpm.getProtocolMessage().getPublicKey();
    }

    public void setClientInfo(ClientMessageHandler cmh, VerifiableProtocolMessage vpm, String token, String newToken) {
        _cmh = cmh;
        _token = token;
        _newToken = newToken;
        _vpm = vpm;
    }

    public void readLocal() {
        System.out.println("readLocal");
        _rid.incrementAndGet();
        _readList.clear();
        List<Announcement> a = _server.getUserAnnouncements(
                _vpm.getProtocolMessage().getPublicKey(), _vpm.getProtocolMessage().getReadNumberAnnouncements());
        _readList.put(_server.getPublicKey(), new AbstractMap.SimpleEntry<>(_values.getKey(), a));
    }

    public ServerMessage read() {
        System.out.println("read");
        //return new ServerMessage(_server.getPublicKey(), _vpm, "READ", _rid);
        return new ServerMessage(_server.getPublicKey(), _clientPubKey, "READ", _rid);
    }

    public ServerMessage value(ServerMessage sm) {
        System.out.println("value");
        List<Announcement> a = _server.getUserAnnouncements(
                _vpm.getProtocolMessage().getPublicKey(), _vpm.getProtocolMessage().getReadNumberAnnouncements());
        AbstractMap.SimpleEntry<Integer, List<Announcement>> readAnnouncements = new AbstractMap.SimpleEntry<>(_values.getKey(), a);
        //return new ServerMessage(_server.getPublicKey(), _vpm.getProtocolMessage().getPublicKey(), "VALUE", sm.getRid(), readAnnouncements);
        return new ServerMessage(_server.getPublicKey(), _clientPubKey, "VALUE", sm.getRid(), ProtocolMessageConverter.objToByteArray(readAnnouncements));
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
                //return new ServerMessage(_server.getPublicKey(), _vpm, "WRITE", sm.getRid(), ProtocolMessageConverter.objToByteArray(value));
                return new ServerMessage(_server.getPublicKey(), _clientPubKey, "WRITE", sm.getRid(), ProtocolMessageConverter.objToByteArray(value));
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
    }
}
