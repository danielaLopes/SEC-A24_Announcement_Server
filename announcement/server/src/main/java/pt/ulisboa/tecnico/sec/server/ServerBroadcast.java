package pt.ulisboa.tecnico.sec.server;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import pt.ulisboa.tecnico.sec.communication_lib.MessageComparator;
import pt.ulisboa.tecnico.sec.communication_lib.ServerMessage;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableProtocolMessage;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableServerMessage;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;

public class ServerBroadcast {

    private int _quorum;
    private AtomicBoolean _sentFinal;

    public class ServerBroadcastData {
        VerifiableProtocolMessage _echo;
        VerifiableServerMessage _sigma;
        AtomicBoolean _sentEcho;

        public ServerBroadcastData() {
            _sentEcho = new AtomicBoolean(false);
        }
    }

    // maps server public key -> received and sent data
    protected ConcurrentHashMap<PublicKey, ServerBroadcastData> _data;
    protected AtomicBoolean _delivered;
    protected VerifiableProtocolMessage _clientMessage;
    protected Server _server;

    public ServerBroadcast(Server server, VerifiableProtocolMessage clientMessage) {
        _delivered = new AtomicBoolean(false);
        _server = server;
        _clientMessage = clientMessage;
        _data = new ConcurrentHashMap<>();
        _quorum = (_server._nServers + _server._nFaults) / 2;
        _sentFinal = new AtomicBoolean(false);
    }

    public ServerBroadcast(Server server) {
        _delivered = new AtomicBoolean(false);
        _server = server;
        _data = new ConcurrentHashMap<>();
        _quorum = (_server._nServers + _server._nFaults) / 2;
        _sentFinal = new AtomicBoolean(false);
    }

    public void localEcho() {
        System.out.println("localEcho");
        ServerBroadcastData sbd = new ServerBroadcastData();
        sbd._echo = _clientMessage;
        _data.put(_server.getPublicKey(), sbd);
    }

    public ServerMessage echo(ServerMessage sm) {
        System.out.println("echo");
        ServerBroadcastData sbd = _data.get(sm.getPublicKey());
        if (sbd == null)
            sbd = _data.put(sm.getPublicKey(), new ServerBroadcastData());
        else {
            // TODO: repeated broadcast
            return null;
        }

        return new ServerMessage(_server.getPublicKey(), "ECHO", _clientMessage);
    }

    public ServerMessage ready(VerifiableServerMessage vsm) {
        System.out.println("ready");
        ServerMessage sm = vsm.getServerMessage();
        ServerBroadcastData sbd = _data.get(sm.getPublicKey());
        if (sbd != null && sbd._echo == null) {
            sbd._echo = sm.getClientMessage();
            sbd._sigma = vsm;
        }
        
        List<VerifiableProtocolMessage> echos = new ArrayList<>();
        for(Map.Entry<PublicKey, ServerBroadcastData> entry : _data.entrySet()) {
            echos.add(entry.getValue()._echo);
        }
        
        if(echos.size() > _quorum) {
            VerifiableProtocolMessage vpm = MessageComparator.compareClientMessages(echos, _quorum);
            if (vpm != null && _sentFinal.compareAndSet(false, true)) {
                List<VerifiableServerMessage> sigma = new ArrayList<>();
                for (Map.Entry<PublicKey, ServerBroadcastData> v: _data.entrySet()) {
                    sigma.add(v.getValue()._sigma);
                }
                ServerMessage s = new ServerMessage(_server.getPublicKey(), "FINAL", vpm);
                s.setSigma(ProtocolMessageConverter.objToByteArray(sigma));
                return s; 
            }
        }
        return null;
    }

    /*public int consensusEchos() {
        int echos;
        switch(operation()) {
            case "POST":
                // echos = comparePostOperations();
                break;
            default:
                break;
        }
        return echos;
    }*/

    public void setClientMessage(VerifiableProtocolMessage clientMessage) {
        _clientMessage = clientMessage;
    }

    public String operation() {
        return _clientMessage.getProtocolMessage().getCommand();
    }
}