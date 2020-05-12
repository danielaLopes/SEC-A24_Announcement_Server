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

    // buffer of ready messages that arrive before the corresponding server's echo message
    // and need to be processed after that echo is received
    protected ConcurrentHashMap<PublicKey, VerifiableServerMessage> _pendingReadys = new ConcurrentHashMap<>();

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

    public List<VerifiableProtocolMessage> getEchos() {
        List<VerifiableProtocolMessage> echos = new ArrayList<>();
        for(Map.Entry<PublicKey, ServerBroadcastData> entry : _data.entrySet()) {
            if (entry.getValue()._echo != null) {
                echos.add(entry.getValue()._echo);
            }
        }
        return echos;
    }

    public List<VerifiableServerMessage> getSigmas() {
        List<VerifiableServerMessage> sigmas = new ArrayList<>();
        for(Map.Entry<PublicKey, ServerBroadcastData> entry : _data.entrySet()) {
            if (entry.getValue()._sigma != null)
                sigmas.add(entry.getValue()._sigma);
        }
        return sigmas;
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
        else if (sbd._echo == null) {
            sbd._echo = sm.getClientMessage();
        }
        else {
            // we already received a broadcast from this server, so we don't consider this message
            return null;
        }

        VerifiableServerMessage pendingReady = _pendingReadys.get(sm.getPublicKey());
        if (pendingReady != null) {
            ready(pendingReady);
            _pendingReadys.remove(sm.getPublicKey());
        }

        return new ServerMessage(_server.getPublicKey(), "ECHO", _clientMessage, sm.getBcb());
    }

    public void localReady() {
        /*synchronized(_sentReady) {
            if (_sentReady.get() == false) {
                List<VerifiableProtocolMessage> echos = getEchos();
                int nReadys = echos.size();
                if (nReadys > _quorumF) {
                    // System.out.println("Amplification step");
                    _sentReady.getAndSet(true);
                    System.out.println("_QUORUM: " + _quorum);
                    for (VerifiableProtocolMessage vpm : echos) {
                        System.out.println("echo post: " + vpm.getProtocolMessage().getPostAnnouncement().getAnnouncement());
                        System.out.println("echo cmd: " + vpm.getProtocolMessage().getCommand());
                    }
                    VerifiableProtocolMessage vpmToDeliver = MessageComparator.compareClientMessages(echos, nReadys/2);
                    if (vpmToDeliver == null) {
                        System.out.println("LOCAL DELIVER  FAILED");
                        _server.deliverFailed(_clientMessage);
                        return ;
                    }
                    ServerMessage s = new ServerMessage(_server.getPublicKey(), "FINAL", vpmToDeliver, _bcb);
                    List<VerifiableServerMessage> sigmas = getSigmas();
                    s.setSigma(ProtocolMessageConverter.objToByteArray(sigmas));
                    _server.sendToAllServers(s); 
                }
            }
        }
        //End Amplification step

        synchronized(_delivered) {
            if (_delivered.get() == false) {
                List<VerifiableServerMessage> sigmas = getSigmas();
                int nSigVerified = sigmas.size();

                System.out.println("sig funky " +  nSigVerified);
                //System.out.println("quorum " + _quorum2F);
                if (nSigVerified > _quorum2F) {
                    List<VerifiableProtocolMessage> echos = new ArrayList<>();
                    for(Map.Entry<PublicKey, ServerBroadcastData> entry : _data.entrySet()) {
                        if (entry.getValue()._echo != null) {
                            echos.add(entry.getValue()._echo);
                        }
                    }
                    System.out.println("ECHOS BEFORE COMPARATOR: " + echos.size());
                    VerifiableProtocolMessage vpmToDeliver = MessageComparator.compareClientMessages(echos, echos.size()/2);
                    if (vpmToDeliver == null) {
                        System.out.println("LOCAL DELIVER  FAILED");
                        _server.deliverFailed(_clientMessage);
                    }
                    else {
                        System.out.println("LOCAL DELIVER");
                        _server.deliver(vpmToDeliver, _clientMessage);
                        _delivered.getAndSet(true);
                    }
                }
            }
        }*/
        List<VerifiableProtocolMessage> echos = getEchos();

        synchronized(_delivered) {
            //System.out.println(echos.size());
            if(echos.size() > _quorum && _delivered.get() == false) {
                VerifiableProtocolMessage vpmToDeliver = MessageComparator.compareClientMessages(echos, echos.size()/2);
                if (vpmToDeliver != null) {
                    _server.deliver(vpmToDeliver, _clientMessage);
                    _delivered.set(true);
                }
            }
        }
    }

    /**
     * Broadcaster server sends ready messages to other servers,
     * after checking if there is a quorum in the echos
     */
    public ServerMessage ready(VerifiableServerMessage vsm) {
        // System.out.println("ready");
        ServerMessage sm = vsm.getServerMessage();

        // verify freshness with bcb
        if (!sm.getBcb().equals(_bcb)) {
            return null;
        }

        // adds sigmas
        ServerBroadcastData sbd = _data.get(sm.getPublicKey());
        // in this case, then a server got his echo message late and the
        // ready message arrived first
        if (sbd == null) {
            /*sbd = new ServerBroadcastData();
            sbd._sigma = vsm;
            _data.put(sm.getPublicKey(), sbd);*/
            _pendingReadys.put(vsm.getServerMessage().getPublicKey(), vsm);
            return null;
        }
        else if (sbd._sigma == null) {
            sbd._sigma = vsm;
        }
        else {
            // we already received a broadcast from this server, so we don't consider this message
            return null;
        }
        
        List<VerifiableProtocolMessage> echos = getEchos();
        
        if(echos.size() > _quorum) {
            VerifiableProtocolMessage vpm = MessageComparator.compareClientMessages(echos, echos.size()/2);
            if (vpm != null && _sentReady.compareAndSet(false, true)) {
                List<VerifiableServerMessage> sigmas = getSigmas();
                ServerMessage s = new ServerMessage(_server.getPublicKey(), "FINAL", vpm, _bcb);
                s.setSigma(ProtocolMessageConverter.objToByteArray(sigmas));
                return s; 
            }
        }
        return null;
    }

    public void finalDelivery(VerifiableServerMessage vsm) {
        //System.out.println("final delivery");
        ServerMessage sm = vsm.getServerMessage();
        VerifiableProtocolMessage finalClientMsg = sm.getClientMessage();
        //Amplification step
        synchronized(_sentReady) {
            if (_sentReady.get() == false) {

                List<VerifiableServerMessage> sigmas = getSigmas();

                int nReadys = 0;
                // we have to check if we have a majority of valid sigmas with the message that the server sent us
                for (VerifiableServerMessage sigma : sigmas) {
                    if (sigma != null) {
                        //if (_server.verifyServerSignature(sigma).equals(StatusCode.OK))
                        StatusCode sc = _server.verifyServerSignature(sigma);
                        if (sc.equals(StatusCode.OK) && sigma.getServerMessage().getClientMessage().equals(finalClientMsg))
                            nReadys++;
                    }
                }
                if (nReadys > _quorumF) {
                    _sentReady.getAndSet(true);
                    ServerMessage s = new ServerMessage(_server.getPublicKey(), "FINAL", finalClientMsg, _bcb);
                    s.setSigma(ProtocolMessageConverter.objToByteArray(sigmas));
                    _server.sendToAllServers(s); 
                }
            }
        }
        //End Amplification step

        synchronized(_delivered) {
            if (_delivered.get() == false) {
                List<VerifiableServerMessage> sigmas = getSigmas();

                int nSigVerified = 0;
                for (VerifiableServerMessage sigma : sigmas) {

                    // sigma can be null in case of having received echo messages but not ready
                    // messages from the same server
                    if (sigma != null ) {
                        StatusCode sc = _server.verifyServerSignature(sigma);

                        /*if (sc.equals(StatusCode.OK) && sigma.getServerMessage().getClientMessage().equals(sm.getClientMessage()) &&
                                receivedBcb.equals(otherBcb)) {*/
                        if (sc.equals(StatusCode.OK) && sigma.getServerMessage().getClientMessage().equals(finalClientMsg)) {
                            nSigVerified++;
                        }
                    }
                }

                System.out.println("sig FUNKY " +  nSigVerified);
                //System.out.println("quorum " + _quorum2F);
                if (nSigVerified > _quorum2F) {
                    List<VerifiableProtocolMessage> echos = getEchos();

                    System.out.println("ECHOS BEFORE COMPARATOR: " + echos.size());
                    VerifiableProtocolMessage vpmToDeliver = MessageComparator.compareClientMessages(echos, echos.size()/2);
                    if (vpmToDeliver == null) {
                        System.out.println("NON LOCAL DELIVER  FAILED");
                        _server.deliverFailed(_clientMessage);
                    }
                    else {
                        System.out.println("NON LOCAL DELIVER");
                        _server.deliver(vpmToDeliver, _clientMessage);
                        _delivered.getAndSet(true);
                    }
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