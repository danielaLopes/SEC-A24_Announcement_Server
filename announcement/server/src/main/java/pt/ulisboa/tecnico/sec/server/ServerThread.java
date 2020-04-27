package pt.ulisboa.tecnico.sec.server;

public class ServerThread extends Thread {

    private Server _server;
    private int _port;

    public ServerThread(Server server, int port) {
        _server = server;
        _port = port;
    }

    @Override
    public void run() {

    }
}