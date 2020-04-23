package pt.ulisboa.tecnico.sec.database_lib;

public class OperationsBoardStructure {
    private String _clientUUID;
    private String _opUUID;

    public OperationsBoardStructure(String clientUUID, String opUUID) {
        _clientUUID = clientUUID;
        _opUUID = opUUID;
    }

    public String getClientUUID() { return _clientUUID; }

    public String getOpUUID() { return _opUUID; }
}