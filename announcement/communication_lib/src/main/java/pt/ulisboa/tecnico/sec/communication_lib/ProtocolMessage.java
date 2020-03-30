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
    private int _opUuid;
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

    public ProtocolMessage(String command, PublicKey publicKey, int opUuid) {
        _opUuid = opUuid;
        _command = command;
        _publicKey = publicKey;
    }

    public ProtocolMessage(String command, PublicKey publicKey, int opUuid, Announcement announcement) {
        _opUuid = opUuid;
        _command = command;
        _publicKey = publicKey;
        _postAnnouncement = announcement;
    }

    public ProtocolMessage(String command, StatusCode statusCode, PublicKey publicKey, int opUuid, Announcement announcement) {
        _opUuid = opUuid;
        _command = command;
        _statusCode = statusCode;
        _publicKey = publicKey;
        _postAnnouncement = announcement;
    }

    // Post Response or Register Response
    /*public ProtocolMessage(String command, StatusCode statusCode, int opUuid) {
        _command = command;
        _statusCode = statusCode;
        _opUuid = opUuid;
    }*/

    public ProtocolMessage(String command, StatusCode statusCode, PublicKey publicKey, int opUuid) {
        _command = command;
        _statusCode = statusCode;
        _opUuid = opUuid;
        _publicKey = publicKey;
    }

    //Read Operation Client -> Server
    public ProtocolMessage(String command, PublicKey publicKey, int opUuid, int numberAnnouncements, PublicKey toReadPublicKey) {
        _opUuid = opUuid;
        _command = command;
        _publicKey = publicKey;
        _numberAnnouncements = numberAnnouncements;
        _toReadPublicKey = toReadPublicKey;
    }

    //ReadGeneral Operation Client -> Server
    public ProtocolMessage(String command, PublicKey publicKey, int opUuid, int numberAnnouncements) {
        _opUuid = opUuid;
        _command = command;
        _publicKey = publicKey;
        _numberAnnouncements = numberAnnouncements;
    }

    //Read Operations Server -> Client
    public ProtocolMessage(String command, StatusCode statusCode, PublicKey publicKey, int opUuid, List<Announcement> announcements) {
        _command = command;
        _statusCode = statusCode;
        _opUuid = opUuid;
        _publicKey = publicKey;
        _announcements = announcements;
    }

    // Read Response

    public String getCommand() { return _command; }

    public PublicKey getPublicKey() { return _publicKey; }

    public PublicKey getToReadPublicKey() { return _toReadPublicKey; }

    public StatusCode getStatusCode() { return _statusCode; }

    public int getOpUuid() { return _opUuid; }

    public List<Announcement> getAnnouncements() { return _announcements; }

    public Announcement getPostAnnouncement() { return _postAnnouncement; }

    public int getReadNumberAnnouncements() { return _numberAnnouncements; }

}