package pt.ulisboa.tecnico.sec.server;

import java.security.PublicKey;
import java.security.Signature;

public class ReadOperation extends Operation {
    private int _number;

    public ReadOperation(int uuid, int number, PublicKey pubKey, byte[] signature) {
        super(uuid, pubKey, signature);
        _number = number;
    }

    public int getNumber() {
        return _number;
    }

    @Override
    public boolean equals(Object other) {

        if (other == this) { return true; }
        if (!(other instanceof ReadOperation)) { return false; }

        ReadOperation otherAnn = (ReadOperation) other;

        return super.equals(other)
                && _number == otherAnn.getNumber();
    }

    // to verify signatures
    @Override
    public byte[] getBytes() {
        return (getUUID() + "|" + _number).getBytes();
    }
}
