package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.Communication;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyPairUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyStorage;
import pt.ulisboa.tecnico.sec.crypto_lib.SignatureUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.UUIDGenerator;

import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private int _port;
    private PublicKey _pubKey;
    private PrivateKey _privateKey;
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

    public Server(boolean activateCC, int port, char[] keyStorePasswd, char[] entryPasswd, String alias) {
        loadPublicKey();
        loadPrivateKey(keyStorePasswd, entryPasswd, alias);
        _port = port;
        _operations = new ConcurrentHashMap<>();
        _users = new ConcurrentHashMap<>();
        _announcementMapper = new ConcurrentHashMap<>();
        // TODO: see if a CopyOnWriteArrayList is more suitable (if very few writes and lots of reads)
        _generalBoard = new ArrayList<>();
        _communication = new Communication();
    }

    public void loadPublicKey() {
        try {
            _pubKey = KeyPairUtil.loadPublicKey("src/main/resources/crypto/public.key");
        } catch (Exception e) {
            System.out.println("Error: Not possible to initialize server because it was not possible to load public key.\n" + e);
            System.exit(-1);
        }
    }

    public void loadPrivateKey(char[] keyStorePasswd, char[] entryPasswd, String alias) {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStorage.loadKeyStore(keyStorePasswd, "src/main/resources/crypto/server_keystore.jks");
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

    /**
     * Opens new socket to listen for client communications and creates
     * a new Thread to handle each client connection.
     */
    public void start() {
        new ClientConnectionHandler(this).start();
    }

    /**
     * Registers the user and associated public key in the system before first use.
     * Makes necessary initializations to enable first use of DPAS
     * @param pubKey
     */
    public void registerUser(PublicKey pubKey) {
        User user = new User(pubKey);
        _users.put(pubKey, user);
    }

    /**
     * Verifies if an operation request is valid, which means having a unique id and signature
     * to ensure the message was not tampered with or replayed.
     * @param operation
     * @param signature
     * @return StatusCode
     */
    public StatusCode verifyOperation(Operation operation, byte[] signature) {
        try {
            boolean verified = SignatureUtil.verifySignature(signature, operation.getPubKey(), operation.getBytes());
            if (verified == false) {
                System.out.println(StatusCode.INVALID_SIGNATURE);
                return StatusCode.INVALID_SIGNATURE;
            }
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error: Algorithm used to verify signature is not valid.\n" + e);
            // TODO: return statusCode?
        }
        catch (InvalidKeyException e) {
            System.out.println(StatusCode.INVALID_KEY + "\n" + e);
            return StatusCode.INVALID_KEY;
        } catch (SignatureException e) {
            System.out.println(StatusCode.INVALID_SIGNATURE + "\n" + e);
            return StatusCode.INVALID_SIGNATURE;
        }
        return StatusCode.OK;
    }

    public StatusCode verifyPostSignature() {
        // TODO
        return null;
    }

    public StatusCode verifyReadSignature() {
        // TODO
        return null;
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
     * Posts an announcement of up to 255 characters to the user's PostOperation Board.
     * Can refer to previous announcements.
     * @param pubKey
     * @param message to be put in the announcement
     * @param opUuid uuid of the operation (assigned by the client to guarantee freshness)
     *               different from the uuid assigned by the server, which is uniquely references an announcement
     * @param announcements are the unique announcement ids of the references to previous announcements
     * @return StatusCode saying if the post was successful
     */
    public StatusCode post(PublicKey pubKey, String message, int opUuid, List<Integer> announcements, byte[] signature) {
        StatusCode status = verifyMessage(message);
        if (status.equals(StatusCode.OK)) {
            int uuid = UUIDGenerator.generateUUID();
            PostOperation newAnnouncement = new PostOperation(opUuid, message, pubKey, announcements, signature);
            StatusCode signStatus = verifyOperation(newAnnouncement, signature);
            if (signStatus.equals(StatusCode.OK)) {
                int index =_users.get(pubKey).postAnnouncementBoard(newAnnouncement);
                // client's public key is used to indicate it's stored in that client's PostOperation Board
                _announcementMapper.put(uuid, new AnnouncementLocation(pubKey, index));
                return StatusCode.OK;
            }
            else return signStatus;
        }
        else return status;
    }

    /**
     * Posts an announcement of up to 255 characters in the General Board.
     * Can refer to previous announcements.
     * @param pubKey
     * @param message to be put in the announcement
     * @param opUuid uuid of the operation (assigned by the client to guarantee freshness)
     *               different from the uuid assigned by the server, which is uniquely references an announcement
     * @param announcements are the unique announcement ids of the references to previous announcements
     * @param signature
     * @return StatusCode saying if the post was successful
     */
    public StatusCode postGeneral(PublicKey pubKey, String message, int opUuid, List<Integer> announcements, byte[] signature) {
        StatusCode status = verifyMessage(message);
        System.out.println("status code: " + status);
        if (status.equals(StatusCode.OK)) {
            int uuid = UUIDGenerator.generateUUID();
            PostOperation newAnnouncement = new PostOperation(opUuid, message, pubKey, announcements, signature);
            StatusCode signStatus = verifyOperation(newAnnouncement, signature);
            System.out.println("Signature status code: " + signStatus);
            if (signStatus.equals(StatusCode.OK)) {
                int index;
                synchronized (_generalBoard) {
                    index = _generalBoard.size();
                    _generalBoard.add(newAnnouncement);
                }
                // server's public key is used to indicate it's stored in the General Board
                _announcementMapper.put(uuid, new AnnouncementLocation(_pubKey, index));
                return StatusCode.OK;
            }
            else return signStatus;
        }
        else return status;
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
        if (number == 0) {
            return user.getAllAnnouncements();
        }
        else if (0 < number && number <= user.getNumAnnouncements()) {
            return user.getAnnouncements(number);
        }
        // invalid number of announcements
        else {
            return null; // TODO
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
            if (number == 0) {
                return _generalBoard;
            }
            else if (0 < number && number <= nAnnouncements) {
                return _generalBoard.subList(nAnnouncements - number, nAnnouncements);
            }
            // invalid number of announcements
            else {
                System.out.println("nAnnouncements: " + nAnnouncements);
                return null; // TODO
            }
        }

    }
}
