package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.List;

public class Announcement implements Serializable, Comparable{
    private String _announcement;
    private List<String> _references;
    private String _announcementID;
    private String _clientUUID;
    private PublicKey _clientPublicKey;

    public Announcement(String announcement, List<String> references) {
        _announcement = announcement;
        _references = references;
    }

    public Announcement(String announcement, List<String> references, String announcementID, String clientUUID) {
        _announcement = announcement;
        _references = references;
        _announcementID = announcementID;
        _clientUUID = clientUUID;
    }

    public void setClientUUID(String clientUUID) { _clientUUID = clientUUID; }

    public void setPublicKey(PublicKey clientPublicKey) { _clientPublicKey = clientPublicKey; }

    public String getAnnouncement() { return _announcement; }

    public List<String> getReferences() { return _references; }

    public String getAnnouncementID() { return _announcementID; }

    public void setAnnouncementID(String id) { _announcementID = id; }

    public String getClientUUID() { return _clientUUID; }

    public PublicKey getClientPublicKey() { return _clientPublicKey; }

    public int compareTo(Announcement other) {
        if (_announcement.equals(other.getAnnouncement()) && _references.equals(other.getReferences())
            && _clientPublicKey.equals(other.getClientPublicKey()))
            return 0;
        return -1;
    }
}