package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.*;
import pt.ulisboa.tecnico.sec.database_lib.*;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLEngineResult.Status;

public class Server extends Thread {

    private ServerSocket _serverSocket;
    private Socket _socket;
    private List<Socket> _serverSocketList;
    private int _port;
    private int _nServers;

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
    private ConcurrentHashMap<String, AnnouncementLocation> _announcementMapper;
    private List<Announcement> _generalBoard;
    private Communication _communication;

    private ConcurrentHashMap<PublicKey, AtomicRegister1N> _atomicRegisters1N = new ConcurrentHashMap<>();
    private RegularRegisterNN _regularRegisterNN = new RegularRegisterNN(this, _nServers);

    /** maps client token to current clientHandler processing the request with that token */
    //private ConcurrentHashMap<String, ClientMessageHandler> _processingRequests;

    private List<ServerThread> _serverThreads = new ArrayList<ServerThread>();

    public Server(boolean activateCC, int nServers, int port, char[] keyStorePasswd, char[] entryPasswd, String alias, String pubKeyPath,
            String keyStorePath) {

        _port = port;
        _nServers = nServers;
        loadPublicKey(pubKeyPath);
        loadPrivateKey(keyStorePath, keyStorePasswd, entryPasswd, alias);
        _users = new ConcurrentHashMap<>();
        _announcementMapper = new ConcurrentHashMap<>();
        // TODO: see if a CopyOnWriteArrayList is more suitable (if very few writes and
        // lots of reads)
        _generalBoard = new ArrayList<>();
        _communication = new Communication();

        //_processingRequests = new ConcurrentHashMap<>();

        String db = "announcement" + port;
        _db = new Database(db);

        retrieveDataStructures();
    }

    public AtomicRegister1N getAtomicRegister1N(PublicKey clientPubKey) {
        return _atomicRegisters1N.get(clientPubKey);
    }

    public RegularRegisterNN getRegularRegisterNN() {
        return _regularRegisterNN;
    }

    public PublicKey getUserUUID(String uuid) {
        for (User u : _users.values()) {
            if (u.getdbTableName().equals(uuid)) {
                return u.getPublicKey();
            }
        }
        return null;
    }

    public void resetDatabase() {
        _db.resetDatabaseTest();
    }

    public void printDataStructures() {
        System.out.println("_users: " + _users + "END");
        System.out.println("_generalBoard: " + _generalBoard + "END");
        System.out.println("_announcementMapper: " + _announcementMapper + "END");
    }

    public void retrieveDataStructures() {
        DBStructure dbs = _db.retrieveStructure();
        // // Retrieve _users from database
        // List<UserStructure> us = dbs.getUsers();
        // for (UserStructure i : us) {
        //     PublicKey pk = (PublicKey) ProtocolMessageConverter.byteArrayToObj(i.getPublicKey());
        //     User u = new User(pk, i.getClientUUID());
        //     _users.put(pk, u);
        // }

        // Retrieve _generalBoard from database
        List<GeneralBoardStructure> gbs = dbs.getGeneralBoard();
        for (GeneralBoardStructure i : gbs) {
            List<String> references = (List<String>) ProtocolMessageConverter.byteArrayToObj(i.getReferences());
            Announcement a = new Announcement(i.getAnnouncement(), references, i.getAnnouncementID(),
                    i.getClientUUID());
            a.setPublicKey(getUserUUID(i.getClientUUID()));

            int index;
            synchronized (_generalBoard) {
                index = _generalBoard.size();
                _generalBoard.add(a);
            }
            // server's public key is used to indicate it's stored in the General Board
            _announcementMapper.put(i.getAnnouncementID(), new AnnouncementLocation(_pubKey, index));
        }

        // Retrieve _announcementMapper from database
        List<UserBoardStructure> ubs = dbs.getUserBoard();
        for (UserBoardStructure i : ubs) {
            List<String> references = (List<String>) ProtocolMessageConverter.byteArrayToObj(i.getReferences());
            Announcement a = new Announcement(i.getAnnouncement(), references, i.getAnnouncementID(),
                    i.getClientUUID());
            PublicKey clientPubKey = getUserUUID(i.getClientUUID());
            a.setPublicKey(clientPubKey);

            int index = _users.get(clientPubKey).postAnnouncementBoard(a);
            // client's public key is used to indicate it's stored in that client's
            // PostOperation Board
            _announcementMapper.put(i.getAnnouncementID(), new AnnouncementLocation(clientPubKey, index));
        }

        // Retrieve _operations from database

        List<OperationsBoardStructure> obs = dbs.getOperations();
        for (OperationsBoardStructure i: obs) {
            User u = _users.get(getUserUUID(i.getClientUUID()));
            u.setToken(i.getOpUUID());
        }

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

    // 1 9002 9003 9001
    // 2 9001 9003
    // 3 9001 9002

    public void bestEffortBroadcast(VerifiableServerMessage vsm) {
        for (ServerThread t: _serverThreads) {
            System.out.println(t);
            System.out.println(vsm);
            t.bestEffortBroadcast(vsm);
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

    // TODO: verify if every time this is called  vpm is defined
    public void deliverPost(VerifiableProtocolMessage vpm, ClientMessageHandler cmh, String token, String newToken) {
        System.out.println("DELIVERPOST");
        // Save Operation
        String announcementUuid = UUIDGenerator.generateUUID();
        ProtocolMessage pm = vpm.getProtocolMessage();
        Announcement a = pm.getPostAnnouncement();
        a.setAnnouncementID(announcementUuid);
        a.setPublicKey(pm.getPublicKey());
        byte[] ref = ProtocolMessageConverter.objToByteArray(a.getReferences());

        byte[] b = ProtocolMessageConverter.objToByteArray(vpm);

        _db.insertAnnouncement(a.getAnnouncement(), ref, announcementUuid, getUserUUID(pm.getPublicKey()),b);

        int index = _users.get(pm.getPublicKey()).postAnnouncementBoard(a);
        // client's public key is used to indicate it's stored in that client's PostOperation Board
        _announcementMapper.put(announcementUuid, new AnnouncementLocation(pm.getPublicKey(), index));

        _atomicRegisters1N.remove(pm.getPublicKey());

        System.out.flush();

        if (cmh != null) {
            cmh.sendMessage(createVerifiableMessage(
                    new ProtocolMessage("POST", StatusCode.OK, _pubKey, a, newToken, token)));
        }
    }

    public void deliverRead(VerifiableProtocolMessage vpm, ClientMessageHandler cmh, String token, String newToken, List<Announcement> announcements) {
        System.out.println("DELIVERREAD");

        ProtocolMessage pm = vpm.getProtocolMessage();

        _atomicRegisters1N.remove(pm.getPublicKey());

        System.out.flush();

        if (cmh != null) {
            cmh.sendMessage(createVerifiableMessage(
                    new ProtocolMessage("READ", StatusCode.OK, _pubKey, announcements, newToken, token)));
        }
    }
/*
    public void deliverPostGeneral(VerifiableProtocolMessage vpm, ClientMessageHandler cmh, String token, String newToken) {
        System.out.println("DELIVERPOSTGENERAL");
        // Save Operation
        String announcementUuid = UUIDGenerator.generateUUID();
        ProtocolMessage pm = vpm.getProtocolMessage();
        Announcement a = pm.getPostAnnouncement();
        a.setAnnouncementID(announcementUuid);
        a.setPublicKey(pm.getPublicKey());
        byte[] ref = ProtocolMessageConverter.objToByteArray(a.getReferences());

        byte[] b = ProtocolMessageConverter.objToByteArray(vpm);

        _db.insertAnnouncement(a.getAnnouncement(), ref, announcementUuid, getUserUUID(pm.getPublicKey()),b);

        int index = _users.get(pm.getPublicKey()).postAnnouncementBoard(a);
        // client's public key is used to indicate it's stored in that client's PostOperation Board
        _announcementMapper.put(announcementUuid, new AnnouncementLocation(pm.getPublicKey(), index));

        System.out.flush();

        if (cmh != null) {
            cmh.sendMessage(createVerifiableMessage(
                    new ProtocolMessage("POSTGENERAL", StatusCode.OK, _pubKey, a, newToken, token)));
        }
    }

    public void deliverReadGeneral(List<Announcement> announcements, PublicKey clientPublicKey) {
        System.out.println("DELIVERREADGENERAL");

        System.out.flush();

        ClientMessageHandler cmh = _users.get(clientPublicKey).getCmh();

        String token= null;
        String newToken = null;

        if (cmh != null) {
            cmh.sendMessage(createVerifiableMessage(
                    new ProtocolMessage("READGENERAL", StatusCode.OK, _pubKey, announcements, newToken, token)));
        }
    }*/

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
                System.out.println("received new client connection in server");
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

    /**
     * Verifies if an operation request is valid, which means having a unique id and
     * signature to ensure the message was not tampered with or replayed.
     * 
     * @param token
     * @return StatusCode
     */
    public StatusCode verifyInvalidToken(PublicKey userPubKey, String token) {

        if (!_users.get(userPubKey).getToken().equals(token)) {
            System.out.println("verifyInvalidToken");
            System.out.println("Invalid token: " + _users.get(userPubKey).getToken());
            System.out.println("token that should be: " + token);
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
                || clientPubKey == null || vpm.getProtocolMessage() == null || vpm.getSignedProtocolMessage() == null) {
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

        if(!vpm.getProtocolMessage().getPublicKey().equals(vpm.getProtocolMessage().getAtomicRegisterMessages().getValues().get(0).getClientPublicKey()))
            return StatusCode.INVALID_ANNOUNCEMENT_PUBLIC_KEY;
        return sc;
    }

    public StatusCode verifyWriteBack(String token, VerifiableProtocolMessage vpm, PublicKey clientPubKey) {
        StatusCode sc;

        if (verifyNullToken(token).equals(StatusCode.NULL_FIELD) || vpm == null
                || clientPubKey == null || vpm.getProtocolMessage() == null || vpm.getSignedProtocolMessage() == null) {
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
                || vpm.getProtocolMessage() == null || vpm.getSignedProtocolMessage() == null) {
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

    public void printStatusCodeDescription(StatusCode sc) {
        System.out.println("======" + sc.getDescription() + "======");
    }

    /**
     * Registers the user and associated public key in the system before first use.
     * Makes necessary initializations to enable first use of DPAS
     * @param vpm
     * @return ProtocolMessage
     */

    public VerifiableProtocolMessage registerUser(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {


        StatusCode sc;

        VerifiableProtocolMessage response;
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();

        if (vpm == null || clientPubKey == null || vpm.getProtocolMessage() == null ||
                vpm.getSignedProtocolMessage() == null) {
            
            response = createVerifiableMessage(new ProtocolMessage(
                    "REGISTER", StatusCode.NULL_FIELD, _pubKey));
            
            // _db.insertOperation(opUuid, operation);

            return response;
        }

        sc = verifySignature(vpm);
        if (!sc.equals(StatusCode.OK)) {
            response = createVerifiableMessage(new ProtocolMessage(
                    "REGISTER", sc, _pubKey));

            // _db.insertOperation(opUuid, operation);

            return response;
        }

        sc = verifyUserRegistered(clientPubKey);
        if (sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            String i = UUIDGenerator.generateUUID();
            String uuid = "T" + i;
            User user = new User(clientPubKey, uuid, new AtomicRegister1N(), cmh);
            user.setRandomToken();
            String token = user.getToken();
            _users.put(clientPubKey, user);
            _db.createUserTable(uuid);

            byte[] b = ProtocolMessageConverter.objToByteArray(clientPubKey);

            _db.insertUser(b, uuid);

            System.out.println("register token: " + token);

            response = createVerifiableMessage(new ProtocolMessage(
                "REGISTER", StatusCode.OK, _pubKey, token));

            _db.createOperationUserRow(uuid, user.getToken());

            return response;
        }
        // user is already registered
        User user = _users.get(clientPubKey);
        user.setRandomToken();
        String token = user.getToken();

        System.out.println("user is already registered token: " + token);
        response = createVerifiableMessage(new ProtocolMessage(
                "REGISTER", sc, _pubKey, token));

        _db.updateOperationUserRow(user.getdbTableName(), user.getToken());
        
        return response;
    }

    /**
     * Posts an announcement of up to 255 characters to the user's PostOperation Board.
     * Can refer to previous announcements.
     * @param vpm
     * @return ProtocolMessage
     */
    public void post(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {
        StatusCode sc;

        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String message = vpm.getProtocolMessage().getPostAnnouncement().getAnnouncement();
        List<String> references = vpm.getProtocolMessage().getPostAnnouncement().getReferences();
        String token = vpm.getProtocolMessage().getToken();
        System.out.println("token: " + token);

        // verifications
        sc = verifyPost(token, vpm, message, references, clientPubKey);
        if (sc.equals(StatusCode.NULL_FIELD) || sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                "POST", sc, _pubKey)));
                return;
        }
        User user = _users.get(clientPubKey);
        user.setRandomToken();
        String newToken = user.getToken();
        System.out.println("newtoken: " + newToken);
        _db.updateOperationUserRow(user.getdbTableName(), newToken);
        if (sc.equals(StatusCode.INVALID_TOKEN)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                "POST", sc, _pubKey, newToken, token)));
            return;
        }
        if (!sc.equals(StatusCode.OK)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                    "POST", sc, _pubKey, newToken, token)));
            // TODO: should this be anything ????
            if (!sc.equals(StatusCode.NULL_FIELD)) {
                // _db.insertOperation(opUuid, operation);
            }
            return;
        }
        
        AtomicRegisterMessages arm = _users.get(clientPubKey).getAtomicRegister1N().acknowledge(vpm.getProtocolMessage().getAtomicRegisterMessages());
        ProtocolMessage p = new ProtocolMessage("ACK", sc, _pubKey, newToken, token);
        p.setAtomicRegisterMessages(arm);
        cmh.sendMessage(createVerifiableMessage(p));
        return;

        // Broadcast, each thread sends to a server
        /*for(ServerThread t: _serverThreads) {
            t.writeValue(clientPubKey, vpm.getProtocolMessage().getPostAnnouncement());
        }*/

    }

    public void writeBack(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {
        StatusCode sc;

        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String token = vpm.getProtocolMessage().getToken();
        System.out.println("token: " + token);

        // verifications
        sc = verifyWriteBack(token, vpm, clientPubKey);
        if (sc.equals(StatusCode.NULL_FIELD) || sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                "POST", sc, _pubKey)));
                return;
        }
        User user = _users.get(clientPubKey);
        user.setRandomToken();
        String newToken = user.getToken();
        System.out.println("newtoken: " + newToken);
        _db.updateOperationUserRow(user.getdbTableName(), newToken);
        if (sc.equals(StatusCode.INVALID_TOKEN)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                "POST", sc, _pubKey, newToken, token)));
            return;
        }
        if (!sc.equals(StatusCode.OK)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                    "POST", sc, _pubKey, newToken, token)));
            // TODO: should this be anything ????
            if (!sc.equals(StatusCode.NULL_FIELD)) {
                // _db.insertOperation(opUuid, operation);
            }
            return;
        }
        
        AtomicRegisterMessages arm = _users.get(clientPubKey).getAtomicRegister1N().acknowledge(vpm.getProtocolMessage().getAtomicRegisterMessages());
        ProtocolMessage p = new ProtocolMessage("ACK", sc, _pubKey, newToken, token);
        p.setAtomicRegisterMessages(arm);
        cmh.sendMessage(createVerifiableMessage(p));
        return;

        // Broadcast, each thread sends to a server
        /*for(ServerThread t: _serverThreads) {
            t.writeValue(clientPubKey, vpm.getProtocolMessage().getPostAnnouncement());
        }*/

    }


    /**
     * Posts an announcement of up to 255 characters in the General Board.
     * Can refer to previous announcements.
     * @param vpm
     * @return ProtocolMessage
     */
    public void postGeneral(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {
        StatusCode sc;

        VerifiableProtocolMessage response;
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String message = vpm.getProtocolMessage().getPostAnnouncement().getAnnouncement();
        List<String> references = vpm.getProtocolMessage().getPostAnnouncement().getReferences();
        String token = vpm.getProtocolMessage().getToken();

        // verifications
        sc = verifyPost(token, vpm, message, references, clientPubKey);
        if (sc.equals(StatusCode.NULL_FIELD) || sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage("POSTGENERAL", sc, _pubKey)));
            return;
        }
        User user = _users.get(clientPubKey);
        user.setRandomToken();
        String newToken = user.getToken();
        _db.updateOperationUserRow(user.getdbTableName(), user.getToken());
        if (sc.equals(StatusCode.INVALID_TOKEN)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                "POSTGENERAL", sc, _pubKey, newToken, token)));
            return;
        }
        if (!sc.equals(StatusCode.OK)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                    "POSTGENERAL", sc, _pubKey, newToken, token)));
            if (!sc.equals(StatusCode.NULL_FIELD)) {
                // _db.insertOperation(opUuid, operation);
            }
            return;
        }

        AtomicRegisterMessages arm = _regularRegisterNN.acknowledge(vpm.getProtocolMessage());
        ProtocolMessage p = new ProtocolMessage("ACK", sc, _pubKey, newToken, token);
        p.setAtomicRegisterMessages(arm);
        cmh.sendMessage(createVerifiableMessage(p));
    }

    public String getUserUUID(PublicKey publicKey) {
        return _users.get(publicKey).getdbTableName(); 
    }

    /**
     * Obtains the most recent number announcements posted by the user with associated key
     * (from the user's PostOperation Board).
     * If number == 0, all announcements should be returned.
     * @param vpm
     * @return a list of announcements
     */
    public void read(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {
        StatusCode sc;

        VerifiableProtocolMessage response;

        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        int number = vpm.getProtocolMessage().getReadNumberAnnouncements();
        PublicKey toReadPublicKey = vpm.getProtocolMessage().getToReadPublicKey();
        String token = vpm.getProtocolMessage().getToken();

        // verifications
        if (toReadPublicKey == null) { cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                "READ", StatusCode.NULL_FIELD, _pubKey)));
                return; }
        sc = verifyUserRegistered(toReadPublicKey);
        if (sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                "READ", sc, _pubKey)));
            return;    
        }
        sc = verifyRead(token, vpm, clientPubKey);
        if (sc.equals(StatusCode.NULL_FIELD)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage("READ", sc, _pubKey)));
            return; 
        }
        
        User user = _users.get(clientPubKey);
        String newToken = user.getToken();
        _db.updateOperationUserRow(user.getdbTableName(), newToken);
        if (sc.equals(StatusCode.INVALID_TOKEN)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                "READ", sc, _pubKey, newToken, token)));
            return;
        }
        if (!sc.equals(StatusCode.OK)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage(
                    "READ", sc, _pubKey, newToken, token)));
            if (!sc.equals(StatusCode.NULL_FIELD)) {
                // _db.insertOperation(opUuid, operation);
            }
            return;
        }

        AtomicRegisterMessages arm = _users.get(clientPubKey).getAtomicRegister1N().value(vpm.getProtocolMessage().getAtomicRegisterMessages(), number);
        ProtocolMessage p = new ProtocolMessage("VALUE", sc, _pubKey, newToken, token);
        p.setAtomicRegisterMessages(arm);
        cmh.sendMessage(createVerifiableMessage(p));
        return;

        // Broadcast, each thread sends to a server
        /*for(ServerThread t: _serverThreads) {
            t.readValue(clientPubKey);
        }*/
    }

    /**
     * Obtains the most recent number announcements on the General Board.
     * If number == 0, all announcements should be returned.
     * @param vpm
     * @return a list of announcements
     */
    public void readGeneral(VerifiableProtocolMessage vpm, ClientMessageHandler cmh) {
        StatusCode sc;

        VerifiableProtocolMessage response;

        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String token = vpm.getProtocolMessage().getToken();


        // verifications
        sc = verifyRead(token, vpm, clientPubKey);
        if (sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage("READGENERAL", sc, _pubKey))); 
            return;       
        }
        if (sc.equals(StatusCode.NULL_FIELD)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage("READGENERAL", sc, _pubKey)));
            return;
        }

        User user = _users.get(clientPubKey);
        user.setRandomToken();
        String newToken = user.getToken();
        _db.updateOperationUserRow(user.getdbTableName(), user.getToken());

        if (!sc.equals(StatusCode.OK)) {
            cmh.sendMessage(createVerifiableMessage(new ProtocolMessage("READGENERAL", sc, _pubKey, newToken, token)));   
            if (!sc.equals(StatusCode.NULL_FIELD)) { ;
                // _db.insertOperation(opUuid, operation);
            }
            return;
        }

        AtomicRegisterMessages arm = _regularRegisterNN.value(vpm.getProtocolMessage());
        ProtocolMessage p = new ProtocolMessage("VALUEGENERAL", sc, _pubKey, newToken, token);
        p.setAtomicRegisterMessages(arm);
        cmh.sendMessage(createVerifiableMessage(p));
    }

    // // does not make changes to the system, so it does not need to taken into account in _operations
    public VerifiableProtocolMessage invalidCommand(VerifiableProtocolMessage vpm) {
        // TODO: make something in client to support this

        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String token = vpm.getProtocolMessage().getToken();

        User user = _users.get(clientPubKey);
        user.setRandomToken();
        String newToken = user.getToken();
        _db.updateOperationUserRow(user.getdbTableName(), newToken);

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
        String newToken = user.getToken();

        response = createVerifiableMessage(new ProtocolMessage(
                "TOKEN", StatusCode.OK, _pubKey, newToken));

        _db.updateOperationUserRow(user.getdbTableName(), user.getToken());
        
        return response;
    }

    public List<Announcement> getUserAnnouncements(PublicKey clientPublicKey) {
        return _users.get(clientPublicKey).getAllAnnouncements();
    }

    /*public List<Announcement> getUserAnnouncements(PublicKey clientPublicKey, int number) {
        // TODO: synchronize ????
        User user = _users.get(clientPublicKey);
        int nAnnouncements = user.getNumAnnouncements();
        if ((0 < number) && (number <= nAnnouncements)) {
            return new ArrayList<>(user.getAnnouncements(number));
        }
        else {
            return user.getAllAnnouncements();
        }
    }*/

    public PublicKey getPublicKey() { return _pubKey; }
    protected PrivateKey getPrivateKey() { return _privateKey; }
}
