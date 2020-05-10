package pt.ulisboa.tecnico.sec.server;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import pt.ulisboa.tecnico.sec.communication_lib.ServerMessage;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableProtocolMessage;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableServerMessage;

public class ServerBroadcast {

    public class ServerBroadcastData {
        VerifiableProtocolMessage _echo;
        VerifiableProtocolMessage _ready;
        AtomicBoolean _sentEcho;
        AtomicBoolean _sentReady;

        public ServerBroadcastData() {
            _sentEcho = new AtomicBoolean(false);
            _sentReady = new AtomicBoolean(false);
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
    }

    public ServerBroadcast(Server server) {
        _delivered = new AtomicBoolean(false);
        _server = server;
        _data = new ConcurrentHashMap<>();
    }

    public void localEcho() {
        System.out.println("localEcho");
        ServerBroadcastData sbd = new ServerBroadcastData();
        sbd._echo = _clientMessage;
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

    public ServerMessage ready(ServerMessage sm) {
        ServerBroadcastData sbd = _data.get(sm.getPublicKey());
        if (sbd != null && sbd._echo != null)
            sbd._echo = sm.getClientMessage();
        return null;
    }

    public int consensusEchos() {
        // Map<Announcement, Integer> echos; 
        // for (Map.Entry<PublicKey, ServerBroadcastData> entry : _data.entrySet()) {

        // }
    }

    public void setClientMessage(VerifiableProtocolMessage clientMessage) {
        _clientMessage = clientMessage;
    }    
}