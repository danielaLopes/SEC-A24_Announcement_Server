package pt.ulisboa.tecnico.sec.server;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private int _acks = 0;
    private int _rid = 0;
    private VerifiableProtocolMessage _vpm;
    private ClientMessageHandler _cmh;
    private String _token;
    private String _newToken;
    //List that contains (ts, value) for each response from server with PublicKey
    private Map<PublicKey, AbstractMap.SimpleEntry<Integer, Announcement>> _readList = new HashMap<PublicKey, AbstractMap.SimpleEntry<Integer, Announcement>>();

    public AtomicRegister1N(Server server, int nServers, VerifiableProtocolMessage vpm, ClientMessageHandler cmh, String token, String newToken) {
        _server = server;
        _nServers = nServers;
        _vpm = vpm;
        _cmh = cmh;
        _token = token;
        _newToken = newToken;
    }

    public void writeLocal(Announcement a) {
        _wts += 1;
        _acks = 0;
        if (_wts > _values.getKey())
            _values = new AbstractMap.SimpleEntry<Integer, Announcement>(_wts, a); 
        _acks += 1;
        //only happens if number of servers = 1
        if (_acks > _nServers/2) {
            _acks = 0;
            _server.deliverPost(_vpm, _cmh, _token, _newToken);
        }
    }
    
    public ServerMessage write(Announcement a, PublicKey serverPublicKey){
        ServerMessage sm = new ServerMessage(serverPublicKey, "WRITE", new AbstractMap.SimpleEntry<Integer, Announcement>(_wts, a));
        return sm;
    }

    public ServerMessage acknowledge(ServerMessage sm) {
        if(sm.getTimestamp() > _values.getKey()) {
            _values = new AbstractMap.SimpleEntry<Integer, Announcement>(sm.getTimestamp(), sm.getValue()); 
        }
        return new ServerMessage(sm.getPublicKey(), "ACK", _values);
    }

    public void writeReturn(ServerMessage sm) {
        _acks += 1;
        if (_acks > _nServers/2) {
            _acks = 0;
            _server.deliverPost(_vpm, _cmh, _token, _newToken);
        }
    }
}