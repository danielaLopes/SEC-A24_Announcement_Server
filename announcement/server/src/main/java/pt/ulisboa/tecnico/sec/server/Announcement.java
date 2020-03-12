package pt.ulisboa.tecnico.sec.server;

import java.security.PublicKey;

public class Announcement {
    private String _message;
    private PublicKey _pubKey;
    // TODO: signature

    public Announcement(String message, PublicKey pubKey) {
        _message = message;
        _pubKey = pubKey;
    }

    public String getMessage() {
        return _message;
    }
}
