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

    public User(PublicKey pubKey, String dbTableName) {
        _pubKey = pubKey;
        _announcementBoard = new ArrayList<>();
        _dbTableName = dbTableName;
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
