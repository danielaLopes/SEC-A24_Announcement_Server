package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;

import pt.ulisboa.tecnico.sec.crypto_lib.UUIDGenerator;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class User {

    private PublicKey _pubKey;
    private String _dbTableName;
    // has to be a list because latest announcements can be requested
    private List<Announcement> _announcementBoard;
    private String _token;
    private AtomicRegister1N _atomicRegister1N;
    private ClientMessageHandler _cmh;

    public User(PublicKey pubKey, String dbTableName, AtomicRegister1N atomicRegister1N, ClientMessageHandler cmh) {
        _pubKey = pubKey;
        _announcementBoard = new ArrayList<>();
        _dbTableName = dbTableName;
        _atomicRegister1N = atomicRegister1N;
        _cmh = cmh;
    }

    public ClientMessageHandler getCMH() {
        return _cmh;
    }

    public String getdbTableName() {
        return _dbTableName;
    }

    public PublicKey getPublicKey() {
        return _pubKey;
    }

    public int postAnnouncementBoard(Announcement announcement) {
        _announcementBoard.add(announcement);
        return getNumAnnouncements() - 1;
    }

    public List<Announcement> getAnnouncements(int number) {
        int nAnnouncements = _announcementBoard.size();
        return _announcementBoard.subList(nAnnouncements - number, nAnnouncements);
    }

    public List<Announcement> getAllAnnouncements() {
        return _announcementBoard;
    }

    public AtomicRegister1N getAtomicRegister1N() {
        return _atomicRegister1N;
    }

    public int getNumAnnouncements() {
        return _announcementBoard.size();
    }

    public String getToken() {
        return _token;
    }

    public void setToken(String token) {
        _token = token;
    }

    public void setRandomToken() {
        _token = UUIDGenerator.generateUUID();
    }

}
