package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.List;

public class ProtocolMessage implements Serializable {
    private String _command;
    private PublicKey _publicKey;
    private StatusCode _statusCode;

    //private String _opUuid;

    private byte[] _registerMessage;

    //TODO Eliminar
    private List<Announcement> _announcements;
    private Announcement _postAnnouncement;
    // Post response
    private String _token;
    private String _oldToken;
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

    public ProtocolMessage(String command, PublicKey publicKey, String token) {
        _token = token;
        _command = command;
        _publicKey = publicKey;
    }

    public ProtocolMessage(String command, StatusCode statusCode, PublicKey publicKey, String token, String oldToken) {
        _command = command;
        _statusCode = statusCode;
        _publicKey = publicKey;
        _token = token;
        _oldToken = oldToken;
    }

    // Post Operation Client -> Server
    public ProtocolMessage(String command, PublicKey publicKey, Announcement announcement, String token) {
        _command = command;
        _publicKey = publicKey;
        _postAnnouncement = announcement;
        _token = token;
    }

    public ProtocolMessage(String command, StatusCode statusCode, PublicKey publicKey, Announcement announcement, String token, String oldToken) {
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

    public ProtocolMessage(String command, StatusCode statusCode, PublicKey publicKey, String token, Announcement announcement) {
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

    public ProtocolMessage(String command, StatusCode statusCode, PublicKey publicKey, String token) {
        _command = command;
        _statusCode = statusCode;
        _token = token;
        _publicKey = publicKey;
    }

    // Read Operation Client -> Server
    public ProtocolMessage(String command, PublicKey publicKey, String token, int numberAnnouncements, PublicKey toReadPublicKey) {
        _token = token;
        _command = command;
        _publicKey = publicKey;
        _numberAnnouncements = numberAnnouncements;
        _toReadPublicKey = toReadPublicKey;
    }

    // ReadGeneral Operation Client -> Server
    public ProtocolMessage(String command, PublicKey publicKey, String token, int numberAnnouncements) {
        _token = token;
        _command = command;
        _publicKey = publicKey;
        _numberAnnouncements = numberAnnouncements;
    }

    // Read Operations Server -> Client
    public ProtocolMessage(String command, StatusCode statusCode, PublicKey publicKey,
                           List<Announcement> announcements, String token, String oldToken) {
        _command = command;
        _statusCode = statusCode;
        _token = token;
        _oldToken = oldToken;
        _publicKey = publicKey;
        _announcements = announcements;
    }


    public void setAtomicRegisterMessages(byte[] a) { _registerMessage = a; }
    // Read Response
    public byte[] getAtomicRegisterMessages() { return _registerMessage; }

    public String getCommand() { return _command; }

    public PublicKey getPublicKey() { return _publicKey; }

    public PublicKey getToReadPublicKey() { return _toReadPublicKey; }

    public StatusCode getStatusCode() { return _statusCode; }

    /*public String getOpUuid() { return _opUuid; }

    public void setOpUuid(String opUuid) { _opUuid = opUuid; }*/

    public String getToken() { return _token; }

    public String getOldToken() { return _oldToken; }

    public List<Announcement> getAnnouncements() { return _announcements; }

    public Announcement getPostAnnouncement() { return _postAnnouncement; }

    public int getReadNumberAnnouncements() { return _numberAnnouncements; }

    public void setToken(String token) { _token = token; }
}