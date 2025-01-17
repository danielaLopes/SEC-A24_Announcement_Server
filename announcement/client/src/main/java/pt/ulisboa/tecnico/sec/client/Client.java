package pt.ulisboa.tecnico.sec.client;

import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyPairUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyStorage;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;
import pt.ulisboa.tecnico.sec.crypto_lib.SignatureUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.UUIDGenerator;

import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Client {

    // testing purposes only
    public boolean postDelivered = false;
    public boolean postGeneralDelivered = false;
    public boolean readDelivered = false;
    public boolean readGeneralDelivered = false;

    public StatusCode postDeliveredSC;
    public StatusCode postGeneralDeliveredSC;
    public StatusCode readDeliveredSC;
    public StatusCode readGeneralDeliveredSC;

    protected PublicKey _pubKey;
    private PrivateKey _privateKey;

    protected List<PublicKey> _usersPubKeys;

    protected final Integer _startingServerPort = 8001;

    protected final String _serverPubKeyPrefix = "../server/src/main/resources/crypto/public";
    protected final String _serverPubKeySufix = ".key";

    protected final int _nServers;
    protected final int _nFaults;
    protected final int _quorum;

    // maps server public keys to the corresponding serverIndex to access for _oos,_ois or _clientSockets
    private ConcurrentHashMap<PublicKey, CommunicationServer> _serverCommunications;

    private ClientUI _clientUI;

    protected final Communication _communication;

    private AtomicRegister1N _atomicRegister1N;
    private RegularRegisterNN _regularRegisterNN;
    
    protected PublicKey _readingUserPubKey;

    protected List<PublicKey> _serversPubKeys;

    // responses received for current request
    private ConcurrentMap<PublicKey, VerifiableProtocolMessage> _responses = new ConcurrentHashMap<>();

    protected static final int TIMEOUT = 5000;

    public Client(String pubKeyPath, String keyStorePath,
                  String keyStorePasswd, String entryPasswd, String alias,
                  int nServers, int nFaults, List<String> otherUsersPubKeyPaths, ClientUI clientUI) {
        loadPublicKey(pubKeyPath);
        loadPrivateKey(keyStorePath, keyStorePasswd, entryPasswd, alias);

        _nServers = nServers;
        _nFaults = nFaults;
        _quorum = (nServers + nFaults) / 2;

        _serverCommunications = new ConcurrentHashMap<>();
        _serversPubKeys = loadServersGroupPublicKeys();

        _usersPubKeys = new ArrayList<>();
        loadUsersPubKeys(otherUsersPubKeyPaths);

        _communication = new Communication();

        startServersGroupCommunication();

        _atomicRegister1N = new AtomicRegister1N(this);
        _regularRegisterNN  = new RegularRegisterNN(this);
        
        _clientUI = clientUI;
    }

    public Client(String pubKeyPath, String keyStorePath,
                  String keyStorePasswd, String entryPasswd, String alias, int nServers, int nFaults) {
        loadPublicKey(pubKeyPath);
        loadPrivateKey(keyStorePath, keyStorePasswd, entryPasswd, alias);

        _nServers = nServers;
        _nFaults = nFaults;
        _quorum = (nServers + nFaults) / 2;

        _serverCommunications = new ConcurrentHashMap<>();
        _serversPubKeys = loadServersGroupPublicKeys();

        _communication = new Communication();

        startServersGroupCommunication();

        _atomicRegister1N = new AtomicRegister1N(this);
        _regularRegisterNN  = new RegularRegisterNN(this);
    }

    public AtomicRegister1N getAtomicRegister1N() { return _atomicRegister1N; }

    public RegularRegisterNN getRegularRegisterNN() { return _regularRegisterNN; }

    public Map<PublicKey, CommunicationServer> getServerCommunications() { return _serverCommunications; }

    public ConcurrentMap<PublicKey, VerifiableProtocolMessage> getServerResponses() { return _responses; }

    public void resetResponses() {
        _responses.clear();
    }

    /**
     * Loads Client's public key to _pubKey.
     */
    public void loadPublicKey(String pubKeyPath) {
        if (pubKeyPath == null) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load public key.\n");
            System.exit(-1);
        }
        try {
            _pubKey = KeyPairUtil.loadPublicKey(pubKeyPath);
        } catch (Exception e) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load public key.\n" + e);
            System.exit(-1);
        }
    }

    /**
     * Loads Client's private key to _privateKey.
     */
    public void loadPrivateKey(String keyStorePath, String keyStorePasswd, String entryPasswd, String alias) {
        if (keyStorePath == null || keyStorePasswd == null || entryPasswd == null || alias == null) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load keystore.\n");
            System.exit(-1);
        }

        KeyStore keyStore = null;
        try {
            keyStore = KeyStorage.loadKeyStore(keyStorePasswd.toCharArray(), keyStorePath);
        } catch(Exception e) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load keystore.\n" + e);
            System.exit(-1);
        }
        try {
            _privateKey = KeyStorage.loadPrivateKey(entryPasswd.toCharArray(), alias, keyStore);
        } catch (Exception e) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load private key.\n" + e);
            System.exit(-1);
        }
        if (_privateKey == null) {
            System.out.println("Error: Not possible to initialize server because it " +
                    "was not possible to load private key due to wrong password or alias.\n");
            System.exit(-1);
        }
    }

    public List<PublicKey> loadServersGroupPublicKeys() {
        List<PublicKey> serverPublicKeys = new ArrayList<>();
        for (int i = 1; i <= _nServers; i++) {
            serverPublicKeys.add(loadServerPublicKey(_serverPubKeyPrefix + i + _serverPubKeySufix));
        }
        return serverPublicKeys;
    }

    /**
     * Loads server's public key to _serverPubKey.
     */
    public PublicKey loadServerPublicKey(String path) {
        PublicKey serverPubKey = null;
        if (path == null) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load server's public key.\n");
            System.exit(-1);
        }

        try {
            serverPubKey = KeyPairUtil.loadPublicKey(path);

        } catch (Exception e) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load server's public key.\n" + e);
            System.exit(-1);
        }
        return serverPubKey;
    }

    /**
     * Loads other known user's public keys to _usersPubKeys.
     */
    public void loadUsersPubKeys(List<String> paths) {
        if (paths == null) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load other users public keys.\n");
            System.exit(-1);
        }
        _usersPubKeys.add(_pubKey);
        for (String path : paths) {
            try {
                _usersPubKeys.add(KeyPairUtil.loadPublicKey(path));
            } catch (Exception e) {
                System.out.println("Error: Not possible to initialize client because it was not possible to load other users public keys.\n" + e);
                System.exit(-1);
            }
        }
    }

    /**
     * Prints other known user's public keys.
     */
    public void printOtherUsersPubKeys() {
        for (int i = 0; i < _usersPubKeys.size(); i++) {
            System.out.println("* " + i + ": " + _usersPubKeys.get(i).toString().substring(0, 120) + "...");
        }
    }

    public List<PublicKey> getServersGroupPubKeys() {
        return new ArrayList<>(_serverCommunications.keySet());
    }

    /**
     * @return the server's public key
     */
    /*public PublicKey getServerPubKey(int serverIndex) {
        for ()
        return _serverPubKeys.get(serverIndex);
    }*/

    /**
     * @return the client's public key
     */
    public PublicKey getPubKey() {
        return _pubKey;
    }

    /**
     * Retrieves the public key of a specific user.
     * @param userIndex describes the index of a user in _usersPubKeys
     */
    public PublicKey getPublicKeyFromUser(int userIndex) {
        return _usersPubKeys.get(userIndex);
    }

    public CommunicationServer createServerCommunication(int port, PublicKey serverPubKey) throws IOException {

        Socket socket = new Socket("localhost", port);
        socket.setSoTimeout(TIMEOUT);

        return new CommunicationServer(
                port, new ObjectOutputStream(socket.getOutputStream()),
                new ObjectInputStream(socket.getInputStream()), socket, serverPubKey);
    }

    /**
     * Starts the communication with the group of active servers for future operations.
     */
    public void startServersGroupCommunication() {
        try {
            int serverIndex = 0;
            for (PublicKey serverPubKey : _serversPubKeys) {
                int port = _startingServerPort + serverIndex++;
                System.out.println("(INFO) Trying connection with server at port " + port);

                _serverCommunications.put(serverPubKey, createServerCommunication(port, serverPubKey));
            }
            registerServersGroup();
        }
        catch(IOException e) {
            System.out.println("(INFO) Error starting client socket. Make sure the server is running.");
        }
    }

    /**
     * Closes the communication with the group of active servers.
     */
    public void closeGroupCommunication() {
        for (CommunicationServer serverCommunication : _serverCommunications.values()) {
            closeCommunication(serverCommunication);
        }
    }

    /**
     * Closes the communication with the server.
     */
    public void closeCommunication(CommunicationServer serverCommunication) {
        try {
            ProtocolMessage pm = new ProtocolMessage("LOGOUT", _pubKey);
            VerifiableProtocolMessage vpm = createVerifiableMessage(pm);
            _communication.sendMessage(vpm, serverCommunication.getObjOutStream());
            _communication.close(serverCommunication.getClientSocket());
        }
        catch(IOException e) {
            System.out.println("Error closing socket.");
        }
    }

    /**
     * Prints a status code. Can be mostly used to analyze the responses
     * given by the server.
     */
    public void printStatusCode(String command, StatusCode sc) {
        if (sc == null) return;

        System.out.println("====== [" + command + "] Status Code: " + sc + "======");
    }

    /**
     * Creates a VerifiableProtocolMessage with pm and signed pm, in order
     * to avoid messages being tampered.
     * @param pm is a ProtocolMessage
     * @return the VerificableProtocolMessage to be sent
     */
    public VerifiableProtocolMessage createVerifiableMessage(ProtocolMessage pm) {
        if (pm == null) return null;

        try {
            byte[] bpm = ProtocolMessageConverter.objToByteArray(pm);
            byte[] signedpm = SignatureUtil.sign(bpm, _privateKey);
            return new VerifiableProtocolMessage(pm, signedpm);
        }
        catch(NoSuchAlgorithmException | InvalidKeyException | SignatureException e) { 
            System.out.println(e);
        }
        return null;
    }

    public VerifiableAnnouncement createVerifiableAnnouncement(Announcement a) {
        if (a == null) return null;

        try {
            byte[] bpm = ProtocolMessageConverter.objToByteArray(a);
            byte[] signedpm = SignatureUtil.sign(bpm, _privateKey);
            return new VerifiableAnnouncement(a, signedpm);
        }
        catch(NoSuchAlgorithmException | InvalidKeyException | SignatureException e) { 
            System.out.println(e);
        }
        return null;
    }

    /**
     * Verifies if the signed ProtocolMessage inside vpm is valid, i.e.
     * if we sign the ProtocolMessage inside vpm it should be equivalent
     * to its signed instance.
     * @param vpm is a VerifiableProtocolMessage
     * @return a boolean, expressing if the signature is valid or not
     */
    public boolean verifySignature(VerifiableProtocolMessage vpm, PublicKey serverPubKey) {
        if (vpm == null) return false;
        try {
            byte[] bpm = ProtocolMessageConverter.objToByteArray(vpm.getProtocolMessage());
            return SignatureUtil.verifySignature(vpm.getSignedProtocolMessage(), serverPubKey, bpm);
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("Error: Algorithm used to verify signature is not valid.\n" + e);
        }
        catch (InvalidKeyException e) {
            System.out.println(StatusCode.INVALID_KEY + "\n" + e);
        } 
        catch (SignatureException e) {
            System.out.println(StatusCode.INVALID_SIGNATURE + "\n" + e);
        }
        return false;
    }

    public boolean verifySignature(VerifiableAnnouncement vpm, PublicKey serverPubKey) {
        if (vpm == null) return false;
        try {
            byte[] bpm = ProtocolMessageConverter.objToByteArray(vpm.getAnnouncement());
            return SignatureUtil.verifySignature(vpm.getSignedAnnouncement(), serverPubKey, bpm);
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("Error: Algorithm used to verify signature is not valid.\n" + e);
        }
        catch (InvalidKeyException e) {
            System.out.println(StatusCode.INVALID_KEY + "\n" + e);
        } 
        catch (SignatureException e) {
            System.out.println(StatusCode.INVALID_SIGNATURE + "\n" + e);
        }
        return false;
    }

    /**
     * Retrieves the StatusCode of a VerifiableProtocolMessage.
     * @param vpm is a VerifiableProtocolMessage
     * @return a StatusCode
     */
    public StatusCode getStatusCodeFromVPM(VerifiableProtocolMessage vpm) {
        if (vpm == null) return null;
        return vpm.getProtocolMessage().getStatusCode();
    }

    public PublicKey getServerPublicKeyFromVPM(VerifiableProtocolMessage vpm) {
        if (vpm == null) return null;
        return vpm.getProtocolMessage().getPublicKey();
    }

    /**
     * Retrieves the list of announcements of a VerifiableProtocolMessage.
     * @param vpm is a VerifiableProtocolMessage
     * @return list of announcements
     */
    public List<Announcement> getAnnouncementsFromVPM(VerifiableProtocolMessage vpm) {
        if (vpm == null) return null;
        return vpm.getProtocolMessage().getAnnouncements();
    }

    public String getTokenFromVPM(VerifiableProtocolMessage vpm) {
        if (vpm == null) return null;
        return vpm.getProtocolMessage().getToken();
    }

    public String getOldTokenFromVPM(VerifiableProtocolMessage vpm) {
        if (vpm == null) return null;
        return vpm.getProtocolMessage().getOldToken();
    }

    /**
     * Makes a request to the group of active Servers to register and to obtain tokens,
     * receiving a list of responses as VerifiableProtocolMessage's
     * @param pm is a ProtocolMessage to be sent to all servers in register phase, when there's no token associated.
     * @return the servers' responses
     */
    public Map<PublicKey, VerifiableProtocolMessage> requestServersGroupRegister(ProtocolMessage pm) {

        Map<PublicKey, VerifiableProtocolMessage> responses = new ConcurrentHashMap<>();
        for (Map.Entry<PublicKey, CommunicationServer> entry : _serverCommunications.entrySet()) {
            VerifiableProtocolMessage response = requestServer(pm, entry.getValue());
            if (response != null)
                responses.put(entry.getKey(), response);
        }

        return responses;
    }

    public void deliverPost(StatusCode sc) {

        resetResponses();
        postDelivered = true;
        postDeliveredSC = sc;
        if (_clientUI != null)
            _clientUI.deliverPost(sc);
        
    }

    public void deliverPostGeneral(StatusCode sc) {

        resetResponses();
        postGeneralDelivered = true;
        postGeneralDeliveredSC = sc;  
        if (_clientUI != null)
            _clientUI.deliverPostGeneral(sc);  
    }

    public void deliverRead(StatusCode sc, List<VerifiableAnnouncement> vas) {

        resetResponses();
        readDelivered = true;
        readDeliveredSC = sc;
        List<Announcement> announcements = new ArrayList<Announcement>();
        for (VerifiableAnnouncement va : vas) {
            if(verifySignature(va, _readingUserPubKey))
                announcements.add(va.getAnnouncement());
        }
        if (_clientUI != null)
            _clientUI.deliverRead(sc, announcements);
    }

    public void deliverReadGeneral(StatusCode sc, List<VerifiableAnnouncement> vas) {

        List<Announcement> announcements = new ArrayList<Announcement>();
        for (VerifiableAnnouncement va : vas) {
            if(verifySignature(va, va.getAnnouncement().getClientPublicKey()))
                announcements.add(va.getAnnouncement());
        }

        resetResponses();
        readGeneralDelivered = true;
        readGeneralDeliveredSC = sc;
        if (_clientUI != null) {
            _clientUI.deliverReadGeneral(sc, announcements);
        }
    }

    public void write(Map<PublicKey, ProtocolMessage> pms, boolean general) {

        for (Map.Entry<PublicKey, ProtocolMessage> pm : pms.entrySet()) {
            Thread thread = new Thread(){
                public void run() {
                    VerifiableProtocolMessage response = requestServer(pm.getValue(), _serverCommunications.get(pm.getKey()));
                    StatusCode sc = verifyReceivedMessage(response);
                    if (sc.equals(StatusCode.OK)) {

                        RegisterMessage registerMessage = new RegisterMessage(response.getProtocolMessage().getAtomicRegisterMessages());
                        _responses.put(pm.getKey(), response);
                        if (general) {
                            _regularRegisterNN.writeReturn(registerMessage);
                        }
                        else {
                            _atomicRegister1N.writeReturn(registerMessage.getRid());
                        }
                    } else {

                        if (response == null) {
                            ProtocolMessage pmNoConsensus = new ProtocolMessage(StatusCode.NO_CONSENSUS);
                            pmNoConsensus.setAtomicRegisterMessages(new RegisterMessage(0, 0, new ArrayList<>()).getBytes());
                            _responses.put(pm.getKey(), createVerifiableMessage(pmNoConsensus));
                        }
                        else
                            _responses.put(pm.getKey(), response);
                        if (_responses.size() == _nServers) {
                            StatusCode finalSc = verifyStatusConsensus();
                            //System.out.println("DELIVERING WRITE WITHOUT CONSENSUS");
                            if (general)
                                deliverPostGeneral(finalSc);
                            else
                                deliverPost(finalSc);
                        }
                    }
                }
            };
            thread.start();
        }
    }

    public StatusCode verifyStatusConsensus() {
        Map.Entry<StatusCode, List<VerifiableAnnouncement>> quorum =
                MessageComparator.compareServerStatusCodes(new ArrayList<>(_responses.values()), _quorum);

        if (quorum != null) return quorum.getKey();

        return StatusCode.NO_CONSENSUS;
    }

    public void writeBack(RegisterMessage arm) {
        Map<PublicKey, ProtocolMessage> pms = new ConcurrentHashMap<>();
            for (Map.Entry<PublicKey, CommunicationServer> entry : _serverCommunications.entrySet()) {
                refreshToken(entry.getValue());
                ProtocolMessage p = new ProtocolMessage("WRITEBACK", _pubKey, entry.getValue().getToken());
                p.setAtomicRegisterMessages(arm.getBytes());
                pms.put(entry.getKey(), p);
            }
        write(pms, false);
    }

    public void readAnnouncements(Map<PublicKey, ProtocolMessage> pms, boolean general) {
        //System.out.println("READ op");
        Map<PublicKey, VerifiableProtocolMessage> responses = new ConcurrentHashMap<>();
        for (Map.Entry<PublicKey, ProtocolMessage> pm : pms.entrySet()) {

            Thread thread = new Thread(){
                public void run() {
                    VerifiableProtocolMessage response = requestServer(pm.getValue(), _serverCommunications.get(pm.getKey()));

                    if (verifyReceivedMessage(response) == StatusCode.OK) {
  
                       System.out.flush();

                        if (general && pm.getValue().getCommand().equals("READGENERAL")) {
                            _responses.put(pm.getKey(), response);
                            _regularRegisterNN.readReturn(response.getProtocolMessage(), new ArrayList<>(_responses.values()));
                        }
                        else if (!general && pm.getValue().getCommand().equals("READ")) {
                            _responses.put(pm.getKey(), response);
                            _atomicRegister1N.writeBack(response.getProtocolMessage());
                        }
                    }
                    else {
                        if (response == null){
                            ProtocolMessage pmNoConsensus = new ProtocolMessage(StatusCode.NO_CONSENSUS);
                            pmNoConsensus.setAtomicRegisterMessages(new RegisterMessage(0, 0, new ArrayList<>()).getBytes());
                            responses.put(pm.getKey(), createVerifiableMessage(pmNoConsensus));
                        }
                        else
                            responses.put(pm.getKey(), response);
                        if (responses.size() == _nServers && responses.get(pm.getKey()).getProtocolMessage().getStatusCode().equals(StatusCode.USER_NOT_REGISTERED)) {
                            deliverRead(StatusCode.USER_NOT_REGISTERED, new ArrayList<VerifiableAnnouncement>());
                        }
                        else if (responses.size() == _nServers) {

                            if (general)
                                deliverReadGeneral(StatusCode.NO_CONSENSUS, new ArrayList<VerifiableAnnouncement>());
                            else
                                deliverRead(StatusCode.NO_CONSENSUS, new ArrayList<VerifiableAnnouncement>());
                        }
                            
                    }
                }
            };
            thread.start();
        }
    }

    /**
     * Makes a request to the Server, receiving a response as a VerifiableProtocolMessage
     * @param pm is the ProtocolMessage to be sent
     * @return the server's response
     */
    public VerifiableProtocolMessage requestServer(ProtocolMessage pm, CommunicationServer serverCommunication) {
        //System.out.println("requestServer");
        if (pm == null) return null;

        try {
            // tries to reconnect with dead servers
            if (serverCommunication.getAlive() == false) {
                CommunicationServer newServerCommunication = createServerCommunication(serverCommunication.getPort(),
                        serverCommunication.getPubKey());
                serverCommunication = newServerCommunication;
                register(serverCommunication);
                pm.setToken(serverCommunication.getToken());
            }
        }
        catch (IOException e) {
            System.out.println("Error reconnecting with server at port " + serverCommunication.getPort() +
                    ". Server is still dead!");
            System.out.flush();

            // does not try to send message if server is still dead
            return null;
        }

        VerifiableProtocolMessage vpm = createVerifiableMessage(pm);
        VerifiableProtocolMessage rvpm = null;
        
        try {
            synchronized(serverCommunication.getObjInStream()) {
                System.out.println("==> Sending [" + pm.getCommand() + "] to server port: " + serverCommunication.getPort());
                _communication.sendMessage(vpm, serverCommunication.getObjOutStream());
                rvpm = (VerifiableProtocolMessage) _communication.receiveMessage(serverCommunication.getObjInStream());
                System.out.println("<== Received [" + vpm.getProtocolMessage().getCommand() + "] from server port: "  + serverCommunication.getPort());
            }

            if (rvpm == null) {
                return null;
            }

            if (!verifySignature(rvpm, serverCommunication.getPubKey())) {
                System.out.println("Could not verify server signature");
                //return null;
            }
        }
        catch(SocketTimeoutException e) {
            System.out.println("(INFO) Could not receive a response from server port: " + serverCommunication.getPort());
            if (serverCommunication._refreshToken == false) refreshToken(serverCommunication);
            return null;
        }
        catch (SocketException e) {
            System.out.println("(INFO) Server at port: " + serverCommunication.getPort() + " is dead!");
            serverCommunication.setAlive(false);
            return null;
        }
        catch (IOException | ClassNotFoundException | ClassCastException e) {
            // reset(serverCommunication);
            closeCommunication(serverCommunication);
            serverCommunication.setAlive(false);
            System.out.println(e);
        }
        finally {
            System.out.flush();
        }

        return rvpm;
    }

    public List<StatusCode> registerServersGroup() {
        ProtocolMessage pm = new ProtocolMessage("REGISTER", _pubKey);
        Map<PublicKey, VerifiableProtocolMessage> vpms = requestServersGroupRegister(pm);

        List<StatusCode> rscs = new ArrayList<>();
        for(Map.Entry<PublicKey, VerifiableProtocolMessage> vpm : vpms.entrySet()) {
            CommunicationServer serverCommunication = _serverCommunications.get(vpm.getKey());

            if (vpm.getValue() == null) {
                rscs.add(StatusCode.NO_RESPONSE);

                System.out.println("Could not register: could not receive a response");
                System.out.flush();
                closeGroupCommunication();
                System.exit(-1);
            }
            else {
                rscs.add(getStatusCodeFromVPM(vpm.getValue()));
                serverCommunication.setToken(getTokenFromVPM(vpm.getValue()));
            }
        }

        return rscs;
    }

    /**
     * Allows the Client to register in the Server, hence providing its Public Key.
     * Must be the first operation to be done in the Client-Server communication.
     * @return the StatusCode of the operation
     */
    public StatusCode register(CommunicationServer cs) {
        ProtocolMessage pm = new ProtocolMessage("REGISTER", _pubKey);
        VerifiableProtocolMessage vpm = requestServer(pm, cs);

        if (vpm == null) {
            System.out.println("Could not register: could not receive a response");
        }
        else {
            cs.setToken(getTokenFromVPM(vpm));
            return StatusCode.OK;
        }

        return StatusCode.NO_RESPONSE;
    }

    public List<StatusCode> post(String message, List<String> references) {
        if (message == null) {
            System.out.println("Message cannot be null.");
            return new ArrayList<StatusCode>(Arrays.asList(StatusCode.NULL_FIELD));
        }
        if (references == null) {
            System.out.println("References cannot be null.");
            return new ArrayList<StatusCode>(Arrays.asList(StatusCode.NULL_FIELD));
        }
        if (invalidMessageLength(message)) {
            System.out.println("Maximum message length to post announcement is 255.");
            return new ArrayList<StatusCode>(Arrays.asList(StatusCode.INVALID_MESSAGE_LENGTH));
        }

        List<StatusCode> rscs = new ArrayList<>();

        Announcement a = new Announcement(message, references);
        String announcementID = UUIDGenerator.generateUUID();
        a.setAnnouncementID(announcementID);
        a.setPublicKey(_pubKey);

        _atomicRegister1N.write();
        int rid = _atomicRegister1N.getRid();
        int wts = _atomicRegister1N.getWts();


        VerifiableAnnouncement va = createVerifiableAnnouncement(a);

        List<VerifiableAnnouncement> values = new ArrayList<VerifiableAnnouncement>(Arrays.asList(va));

        RegisterMessage arm = new RegisterMessage(rid, wts, values);

        Map<PublicKey, ProtocolMessage> pms = new ConcurrentHashMap<>();
        for (Map.Entry<PublicKey, CommunicationServer> entry : _serverCommunications.entrySet()) {
            ProtocolMessage p = new ProtocolMessage("POST", _pubKey, a, entry.getValue().getToken());
            p.setAtomicRegisterMessages(arm.getBytes());
            pms.put(entry.getKey(), p);
        }

        write(pms, false);

        return rscs;
    }

    public List<StatusCode> postGeneral(String message, List<String> references) {
        if (message == null) {
            System.out.println("Message cannot be null.");
            return new ArrayList<StatusCode>(Arrays.asList(StatusCode.NULL_FIELD));
        }
        if (references == null) {
            System.out.println("References cannot be null.");
            return new ArrayList<StatusCode>(Arrays.asList(StatusCode.NULL_FIELD));
        }
        if (invalidMessageLength(message)) {
            System.out.println("Maximum message length to post announcement is 255.");
            return new ArrayList<StatusCode>(Arrays.asList(StatusCode.INVALID_MESSAGE_LENGTH));
        }

        List<StatusCode> rscs = new ArrayList<>();

        Announcement a = new Announcement(message, references);
        String announcementID = UUIDGenerator.generateUUID();
        a.setAnnouncementID(announcementID);
        a.setPublicKey(_pubKey);

        _regularRegisterNN.write();
        int wts = _regularRegisterNN.getWts();

        VerifiableAnnouncement va = createVerifiableAnnouncement(a);
        List<VerifiableAnnouncement> values = new ArrayList<VerifiableAnnouncement>(Arrays.asList(va));

        RegisterMessage arm = new RegisterMessage(wts, values);

        Map<PublicKey, ProtocolMessage> pms = new ConcurrentHashMap<>();
        for (Map.Entry<PublicKey, CommunicationServer> entry : _serverCommunications.entrySet()) {
            ProtocolMessage p = new ProtocolMessage("POSTGENERAL", _pubKey, a, entry.getValue().getToken());
            p.setAtomicRegisterMessages(arm.getBytes());
            pms.put(entry.getKey(), p);
        }

        write(pms, true);

        return rscs;
    }

    public List<AbstractMap.SimpleEntry<StatusCode, List<Announcement>>> read(PublicKey user, int number) {

        List<AbstractMap.SimpleEntry<StatusCode, List<Announcement>>> announcementsPerServer = new ArrayList<>();
        if (user == null) {
            System.out.println("Invalid user.");
            announcementsPerServer.add(new AbstractMap.SimpleEntry<>(StatusCode.NULL_FIELD, new ArrayList<>()));
        }
        _readingUserPubKey = user;

        RegisterMessage arm = _atomicRegister1N.read();

        Map<PublicKey, ProtocolMessage> pms = new ConcurrentHashMap<>();
        for (Map.Entry<PublicKey, CommunicationServer> entry : _serverCommunications.entrySet()) {
            ProtocolMessage pm = new ProtocolMessage("READ", _pubKey, entry.getValue().getToken(), number, user);
            pm.setAtomicRegisterMessages(arm.getBytes());
            pms.put(entry.getKey(), pm);
        }

        readAnnouncements(pms, false);

        return announcementsPerServer;
    }

    public List<AbstractMap.SimpleEntry<StatusCode, List<Announcement>>> readGeneral(int number) {

        List<AbstractMap.SimpleEntry<StatusCode, List<Announcement>>> announcementsPerServer = new ArrayList<>();

        RegisterMessage arm = _regularRegisterNN.read();

        Map<PublicKey, ProtocolMessage> pms = new ConcurrentHashMap<>();
        for (Map.Entry<PublicKey, CommunicationServer> entry : _serverCommunications.entrySet()) {
            ProtocolMessage pm = new ProtocolMessage("READGENERAL", _pubKey, entry.getValue().getToken(), number);
            pm.setAtomicRegisterMessages(arm.getBytes());
            pms.put(entry.getKey(), pm);
        }
        
        readAnnouncements(pms, true);

        return announcementsPerServer;
    }

    /**
     * Verifies if a message has valid length.
     * @param message
     */
    public boolean invalidMessageLength(String message) {
        if (message == null) return false;
        return message.length() >= 255;
    }

    public boolean invalidToken(String token, PublicKey serverPubKey) {
        if(_serverCommunications.get(serverPubKey).getToken() == null)
            return false;
        return !token.equals(_serverCommunications.get(serverPubKey).getToken());
    }

    public StatusCode verifyReceivedMessage(VerifiableProtocolMessage vpm) {
        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            PublicKey serverPubKey = getServerPublicKeyFromVPM(vpm);

            if (getOldTokenFromVPM(vpm) == null) {
                rsc = StatusCode.INVALID_TOKEN;
            }

            else if (invalidToken(getOldTokenFromVPM(vpm), serverPubKey)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                //_token = getTokenFromVPM(vpm);
                _serverCommunications.get(serverPubKey).setToken(getTokenFromVPM(vpm));
            }
        }
        return rsc;
    }

    public StatusCode refreshToken(CommunicationServer sc) {
        System.out.println("(INFO) Refreshing token!");
        sc._refreshToken = true;
        ProtocolMessage p = new ProtocolMessage("TOKEN", _pubKey);
        VerifiableProtocolMessage vpm = requestServer(p, sc);
        sc.setToken(getTokenFromVPM(vpm));
        sc._refreshToken = false;
        return getStatusCodeFromVPM(vpm);
    }

    public List<StatusCode> refreshTokenServersGroup() {
        List<StatusCode> statusCodes = new ArrayList<>();
        for (Map.Entry<PublicKey, CommunicationServer> entry : _serverCommunications.entrySet()) {
            StatusCode sc = refreshToken(entry.getValue());
            statusCodes.add(sc);
        }
        return statusCodes;
    }

}
    