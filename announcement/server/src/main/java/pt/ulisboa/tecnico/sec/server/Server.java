package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.*;
import pt.ulisboa.tecnico.sec.database_lib.*;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLEngineResult.Status;

public class Server {

    private ServerSocket _serverSocket;
    private Socket _socket;

    private PublicKey _pubKey;
    private PrivateKey _privateKey;
    private Database _db;
    /** Maps the unique id of an operation to the status it received so that
     * we can know if  a message was replayed or if it's fresh and to not repeat
     * operations in case a client resends it due to loss of message. Sends the
     * message that was originally formulated to answer that request.
     */
    private ConcurrentHashMap<Integer, VerifiableProtocolMessage> _operations;
    private ConcurrentHashMap<PublicKey, User> _users; // TODO : is synchronization required inside userdata structures
    /**
     * maps the announcement unique id to the public key of the entity
     * where it's stored and the index on the PostOperation Board,
     * if it's the server's public key it's in the
     * General Board, otherwise it's in the respective User's PostOperation Board
     */
    private ConcurrentHashMap<Integer, AnnouncementLocation> _announcementMapper;
    private List<Announcement> _generalBoard;
    private Communication _communication;

    public Server(boolean activateCC, char[] keyStorePasswd, char[] entryPasswd, String alias,
                  String pubKeyPath, String keyStorePath) {
        loadPublicKey(pubKeyPath);
        loadPrivateKey(keyStorePath, keyStorePasswd, entryPasswd, alias);
        _operations = new ConcurrentHashMap<>();
        _users = new ConcurrentHashMap<>();
        _announcementMapper = new ConcurrentHashMap<>();
        // TODO: see if a CopyOnWriteArrayList is more suitable (if very few writes and lots of reads)
        _generalBoard = new ArrayList<>();
        _communication = new Communication();
        _db = new Database();
        
        retrieveDataStructures();
        //printDataStructures();
    }

    public PublicKey getUserUUID(String uuid) {
        for(User u: _users.values()) {
            if (u.getdbTableName().equals(uuid)) {
                return u.getPublicKey();
            }
        }
        return null;
    }

    public void printDataStructures() {
        System.out.println("_users: " + _users + "END");
        System.out.println("_generalBoard: " + _generalBoard + "END");
        System.out.println("_announcementMapper: " + _announcementMapper + "END");
        System.out.println("_operations: " + _operations + "END");
    }

    public void retrieveDataStructures() {
        DBStructure dbs = _db.retrieveStructure();
        //Retrieve _users from database
        List<UserStructure> us = dbs.getUsers();
        for (UserStructure i: us) {
            PublicKey pk = (PublicKey) ProtocolMessageConverter.byteArrayToObj(i.getPublicKey());
            User u = new User(pk, i.getClientUUID());
            _users.put(pk, u);
        }

        //Retrieve _generalBoard from database
        List<GeneralBoardStructure> gbs = dbs.getGeneralBoard();
        for (GeneralBoardStructure i: gbs) {
            List<Integer> references = (List<Integer>) ProtocolMessageConverter.byteArrayToObj(i.getReferences());
            Announcement a = new Announcement(i.getAnnouncement(), references, i.getAnnouncementID(), i.getClientUUID());
            
            int index;
            synchronized (_generalBoard) {
                index = _generalBoard.size();
                _generalBoard.add(a);
            }
            // server's public key is used to indicate it's stored in the General Board
            _announcementMapper.put(i.getAnnouncementID(), new AnnouncementLocation(_pubKey, index));
        }

        //Retrieve _announcementMapper from database
        List<UserBoardStructure> ubs = dbs.getUserBoard();
        for (UserBoardStructure i: ubs) {
            List<Integer> references = (List<Integer>) ProtocolMessageConverter.byteArrayToObj(i.getReferences());
            Announcement a = new Announcement(i.getAnnouncement(), references, i.getAnnouncementID(), i.getClientUUID());
            PublicKey clientPubKey = getUserUUID(i.getClientUUID());

            int index = _users.get(clientPubKey).postAnnouncementBoard(a);
            // client's public key is used to indicate it's stored in that client's PostOperation Board
            _announcementMapper.put(i.getAnnouncementID(), new AnnouncementLocation(clientPubKey, index));
        }

        //Retrieve _operations from database
        List<OperationsBoardStructure> obs = dbs.getOperations();
        for (OperationsBoardStructure i: obs) {
            VerifiableProtocolMessage vpm = (VerifiableProtocolMessage) ProtocolMessageConverter.byteArrayToObj(i.getOperation());
            _operations.put(i.getOpUUID(), vpm);
        }
        
    }

    public void loadPublicKey(String pubKeyPath) {
        try {
            _pubKey = KeyPairUtil.loadPublicKey(pubKeyPath);
        } catch (Exception e) {
            System.out.println("Error: Not possible to initialize server because it was not possible to load public key.\n" + e);
            System.exit(-1);
        }
    }

    public void loadPrivateKey(String keyStorePath, char[] keyStorePasswd, char[] entryPasswd, String alias) {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStorage.loadKeyStore(keyStorePasswd, keyStorePath);
        } catch(Exception e) {
            System.out.println("Error: Not possible to initialize server because it " +
                    "was not possible to load keystore.\n" + e);
            System.exit(-1);
        }
        try {
            _privateKey = KeyStorage.loadPrivateKey(entryPasswd, alias, keyStore);
        } catch (Exception e) {
            System.out.println("Error: Not possible to initialize server because it " +
                    "was not possible to load private key.\n" + e);
            System.exit(-1);
        }
        if (_privateKey == null) {
            System.out.println("Error: Not possible to initialize server because it " +
                    "was not possible to load private key due to wrong password or alias.\n");
            System.exit(-1);
        }
    }

    public void startClientCommunication() {
        try {
            _serverSocket = _communication.createServerSocket(8888);
        }
        catch(IOException e) {
            System.out.println("Error starting server socket");
        }
    }
    /**
     * Opens new socket to listen for client communications and creates
     * a new Thread to handle each client connection.
     */
    public void start() {
        startClientCommunication();
        while(true) {
            try{
                _socket = _communication.accept(_serverSocket);
                new ClientMessageHandler(this, _socket).start();
            }
            catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public StatusCode verifySignature(VerifiableProtocolMessage vpm) {
        try {
            byte[] bpm = ProtocolMessageConverter.objToByteArray(vpm.getProtocolMessage());
            boolean verified = SignatureUtil.verifySignature(
                    vpm.getSignedProtocolMessage(), vpm.getProtocolMessage().getPublicKey(), bpm);
            if (verified == true) return StatusCode.OK;
            else return StatusCode.INVALID_SIGNATURE;
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println(StatusCode.INVALID_ALGORITHM + "\n" + e);
            return StatusCode.INVALID_ALGORITHM;
        }
        catch (InvalidKeyException e) {
            System.out.println(StatusCode.INVALID_KEY + "\n" + e);
            return StatusCode.INVALID_KEY;
        } 
        catch (SignatureException e) {
            System.out.println(StatusCode.INVALID_SIGNATURE + "\n" + e);
            return StatusCode.INVALID_SIGNATURE;
        }
    }

    /**
     * Verifies if an operation request is valid, which means having a unique id and signature
     * to ensure the message was not tampered with or replayed.
     * @param opUuid
     * @return StatusCode
     */
    public StatusCode verifyDupOperation(int opUuid) {
        // checks if this operation was already performed
        if (_operations.containsKey(opUuid)) return StatusCode.DUPLICATE_OPERATION;
        else return StatusCode.OK;
    }

    /**
     * Verifies if a message is valid to be posted.
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
     * @param references to be verified
     * @return StatusCode
     */
    public StatusCode verifyReferences(List<Integer> references) {
        /*for (int exRef : _announcementMapper.keySet()) {
            System.out.println("===REF===: " + exRef);
        }*/
        for (int reference : references) {
            if (!_announcementMapper.containsKey(reference)) {
                return StatusCode.INVALID_REFERENCE;
            }
        }
        return StatusCode.OK;
    }

    /**
     * Verifies if all the references added exist.
     * @param clientPubKey to be verified
     * @return StatusCode
     */
    public StatusCode verifyUserRegistered(PublicKey clientPubKey) {
        if (_users.containsKey(clientPubKey)) {
            return StatusCode.USER_ALREADY_REGISTERED;
        }
        return StatusCode.USER_NOT_REGISTERED;
    }

    public StatusCode verifyOpUuid(int opUuid) {
        if (opUuid <= 0) {
            return StatusCode.NULL_FIELD;
        }
        else {
            return StatusCode.OK;
        }
    }

    public StatusCode verifyPost(int opUuid, VerifiableProtocolMessage vpm, String message,
                                 List<Integer> references, PublicKey clientPubKey) {
        StatusCode sc;

        if (verifyOpUuid(opUuid).equals(StatusCode.NULL_FIELD) || vpm == null || message == null ||
                references == null || clientPubKey == null || vpm.getProtocolMessage() == null ||
                vpm.getSignedProtocolMessage() == null) {
            return StatusCode.NULL_FIELD;
        }

        sc = verifyDupOperation(opUuid);
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

        sc = verifyUserRegistered(clientPubKey);
        if (!sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            return StatusCode.OK;
        }

        return sc;
    }

    public StatusCode verifyRead(int opUuid, VerifiableProtocolMessage vpm, PublicKey clientPubKey) {
        StatusCode sc;

        if (verifyOpUuid(opUuid).equals(StatusCode.NULL_FIELD) || vpm == null || clientPubKey == null ||
                vpm.getProtocolMessage() == null || vpm.getSignedProtocolMessage() == null) {
            return StatusCode.NULL_FIELD;
        }

        sc = verifyDupOperation(opUuid);
        if (!sc.equals(StatusCode.OK)) {
            return sc;
        }

        sc = verifySignature(vpm);
        if (!sc.equals(StatusCode.OK)) {
            return sc;
        }

        sc = verifyUserRegistered(clientPubKey);
        if (!sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            return StatusCode.OK;
        }
        return sc;
    }


    public VerifiableProtocolMessage createVerifiableMessage(ProtocolMessage pm) {
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

    public void printStatusCodeDescription(StatusCode sc) {
        System.out.println("======" + sc.getDescription() + "======");
    }

    /**
     * Registers the user and associated public key in the system before first use.
     * Makes necessary initializations to enable first use of DPAS
     * @param vpm
     * @return ProtocolMessage
     */

    public VerifiableProtocolMessage registerUser(VerifiableProtocolMessage vpm) {
        StatusCode sc;

        int opUuid = vpm.getProtocolMessage().getOpUuid();

        VerifiableProtocolMessage response;
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();

        if (verifyOpUuid(opUuid).equals(StatusCode.NULL_FIELD) || vpm == null ||
                clientPubKey == null || vpm.getProtocolMessage() == null ||
                vpm.getSignedProtocolMessage() == null) {
            response = createVerifiableMessage(new ProtocolMessage(
                    "REGISTER", StatusCode.NULL_FIELD, _pubKey, opUuid));
            _operations.put(opUuid, response);

            byte[] operation = ProtocolMessageConverter.objToByteArray(response);
            _db.insertOperation(opUuid, operation);

            return response;
        }

        sc = verifyDupOperation(opUuid);
        if (sc.equals(StatusCode.DUPLICATE_OPERATION)) {
            return _operations.get(opUuid);
        }

        sc = verifySignature(vpm);
        if (!sc.equals(StatusCode.OK)) {
            response = createVerifiableMessage(new ProtocolMessage(
                    "REGISTER", sc, _pubKey, opUuid));
            _operations.put(opUuid, response);

            byte[] operation = ProtocolMessageConverter.objToByteArray(response);
            _db.insertOperation(opUuid, operation);

            return response;
        }

        sc = verifyUserRegistered(clientPubKey);
        if (sc.equals(StatusCode.USER_NOT_REGISTERED)) {
            int i = UUIDGenerator.generateUUID();
            String uuid = "T" + Integer.toString(i);
            User user = new User(clientPubKey, uuid);
            _users.put(clientPubKey, user);
            _db.createUserTable(uuid);

            byte[] b = ProtocolMessageConverter.objToByteArray(clientPubKey);

            _db.insertUser(b, uuid);

        }
        else {
            response = createVerifiableMessage(new ProtocolMessage(
                    "REGISTER", sc, _pubKey, vpm.getProtocolMessage().getOpUuid()));
            _operations.put(opUuid, response);

            byte[] operation = ProtocolMessageConverter.objToByteArray(response);
            _db.insertOperation(opUuid, operation);

            return response;
        }

        response = createVerifiableMessage(new ProtocolMessage(
                "REGISTER", StatusCode.OK, _pubKey, vpm.getProtocolMessage().getOpUuid()));
        _operations.put(opUuid, response);

        byte[] operation = ProtocolMessageConverter.objToByteArray(response);
        _db.insertOperation(opUuid, operation);

        return response;
    }

    /**
     * Posts an announcement of up to 255 characters to the user's PostOperation Board.
     * Can refer to previous announcements.
     * @param vpm
     * @return ProtocolMessage
     */
    public VerifiableProtocolMessage post(VerifiableProtocolMessage vpm) {
        StatusCode sc;

        int opUuid = vpm.getProtocolMessage().getOpUuid();
        //System.out.println("opUuid: " + opUuid);

        VerifiableProtocolMessage response;

        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String message = vpm.getProtocolMessage().getPostAnnouncement().getAnnouncement();
        List<Integer> references = vpm.getProtocolMessage().getPostAnnouncement().getReferences();

        // verifications
        sc = verifyPost(opUuid, vpm, message, references, clientPubKey);
        if (sc.equals(StatusCode.DUPLICATE_OPERATION)) {
            //System.out.println("duplicate operation!");
            //System.out.println("operation: " + _operations.get(opUuid));
            return _operations.get(opUuid);
        }
        if (!sc.equals(StatusCode.OK)) {
            response = createVerifiableMessage(new ProtocolMessage(
                    "POST", sc, _pubKey, vpm.getProtocolMessage().getOpUuid()));
            _operations.put(opUuid, response);

            byte[] operation = ProtocolMessageConverter.objToByteArray(response);
            _db.insertOperation(opUuid, operation);

            return response;
        }

        // Save Operation
        System.out.println("User posting announcement in user table");
        int announcementUuid = UUIDGenerator.generateUUID();
        ProtocolMessage pm = vpm.getProtocolMessage();
        Announcement a = pm.getPostAnnouncement();
        a.setAnnouncementID(announcementUuid);
        byte[] ref = ProtocolMessageConverter.objToByteArray(a.getReferences());

        byte[] b = ProtocolMessageConverter.objToByteArray(vpm);

        _db.insertAnnouncement(a.getAnnouncement(), ref, announcementUuid, getUserUUID(pm.getPublicKey()),b);

        int index = _users.get(clientPubKey).postAnnouncementBoard(a);
        // client's public key is used to indicate it's stored in that client's PostOperation Board
        _announcementMapper.put(announcementUuid, new AnnouncementLocation(clientPubKey, index));

        response = createVerifiableMessage(new ProtocolMessage(
                "POST", StatusCode.OK, _pubKey, vpm.getProtocolMessage().getOpUuid(), a));
        _operations.put(opUuid, response);

        byte[] operation = ProtocolMessageConverter.objToByteArray(response);
        _db.insertOperation(opUuid, operation);

        printDataStructures();

        return response;
    }

    /**
     * Posts an announcement of up to 255 characters in the General Board.
     * Can refer to previous announcements.
     * @param vpm
     * @return ProtocolMessage
     */
    public VerifiableProtocolMessage postGeneral(VerifiableProtocolMessage vpm) {

        StatusCode sc;

        int opUuid = vpm.getProtocolMessage().getOpUuid();

        VerifiableProtocolMessage response;
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String message = vpm.getProtocolMessage().getPostAnnouncement().getAnnouncement();
        List<Integer> references = vpm.getProtocolMessage().getPostAnnouncement().getReferences();

        // verifications
        sc = verifyPost(opUuid, vpm, message, references, clientPubKey);
        if (sc.equals(StatusCode.DUPLICATE_OPERATION)) {
            return _operations.get(opUuid);
        }
        if (!sc.equals(StatusCode.OK)) {
            response = createVerifiableMessage(new ProtocolMessage(
                    "POST", sc, _pubKey, vpm.getProtocolMessage().getOpUuid()));
            _operations.put(opUuid, response);
            byte[] operation = ProtocolMessageConverter.objToByteArray(response);
            _db.insertOperation(opUuid, operation);
            return response;
        }

        System.out.println("User posting announcement in general board");
        int announcementUuid = UUIDGenerator.generateUUID();

        ProtocolMessage pm = vpm.getProtocolMessage();
        Announcement a = pm.getPostAnnouncement();
        a.setAnnouncementID(announcementUuid);
        byte[] ref = ProtocolMessageConverter.objToByteArray(a.getReferences());

        _db.insertAnnouncementGB(a.getAnnouncement(), ref, announcementUuid, getUserUUID(pm.getPublicKey()));

        int index;
        synchronized (_generalBoard) {
            index = _generalBoard.size();
            _generalBoard.add(a);
        }
        // server's public key is used to indicate it's stored in the General Board
        _announcementMapper.put(announcementUuid, new AnnouncementLocation(_pubKey, index));

        response = createVerifiableMessage(new ProtocolMessage(
                "POSTGENERAL", StatusCode.OK, _pubKey, vpm.getProtocolMessage().getOpUuid(), a));
        _operations.put(opUuid, response);
        byte[] operation = ProtocolMessageConverter.objToByteArray(response);
        _db.insertOperation(opUuid, operation);
        return response;
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
    public VerifiableProtocolMessage read(VerifiableProtocolMessage vpm) {
        StatusCode sc;

        int opUuid = vpm.getProtocolMessage().getOpUuid();

        VerifiableProtocolMessage response;

        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        int number = vpm.getProtocolMessage().getReadNumberAnnouncements();
        PublicKey toReadPublicKey = vpm.getProtocolMessage().getToReadPublicKey();

        // verifications
        if (toReadPublicKey == null) return createVerifiableMessage(new ProtocolMessage(
                "READ", StatusCode.NULL_FIELD, _pubKey, vpm.getProtocolMessage().getOpUuid()));
        sc = verifyUserRegistered(toReadPublicKey);
        if (sc.equals(StatusCode.USER_NOT_REGISTERED)) return createVerifiableMessage(new ProtocolMessage(
                "READ", sc, _pubKey, vpm.getProtocolMessage().getOpUuid()));
        sc = verifyRead(opUuid, vpm, clientPubKey);
        if (sc.equals(StatusCode.DUPLICATE_OPERATION)) {
            return _operations.get(opUuid);
        }
        if (!sc.equals(StatusCode.OK)) {
            response = createVerifiableMessage(new ProtocolMessage(
                    "READ", sc, _pubKey, vpm.getProtocolMessage().getOpUuid()));
            _operations.put(opUuid, response);
            byte[] operation = ProtocolMessageConverter.objToByteArray(response);
            _db.insertOperation(opUuid, operation);
            return response;
        }

        System.out.println("User reading announcement from user board");

        byte[] b = ProtocolMessageConverter.objToByteArray(toReadPublicKey);
        byte[] encodedhash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            encodedhash = digest.digest(b);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }

        ProtocolMessage pm = vpm.getProtocolMessage();
        // System.out.println("checking database");
        // List<Announcement> announcements = _db.getUserAnnouncements(number, encodedhash);
        // TODO: Announcements need to have a signature
        List<Announcement> announcements;
        int nAnnouncements = _users.get(toReadPublicKey).getNumAnnouncements();
        if ((0 < number) && (number <= nAnnouncements)) {
            announcements = new ArrayList<>(_users.get(toReadPublicKey).getAnnouncements(number));
        }
        else {
            announcements = _users.get(toReadPublicKey).getAllAnnouncements();
        }
        response = createVerifiableMessage(new ProtocolMessage(
                "READ", StatusCode.OK, _pubKey, pm.getOpUuid(), announcements));
        _operations.put(opUuid, response);
        byte[] operation = ProtocolMessageConverter.objToByteArray(response);
        _db.insertOperation(opUuid, operation);
        return response;
    }

    /**
     * Obtains the most recent number announcements on the General Board.
     * If number == 0, all announcements should be returned.
     * @param vpm
     * @return a list of announcements
     */
    public VerifiableProtocolMessage readGeneral(VerifiableProtocolMessage vpm) {
        StatusCode sc;

        int opUuid = vpm.getProtocolMessage().getOpUuid();

        VerifiableProtocolMessage response;

        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        int number = vpm.getProtocolMessage().getReadNumberAnnouncements();

        // verifications
        sc = verifyRead(opUuid, vpm, clientPubKey);
        if (sc.equals(StatusCode.DUPLICATE_OPERATION)) {
            return _operations.get(opUuid);
        }
        if (!sc.equals(StatusCode.OK)) {
            response = createVerifiableMessage(new ProtocolMessage(
                    "READGENERAL", sc, _pubKey, vpm.getProtocolMessage().getOpUuid()));
            _operations.put(opUuid, response);
            byte[] operation = ProtocolMessageConverter.objToByteArray(response);
            _db.insertOperation(opUuid, operation);
            return response;
        }
        // TODO : this if is wrong!
        // TODO : access memmory
        System.out.println("User reading announcement from general board");
        ProtocolMessage pm = vpm.getProtocolMessage();
        // List<Announcement> announcements = _db.getGBAnnouncements(number);
        List<Announcement> announcements;
        int nAnnouncements = _generalBoard.size();
        if ((0 < number) && (number <= nAnnouncements)) {
            announcements = new ArrayList<>(_generalBoard.subList(nAnnouncements - number, nAnnouncements));
        }
        else {
            announcements = _generalBoard;
        }
        // TODO: Announcements need to have a signature

        response = createVerifiableMessage(new ProtocolMessage(
                "READGENERAL", StatusCode.OK, _pubKey, pm.getOpUuid(), announcements));
        _operations.put(opUuid, response);
        byte[] operation = ProtocolMessageConverter.objToByteArray(response);
        _db.insertOperation(opUuid, operation);
        return response;
    }

    // does not make changes to the system, so it does not need to taken into account in _operations
    public VerifiableProtocolMessage invalidCommand(VerifiableProtocolMessage vpm) {

        int opUuid = vpm.getProtocolMessage().getOpUuid();

        // TODO: make something in client to support this
        return createVerifiableMessage(new ProtocolMessage(
                "INVALID", StatusCode.INVALID_COMMAND, _pubKey, opUuid));

    }
}
