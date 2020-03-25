package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyPairUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyStorage;
import pt.ulisboa.tecnico.sec.crypto_lib.UUIDGenerator;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private int _port;
    private ServerSocket _serverSocket;
    private Socket _socket;
    private String _pathToKeyStorePasswd;
    private String _pathToEntryPasswd;
    private PublicKey _pubKey;
    private PrivateKey _privateKey;
    private ConcurrentHashMap<PublicKey, User> _users;
    /**
     * maps the announcement unique id to the public key of the entity
     * where it's stored and the index on the Announcement Board,
     * if it's the server's public key it's in the
     * General Board, otherwise it's in the respective User's Announcement Board
     */
    private ConcurrentHashMap<Integer, AnnouncementLocation> _announcementMapper;
    // TODO: in General Board, posts should remain accountable, so should the value be a signature(post) + post = Announcement?
    private List<Announcement> _generalBoard;
    private Communication _communication;

    public Server(boolean activateCC, int port, char[] keyStorePasswd, char[] entryPasswd, String alias) {
        loadPublicKey();
        loadPrivateKey(keyStorePasswd, entryPasswd, alias);
        _port = port;
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
     * Verifies if a message is valid to be posted.
     * @param message to be verified
     * @return a boolean to decide whether the message is valid or not
     */
    public boolean verifyMessage(String message) {
        if (message.length() < 255) {
            return true;
        }
        // TODO: make more verifications
        return false;
    }

    /**
     * Posts an announcement of up to 255 characters to the user's Announcement Board.
     * Can refer to previous announcements.
     * @param pubKey
     * @param message to be put in the announcement
     * @param announcements are the unique announcement ids of the references to previous announcements
     * @return StatusCode saying if the post was successful
     */
    public StatusCode post(PublicKey pubKey, String message, List<Integer> announcements) {
        // TODO: decide how to reference announcements -> unique ids
        if (verifyMessage(message)) {
            int uuid = UUIDGenerator.generateUUID();
            Announcement newAnnouncement = new Announcement(uuid, message, pubKey, announcements);
            int index =_users.get(pubKey).postAnnouncementBoard(newAnnouncement);
            // client's public key is used to indicate it's stored in that client's Announcement Board
            _announcementMapper.put(uuid, new AnnouncementLocation(pubKey, index));
            return new StatusCode("OK");
        }
        else {
            return new StatusCode("Invalid Message");
        }
    }

    /**
     * Posts an announcement of up to 255 characters in the General Board.
     * Can refer to previous announcements.
     * @param pubKey
     * @param message to be put in the announcement
     * @param announcements are the unique announcement ids of the references to previous announcements
     */
    public StatusCode postGeneral(PublicKey pubKey, String message, List<Integer> announcements) {
        // TODO: decide how to reference announcements -> unique id
        if (verifyMessage(message)) {
            int uuid = UUIDGenerator.generateUUID();
            Announcement newAnnouncement = new Announcement(uuid, message, pubKey, announcements);
            int index;
            synchronized (_generalBoard) {
                index = _generalBoard.size();
                _generalBoard.add(newAnnouncement);
            }
            // server's public key is used to indicate it's stored in the General Board
            _announcementMapper.put(uuid, new AnnouncementLocation(_pubKey, index));
            return new StatusCode("OK");
        }
        else {
            return new StatusCode("Invalid Message");
        }
    }

    /**
     * Obtains the most recent number announcements posted by the user with associated key
     * (from the user's Announcement Board).
     * If number == 0, all announcements should be returned.
     * @param pubKey
     * @param number of announcements to be returned
     * @return a list of announcements
     */
    public List<Announcement> read(PublicKey pubKey, int number) {
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
    public List<Announcement> readGeneral(int number) {
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
                return null; // TODO
            }
        }

    }
}
