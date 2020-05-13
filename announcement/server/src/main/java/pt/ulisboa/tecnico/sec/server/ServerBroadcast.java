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

    protected AtomicBoolean _delivered;
    protected VerifiableProtocolMessage _clientMessage;
    protected Server _server;

    private String _bcb; // prevent replay attacks
    private int _quorum;
    private int _quorumF;
    private int _quorum2F;
    private AtomicBoolean _sentEcho = new AtomicBoolean(false);
    private AtomicBoolean _sentReady;

    protected ConcurrentHashMap<PublicKey, VerifiableProtocolMessage> _echos = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<PublicKey, VerifiableServerMessage> _sigmas = new ConcurrentHashMap<>();

    public ServerBroadcast(Server server, VerifiableProtocolMessage clientMessage) {
        _delivered = new AtomicBoolean(false);
        _server = server;
        _clientMessage = clientMessage;
        _quorum = (_server._nServers + _server._nFaults) / 2;
        _quorumF = _server._nFaults;
        _quorum2F = 2 * _server._nFaults;
        _sentReady = new AtomicBoolean(false);
    }

    public ServerBroadcast(Server server) {
        _delivered = new AtomicBoolean(false);
        _server = server;
        _quorum = (_server._nServers + _server._nFaults) / 2;
        _quorumF = _server._nFaults;
        _quorum2F = 2 * _server._nFaults;
        _sentReady = new AtomicBoolean(false);
    }

    public List<VerifiableProtocolMessage> getEchos() {
        List<VerifiableProtocolMessage> echos = new ArrayList<>();
        for(Map.Entry<PublicKey, VerifiableProtocolMessage> entry : _echos.entrySet()) {
            if (entry.getValue() != null) {
                echos.add(entry.getValue());
            }
        }
        return echos;
    }

    public List<VerifiableServerMessage> getSigmas() {
        List<VerifiableServerMessage> sigmas = new ArrayList<>();
        for(Map.Entry<PublicKey, VerifiableServerMessage> entry : _sigmas.entrySet()) {
            if (entry.getValue() != null)
                sigmas.add(entry.getValue());
        }
        return sigmas;
    }

    public void setBcb(String bcb) { _bcb = bcb; }

    public void localEcho() {
        localReady();
    }

    public synchronized ServerMessage echo(ServerMessage sm) {

        _sentEcho.compareAndSet(false, true);

        return new ServerMessage(_server.getPublicKey(), "ECHO", _clientMessage, sm.getBcb());
    }

    public synchronized void localReady() {

        ServerMessage sm = new ServerMessage(_server.getPublicKey(), "ECHO", _clientMessage, _bcb);
        VerifiableServerMessage vsm = _server.createVerifiableServerMessage(sm);

        _echos.put(sm.getPublicKey(), vsm.getServerMessage().getClientMessage());
    }


    /**
     * Broadcaster server sends ready messages to other servers,
     * after checking if there is a quorum in the echos
     */
    public synchronized ServerMessage ready(VerifiableServerMessage vsm) {
        //System.out.println("-----------ready");
        System.out.flush();
        ServerMessage sm = vsm.getServerMessage();
        
        // verify freshness with bcb
        if (!sm.getBcb().equals(_bcb)) {
            return null;
        }
        _echos.put(sm.getPublicKey(), vsm.getServerMessage().getClientMessage());
        
        List<VerifiableProtocolMessage> echos = getEchos();
        System.out.println("==========ECHOS SIZE: " + echos.size() + "quorum: " +  _quorum);
        System.out.flush();
        if(echos.size() > _quorum) {
            localFinalDelivery();
            if (_sentReady.compareAndSet(false, true)) {
                ServerMessage s = new ServerMessage(_server.getPublicKey(), "FINAL", _clientMessage, _bcb);
                return s; 
            }
        }
        return null;
    }

    public synchronized void localFinalDelivery() {
        System.out.flush();
        ServerMessage sm = new ServerMessage(_server.getPublicKey(), "FINAL", _clientMessage, _bcb);
        VerifiableServerMessage vsm = _server.createVerifiableServerMessage(sm);

        _sigmas.put(_server.getPublicKey(), vsm);

        //Amplification step
        if (_sentReady.get() == false) {

            List<VerifiableServerMessage> sigmas = getSigmas();

            int nReadys = 0;
            // we have to check if we have a majority of valid sigmas with the message that the server sent us
            for (VerifiableServerMessage sigma : sigmas) {
                    StatusCode sc = _server.verifyServerSignature(sigma);
                    if (sc.equals(StatusCode.OK))
                        nReadys++;
            }
            if (nReadys > _quorumF) {
                _sentReady.getAndSet(true);
                ServerMessage s = new ServerMessage(_server.getPublicKey(), "FINAL", _clientMessage, _bcb);
                _server.sendToAllServers(s); 
            }
        }
        //End Amplification step

        if (_delivered.get() == false) {
            List<VerifiableServerMessage> sigmas = getSigmas();

            int nSigVerified = 0;
            for (VerifiableServerMessage sigma : sigmas) {

                // sigma can be null in case of having received echo messages but not ready
                // messages from the same server
                    StatusCode sc = _server.verifyServerSignature(sigma);
                    if (sc.equals(StatusCode.OK)) {
                        nSigVerified++;
                    }
            }

            System.out.flush();
            if (nSigVerified > _quorum2F) {
                List<VerifiableProtocolMessage> echos = getEchos();

                //System.out.println("ECHOS BEFORE COMPARATOR: " + echos.size());
                VerifiableProtocolMessage vpmToDeliver = MessageComparator.compareClientMessages(echos, echos.size()/2);
                if (vpmToDeliver == null) {
                    _server.deliverFailed(_clientMessage);
                }
                else {
                    _server.deliver(vpmToDeliver, _clientMessage);
                    _delivered.getAndSet(true);
                }
            }
        }
    }

    public synchronized void finalDelivery(VerifiableServerMessage vsm) {

        ServerMessage sm = vsm.getServerMessage();
        VerifiableProtocolMessage finalClientMsg = sm.getClientMessage();
        
        if(!_sigmas.containsKey(sm.getPublicKey())) {
            _sigmas.put(sm.getPublicKey(), vsm);
        }

        // Amplification step
        if (_sentReady.get() == false) {

            List<VerifiableServerMessage> sigmas = getSigmas();

            int nReadys = 0;
            // we have to check if we have a majority of valid sigmas with the message that the server sent us
            for (VerifiableServerMessage sigma : sigmas) {
                    //if (_server.verifyServerSignature(sigma).equals(StatusCode.OK))
                    StatusCode sc = _server.verifyServerSignature(sigma);
                    if (sc.equals(StatusCode.OK))
                        nReadys++;
            }
            if (nReadys > _quorumF) {
                _sentReady.getAndSet(true);
                ServerMessage s = new ServerMessage(_server.getPublicKey(), "FINAL", finalClientMsg, _bcb);
                _server.sendToAllServers(s); 
            }
        }
        // End Amplification step

    
        if (_delivered.get() == false) {
            List<VerifiableServerMessage> sigmas = getSigmas();

            int nSigVerified = 0;
            for (VerifiableServerMessage sigma : sigmas) {

                // sigma can be null in case of having received echo messages but not ready
                // messages from the same server
                    StatusCode sc = _server.verifyServerSignature(sigma);
                    if (sc.equals(StatusCode.OK)) {
                        nSigVerified++;
                    }
            }

            if (nSigVerified > _quorum2F) {
                List<VerifiableProtocolMessage> echos = getEchos();

                VerifiableProtocolMessage vpmToDeliver = MessageComparator.compareClientMessages(echos, echos.size()/2);
                if (vpmToDeliver == null) {
                    _server.deliverFailed(_clientMessage);
                }
                else {
                    _server.deliver(vpmToDeliver, _clientMessage);
                    _delivered.getAndSet(true);
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