package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.Serializable;

public class ProtocolMessage implements Serializable {
    private final String _command;
    
    public ProtocolMessage(String command) {
        _command = command;
    }

    public String getCommand() {
        return _command;
    }
}