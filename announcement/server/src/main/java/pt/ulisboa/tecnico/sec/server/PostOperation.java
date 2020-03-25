package pt.ulisboa.tecnico.sec.server;

import java.security.PublicKey;
import java.security.Signature;
import java.util.List;

public class PostOperation extends Operation {

    private String _message;
    // saves the unique announcement ids of the referenced references
    private List<Integer> _references;

    public PostOperation(int uuid, String message, PublicKey pubKey,
                         List<Integer> references, byte[] signature) {
        super(uuid, pubKey, signature);
        _message = message;
        _references = references;
    }

    public String getMessage() {
        return _message;
    }

    public List<Integer> getReferences() { return _references; }


    @Override
    public boolean equals(Object other) {

        if (other == this) { return true; }
        if (!(other instanceof PostOperation)) { return false; }

        PostOperation otherAnn = (PostOperation) other;

        return super.equals(other)
                && _message.equals(otherAnn.getMessage())
                && _references.equals(otherAnn.getReferences());
    }

    public String getReferencesStr() {
        String referencesStr = "";
        for (int i = 0; i < _references.size(); i++) {
            referencesStr  += Integer.toString(_references.get(i));
            if (i != (_references.size()-1)) referencesStr  += ",";
        }
        return referencesStr;
    }

    // to verify signatures
    @Override
    public byte[] getBytes() {
        System.out.println("operation: " + getUUID() + "|" + _message + "|" + getReferencesStr());
        return (getUUID() + "|" + _message + "|" + getReferencesStr()).getBytes();
    }
}
