package pt.ulisboa.tecnico.sec.server;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class User {

    private PublicKey _pubKey;
    // has to be a list because latest announcements can be requested
    private List<Announcement> _announcementBoard;

    public User(PublicKey pubKey) {
        _pubKey = pubKey;
        _announcementBoard = new ArrayList<Announcement>();
    }

    public void postAnnouncementBoard(Announcement announcement) {
        _announcementBoard.add(announcement);
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
}
