package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.Serializable;
import java.util.List;

public class ProtocolMessage implements Serializable {
    private final String _command;

    StatusCode _status;

    // Post response
    private int _opUuid;
    private byte[] _signature;
    // Read response

    List<Integer> _announcements; // TODO: what to show of the announcements
    
    public ProtocolMessage(String command) {
        _command = command;
    }

    // Post Response or Register Response
    public ProtocolMessage(String command, StatusCode status, int opUuid, byte[] signature) {

        _command = command;
        _status = status;
        _opUuid = opUuid;
        _signature = signature;
    }

    // Read Response
    public ProtocolMessage(String command, StatusCode status, int opUuid, byte[] signature, List<Integer> announcements) {

        _command = command;
        _status = status;
        _opUuid = opUuid;
        _signature = signature;
        _announcements = announcements;
    }

    public String getCommand() {
        return _command;
    }

    public StatusCode getStatus() { return _status; }

    public int getOpUuid() { return _opUuid; }

    public byte[] getSignature() { return _signature; }

    public List<Integer> getAnnouncements() { return _announcements; }
}