package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.Communication;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private final PublicKey _pubKey;
    private int _port;
    private ConcurrentHashMap<PublicKey, User> _users;
    private ConcurrentHashMap<Integer, PublicKey> _announcementMapper;
    // TODO: in General Board, posts should remain accountable, so should the value be a signature(post) + post = Announcement?
    private ConcurrentHashMap<Integer, Announcement> _generalBoard;
    private AtomicInteger _nAnnouncements; // each Announcement needs to have a unique id
    private Communication _communication;

    public Server(boolean activateCC, int port) {
        _pubKey = generateKeyPair(activateCC);
        _port = port;
        _users = new ConcurrentHashMap<PublicKey, User>();
        _announcementMapper = new ConcurrentHashMap<Integer, PublicKey>();
        _generalBoard = new ConcurrentHashMap<Integer, Announcement>();
        _nAnnouncements = new AtomicInteger(0);
        _communication = new Communication();
    }

    public PublicKey generateKeyPair(boolean activateCC) {
        PublicKey pubKey = null;
        // OpenSSL
        if (activateCC == false) {
            KeyGenerator keyGen = new KeyGenerator();
            KeyPair keys = keyGen.generateKeyPair("RSA", 1024);
            // TODO: store private key in a keystore
            pubKey = keys.getPublic();
        }
        // CC
        else {
            // TODO
        }
        return pubKey;
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
     * @param announcements are references to previous announcements
     * @return PostStatus saying if the post was successful
     */
    public PostStatus post(PublicKey pubKey, String message, List<Announcement> announcements) {
        // TODO: decide how to reference announcements -> unique ids
        if (verifyMessage(message)) {
            Announcement newAnnouncement = new Announcement(message, pubKey);
            _users.get(pubKey).postAnnouncementBoard(newAnnouncement);
            // client's public key is used to indicate it's stored in that client's Announcement Board
            _announcementMapper.put(_nAnnouncements.getAndIncrement(), pubKey);
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
     * @param announcements are references to previous announcements
     */
    public PostStatus postGeneral(PublicKey pubKey, String message, List<Announcement> announcements) {
        // TODO: decide how to reference announcements -> unique id
        if (verifyMessage(message)) {
            Announcement newAnnouncement = new Announcement(message, pubKey);
            _generalBoard.put(_nAnnouncements.get(), newAnnouncement);
            // server's public key is used to indicate it's stored in the General Board
            _announcementMapper.put(_nAnnouncements.getAndIncrement(), _pubKey);
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
        List<Announcement> announcements = new ArrayList<Announcement>(_generalBoard.values());
        int nAnnouncements = announcements.size();
        if (number == 0) {
            return announcements;
        }
        else if (0 < number && number <= nAnnouncements) {
            return announcements.subList(nAnnouncements - number, nAnnouncements);
        }
        // invalid number of announcements
        else {
            return null; // TODO
        }
    }
}
