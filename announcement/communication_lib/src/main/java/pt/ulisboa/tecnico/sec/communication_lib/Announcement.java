package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.List;

public class Announcement implements Serializable{
    private String _announcement;
    private List<Integer> _references;
    private int _announcementID;
    private String _clientUUID;
    private PublicKey _clientPublicKey;

    public Announcement(String announcement, List<Integer> references) {
        _announcement = announcement;
        _references = references;
    }

    public Announcement(String announcement, List<Integer> references, int announcementID, String clientUUID) {
        _announcement = announcement;
        _references = references;
        _announcementID = announcementID;
        _clientUUID = clientUUID;
    }

    public void setClientUUID(String clientUUID) { _clientUUID = clientUUID; }

    public void setPublicKey(PublicKey clientPublicKey) { _clientPublicKey = clientPublicKey; }

    public String getAnnouncement() { return _announcement; }

    public List<Integer> getReferences() { return _references; }

    public int getAnnouncementID() { return _announcementID; }

    public void setAnnouncementID(int id) { _announcementID = id; }

    public String getClientUUID() { return _clientUUID; }

    public PublicKey getClientPublicKey() { return _clientPublicKey; }


}