package pt.ulisboa.tecnico.sec.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class CommunicationServer {

    private int _port;
    private ObjectOutputStream _oos;
    private ObjectInputStream _ois;
    private Socket _clientSocket;
    private String _token;
    private boolean _alive;

    public CommunicationServer(int port, ObjectOutputStream oos, ObjectInputStream ois,
                               Socket clientSocket) {

        _port = port;
        _oos = oos;
        _ois = ois;
        _clientSocket = clientSocket;
        _alive = true;
    }

    public CommunicationServer(int port, ObjectOutputStream oos, ObjectInputStream ois,
                               Socket clientSocket, String token) {

        _port = port;
        _oos = oos;
        _ois = ois;
        _clientSocket = clientSocket;
        _token = token;
        _alive = true;
    }

    protected int getPort() { return _port; }
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

    protected void setAlive(boolean alive) { _alive = alive; }

    protected  boolean getAlive() { return _alive; }
}
