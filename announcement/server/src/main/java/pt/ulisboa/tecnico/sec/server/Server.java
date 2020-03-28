package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.*;
import pt.ulisboa.tecnico.sec.database_lib.*;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.ArrayList;
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
     * operations in case a client resends it due to loss of message.
     */
    private ConcurrentHashMap<Integer, StatusCode> _operations;
    private ConcurrentHashMap<PublicKey, User> _users;
    /**
     * maps the announcement unique id to the public key of the entity
     * where it's stored and the index on the PostOperation Board,
     * if it's the server's public key it's in the
     * General Board, otherwise it's in the respective User's PostOperation Board
     */
    private ConcurrentHashMap<Integer, AnnouncementLocation> _announcementMapper;
    // TODO: in General Board, posts should remain accountable, so should the value be a signature(post) + post = PostOperation?
    private List<PostOperation> _generalBoard;
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
            System.out.println("Error: Not possible to initialize server because it was not possible to load keystore.\n" + e);
            System.exit(-1);
        }
        try {
            _privateKey = KeyStorage.loadPrivateKey(entryPasswd, alias, keyStore);
        } catch (Exception e) {
            System.out.println("Error: Not possible to initialize server because it was not possible to load private key.\n" + e);
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
            SignatureUtil.verifySignature(vpm.getSignedProtocolMessage(), vpm.getProtocolMessage().getPublicKey(), bpm);
            return StatusCode.OK;
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

        return StatusCode.DUPLICATE_OPERATION;
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
        if (!_users.containsKey(clientPubKey)) {
            return StatusCode.USER_NOT_REGISTERED;
        }
        return StatusCode.OK;
    }

    public StatusCode verifyPost(int opUuid, VerifiableProtocolMessage vpm, String message,
                                 List<Integer> references, PublicKey clientPubKey) {
        StatusCode sc;

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
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();

        sc = verifySignature(vpm);
        if (!sc.equals(StatusCode.OK)) {
            return createVerifiableMessage(new ProtocolMessage(
                    "REGISTER", sc, _pubKey, vpm.getProtocolMessage().getOpUuid()));
        }
        sc = verifyUserRegistered(clientPubKey);
        if (sc.equals(StatusCode.OK)) {
            int i = UUIDGenerator.generateUUID();
            String uuid = "T" + Integer.toString(i);
            User user = new User(clientPubKey, uuid);
            _users.put(clientPubKey, user);
            _db.createUserTable(uuid);

            byte[] b = ProtocolMessageConverter.objToByteArray(clientPubKey);
            byte[] encodedhash = null;
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                encodedhash = digest.digest(b);
            }
            catch(NoSuchAlgorithmException e) {
                System.out.println(e);
            }

            _db.insertUser(encodedhash, uuid);
        }
        else {
            return createVerifiableMessage(new ProtocolMessage(
                "REGISTER", sc, _pubKey, vpm.getProtocolMessage().getOpUuid()));
        }

        return createVerifiableMessage(new ProtocolMessage(
                "REGISTER", StatusCode.OK, _pubKey, vpm.getProtocolMessage().getOpUuid()));
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
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String message = vpm.getProtocolMessage().getPostAnnouncement().getAnnouncement();
        List<Integer> references = vpm.getProtocolMessage().getPostAnnouncement().getReferences();

        // verifications
        sc = verifyPost(opUuid, vpm, message, references, clientPubKey);
        if (!sc.equals(StatusCode.OK)) {
            return createVerifiableMessage(new ProtocolMessage(
                    "POST", sc, _pubKey, vpm.getProtocolMessage().getOpUuid()));
        }

        System.out.println("User posting announcement in user table");

        // Save Operation
        System.out.println("User posting announcement in user table");
        int uuid = UUIDGenerator.generateUUID();
        /*PostOperation newAnnouncement = new PostOperation(opUuid, message, pubKey, announcements, clientSignature);
        StatusCode signStatus = verifyOperation(newAnnouncement);
        if (signStatus.equals(StatusCode.OK)) {
            status = signStatus;
            int index =_users.get(pubKey).postAnnouncementBoard(newAnnouncement);
            // client's public key is used to indicate it's stored in that client's PostOperation Board
            _announcementMapper.put(uuid, new AnnouncementLocation(pubKey, index));
        }*/
        ProtocolMessage pm = vpm.getProtocolMessage();
        Announcement a = pm.getPostAnnouncement();
        byte[] ref = ProtocolMessageConverter.objToByteArray(a.getReferences());
        _db.insertAnnouncement(a.getAnnouncement(), ref, uuid, getUserUUID(pm.getPublicKey()));

        return createVerifiableMessage(new ProtocolMessage(
                "POST", StatusCode.OK, _pubKey, vpm.getProtocolMessage().getOpUuid()));
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
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        String message = vpm.getProtocolMessage().getPostAnnouncement().getAnnouncement();
        List<Integer> references = vpm.getProtocolMessage().getPostAnnouncement().getReferences();

        // verifications
        sc = verifyPost(opUuid, vpm, message, references, clientPubKey);
        if (!sc.equals(StatusCode.OK)) {
            return createVerifiableMessage(new ProtocolMessage(
                    "POST", sc, _pubKey, vpm.getProtocolMessage().getOpUuid()));
        }

        System.out.println("User posting announcement in general board");
        int uuid = UUIDGenerator.generateUUID();
        /*PostOperation newAnnouncement = new PostOperation(opUuid, message, pubKey, announcements, clientSignature);
        StatusCode signStatus = verifyOperation(newAnnouncement);
        System.out.println("Signature status code: " + signStatus);
        if (signStatus.equals(StatusCode.OK)) {
            status = signStatus;
            int index;
            synchronized (_generalBoard) {
                index = _generalBoard.size();
                _generalBoard.add(newAnnouncement);
            }
            // server's public key is used to indicate it's stored in the General Board
            _announcementMapper.put(uuid, new AnnouncementLocation(_pubKey, index));
        }*/
        ProtocolMessage pm = vpm.getProtocolMessage();
        Announcement a = pm.getPostAnnouncement();
        byte[] ref = ProtocolMessageConverter.objToByteArray(a.getReferences());
        _db.insertAnnouncementGB(a.getAnnouncement(), ref, uuid, getUserUUID(pm.getPublicKey()));

        return createVerifiableMessage(new ProtocolMessage("POSTGENERAL", StatusCode.OK, _pubKey, vpm.getProtocolMessage().getOpUuid()));
    }

    public String getUserUUID(PublicKey publicKey) {
        return _users.get(publicKey).getdbTableName(); 
    }

    /**
     * Obtains the most recent number announcements posted by the user with associated key
     * (from the user's PostOperation Board).
     * If number == 0, all announcements should be returned.
     * @param pubKey
     * @param number of announcements to be returned
     * @return a list of announcements
     */
    public List<PostOperation> read(PublicKey pubKey, int number) {
        // TODO : signature
        User user =_users.get(pubKey);
        if (0 < number && number <= user.getNumAnnouncements()) {
            return user.getAnnouncements(number);
        }
        else {
            return user.getAllAnnouncements(); // TODO: even if it's an invalid number, like -1
        }
    }

    /**
     * Obtains the most recent number announcements on the General Board.
     * If number == 0, all announcements should be returned.
     * @param number
     * @return a list of announcements
     */
    public List<PostOperation> readGeneral(int number) {
        // TODO : signature
        synchronized (_generalBoard) {
            int nAnnouncements = _generalBoard.size();
            if (0 < number && number <= nAnnouncements) {
                return _generalBoard.subList(nAnnouncements - number, nAnnouncements);
            }
            else {
                return _generalBoard;
            }
        }

    }
}
