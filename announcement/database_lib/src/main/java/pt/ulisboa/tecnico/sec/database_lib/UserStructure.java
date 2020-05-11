package pt.ulisboa.tecnico.sec.database_lib;

public class UserStructure {
    private byte[] _publicKey;
    private String _clientUUID;
    private byte[] _atomicRegister1N;
    private byte[] _cmh;
    private String _token;

    public UserStructure(byte[] publicKey, String clientUUID, byte[] atomicRegister1N, byte[] cmh, String token) {
        _publicKey = publicKey;
        _clientUUID = clientUUID;
        _atomicRegister1N = atomicRegister1N;
        _cmh = cmh;
        _token = token;
    }

    public byte[] getPublicKey() { return _publicKey; }

    public String getClientUUID() { return _clientUUID; }

    public byte[] getAtomicRegister1N() { return _atomicRegister1N; }

    public byte[] getCMH() { return _cmh; }

    public String getToken() { return _token; }
    
}