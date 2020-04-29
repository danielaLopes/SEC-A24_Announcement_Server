package pt.ulisboa.tecnico.sec.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class CommunicationServer {

    private ObjectOutputStream _oos;
    private ObjectInputStream _ois;
    private Socket _clientSocket;
    private String _token;

    public CommunicationServer(ObjectOutputStream oos, ObjectInputStream ois,
                               Socket clientSocket) {

        _oos = oos;
        _ois = ois;
        _clientSocket = clientSocket;
    }

    public CommunicationServer(ObjectOutputStream oos, ObjectInputStream ois,
                               Socket clientSocket, String token) {

        _oos = oos;
        _ois = ois;
        _clientSocket = clientSocket;
        _token = token;
    }

    protected ObjectOutputStream getObjOutStream() {
        return _oos;
    }

    protected ObjectInputStream getObjInStream() {
        return _ois;
    }

    protected Socket getClientSocket() {
        return _clientSocket;
    }

    protected String getToken() {
        return _token;
    }

    protected void setToken(String newToken) {
        _token = newToken;
    }
}
