package pt.ulisboa.tecnico.sec.server;

import java.security.PublicKey;
import java.util.List;

public class Announcement {
    private String _message;
    private PublicKey _pubKey;
    // saves the unique announcement ids of the referenced references
    private List<Integer> _references;
    // TODO: signature

    public Announcement(String message, PublicKey pubKey, List<Integer> references) {
        _message = message;
        _pubKey = pubKey;
        _references = references;
    }

    public String getMessage() {
        return _message;
    }

    public List<Integer> getReferences() { return _references; }
}
