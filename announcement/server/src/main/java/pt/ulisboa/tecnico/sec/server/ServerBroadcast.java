package pt.ulisboa.tecnico.sec.server;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;

public class ServerBroadcast {

    public class ServerBroadcastData {
        VerifiableProtocolMessage _echo; // client message received by this server
        VerifiableServerMessage _sigma; // signed message sent by this server
        AtomicBoolean _sentEcho; // tells whether we already sent an echo to this server

        public ServerBroadcastData() {
            _sentEcho = new AtomicBoolean(false);
        }
    }

    // maps server public key -> received and sent data
    protected ConcurrentHashMap<PublicKey, ServerBroadcastData> _data;
    protected AtomicBoolean _delivered;
    protected VerifiableProtocolMessage _clientMessage;
    protected Server _server;

    private String _bcb; // prevent replay attacks
    private int _quorum;
    private AtomicBoolean _sentFinal;

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

    public void setBcb(String bcb) { _bcb = bcb; }

    public void localEcho() {
        System.out.println("localEcho");
        ServerBroadcastData sbd = new ServerBroadcastData();
        sbd._echo = _clientMessage;
        _data.put(_server.getPublicKey(), sbd);
    }

    public ServerMessage echo(ServerMessage sm) {
        System.out.println("echo");
        ServerBroadcastData sbd = _data.get(sm.getPublicKey());
        // first echo message received by this server
        if (sbd == null) {
            sbd = new ServerBroadcastData();
            sbd._echo = sm.getClientMessage();
            _data.put(sm.getPublicKey(), sbd);
        }
        // we already received a broadcast from this server, so we don't consider this message
        else {
            // TODO: repeated broadcast
            System.out.println("REPEATED BROADCAST");
            return null;
        }

        return new ServerMessage(_server.getPublicKey(), "ECHO", _clientMessage, sm.getBcb());
    }

    public void localReady() {
        System.out.println("localReady");
        ServerBroadcastData sbd = _data.get(_server.getPublicKey());

        List<VerifiableProtocolMessage> echos = new ArrayList<>();
        for(Map.Entry<PublicKey, ServerBroadcastData> entry : _data.entrySet()) {
            echos.add(entry.getValue()._echo);
        }
        if(echos.size() > _quorum) {
            VerifiableProtocolMessage vpmToDeliver = MessageComparator.compareClientMessages(echos, _quorum);
            if (vpmToDeliver != null)
                _server.deliver(vpmToDeliver);
            else
                _server.deliverFailed(_clientMessage.getProtocolMessage().getPublicKey());
        }
    }

    /**
     * Broadcaster server sends ready messages to other servers,
     * after checking if there isa quorum in the echos
     */
    public ServerMessage ready(VerifiableServerMessage vsm) {
        System.out.println("ready");
        ServerMessage sm = vsm.getServerMessage();

        // verify freshness with bcb
        if (!sm.getBcb().equals(_bcb)) {
            return null;
        }

        // adds echo
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
                List<VerifiableServerMessage> sigmas = new ArrayList<>();
                for (Map.Entry<PublicKey, ServerBroadcastData> v: _data.entrySet()) {
                    System.out.println("gathering all sigmas: " + v.getValue()._sigma);
                    sigmas.add(v.getValue()._sigma);
                }
                ServerMessage s = new ServerMessage(_server.getPublicKey(), "FINAL", vpm, _bcb);
                s.setSigma(ProtocolMessageConverter.objToByteArray(sigmas));
                return s; 
            }
        }
        return null;
    }

    public synchronized void finalDelivery(VerifiableServerMessage vsm) {
        System.out.println("final delivery");
        if (_delivered.get() == false) {
            ServerMessage sm = vsm.getServerMessage();
            List<VerifiableServerMessage> sigmas = (List<VerifiableServerMessage>)(ProtocolMessageConverter.byteArrayToObj(sm.getSigma()));

            int nSigVerified = 0;
            // TODO: deliverFailed should not happen with no byzantine servers
            for (VerifiableServerMessage sigma : sigmas) {
                //if (_server.verifyServerSignature(sigma).equals(StatusCode.OK))
                StatusCode sc = _server.verifyServerSignature(sigma);
                System.out.println("Verify server signature of sigmas status: " + sc);
                if (sc.equals(StatusCode.OK))
                    nSigVerified++;
            }
            if (nSigVerified > _quorum) {
                _delivered.getAndSet(true);
                VerifiableProtocolMessage vpmToDeliver = sm.getClientMessage();
                _server.deliver(vpmToDeliver);
            }
            else {
                _server.deliverFailed(_clientMessage.getProtocolMessage().getPublicKey());
            }
        }
    }

    public void setClientMessage(VerifiableProtocolMessage clientMessage) {
        _clientMessage = clientMessage;
    }

    public String operation() {
        return _clientMessage.getProtocolMessage().getCommand();
    }
}