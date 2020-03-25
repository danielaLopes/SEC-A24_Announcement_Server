package pt.ulisboa.tecnico.sec.server;

import java.security.PublicKey;

public abstract class Operation {

    private int _uuid;
    private PublicKey _pubKey;
    private byte[] _signature;

    public Operation(int uuid, PublicKey pubKey, byte[] signature) {
        _uuid = uuid;
        _pubKey = pubKey;
        _signature = signature;
    }

    public int getUUID() { return _uuid; }

    public PublicKey getPubKey() { return _pubKey; }

    public byte[] getSignature() { return _signature; }

    @Override
    public boolean equals(Object other) {

        if (other == this) { return true; }
        if (!(other instanceof Operation)) { return false; }

        Operation otherAnn = (Operation) other;

        return _uuid == otherAnn.getUUID()
                && _pubKey.equals(otherAnn.getPubKey())
                && _signature.equals(otherAnn.getSignature());
    }

    public abstract byte[] getBytes();
}
