package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.List;

public class ProtocolMessage implements Serializable {
    private String _command;
    private PublicKey _publicKey;
    private StatusCode _statusCode;
    
    // Post response
    private int _opUuid;
    private byte[] _signature;
    // Read response

    List<Integer> _announcements; // TODO: what to show of the announcements
    
    public ProtocolMessage(String command) {
        _command = command;
    }

    public ProtocolMessage(StatusCode statusCode) {
        _statusCode = statusCode;
    }

    public ProtocolMessage(String command, PublicKey publicKey) {
        _command = command;
        _publicKey = publicKey;
    }
    
    // Post Response or Register Response
    public ProtocolMessage(String command, StatusCode statusCode, int opUuid, byte[] signature) {

        _command = command;
        _statusCode = statusCode;
        _opUuid = opUuid;
        _signature = signature;
    }

    // Read Response
    public ProtocolMessage(String command, StatusCode statusCode, int opUuid, byte[] signature, List<Integer> announcements) {

        _command = command;
        _statusCode = statusCode;
        _opUuid = opUuid;
        _signature = signature;
        _announcements = announcements;
    }

    public String getCommand() { return _command; }

    public PublicKey getPublicKey() { return _publicKey; }

    public StatusCode getStatusCode() { return _statusCode; }

    public int getOpUuid() { return _opUuid; }

    public byte[] getSignature() { return _signature; }

    public List<Integer> getAnnouncements() { return _announcements; }
}