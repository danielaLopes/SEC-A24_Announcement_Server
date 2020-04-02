package pt.ulisboa.tecnico.sec.database_lib;

public class OperationsBoardStructure {
    private int _opUUID;
    private byte[] _operation;

    public OperationsBoardStructure(int opUUID, byte[] operation) {
        _opUUID = opUUID;
        _operation = operation;
    }

    public int getOpUUID() { return _opUUID; }

    public byte[] getOperation() { return _operation; }
}