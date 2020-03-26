package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.Serializable;
import java.security.PublicKey;

public class ProtocolMessage implements Serializable {
    private String _command;
    private PublicKey _publicKey;
    private StatusCode _statusCode;
    
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

    public String getCommand() {
        return _command;
    }

    public PublicKey getPublicKey() {
        return _publicKey;
    }
}