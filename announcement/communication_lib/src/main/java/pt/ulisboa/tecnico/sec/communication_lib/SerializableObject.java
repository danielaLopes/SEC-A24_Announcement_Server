package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.*;

public abstract class SerializableObject implements Serializable {

    public byte[] objToByteArray(Object obj) {
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(obj);
        }
        catch(IOException e) {
            System.out.println(e);
        }
        return out.toByteArray();
    }

    public Object byteArrayToObj(byte[] b) {
        ObjectInputStream is = null;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(b);
            is = new ObjectInputStream(in);
            return is.readObject();
        }
        catch(IOException | ClassNotFoundException e) {
            System.out.println(e);
        }
        return null;
    }
}
