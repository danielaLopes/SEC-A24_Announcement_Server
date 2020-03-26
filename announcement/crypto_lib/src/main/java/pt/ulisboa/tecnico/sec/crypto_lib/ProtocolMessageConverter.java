package pt.ulisboa.tecnico.sec.crypto_lib;

import pt.ulisboa.tecnico.sec.communication_lib.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ProtocolMessageConverter {

    public static byte[] pmToByteArray(ProtocolMessage pm) {
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(pm);
        }
        catch(IOException e) {
            System.out.println(e);
        }
        return out.toByteArray();
    }

    public static ProtocolMessage byteArrayToPm(byte[] b) {
        ObjectInputStream is = null;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(b);
            is = new ObjectInputStream(in);
            return (ProtocolMessage) is.readObject();
        }
        catch(IOException | ClassNotFoundException e) {
            System.out.println(e);
        }
        return null;
    }
}