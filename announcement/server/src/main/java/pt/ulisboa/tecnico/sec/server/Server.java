package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.*;
import pt.ulisboa.tecnico.sec.database_lib.*;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLEngineResult.Status;

public class Server extends Thread {

    private ServerSocket _serverSocket;
    private Socket _socket;
    private int _port;
    protected int _nServers;
    protected int _nFaults;

    private PublicKey _pubKey;
    private PrivateKey _privateKey;
    private Database _db;
    /**
     * Maps the unique id of an operation to the status it received so that we can
     * know if a message was replayed or if it's fresh and to not repeat operations
     * in case a client resends it due to loss of message. Sends the message that
     * was originally formulated to answer that request.
     */
    protected ConcurrentHashMap<PublicKey, User> _users;
    /**
     * maps the announcement unique id to the public key of the entity where it's
     * stored and the index on the PostOperation Board, if it's the server's public
     * key it's in the General Board, otherwise it's in the respective User's
     * PostOperation Board
     */
    protected ConcurrentHashMap<String, AnnouncementLocation> _announcementMapper;
    private Communication _communication;

    private RegularRegisterNN _regularRegisterNN = new RegularRegisterNN(this, _nServers);


    private List<ServerThread> _serverThreads = new ArrayList<ServerThread>();

    // maps client public key -> server broadcast data
    protected ConcurrentHashMap<PublicKey, ServerBroadcast> _serverBroadcasts = new ConcurrentHashMap<>();

    public Server(boolean activateCC, int nServers, int nFaults, int port, char[] keyStorePasswd, char[] entryPasswd, String alias, String pubKeyPath,
            String keyStorePath) {

        _nFaults = nFaults;
        _port = port;
        _nServers = nServers;
        loadPublicKey(pubKeyPath);
        loadPrivateKey(keyStorePath, keyStorePasswd, entryPasswd, alias);
        _users = new ConcurrentHashMap<>();
        _announcementMapper = new ConcurrentHashMap<>();
        _communication = new Communication();

        String db = "announcement" + port;
        _db = new Database(db);

        retrieveDataStructures();
    }

    public RegularRegisterNN getRegularRegisterNN() {
        return _regularRegisterNN;
    }

    public PublicKey getServerPubKey() { return _pubKey; }

    public void retrieveDataStructures() {
        DBStructure dbs = _db.retrieveStructure();

        // Retrieve _users from database
        List<UserStructure> us = dbs.getUsers();
        for (UserStructure i : us) {
            PublicKey pk = (PublicKey) ProtocolMessageConverter.byteArrayToObj(i.getPublicKey());
            AtomicRegister1N ar = (AtomicRegister1N) ProtocolMessageConverter.byteArrayToObj(i.getAtomicRegister1N());
            ClientMessageHandler cmh = (ClientMessageHandler) ProtocolMessageConverter.byteArrayToObj(i.getCMH());
            String token = i.getToken(); 
            User u = new User(pk, i.getClientUUID(), ar, cmh);
            u.setToken(token);
            _users.put(pk, u);
        }

        // Retrieve RegularRegisterNN from database
        RegularRegisterNNStructure rr = dbs.getRegularRegisterNN();
        if(rr != null)
            _regularRegisterNN = (RegularRegisterNN) ProtocolMessageConverter.byteArrayToObj(rr.getGeneralBoard());

    }

    public void loadPublicKey(String pubKeyPath) {
        try {
            _pubKey = KeyPairUtil.loadPublicKey(pubKeyPath);
        } catch (Exception e) {
            System.out.println(
                    "Error: Not possible to initialize server because it was not possible to load public key.\n" + e);
            System.exit(-1);
        }
    }

    public void loadPrivateKey(String keyStorePath, char[] keyStorePasswd, char[] entryPasswd, String alias) {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStorage.loadKeyStore(keyStorePasswd, keyStorePath);
        } catch (Exception e) {
            System.out.println("Error: Not possible to initialize server because it "
                    + "was not possible to load keystore.\n" + e);
            System.exit(-1);
        }
        try {
            _privateKey = KeyStorage.loadPrivateKey(entryPasswd, alias, keyStore);
        } catch (Exception e) {
            System.out.println("Error: Not possible to initialize server because it "
                    + "was not possible to load private key.\n" + e);
            System.exit(-1);
        }
        if (_privateKey == null) {
            System.out.println("Error: Not possible to initialize server because it "
                    + "was not possible to load private key due to wrong password or alias.\n");
            System.exit(-1);
        }
    }

    public void startClientCommunication() {
        try {
            _serverSocket = _communication.createServerSocket(_port - 1000);
        } catch (IOException e) {
            System.out.println("Error starting server socket in port:" + (_port - 1000));
        }
    }

    public VerifiableServerMessage createVerifiableServerMessage(ServerMessage sm) {
        try {
            byte[] spm = ProtocolMessageConverter.objToByteArray(sm);
            byte[] signedsm = SignatureUtil.sign(spm, getPrivateKey());
            return new VerifiableServerMessage(sm, signedsm);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            System.out.println(e);
        }
        return null;
    }

    public void sendToAllServers(ServerMessage sm) {
        for (ServerThread t: _serverThreads) {
            t.sendServerMessage(sm);
        }
    }

    public void serverBroadcast(PublicKey clientPubKey, ServerMessage sm, VerifiableProtocolMessage clientMessage) {

        // no other servers to contact
        if (_nServers == 1) {
            deliver(clientMessage, clientMessage);
        }
        else {
            System.out.println("(INFO) Broadcast to other servers!");
            ServerBroadcast sb = new ServerBroadcast(this, clientMessage);
            String bcb = UUIDGenerator.generateUUID();
            sb.setBcb(bcb);
            sb.localEcho();
            _serverBroadcasts.put(clientPubKey, sb);
            sm.setBcb(bcb);
            Thread thread = new Thread() {
                public void run() {
                    for (ServerThread t: _serverThreads) {
                        t.sendQueueMessages(clientPubKey);
                        t.sendServerMessage(sm);
                    }
                }
            };
            thread.start();
        }

    }

    public List<Integer> getOtherServersPorts() {
        int initialPort = 9001;
        List<Integer> otherPorts = new ArrayList<Integer>();
        for (int i = initialPort; i < initialPort + _nServers; i++) {
            if (i != _port) {
                otherPorts.add(i);
            }
        }
        return otherPorts;
    }

    public void deliver(VerifiableProtocolMessage highestVPM, VerifiableProtocolMessage clientVPM) {
        switch (highestVPM.getProtocolMessage().getCommand()) {
            case "POST":
                deliverPost(highestVPM, clientVPM);
                break;
            case "READ":
                deliverRead(highestVPM, clientVPM);
                break;
            case "POSTGENERAL":
                deliverPostGeneral(highestVPM, clientVPM);
                break;
            case "READGENERAL":
                deliverReadGeneral(highestVPM, clientVPM);
                break;
            case "WRITEBACK":
                deliverWriteBack(highestVPM, clientVPM);
            default:
                break;
        }

        _serverBroadcasts.remove(clientVPM.getProtocolMessage().getPublicKey());
        for (ServerThread t: _serverThreads)
            t._serverMessageQueue.remove(clientVPM.getProtocolMessage().getPublicKey());
        
    }

    
    public void deliverWriteBack(VerifiableProtocolMessage highestVPM, VerifiableProtocolMessage clientVPM) {
        PublicKey clientPubKey = highestVPM.getProtocolMessage().getPublicKey();
        String token = clientVPM.getProtocolMessage().getToken();
        User user = _users.get(clientPubKey);
        ClientMessageHandler cmh = user.getCMH();
        String newToken = user.getToken();
        RegisterMessage registerMessage = new RegisterMessage(highestVPM.getProtocolMessage().getAtomicRegisterMessages());
        RegisterMessage arm = _users.get(clientPubKey).getAtomicRegister1N().acknowledge(registerMessage);

        byte[] b = ProtocolMessageConverter.objToByteArray(_users.get(clientPubKey).getAtomicRegister1N());
        _db.updateUserAtomicRegister1N(_users.get(clientPubKey).getdbTableName(), b);

        ProtocolMessage p = new ProtocolMessage("ACK", StatusCode.OK, _pubKey, newToken, token);
        p.setAtomicRegisterMessages(arm.getBytes());
        cmh.sendMessage(createVerifiableMessage(p));
    }

    public void deliverPost(VerifiableProtocolMessage highestVPM, VerifiableProtocolMessage clientVPM) {
        PublicKey clientPubKey = highestVPM.getProtocolMessage().getPublicKey();
        String token = clientVPM.getProtocolMessage().getToken();
        User user = _users.get(clientPubKey);
        ClientMessageHandler cmh = user.getCMH();
        String newToken = user.getToken();
        RegisterMessage registerMessage = new RegisterMessage(highestVPM.getProtocolMessage().getAtomicRegisterMessages());
        RegisterMessage arm = _users.get(clientPubKey).getAtomicRegister1N().acknowledge(registerMessage);

        byte[] b = ProtocolMessageConverter.objToByteArray(_users.get(clientPubKey).getAtomicRegister1N());
        _db.updateUserAtomicRegister1N(_users.get(clientPubKey).getdbTableName(), b);

        ProtocolMessage p = new ProtocolMessage("POST",  StatusCode.OK, _pubKey, newToken, token);
        p.setAtomicRegisterMessages(arm.getBytes());
        cmh.sendMessage(createVerifiableMessage(p));

        System.out.println("------------------------END WRITEBACK------------------------");
    }

    public void deliverRead(VerifiableProtocolMessage highestVPM, VerifiableProtocolMessage clientVPM) {
        PublicKey clientPubKey = highestVPM.getProtocolMessage().getPublicKey();
        String token = clientVPM.getProtocolMessage().getToken();
        User user = _users.get(clientPubKey);
        ClientMessageHandler cmh = user.getCMH();
        String newToken = user.getToken();
        int number = highestVPM.getProtocolMessage().getReadNumberAnnouncements();

        RegisterMessage registerMessage = new RegisterMessage(highestVPM.getProtocolMessage().getAtomicRegisterMessages());
        RegisterMessage arm = _users.get(clientPubKey).getAtomicRegister1N().value(registerMessage, number);

        byte[] b = ProtocolMessageConverter.objToByteArray(_users.get(clientPubKey).getAtomicRegister1N());
        _db.updateUserAtomicRegister1N(_users.get(clientPubKey).getdbTableName(), b);

        ProtocolMessage p = new ProtocolMessage("READ", StatusCode.OK, _pubKey, newToken, token);
        p.setAtomicRegisterMessages(arm.getBytes());
        VerifiableProtocolMessage response = createVerifiableMessage(p);

        cmh.sendMessage(response);

        System.out.println("------------------------END READ------------------------");
    }

    public void deliverPostGeneral(VerifiableProtocolMessage highestVPM, VerifiableProtocolMessage clientVPM) {

        PublicKey clientPubKey = highestVPM.getProtocolMessage().getPublicKey();
        String token = clientVPM.getProtocolMessage().getToken();
        User user = _users.get(clientPubKey);
        ClientMessageHandler cmh = user.getCMH();
        String newToken = user.getToken();

        RegisterMessage arm = _regularRegisterNN.acknowledge(highestVPM.getProtocolMessage());

        byte[] b = ProtocolMessageConverter.objToByteArray(_regularRegisterNN);
        // TODO: uncomment this
        //_db.updateRegularRegisterNNTable(b);

        ProtocolMessage p = new ProtocolMessage("POSTGENERAL", StatusCode.OK, _pubKey, newToken, token);
        p.setAtomicRegisterMessages(arm.getBytes());
        cmh.sendMessage(createVerifiableMessage(p));

        System.out.println("------------------------END POSTGENERAL------------------------");
    }

    public void deliverReadGeneral(VerifiableProtocolMessage highestVPM, VerifiableProtocolMessage clientVPM) {

        PublicKey clientPubKey = highestVPM.getProtocolMessage().getPublicKey();
        String token = clientVPM.getProtocolMessage().getToken();
        User user = _users.get(clientPubKey);
        ClientMessageHandler cmh = user.getCMH();
        String newToken = user.getToken();

        RegisterMessage arm = _regularRegisterNN.value(highestVPM.getProtocolMessage());

        byte[] b = ProtocolMessageConverter.objToByteArray(_regularRegisterNN);
        // TODO: uncomment this
        //_db.updateRegularRegisterNNTable(b);

        ProtocolMessage p = new ProtocolMessage("READGENERAL", StatusCode.OK, _pubKey, newToken, token);
        p.setAtomicRegisterMessages(arm.getBytes());
        cmh.sendMessage(createVerifiableMessage(p));

        System.out.println("------------------------END READGENERAL------------------------");
    }

    public void deliverFailed(VerifiableProtocolMessage clientVPM) {
        System.out.println("(INFO) Deliver failed: could not reach consensus");
    }


    public void startServerCommunication() {
        try{
            ServerSocket serverSocket = _communication.createServerSocket(_port);
            for (int port : getOtherServersPorts()) {
                ServerThread t = new ServerThread(this, _nServers, port, _port, serverSocket);
                _serverThreads.add(t);
                t.start();
            }
        }
        catch(IOException e) {

        }
    }

    /**
     * Opens new socket to listen for client communications and creates a new Thread
     * to handle each client connection.
     */
    public void start() {
        startClientCommunication();
        startServerCommunication();
        while (true) {
            try {
                _socket = _communication.accept(_serverSocket);
                new ClientMessageHandler(this, _socket).start();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public StatusCode verifySignature(VerifiableProtocolMessage vpm) {
        try {
            byte[] bpm = ProtocolMessageConverter.objToByteArray(vpm.getProtocolMessage());
            boolean verified = SignatureUtil.verifySignature(vpm.getSignedProtocolMessage(),
                    vpm.getProtocolMessage().getPublicKey(), bpm);
            if (verified == true)
                return StatusCode.OK;
            else
                return StatusCode.INVALID_SIGNATURE;
        } catch (NoSuchAlgorithmException e) {
            System.out.println(StatusCode.INVALID_ALGORITHM + "\n" + e);
            return StatusCode.INVALID_ALGORITHM;
        } catch (InvalidKeyException e) {
            System.out.println(StatusCode.INVALID_KEY + "\n" + e);
            return StatusCode.INVALID_KEY;
        } catch (SignatureException e) {
            System.out.println(StatusCode.INVALID_SIGNATURE + "\n" + e);
            return StatusCode.INVALID_SIGNATURE;
        }
    }

    public StatusCode verifyServerSignature(VerifiableServerMessage vsm) {
        if (vsm == null || vsm.getServerMessage() == null || vsm.getSignedServerMessage() == null) {
            return StatusCode.NULL_FIELD;
        }
        try {
            byte[] bsm = ProtocolMessageConverter.objToByteArray(vsm.getServerMessage());
            boolean verified = SignatureUtil.verifySignature(vsm.getSignedServerMessage(),
                    vsm.getServerMessage().getPublicKey(), bsm);
            if (verified == true)
                return StatusCode.OK;
            else
                return StatusCode.INVALID_SIGNATURE;
        } catch (NoSuchAlgorithmException e) {
            System.out.println(StatusCode.INVALID_ALGORITHM + "\n" + e);
            return StatusCode.INVALID_ALGORITHM;
        } catch (InvalidKeyException e) {
            System.out.println(StatusCode.INVALID_KEY + "\n" + e);
            return StatusCode.INVALID_KEY;
        } catch (SignatureException e) {
            System.out.println(StatusCode.INVALID_SIGNATURE + "\n" + e);
            return StatusCode.INVALID_SIGNATURE;
        }
    }

    /**
     * Verifies if an operation request is valid, which means having a unique id and
     * signature to ensure the message was not tampered with or replayed.
     * 
     * @param token
     * @return StatusCode
     */
    public StatusCode verifyInvalidToken(PublicKey userPubKey, String token) {

        if (!_users.get(userPubKey).getToken().equals(token)) {
            return StatusCode.INVALID_TOKEN;
        }
        else
            return StatusCode.OK;
    }

    /**
     * Verifies if a message is valid to be posted.
     * 
     * @param message to be verified
     * @return StatusCode
     */
    public StatusCode verifyMessage(String message) {
        // TODO: 255 or 256?
        if (message.length() >= 255) {
            return StatusCode.INVALID_MESSAGE_LENGTH;
        }
        // TODO: make more verifications
        return StatusCode.OK;
    }

    /**
     * Verifies if all the references added exist.
     * 
     * @param references to be verified
     * @return StatusCode
     */
    public StatusCode verifyReferences(List<String> references) {
        if (references == null)
            return StatusCode.NULL_FIELD;

        for (String reference : references) {
            if (!_announcementMapper.containsKey(reference)) {
                return StatusCode.INVALID_REFERENCE;
            }
        }
        // server eliminates repeated references
        Set<String> set = new HashSet<>(references);
        if (set.size() != references.size())
            return StatusCode.DUPLICATE_REFERENCE;
        return StatusCode.OK;
    }

    /**
     * Verifies if all the references added exist.
     * 
     * @param clientPubKey to be verified
     * @return StatusCode
     */
    public StatusCode verifyUserRegistered(PublicKey clientPubKey) {
        if (_users.containsKey(clientPubKey)) {
            return StatusCode.USER_ALREADY_REGISTERED;
        }
        return StatusCode.USER_NOT_REGISTERED;
    }

    public StatusCode verifyNullToken(String token) {
        if (token == null) {
            return StatusCode.NULL_FIELD;
        } else {
            return StatusCode.OK;
        }
    }

    public StatusCode verifyPost(String token, VerifiableProtocolMessage vpm, String message, List<String> references,
            PublicKey clientPubKey) {
        StatusCode sc;

        if (verifyNullToken(token).equals(StatusCode.NULL_FIELD) || vpm == null || message == null || references == null
                || clientPubKey == null || vpm.getProtocolMessage() == null || vpm.getSignedProtocolMessage() == null
                || vpm.getProtocolMessage().getAtomicRegisterMessages() == null) {
            return StatusCode.NULL_FIELD;
        }

        sc = verifyUserRegistered(clientPubKey);
        if (sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            return sc;
        }

        sc = verifyInvalidToken(vpm.getProtocolMessage().getPublicKey(), token);
        if (!sc.equals(StatusCode.OK)) {
            return sc;
        }

        sc = verifySignature(vpm);
        if (!sc.equals(StatusCode.OK)) {
            return sc;
        }


        sc = verifyMessage(message);
        if (!sc.equals(StatusCode.OK)) {
            return sc;
        }

        sc = verifyReferences(references);
        if (!sc.equals(StatusCode.OK)) {
            return sc;
        }

        RegisterMessage registerMessage = new RegisterMessage(vpm.getProtocolMessage().getAtomicRegisterMessages());
        if(!vpm.getProtocolMessage().getPublicKey().equals(registerMessage.getValues().get(0).getAnnouncement().getClientPublicKey()))
            return StatusCode.INVALID_ANNOUNCEMENT_PUBLIC_KEY;
        return sc;
    }

    public StatusCode verifyWriteBack(String token, VerifiableProtocolMessage vpm, PublicKey clientPubKey) {
        StatusCode sc;

        if (verifyNullToken(token).equals(StatusCode.NULL_FIELD) || vpm == null
                || clientPubKey == null || vpm.getProtocolMessage() == null || vpm.getSignedProtocolMessage() == null
                || vpm.getProtocolMessage().getAtomicRegisterMessages() == null) {
            return StatusCode.NULL_FIELD;
        }

        sc = verifyUserRegistered(clientPubKey);
        if (sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            return sc;
        }

        sc = verifyInvalidToken(vpm.getProtocolMessage().getPublicKey(), token);
        if (!sc.equals(StatusCode.OK)) {
            return sc;
        }

        sc = verifySignature(vpm);
        if (!sc.equals(StatusCode.OK)) {
            return sc;
        }

        return sc;
    }

    public StatusCode verifyRead(String token, VerifiableProtocolMessage vpm, PublicKey clientPubKey) {
        StatusCode sc;

        if (verifyNullToken(token).equals(StatusCode.NULL_FIELD) || vpm == null || clientPubKey == null
                || vpm.getProtocolMessage() == null || vpm.getSignedProtocolMessage() == null
                || vpm.getProtocolMessage().getAtomicRegisterMessages() == null) {
            return StatusCode.NULL_FIELD;
        }

        sc = verifyUserRegistered(clientPubKey);
        if (sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            return sc;
        }

        sc = verifyInvalidToken(vpm.getProtocolMessage().getPublicKey(), token);
        if (!sc.equals(StatusCode.OK)) {
            return sc;
        }

        sc = verifySignature(vpm);
        if (!sc.equals(StatusCode.OK)) {
            return sc;
        }

        return sc;
    }

    public VerifiableProtocolMessage createVerifiableMessage(ProtocolMessage pm) {
        try {
            byte[] bpm = ProtocolMessageConverter.objToByteArray(pm);
            byte[] signedpm = SignatureUtil.sign(bpm, _privateKey);
            return new VerifiableProtocolMessage(pm, signedpm);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            System.out.println(e);
        }
        return null;
    }

    /**
     * Registers the user and associated public key in the system before first use.
     * Makes necessary initializations to enable first use of DPAS
     * @param vpm
     * @param cmh
     * @return VerifiableProtocolMessage
     */
    public VerifiableProtocolMessage registerUser(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {
        System.out.println("register");
        StatusCode sc;

        VerifiableProtocolMessage response;
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();

        if (vpm == null || clientPubKey == null || vpm.getProtocolMessage() == null ||
                vpm.getSignedProtocolMessage() == null) {
            
            response = createVerifiableMessage(new ProtocolMessage(
                    "REGISTER", StatusCode.NULL_FIELD, _pubKey));

            return response;
        }

        sc = verifySignature(vpm);
        if (!sc.equals(StatusCode.OK)) {
            response = createVerifiableMessage(new ProtocolMessage(
                    "REGISTER", sc, _pubKey));

            return response;
        }

        sc = verifyUserRegistered(clientPubKey);
        if (sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            String i = UUIDGenerator.generateUUID();
            String uuid = "T" + i;
            AtomicRegister1N ar = new AtomicRegister1N(this);
            User user = new User(clientPubKey, uuid, ar, cmh);
            user.setRandomToken();
            _db.updateUserToken(user.getdbTableName(), user.getToken());
            String token = user.getToken();
            _users.put(clientPubKey, user);
            _db.createUserTable(uuid);
            
            byte[] b1 = ProtocolMessageConverter.objToByteArray(clientPubKey);
            byte[] b2 = ProtocolMessageConverter.objToByteArray(ar);
            byte[] b3 = ProtocolMessageConverter.objToByteArray(cmh);

            _db.insertUser(b1, uuid, b2, b3);

            response = createVerifiableMessage(new ProtocolMessage(
                "REGISTER", StatusCode.OK, _pubKey, token));

            return response;
        }
        // user is already registered
        User user = _users.get(clientPubKey);
        user.setRandomToken();
        _db.updateUserToken(user.getdbTableName(), user.getToken());
        String token = user.getToken();
        user.setCMH(cmh);

        response = createVerifiableMessage(new ProtocolMessage(
                "REGISTER", sc, _pubKey, token));

                return response;
    }

    /**
     * Posts an announcement of up to 255 characters to the user's PostOperation Board.
     * Can refer to previous announcements.
     * @param vpm
     * @param cmh
     */
    public void post(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {
        StatusCode sc;

        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String message = vpm.getProtocolMessage().getPostAnnouncement().getAnnouncement();
        List<String> references = vpm.getProtocolMessage().getPostAnnouncement().getReferences();
        String token = vpm.getProtocolMessage().getToken();

        sc = verifyPost(token, vpm, message, references, clientPubKey);

        User user = _users.get(clientPubKey);
        user.setRandomToken();
        _db.updateUserToken(user.getdbTableName(), user.getToken());
        String newToken = user.getToken();
        
        if (sc.equals(StatusCode.NULL_FIELD) || sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                "POST", sc, _pubKey)));
                return;
        }
                
        if (sc.equals(StatusCode.INVALID_TOKEN)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                "POST", sc, _pubKey)));
            return;
        }
        if (!sc.equals(StatusCode.OK)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                    "POST", sc, _pubKey)));
            return;
        }
                
        ServerMessage sm = new ServerMessage(_pubKey, "SERVER_POST", vpm);
        serverBroadcast(clientPubKey, sm, vpm);
        return;

    }

    public void writeBack(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {
        StatusCode sc;

        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String token = vpm.getProtocolMessage().getToken();

        // verifications
        sc = verifyWriteBack(token, vpm, clientPubKey);
        if (sc.equals(StatusCode.NULL_FIELD) || sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                "POST", sc, _pubKey)));
                return;
        }
        User user = _users.get(clientPubKey);
        user.setRandomToken();
        _db.updateUserToken(user.getdbTableName(), user.getToken());
        String newToken = user.getToken();
        
        if (sc.equals(StatusCode.INVALID_TOKEN)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                "POST", sc, _pubKey, newToken, token)));
            return;
        }
        if (!sc.equals(StatusCode.OK)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                    "POST", sc, _pubKey, newToken, token)));
            return;
        }

        ServerMessage sm = new ServerMessage(_pubKey, "SERVER_WRITEBACK", vpm);
        serverBroadcast(clientPubKey, sm, vpm);

        return;

    }


    /**
     * Posts an announcement of up to 255 characters in the General Board.
     * Can refer to previous announcements.
     * @param vpm
     * @return ProtocolMessage
     */
    public void postGeneral(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {
        StatusCode sc;

        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String message = vpm.getProtocolMessage().getPostAnnouncement().getAnnouncement();
        List<String> references = vpm.getProtocolMessage().getPostAnnouncement().getReferences();
        String token = vpm.getProtocolMessage().getToken();

        sc = verifyPost(token, vpm, message, references, clientPubKey);

        User user = _users.get(clientPubKey);
        user.setRandomToken();
        _db.updateUserToken(user.getdbTableName(), user.getToken());
        String newToken = user.getToken();
        

        // verifications
        
        if (sc.equals(StatusCode.NULL_FIELD) || sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage("POSTGENERAL", sc, _pubKey, newToken, token)));
            return;
        }
        
        if (sc.equals(StatusCode.INVALID_TOKEN)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                "POSTGENERAL", sc, _pubKey, newToken, token)));
            return;
        }
        if (!sc.equals(StatusCode.OK)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                    "POSTGENERAL", sc, _pubKey, newToken, token)));
            return;
        }

        ServerMessage sm = new ServerMessage(_pubKey, "SERVER_POSTGENERAL", vpm);
        serverBroadcast(clientPubKey, sm, vpm);
    }

    public String getUserUUID(PublicKey publicKey) {
        return _users.get(publicKey).getdbTableName(); 
    }

    /**
     * Obtains the most recent number announcements posted by the user with associated key
     * (from the user's PostOperation Board).
     * If number == 0, all announcements should be returned.
     * @param vpm
     * @param cmh
     */
    public void read(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {
        StatusCode sc;

        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        PublicKey toReadPublicKey = vpm.getProtocolMessage().getToReadPublicKey();
        String token = vpm.getProtocolMessage().getToken();

        sc = verifyRead(token, vpm, clientPubKey);

        User user = _users.get(clientPubKey);
        user.setRandomToken();
        _db.updateUserToken(user.getdbTableName(), user.getToken());
        String newToken = user.getToken();

        // verifications
        if (toReadPublicKey == null) { cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                "READ", StatusCode.NULL_FIELD, _pubKey, newToken, token)));
                return; }

        if (verifyUserRegistered(toReadPublicKey).equals(StatusCode.USER_NOT_REGISTERED)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                "READ", StatusCode.USER_NOT_REGISTERED, _pubKey, newToken, token)));
            return;    
        }
        
        if (sc.equals(StatusCode.NULL_FIELD)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage("READ", sc, _pubKey, newToken, token)));
            return; 
        }
        
        if (sc.equals(StatusCode.INVALID_TOKEN)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                "READ", sc, _pubKey, newToken, token)));
            return;
        }
        if (!sc.equals(StatusCode.OK)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                    "READ", sc, _pubKey, newToken, token)));
            return;
        }

        ServerMessage sm = new ServerMessage(_pubKey, "SERVER_READ", vpm);
        serverBroadcast(clientPubKey, sm, vpm);
    }

    /**
     * Obtains the most recent number announcements on the General Board.
     * If number == 0, all announcements should be returned.
     * @param vpm
     * @param cmh
     */
    public void readGeneral(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {
        StatusCode sc;

        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String token = vpm.getProtocolMessage().getToken();

        sc = verifyRead(token, vpm, clientPubKey);

        User user = _users.get(clientPubKey);
        user.setRandomToken();
        _db.updateUserToken(user.getdbTableName(), user.getToken());
        String newToken = user.getToken();


        // verifications
        if (sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage("READGENERAL", sc, _pubKey, newToken, token))); 
            return;       
        }
        if (sc.equals(StatusCode.NULL_FIELD)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage("READGENERAL", sc, _pubKey, newToken, token)));
            return;
        }        

        if (!sc.equals(StatusCode.OK)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage("READGENERAL", sc, _pubKey, newToken, token)));   
            if (!sc.equals(StatusCode.NULL_FIELD)) { ;
                // _db.insertOperation(opUuid, operation);
            }
            return;
        }

        ServerMessage sm = new ServerMessage(_pubKey, "SERVER_READGENERAL", vpm);
        serverBroadcast(clientPubKey, sm, vpm);
    }

    public VerifiableProtocolMessage invalidCommand(VerifiableProtocolMessage vpm) {
        // TODO: make something in client to support this

        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String token = vpm.getProtocolMessage().getToken();

        User user = _users.get(clientPubKey);
        user.setRandomToken();
        _db.updateUserToken(user.getdbTableName(), user.getToken());
        String newToken = user.getToken();
        

        return createVerifiableMessage(new ProtocolMessage(
                "INVALID", StatusCode.INVALID_COMMAND, _pubKey, newToken, token));
    }

    public VerifiableProtocolMessage refreshToken(VerifiableProtocolMessage vpm) {
        StatusCode sc;
        VerifiableProtocolMessage response;
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();

        if (vpm == null || clientPubKey == null || vpm.getProtocolMessage() == null ||
                vpm.getSignedProtocolMessage() == null) {
            
            response = createVerifiableMessage(new ProtocolMessage(
                    "TOKEN", StatusCode.NULL_FIELD, _pubKey));
            return response;
        }
        sc = verifySignature(vpm);
        if (!sc.equals(StatusCode.OK)) {
            response = createVerifiableMessage(new ProtocolMessage(
                    "TOKEN", sc, _pubKey));
            return response;
        }
        sc = verifyUserRegistered(clientPubKey);
        if (sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            response = createVerifiableMessage(new ProtocolMessage(
                    "TOKEN", sc, _pubKey));
            return response;
        }
        User user = _users.get(clientPubKey);
        user.setRandomToken();
        _db.updateUserToken(user.getdbTableName(), user.getToken());
        String newToken = user.getToken();

        response = createVerifiableMessage(new ProtocolMessage(
                "TOKEN", StatusCode.OK, _pubKey, newToken));

        return response;
    }
  
    public void addAnnouncementMapper(List<VerifiableAnnouncement> announcements) {
        for (VerifiableAnnouncement va: announcements) {
            _announcementMapper.put(va.getAnnouncement().getAnnouncementID(), new AnnouncementLocation(va.getAnnouncement().getClientPublicKey(), 0));
        }
    }

    public PublicKey getPublicKey() { return _pubKey; }
    protected PrivateKey getPrivateKey() { return _privateKey; }
}
