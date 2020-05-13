package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;

import pt.ulisboa.tecnico.sec.crypto_lib.UUIDGenerator;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class User {

    private PublicKey _pubKey;
    private String _dbTableName;
    private String _token;
    private AtomicRegister1N _atomicRegister1N;
    private ClientMessageHandler _cmh;

    public User(PublicKey pubKey, String dbTableName, AtomicRegister1N atomicRegister1N, ClientMessageHandler cmh) {
        _pubKey = pubKey;
        _dbTableName = dbTableName;
        _atomicRegister1N = atomicRegister1N;
        _cmh = cmh;
    }

    public ClientMessageHandler getCMH() {
        return _cmh;
    }

    public void setCMH(ClientMessageHandler cmh) {
        _cmh = cmh;
    }

    public String getdbTableName() {
        return _dbTableName;
    }

    public PublicKey getPublicKey() {
        return _pubKey;
    }


    public AtomicRegister1N getAtomicRegister1N() {
        return _atomicRegister1N;
    }

    public String getToken() {
        return _token;
    }

    public void setToken(String token) {
        _token = token;
    }

    public void setRandomToken() {
        _token = UUIDGenerator.generateUUID();
    }

}
