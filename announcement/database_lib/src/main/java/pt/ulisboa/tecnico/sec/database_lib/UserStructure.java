package pt.ulisboa.tecnico.sec.database_lib;

public class UserStructure {
    private byte[] _publicKey;
    private String _clientUUID;

    public UserStructure(byte[] publicKey, String clientUUID) {
        _publicKey = publicKey;
        _clientUUID = clientUUID;
    }

    public byte[] getPublicKey() { return _publicKey; }

    public String getClientUUID() { return _clientUUID; }
    
}