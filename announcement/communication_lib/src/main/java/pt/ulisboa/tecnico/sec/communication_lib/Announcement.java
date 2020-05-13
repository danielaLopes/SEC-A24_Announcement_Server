package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.List;

public class Announcement extends SerializableObject{
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

    public Announcement(byte[] bytes) {
        Announcement va = (Announcement) super.byteArrayToObj(bytes);
        _announcement = va.getAnnouncement();
        _references = va.getReferences();
        _announcementID = va.getAnnouncementID();
        _clientUUID = va.getClientUUID();
        _clientPublicKey = va.getClientPublicKey();
    }

    public void setClientUUID(String clientUUID) { _clientUUID = clientUUID; }

    public void setPublicKey(PublicKey clientPublicKey) { _clientPublicKey = clientPublicKey; }

    public String getAnnouncement() { return _announcement; }

    public List<String> getReferences() { return _references; }

    public String getAnnouncementID() { return _announcementID; }

    public void setAnnouncementID(String id) { _announcementID = id; }

    public String getClientUUID() { return _clientUUID; }

    public PublicKey getClientPublicKey() { return _clientPublicKey; }

    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Announcement)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        Announcement a = (Announcement) o;

        // Compare the data members and return accordingly
        return this._announcement.equals(a._announcement) &&
                this._references.equals(a._references) &&
                this._clientPublicKey.equals(a._clientPublicKey);
    }

    public byte[] getBytes() {
        return super.objToByteArray(this);
    }

    @Override
    public Object byteArrayToObj(byte[] b) {
        return super.byteArrayToObj(b);
    }
}