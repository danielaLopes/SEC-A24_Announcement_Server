package pt.ulisboa.tecnico.sec.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;

public class CommunicationServer {

    private int _port;
    private PublicKey _serverPubKey;
    private ObjectOutputStream _oos;
    private ObjectInputStream _ois;
    private Socket _clientSocket;
    private String _token;
    private boolean _alive;
    protected boolean _refreshToken = false;

    public CommunicationServer(int port, ObjectOutputStream oos, ObjectInputStream ois,
                               Socket clientSocket,PublicKey serverPubKey) {

        _port = port;
        _oos = oos;
        _ois = ois;
        _clientSocket = clientSocket;
        _alive = true;
        _serverPubKey = serverPubKey;
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

    protected void updateObjInStream() throws IOException {
        _ois = new ObjectInputStream(_clientSocket.getInputStream());
    }

    protected void setAlive(boolean alive) { _alive = alive; }

    protected  boolean getAlive() { return _alive; }

    protected PublicKey getPubKey() { return _serverPubKey; }
}
