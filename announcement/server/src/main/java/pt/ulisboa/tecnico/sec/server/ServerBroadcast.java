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
    private int _quorumF;
    private int _quorum2F;
    private AtomicBoolean _sentReady;

    public ServerBroadcast(Server server, VerifiableProtocolMessage clientMessage) {
        _delivered = new AtomicBoolean(false);
        _server = server;
        _clientMessage = clientMessage;
        _data = new ConcurrentHashMap<>();
        _quorum = (_server._nServers + _server._nFaults) / 2;
        _quorumF = _server._nFaults;
        _quorum2F = 2 * _server._nFaults;
        _sentReady = new AtomicBoolean(false);
    }

    public ServerBroadcast(Server server) {
        _delivered = new AtomicBoolean(false);
        _server = server;
        _data = new ConcurrentHashMap<>();
        _quorum = (_server._nServers + _server._nFaults) / 2;
        _quorumF = _server._nFaults;
        _quorum2F = 2 * _server._nFaults;
        _sentReady = new AtomicBoolean(false);
    }

    public void setBcb(String bcb) { _bcb = bcb; }

    public void localEcho() {
        // System.out.println("localEcho");
        ServerBroadcastData sbd = new ServerBroadcastData();
        sbd._echo = _clientMessage;
        VerifiableServerMessage vsm = _server.createVerifiableServerMessage(new ServerMessage(_server.getPublicKey(), "ECHO", _clientMessage, _bcb));
        sbd._sigma = vsm;
        _data.put(_server.getPublicKey(), sbd);
    }

    public ServerMessage echo(ServerMessage sm) {
        // System.out.println("echo");
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
            // System.out.println("REPEATED BROADCAST");
            return null;
        }

        return new ServerMessage(_server.getPublicKey(), "ECHO", _clientMessage, sm.getBcb());
    }

    public void localReady() {
        // System.out.println("localReady");

        List<VerifiableProtocolMessage> echos = new ArrayList<>();
        for(Map.Entry<PublicKey, ServerBroadcastData> entry : _data.entrySet()) {
            echos.add(entry.getValue()._echo);
        }
        synchronized(_delivered) {
            System.out.println(echos.size());
            if(echos.size() > _quorum && _delivered.get() == false) {
                VerifiableProtocolMessage vpmToDeliver = MessageComparator.compareClientMessages(echos, _quorum);
                if (vpmToDeliver != null) {
                    _server.deliver(vpmToDeliver, _clientMessage);
                    _delivered.set(true);
                }
            }
        }
    }

    /**
     * Broadcaster server sends ready messages to other servers,
     * after checking if there isa quorum in the echos
     */
    public ServerMessage ready(VerifiableServerMessage vsm) {
        // System.out.println("ready");
        ServerMessage sm = vsm.getServerMessage();

        // verify freshness with bcb
        if (!sm.getBcb().equals(_bcb)) {
            return null;
        }

        // adds echo
        ServerBroadcastData sbd = _data.get(sm.getPublicKey());
        if (sbd != null) {
            sbd._echo = sm.getClientMessage();
            sbd._sigma = vsm;
        }
        
        List<VerifiableProtocolMessage> echos = new ArrayList<>();
        for(Map.Entry<PublicKey, ServerBroadcastData> entry : _data.entrySet()) {
            echos.add(entry.getValue()._echo);
        }
        
        if(echos.size() > _quorum) {
            VerifiableProtocolMessage vpm = MessageComparator.compareClientMessages(echos, _quorum);
            if (vpm != null && _sentReady.compareAndSet(false, true)) {
                List<VerifiableServerMessage> sigmas = new ArrayList<>();
                for (Map.Entry<PublicKey, ServerBroadcastData> v: _data.entrySet()) {
                    sigmas.add(v.getValue()._sigma);
                }
                ServerMessage s = new ServerMessage(_server.getPublicKey(), "FINAL", vpm, _bcb);
                s.setSigma(ProtocolMessageConverter.objToByteArray(sigmas));
                return s; 
            }
        }
        return null;
    }

    public void finalDelivery(VerifiableServerMessage vsm) {
        System.out.println("final delivery");
        
        //Amplification step
        synchronized(_sentReady) {
            if (_sentReady.get() == false) {
                ServerMessage sm = vsm.getServerMessage();
                List<VerifiableServerMessage> sigmas = (List<VerifiableServerMessage>)(ProtocolMessageConverter.byteArrayToObj(sm.getSigma()));

                int nReadys = 0;
                // TODO: deliverFailed should not happen with no byzantine servers
                for (VerifiableServerMessage sigma : sigmas) {
                    //if (_server.verifyServerSignature(sigma).equals(StatusCode.OK))
                    StatusCode sc = _server.verifyServerSignature(sigma);
                    if (sc.equals(StatusCode.OK) && sigma.getServerMessage().getClientMessage().equals(sm.getClientMessage()))
                        nReadys++;
                }
                if (nReadys > _quorumF) {
                    // System.out.println("Amplification step");
                    _sentReady.getAndSet(true);
                    ServerMessage s = new ServerMessage(_server.getPublicKey(), "FINAL", sm.getClientMessage(), _bcb);
                    s.setSigma(ProtocolMessageConverter.objToByteArray(sigmas));
                    _server.sendToAllServers(s); 
                }
            }
        }
        //End Amplification step

        synchronized(_delivered) {
            if (_delivered.get() == false) {
                ServerMessage sm = vsm.getServerMessage();
                List<VerifiableServerMessage> sigmas = (List<VerifiableServerMessage>)(ProtocolMessageConverter.byteArrayToObj(sm.getSigma()));

                int nSigVerified = 0;
                // TODO: deliverFailed should not happen with no byzantine servers
                for (VerifiableServerMessage sigma : sigmas) {
                    //if (_server.verifyServerSignature(sigma).equals(StatusCode.OK))
                    StatusCode sc = _server.verifyServerSignature(sigma);
                    if (sc.equals(StatusCode.OK) && sigma.getServerMessage().getClientMessage().equals(sm.getClientMessage()));
                        nSigVerified++;
                }
                System.out.println("sig funky " +  nSigVerified);
                System.out.println("quorum " + _quorum2F);
                if (nSigVerified > _quorum2F) {
                    _delivered.getAndSet(true);
                    VerifiableProtocolMessage vpmToDeliver = sm.getClientMessage();
                    _server.deliver(vpmToDeliver, _clientMessage);
                }
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