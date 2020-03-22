package pt.ulisboa.tecnico.sec.server;

import java.security.PublicKey;

public class AnnouncementLocation {
    private PublicKey _publicKey;
    private int _index;

    public AnnouncementLocation(PublicKey publicKey, int index) {
        _publicKey = publicKey;
        _index = index;
    }

    public PublicKey getPublicKey() {
        return _publicKey;
    }

    public int getIndex() {
        return _index;
    }
}
