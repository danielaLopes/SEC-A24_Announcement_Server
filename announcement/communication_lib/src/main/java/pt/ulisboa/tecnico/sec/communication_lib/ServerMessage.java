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
    private byte[] _readAnnouncements;
    private AtomicInteger _rid;

    public ServerMessage(PublicKey publicKey, PublicKey clientPublicKey, String command, byte[] readAnnouncements) {
        _publicKey = publicKey;
        _command = command;
        _readAnnouncements = readAnnouncements;
        _clientPublicKey = clientPublicKey;
    }

    public ServerMessage(PublicKey publicKey, PublicKey clientPublicKey, String command, AtomicInteger rid) {
        _publicKey = publicKey;
        _command = command;
        _rid = rid;
        _clientPublicKey = clientPublicKey;
    }

    public ServerMessage(PublicKey publicKey, PublicKey clientPublicKey, String command, AtomicInteger rid, byte[] readAnnouncements) {
        _publicKey = publicKey;
        _command = command;
        _rid = rid;
        _readAnnouncements = readAnnouncements;
        _clientPublicKey = clientPublicKey;
    }

    public PublicKey getPublicKey() {return _publicKey; }
    public PublicKey getClientPublicKey() {return _clientPublicKey; }
    public String getCommand() {return _command; }
    public AtomicInteger getRid() {return _rid; }
    public byte[] getReadAnnouncements() {return _readAnnouncements;}
}