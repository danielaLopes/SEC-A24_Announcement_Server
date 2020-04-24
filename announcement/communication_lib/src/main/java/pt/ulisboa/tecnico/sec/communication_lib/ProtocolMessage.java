package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.List;

public class ProtocolMessage implements Serializable {
    private String _command;
    private PublicKey _publicKey;
    private StatusCode _statusCode;

    private List<Announcement> _announcements;
    private Announcement _postAnnouncement;
    // Post response
    private byte[] _token;
    private byte[] _oldToken;
    //Read operation: Number of announcements to be read.
    private int _numberAnnouncements;
    //Read operation: Public key of user to read
    private PublicKey _toReadPublicKey;
      
    public ProtocolMessage(String command) {
        _command = command;
    }

    public ProtocolMessage(StatusCode statusCode) {
        _statusCode = statusCode;
    }

    public ProtocolMessage(List<Announcement> announcements) {
        _announcements = announcements;
    }

    public ProtocolMessage(Announcement announcement) {
        _postAnnouncement = announcement;
    }

    public ProtocolMessage(String command, PublicKey publicKey) {
        _command = command;
        _publicKey = publicKey;
    }

    public ProtocolMessage(String command, PublicKey publicKey, byte[] token) {
        _token = token;
        _command = command;
        _publicKey = publicKey;
    }

    public ProtocolMessage(String command, StatusCode statusCode, PublicKey publicKey, byte[] token, byte[] oldToken) {
        _command = command;
        _statusCode = statusCode;
        _publicKey = publicKey;
        _token = token;
        _oldToken = oldToken;
    }

    public ProtocolMessage(String command, PublicKey publicKey, Announcement announcement, byte[] token) {
        _command = command;
        _publicKey = publicKey;
        _postAnnouncement = announcement;
        _token = token;
    }

    public ProtocolMessage(String command, StatusCode statusCode, PublicKey publicKey, Announcement announcement, byte[] token, byte[] oldToken) {
        _command = command;
        _statusCode = statusCode;
        _publicKey = publicKey;
        _postAnnouncement = announcement;
        _token = token;
        _oldToken = oldToken;
    }

    public ProtocolMessage(String command, StatusCode statusCode, PublicKey publicKey) {
        _command = command;
        _statusCode = statusCode;
        _publicKey = publicKey;
    }

    public ProtocolMessage(String command, StatusCode statusCode, PublicKey publicKey, byte[] token, Announcement announcement) {
        _token = token;
        _command = command;
        _statusCode = statusCode;
        _publicKey = publicKey;
        _postAnnouncement = announcement;
    }

    // Post Response or Register Response
    /*public ProtocolMessage(String command, StatusCode statusCode, byte[] token) {
        _command = command;
        _statusCode = statusCode;
        _token = token;
    }*/

    public ProtocolMessage(String command, StatusCode statusCode, PublicKey publicKey, byte[] token) {
        _command = command;
        _statusCode = statusCode;
        _token = token;
        _publicKey = publicKey;
    }

    //Read Operation Client -> Server
    public ProtocolMessage(String command, PublicKey publicKey, byte[] token, int numberAnnouncements, PublicKey toReadPublicKey) {
        _token = token;
        _command = command;
        _publicKey = publicKey;
        _numberAnnouncements = numberAnnouncements;
        _toReadPublicKey = toReadPublicKey;
    }

    //ReadGeneral Operation Client -> Server
    public ProtocolMessage(String command, PublicKey publicKey, byte[] token, int numberAnnouncements) {
        _token = token;
        _command = command;
        _publicKey = publicKey;
        _numberAnnouncements = numberAnnouncements;
    }

    //Read Operations Server -> Client
    public ProtocolMessage(String command, StatusCode statusCode, PublicKey publicKey, List<Announcement> announcements, byte[] token, byte[] oldToken) {
        _command = command;
        _statusCode = statusCode;
        _token = token;
        _oldToken = oldToken;
        _publicKey = publicKey;
        _announcements = announcements;
    }

    // Read Response

    public String getCommand() { return _command; }

    public PublicKey getPublicKey() { return _publicKey; }

    public PublicKey getToReadPublicKey() { return _toReadPublicKey; }

    public StatusCode getStatusCode() { return _statusCode; }

    public byte[] getToken() { return _token; }

    public byte[] getOldToken() { return _oldToken; }

    public List<Announcement> getAnnouncements() { return _announcements; }

    public Announcement getPostAnnouncement() { return _postAnnouncement; }

    public int getReadNumberAnnouncements() { return _numberAnnouncements; }

}