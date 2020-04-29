package pt.ulisboa.tecnico.sec.communication_lib;

import java.security.PublicKey;
import java.util.AbstractMap;

public class ServerMessage {
    private PublicKey _publicKey;
    private String _command;
    private AbstractMap.SimpleEntry<Integer, Announcement> _values;

    public ServerMessage(PublicKey publicKey, String command, AbstractMap.SimpleEntry<Integer, Announcement> values) {
        _publicKey = publicKey;
        _command = command;
        _values = values;
    }

    public PublicKey getPublicKey() {return _publicKey; }
    public String getCommand() {return _command; }
    public int getTimestamp() {return _values.getKey(); }
    public Announcement getValue() {return _values.getValue(); }
}