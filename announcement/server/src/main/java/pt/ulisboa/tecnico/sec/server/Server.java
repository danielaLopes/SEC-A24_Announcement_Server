package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.Communication;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyPairUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyStorage;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private PublicKey _pubKey;
    private int _port;
    private String _pathToKeyStorePasswd;
    private String _pathToEntryPasswd;
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
    private AtomicInteger _nAnnouncements; // each Announcement needs to have a unique id
    private Communication _communication;

    public Server(boolean activateCC, int port, String pathToKeyStorePasswd, String pathToEntryPasswd) {
        try {
            _pubKey = KeyPairUtil.loadPublicKey("src/main/resources/crypto/public.key");
        } catch (IOException e) {

        } catch (NoSuchAlgorithmException e) {

        } catch (InvalidKeySpecException e) {

        }
        _port = port;
        _pathToKeyStorePasswd = pathToKeyStorePasswd;
        _pathToEntryPasswd = pathToEntryPasswd;
        _users = new ConcurrentHashMap<>();
        _announcementMapper = new ConcurrentHashMap<>();
        // TODO: see if a CopyOnWriteArrayList is more suitable (if very few writes and lots of reads)
        _generalBoard = new ArrayList<>();
        _nAnnouncements = new AtomicInteger(0);
        _communication = new Communication();
    }

    /*public PublicKey generateKeyPair(boolean activateCC) {
        PublicKey pubKey = null;
        // OpenSSL
        if (activateCC == false) {
            KeyGenerator keyGen = new KeyGenerator();
            KeyPair keys = keyGen.generateKeyPair("RSA", 1024);
            // TODO: store private key in a keystore
            pubKey = keys.getPublic();

            // store private key in a keystore
            KeyStorage keyStorage = new KeyStorage();
            try {
                System.out.println("Before Storing private key");
                KeyStore keyStore = keyStorage.createKeyStore("1");
                X509Certificate cert = loadCertificate();
                // System.out.println("cert: " + cert);
                keyStorage.storePrivateKey(keyStore, keys.getPrivate(), cert);
                KeyStore loadedKeyStore = keyStorage.loadKeyStore();
                PrivateKey privKey = keyStorage.loadPrivateKey(loadedKeyStore);
                System.out.println("After Storing private key");
            }catch (KeyStoreException e) {

            } catch () {

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        // CC
        else {
            // TODO
        }
        return pubKey;
    }*/

    public PrivateKey loadPrivateKey() {
        // load private key
        PrivateKey privateKey = null;
        try {
            KeyStore keyStore = KeyStorage.loadKeyStore(_pathToKeyStorePasswd, "src/main/resources/crypto/server1_keystore.jks");
            privateKey = KeyStorage.loadPrivateKey(_pathToEntryPasswd, "ola", keyStore);
            System.out.println("assigning private key");
        } catch (Exception e) {
            System.out.println("Error: Not possible to load private key from keystore");
        }
        return privateKey;
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
     * @return PostStatus saying if the post was successful
     */
    public PostStatus post(PublicKey pubKey, String message, List<Integer> announcements) {
        // TODO: decide how to reference announcements -> unique ids
        if (verifyMessage(message)) {
            Announcement newAnnouncement = new Announcement(message, pubKey, announcements);
            int index =_users.get(pubKey).postAnnouncementBoard(newAnnouncement);
            // client's public key is used to indicate it's stored in that client's Announcement Board
            _announcementMapper.put(_nAnnouncements.getAndIncrement(), new AnnouncementLocation(pubKey, index));
            return new PostStatus("OK");
        }
        else {
            return new PostStatus("Invalid Message");
        }
    }

    /**
     * Posts an announcement of up to 255 characters in the General Board.
     * Can refer to previous announcements.
     * @param pubKey
     * @param message to be put in the announcement
     * @param announcements are the unique announcement ids of the references to previous announcements
     */
    public PostStatus postGeneral(PublicKey pubKey, String message, List<Integer> announcements) {
        // TODO: decide how to reference announcements -> unique id
        if (verifyMessage(message)) {
            Announcement newAnnouncement = new Announcement(message, pubKey, announcements);
            int index;
            synchronized (_generalBoard) {
                index = _generalBoard.size();
                _generalBoard.add(newAnnouncement);
            }
            // server's public key is used to indicate it's stored in the General Board
            _announcementMapper.put(_nAnnouncements.getAndIncrement(), new AnnouncementLocation(_pubKey, index));
            return new PostStatus("OK");
        }
        else {
            return new PostStatus("Invalid Message");
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
