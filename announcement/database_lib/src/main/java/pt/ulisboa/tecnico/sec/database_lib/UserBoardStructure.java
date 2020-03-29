package pt.ulisboa.tecnico.sec.database_lib;

public class UserBoardStructure {
    private String _announcement;
    private byte[] _references;
    private int _announcementID;

    public UserBoardStructure(String announcement, byte[] references, int announcementID) {
        _announcement = announcement;
        _references = references;
        _announcementID = announcementID;
    }

    public String getAnnouncement() { return _announcement; }

    public byte[] getReferences() { return _references; }

    public int getAnnouncementID() { return _announcementID; }
    
}