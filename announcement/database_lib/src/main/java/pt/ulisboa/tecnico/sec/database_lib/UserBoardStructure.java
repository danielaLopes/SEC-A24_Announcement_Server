package pt.ulisboa.tecnico.sec.database_lib;

public class UserBoardStructure {
    private String _announcement;
    private byte[] _references;
    private String _announcementID;
    private String _clientUUID;

    public UserBoardStructure(String announcement, byte[] references, String announcementID, String clientUUID) {
        _announcement = announcement;
        _references = references;
        _announcementID = announcementID;
        _clientUUID = clientUUID;
    }

    public String getAnnouncement() { return _announcement; }

    public byte[] getReferences() { return _references; }

    public String getAnnouncementID() { return _announcementID; }

    public String getClientUUID() { return _clientUUID; }
    
}