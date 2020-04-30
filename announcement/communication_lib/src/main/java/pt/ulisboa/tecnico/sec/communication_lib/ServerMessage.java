package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.AbstractMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerMessage implements Serializable {
    private PublicKey _publicKey;
    private PublicKey _clientPublicKey;
    private String _command;
    private AbstractMap.SimpleEntry<Integer, Announcement> _values;
    private AtomicInteger _rid;
    private List<Announcement> _announcements;

    public ServerMessage(PublicKey publicKey, PublicKey clientPublicKey, String command, AbstractMap.SimpleEntry<Integer, Announcement> values) {
        _publicKey = publicKey;
        _command = command;
        _values = values;
        _clientPublicKey = clientPublicKey;
    }

    public ServerMessage(PublicKey publicKey, PublicKey clientPublicKey, String command, AtomicInteger rid) {
        _publicKey = publicKey;
        _command = command;
        _rid = rid;
        _clientPublicKey = clientPublicKey;
    }

    public ServerMessage(PublicKey publicKey, PublicKey clientPublicKey, String command, AtomicInteger rid, List<Announcement> announcements) {
        _publicKey = publicKey;
        _command = command;
        _rid = rid;
        _announcements = announcements;
        _clientPublicKey = clientPublicKey;
    }

    public PublicKey getPublicKey() {return _publicKey; }
    public PublicKey getClientPublicKey() {return _clientPublicKey; }
    public String getCommand() {return _command; }
    public int getTimestamp() {return _values.getKey(); }
    public Announcement getValue() {return _values.getValue(); }
    public AtomicInteger getRid() {return _rid; }
    public List<Announcement> getAnnouncements() {return _announcements; }
}