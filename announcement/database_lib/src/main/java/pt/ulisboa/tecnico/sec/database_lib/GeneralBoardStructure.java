package pt.ulisboa.tecnico.sec.database_lib;

public class GeneralBoardStructure {
    private String _announcement;
    private byte[] _references;
    private int _announcementID;
    private String _clientUUID;

    public GeneralBoardStructure(String announcement, byte[] references, int announcementID, String clientUUID) {
        _announcement = announcement;
        _references = references;
        _announcementID = announcementID;
        _clientUUID = clientUUID;
    }

    public String getAnnouncement() { return _announcement; }

    public byte[] getReferences() { return _references; }

    public int getAnnouncementID() { return _announcementID; }

    public String getClientUUID() { return _clientUUID; }
    
}