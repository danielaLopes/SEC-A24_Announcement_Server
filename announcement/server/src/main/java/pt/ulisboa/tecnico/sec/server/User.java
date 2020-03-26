package pt.ulisboa.tecnico.sec.server;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class User {

    private PublicKey _pubKey;
    private String _dbTableName;
    // has to be a list because latest announcements can be requested
    private List<PostOperation> _announcementBoard;

    public User(PublicKey pubKey, String dbTableName) {
        _pubKey = pubKey;
        _announcementBoard = new ArrayList<>();
        _dbTableName = dbTableName;        
    }

    public int postAnnouncementBoard(PostOperation announcement) {
        _announcementBoard.add(announcement);
        return getNumAnnouncements() - 1;
    }

    public List<PostOperation> getAnnouncements(int number) {
        int nAnnouncements = _announcementBoard.size();
        return _announcementBoard.subList(nAnnouncements - number, nAnnouncements);
    }

    public List<PostOperation> getAllAnnouncements() {
        return _announcementBoard;
    }

    public int getNumAnnouncements() {
        return _announcementBoard.size();
    }
}
