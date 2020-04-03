package pt.ulisboa.tecnico.sec.database_lib;

public class OperationsBoardStructure {
    private String _opUUID;
    private byte[] _operation;

    public OperationsBoardStructure(String opUUID, byte[] operation) {
        _opUUID = opUUID;
        _operation = operation;
    }

    public String getOpUUID() { return _opUUID; }

    public byte[] getOperation() { return _operation; }
}