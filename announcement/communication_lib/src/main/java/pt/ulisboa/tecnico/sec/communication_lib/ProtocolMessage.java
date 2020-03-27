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

    // Post Response or Register Response
    public ProtocolMessage(String command, StatusCode statusCode, int opUuid) {
        _command = command;
        _statusCode = statusCode;
        _opUuid = opUuid;
    }

    public ProtocolMessage(String command, StatusCode statusCode, PublicKey publicKey, int opUuid) {
        _command = command;
        _statusCode = statusCode;
        _opUuid = opUuid;
        _publicKey = publicKey;
    }

    // Read Response

    public String getCommand() { return _command; }

    public PublicKey getPublicKey() { return _publicKey; }

    public StatusCode getStatusCode() { return _statusCode; }

    public int getOpUuid() { return _opUuid; }

    public List<Announcement> getAnnouncements() { return _announcements; }

    public Announcement getPostAnnouncement() { return _postAnnouncement; }

}