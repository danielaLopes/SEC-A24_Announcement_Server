package pt.ulisboa.tecnico.sec.communication_lib;

public class VerifiableAnnouncement extends SerializableObject {
    private byte[] _ann;
    private byte[] _signedAnn;

    public VerifiableAnnouncement(Announcement sm, byte[] signedsm) {
        _ann = sm.getBytes();
        _signedAnn = signedsm;
    }

    public VerifiableAnnouncement(byte[] bytes) {
        VerifiableAnnouncement va = (VerifiableAnnouncement) super.byteArrayToObj(bytes);
        _ann = va.getAnnouncement().getBytes();
        _signedAnn = va.getSignedAnnouncement();
    }

    public Announcement getAnnouncement() { return (Announcement) super.byteArrayToObj(_ann); }
    public byte[] getSignedAnnouncement() { return _signedAnn; }

    public byte[] getBytes() {
        return super.objToByteArray(this);
    }

    @Override
    public Object byteArrayToObj(byte[] b) {
        return super.byteArrayToObj(b);
    }
    
    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof VerifiableAnnouncement)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        VerifiableAnnouncement a = (VerifiableAnnouncement) o;

        // Compare the data members and return accordingly
        return this.getAnnouncement().equals(a.getAnnouncement());
    }
}
